import uuid
from functools import lru_cache
from typing import Annotated, Any

from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from jwt import InvalidTokenError, PyJWKClient, decode as jwt_decode, get_unverified_header
from sqlalchemy.ext.asyncio import AsyncSession
from sqlmodel import select

from app.core.config import settings
from app.core.database import get_db
from app.users.models import Profile

security_scheme = HTTPBearer()


@lru_cache
def _jwks_client() -> PyJWKClient:
    jwks_url = f"{settings.SUPABASE_URL.rstrip('/')}/auth/v1/.well-known/jwks.json"
    return PyJWKClient(jwks_url, cache_keys=True)


def decode_supabase_access_token(token: str) -> dict[str, Any]:
    """Validate Supabase access tokens (HS256 legacy or ES256/RS256 via JWKS)."""
    try:
        header = get_unverified_header(token)
    except InvalidTokenError as exc:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Token de autenticación inválido: {exc}",
        ) from exc

    algorithm = header.get("alg", "HS256")

    try:
        if algorithm == "HS256":
            return jwt_decode(
                token,
                settings.SUPABASE_JWT_SECRET,
                algorithms=["HS256"],
                options={"verify_aud": False},
            )

        if algorithm in {"ES256", "RS256"}:
            signing_key = _jwks_client().get_signing_key_from_jwt(token)
            return jwt_decode(
                token,
                signing_key.key,
                algorithms=[algorithm],
                options={"verify_aud": False},
            )

        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Token de autenticación inválido: algoritmo no soportado ({algorithm})",
        )
    except InvalidTokenError as exc:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Token de autenticación inválido: {exc}",
        ) from exc


async def get_current_user(
    credentials: Annotated[HTTPAuthorizationCredentials, Depends(security_scheme)],
    db: Annotated[AsyncSession, Depends(get_db)],
) -> Profile:
    token = credentials.credentials
    payload = decode_supabase_access_token(token)

    user_id = payload.get("sub")
    if user_id is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token inválido: falta el identificador de usuario",
        )

    result = await db.execute(select(Profile).where(Profile.id == uuid.UUID(str(user_id))))
    profile = result.scalar_one_or_none()

    if not profile:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Perfil de usuario no encontrado. Por favor regístrate.",
        )

    return profile


def require_role(allowed_roles: list[str]):
    """Dependency factory to restrict endpoints to specific roles."""

    async def role_checker(
        current_user: Annotated[Profile, Depends(get_current_user)],
    ) -> Profile:
        if current_user.role not in allowed_roles:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Operación no permitida para tu rol de usuario",
            )
        return current_user

    return role_checker
