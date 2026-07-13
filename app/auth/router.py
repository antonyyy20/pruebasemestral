import uuid
from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlmodel import select
from supabase import Client, create_client

from app.auth.schemas import TokenRefresh, TokenResponse, UserLogin, UserRegister
from app.core.config import settings
from app.core.database import get_db
from app.users.models import Profile
from app.users.profile_service import sync_profile_role

router = APIRouter(prefix="/auth", tags=["Authentication"])

supabase_client: Client = create_client(settings.SUPABASE_URL, settings.SUPABASE_KEY)


async def get_profile(db: AsyncSession, user_id: uuid.UUID) -> Profile | None:
    result = await db.execute(select(Profile).where(Profile.id == user_id))
    return result.scalar_one_or_none()


async def get_or_create_profile(
    db: AsyncSession,
    user_id: uuid.UUID,
    name: str,
    role: str,
) -> Profile:
    """Create a profile on first login without overwriting an existing role."""
    profile = await get_profile(db, user_id)
    if profile:
        return profile

    profile = Profile(id=user_id, name=name, role=role)
    db.add(profile)
    await db.commit()
    await db.refresh(profile)
    return profile


async def upsert_profile_for_register(
    db: AsyncSession,
    user_id: uuid.UUID,
    name: str,
    role: str,
) -> Profile:
    """Create or update profile on register so the chosen role is always persisted."""
    profile = await get_profile(db, user_id)
    if profile:
        if profile.role == "STAFF":
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=(
                    "Esta cuenta es de staff y fue creada por un organizador. "
                    "Usa inicio de sesión con el correo y contraseña que te dieron."
                ),
            )
        profile.name = name
        if profile.role != "ORGANIZER":
            profile.role = role
    else:
        profile = Profile(id=user_id, name=name, role=role)
        db.add(profile)

    await db.commit()
    await db.refresh(profile)
    return profile


def build_token_response(auth_response, profile: Profile) -> TokenResponse:
    if not auth_response.session:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Registro iniciado. Confirma tu email en Supabase antes de iniciar sesión.",
        )

    return TokenResponse(
        access_token=auth_response.session.access_token,
        refresh_token=auth_response.session.refresh_token,
        user_id=str(profile.id),
        role=profile.role,
        name=profile.name,
    )


@router.post("/register", response_model=TokenResponse)
async def register(
    user_data: UserRegister,
    db: Annotated[AsyncSession, Depends(get_db)],
):
    """Register in Supabase Auth and ensure a local profile exists."""
    requested_role = user_data.role.strip().upper()
    if requested_role not in ["ATTENDEE", "ORGANIZER"]:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="El rol debe ser ATTENDEE u ORGANIZER",
        )

    try:
        auth_response = supabase_client.auth.sign_up(
            {
                "email": user_data.email,
                "password": user_data.password,
            }
        )

        if not auth_response.user:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="No se pudo crear el usuario en el servicio de autenticación.",
            )

        user_id = uuid.UUID(str(auth_response.user.id))
        profile = await upsert_profile_for_register(
            db=db,
            user_id=user_id,
            name=user_data.name,
            role=requested_role,
        )
        return build_token_response(auth_response, profile)

    except HTTPException:
        raise
    except Exception as e:
        error_msg = str(e)
        if "User already registered" in error_msg or "already been registered" in error_msg:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=(
                    "Este email ya está registrado. Si eres staff, usa inicio de sesión "
                    "con las credenciales que te dio el organizador."
                ),
            ) from e
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Error en el registro: {error_msg}",
        ) from e


@router.post("/login", response_model=TokenResponse)
async def login(
    credentials: UserLogin,
    db: Annotated[AsyncSession, Depends(get_db)],
):
    """Authenticate with Supabase and return the local profile."""
    try:
        auth_response = supabase_client.auth.sign_in_with_password(
            {
                "email": credentials.email,
                "password": credentials.password,
            }
        )

        if not auth_response.user:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Credenciales inválidas",
            )

        user_id = uuid.UUID(str(auth_response.user.id))
        metadata = auth_response.user.user_metadata or {}
        default_role = str(metadata.get("role", "ATTENDEE")).upper()
        if default_role not in {"ATTENDEE", "ORGANIZER", "STAFF"}:
            default_role = "ATTENDEE"
        default_name = str(metadata.get("name") or credentials.email.split("@")[0])
        profile = await get_or_create_profile(
            db=db,
            user_id=user_id,
            name=default_name,
            role=default_role,
        )
        profile = await sync_profile_role(db, profile, metadata)
        return build_token_response(auth_response, profile)

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Error en el inicio de sesión: {str(e)}",
        ) from e


@router.post("/refresh", response_model=TokenResponse)
async def refresh_token(
    refresh_data: TokenRefresh,
    db: Annotated[AsyncSession, Depends(get_db)],
):
    """Refresh a session using a Supabase refresh token."""
    try:
        auth_response = supabase_client.auth.refresh_session(refresh_data.refresh_token)

        if not auth_response.user:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Token de actualización inválido o expirado",
            )

        user_id = uuid.UUID(str(auth_response.user.id))
        profile = await get_profile(db, user_id)

        if not profile:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Perfil de usuario no encontrado",
            )

        metadata = auth_response.user.user_metadata or {}
        profile = await sync_profile_role(db, profile, metadata)
        return build_token_response(auth_response, profile)

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Error al actualizar la sesión: {str(e)}",
        ) from e
