import asyncio
import uuid

import httpx
from fastapi import HTTPException, status
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.asyncio import AsyncSession
from sqlmodel import select
from supabase import Client, create_client

from app.core.config import settings
from app.users.models import Profile


def get_admin_client() -> Client:
    if not settings.SUPABASE_SERVICE_ROLE_KEY:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=(
                "La creación de cuentas staff requiere SUPABASE_SERVICE_ROLE_KEY "
                "configurada en el servidor."
            ),
        )
    return create_client(settings.SUPABASE_URL, settings.SUPABASE_SERVICE_ROLE_KEY)


async def lookup_auth_user_id_by_email(email: str) -> str | None:
    """Resolve an existing Supabase Auth user id by email (admin API)."""
    headers = {
        "apikey": settings.SUPABASE_SERVICE_ROLE_KEY,
        "Authorization": f"Bearer {settings.SUPABASE_SERVICE_ROLE_KEY}",
    }
    url = f"{settings.SUPABASE_URL.rstrip('/')}/auth/v1/admin/users"
    encoded_email = email.strip().lower()
    async with httpx.AsyncClient(timeout=15.0) as client:
        response = await client.get(
            url,
            params={"page": 1, "per_page": 1, "filter": f"email.eq.{encoded_email}"},
            headers=headers,
        )
        if response.status_code != 200:
            return None
        payload = response.json()
        users = payload.get("users") if isinstance(payload, dict) else None
        if not users:
            return None
        return str(users[0]["id"])


def _create_supabase_staff_user_sync(email: str, password: str, name: str) -> uuid.UUID | None:
    """
    Create a confirmed Supabase Auth user for staff.
    Returns user id on success, None if the email is already registered.
    """
    admin = get_admin_client()
    try:
        response = admin.auth.admin.create_user(
            {
                "email": email,
                "password": password,
                "email_confirm": True,
                "user_metadata": {"name": name, "role": "STAFF"},
            }
        )
        if not response.user:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="No se pudo crear el usuario staff en autenticación.",
            )
        return uuid.UUID(str(response.user.id))
    except HTTPException:
        raise
    except Exception as exc:
        message = str(exc)
        if "already been registered" in message or "User already registered" in message:
            return None
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Error al crear cuenta staff: {message}",
        ) from exc


async def create_supabase_staff_user(email: str, password: str, name: str) -> uuid.UUID | None:
    """Run Supabase admin user creation off the async event loop."""
    return await asyncio.to_thread(
        _create_supabase_staff_user_sync,
        email.strip().lower(),
        password,
        name.strip(),
    )


async def get_profile_by_id(db: AsyncSession, user_id: uuid.UUID) -> Profile | None:
    result = await db.execute(select(Profile).where(Profile.id == user_id))
    return result.scalar_one_or_none()


def ensure_staff_eligible(profile: Profile) -> None:
    if profile.role == "ATTENDEE":
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Los asistentes no pueden ser asignados como staff.",
        )
    if profile.role == "ORGANIZER":
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Los organizadores no pueden ser asignados como staff.",
        )
    if profile.role != "STAFF":
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Solo usuarios con rol STAFF pueden ser asignados a eventos.",
        )


async def _load_or_prepare_staff_profile(
    db: AsyncSession,
    user_id: uuid.UUID,
    name: str,
) -> Profile:
    profile = await get_profile_by_id(db, user_id)
    if profile:
        ensure_staff_eligible(profile)
        if name and profile.name != name:
            profile.name = name
            db.add(profile)
        return profile

    profile = Profile(id=user_id, name=name, role="STAFF")
    db.add(profile)
    try:
        await db.flush()
    except IntegrityError:
        await db.rollback()
        profile = await get_profile_by_id(db, user_id)
        if not profile:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="No se pudo crear el perfil staff. Intenta de nuevo.",
            ) from None
        ensure_staff_eligible(profile)
    return profile


async def resolve_or_create_staff_profile(
    db: AsyncSession,
    email: str,
    password: str,
    name: str,
) -> Profile:
    """Create or reuse a STAFF profile without committing (caller commits)."""
    normalized_email = email.strip().lower()
    clean_name = name.strip()

    user_id = await create_supabase_staff_user(normalized_email, password, clean_name)
    if user_id is not None:
        return await _load_or_prepare_staff_profile(db, user_id, clean_name)

    auth_user_id = await lookup_auth_user_id_by_email(normalized_email)
    if not auth_user_id:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="El email ya está registrado pero no se pudo recuperar el usuario.",
        )

    return await _load_or_prepare_staff_profile(db, uuid.UUID(auth_user_id), clean_name)
