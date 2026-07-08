import uuid
from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from supabase import create_client, Client

from app.core.config import settings
from app.core.database import get_db
from app.auth.schemas import UserRegister, UserLogin, TokenRefresh, TokenResponse
from app.users.models import Profile

router = APIRouter(prefix="/auth", tags=["Authentication"])

# Initialize supabase client
supabase_client: Client = create_client(settings.SUPABASE_URL, settings.SUPABASE_KEY)

@router.post("/register", response_model=TokenResponse)
async def register(
    user_data: UserRegister,
    db: Annotated[AsyncSession, Depends(get_db)]
):
    """
    Register a new user in Supabase Auth and create a corresponding profile in the local database.
    """
    if user_data.role not in ["ATTENDEE", "ORGANIZER"]:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="El rol debe ser ATTENDEE u ORGANIZER"
        )
    
    try:
        # Register user in Supabase Auth
        auth_response = supabase_client.auth.sign_up({
            "email": user_data.email,
            "password": user_data.password
        })
        
        if not auth_response.user:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="No se pudo crear el usuario en el servicio de autenticación."
            )
        
        user_id = uuid.UUID(str(auth_response.user.id))

        # Create profile in our database
        profile = Profile(
            id=user_id,
            name=user_data.name,
            role=user_data.role
        )
        
        db.add(profile)
        await db.commit()
        await db.refresh(profile)
        
        # Return tokens
        return TokenResponse(
            access_token=auth_response.session.access_token,
            refresh_token=auth_response.session.refresh_token,
            user_id=str(user_id),
            role=profile.role,
            name=profile.name
        )
        
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Error en el registro: {str(e)}"
        )

@router.post("/login", response_model=TokenResponse)
async def login(
    credentials: UserLogin,
    db: Annotated[AsyncSession, Depends(get_db)]
):
    """
    Authenticate user using Supabase Auth and retrieve their local profile role.
    """
    try:
        # Delegate login to Supabase Auth
        auth_response = supabase_client.auth.sign_in_with_password({
            "email": credentials.email,
            "password": credentials.password
        })
        
        if not auth_response.user:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Credenciales inválidas"
            )
        
        user_id = uuid.UUID(str(auth_response.user.id))

        # Fetch profile
        from sqlmodel import select
        result = await db.execute(select(Profile).where(Profile.id == user_id))
        profile = result.scalar_one_or_none()

        if not profile:
            # Create a default profile if for some reason it didn't exist
            profile = Profile(
                id=user_id,
                name=credentials.email.split("@")[0],
                role="ATTENDEE"
            )
            db.add(profile)
            await db.commit()
            await db.refresh(profile)
            
        return TokenResponse(
            access_token=auth_response.session.access_token,
            refresh_token=auth_response.session.refresh_token,
            user_id=str(user_id),
            role=profile.role,
            name=profile.name
        )
        
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Error en el inicio de sesión: {str(e)}"
        )

@router.post("/refresh", response_model=TokenResponse)
async def refresh_token(
    refresh_data: TokenRefresh,
    db: Annotated[AsyncSession, Depends(get_db)]
):
    """
    Refresh a session using a Supabase refresh token.
    """
    try:
        auth_response = supabase_client.auth.refresh_session(refresh_data.refresh_token)
        
        if not auth_response.user:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Token de actualización inválido o expirado"
            )
            
        user_id = uuid.UUID(str(auth_response.user.id))

        # Fetch profile
        from sqlmodel import select
        result = await db.execute(select(Profile).where(Profile.id == user_id))
        profile = result.scalar_one_or_none()
        
        if not profile:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Perfil de usuario no encontrado"
            )
            
        return TokenResponse(
            access_token=auth_response.session.access_token,
            refresh_token=auth_response.session.refresh_token,
            user_id=str(user_id),
            role=profile.role,
            name=profile.name
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Error al actualizar la sesión: {str(e)}"
        )
