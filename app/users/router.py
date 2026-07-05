from typing import Annotated
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.core.security import get_current_user
from app.users.models import Profile
from app.users.schemas import ProfileUpdate, ProfileResponse

router = APIRouter(prefix="/users", tags=["Users"])

@router.get("/me", response_model=ProfileResponse)
async def get_me(
    current_user: Annotated[Profile, Depends(get_current_user)]
):
    """Retrieve the current authenticated user's profile."""
    return current_user

@router.put("/me", response_model=ProfileResponse)
async def update_me(
    current_user: Annotated[Profile, Depends(get_current_user)],
    profile_update: ProfileUpdate,
    db: Annotated[AsyncSession, Depends(get_db)]
):
    """Update the current authenticated user's profile name."""
    if profile_update.name is not None:
        current_user.name = profile_update.name
    
    db.add(current_user)
    await db.commit()
    await db.refresh(current_user)
    return current_user
