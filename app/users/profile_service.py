import uuid

from sqlalchemy.ext.asyncio import AsyncSession
from sqlmodel import select

from app.events.models import StaffAssignment
from app.users.models import Profile


async def has_staff_assignments(db: AsyncSession, user_id: uuid.UUID) -> bool:
    result = await db.execute(
        select(StaffAssignment.id)
        .where(StaffAssignment.user_id == user_id)
        .limit(1)
    )
    return result.scalar_one_or_none() is not None


async def sync_profile_role(db: AsyncSession, profile: Profile, metadata: dict | None = None) -> Profile:
    """
    Keep profile.role consistent with staff assignments and Supabase metadata.
    Fixes staff accounts that logged in before being assigned or were downgraded.
    """
    metadata = metadata or {}
    metadata_role = str(metadata.get("role", "")).upper()
    assigned = await has_staff_assignments(db, profile.id)

    new_role = profile.role
    if assigned and profile.role != "ORGANIZER":
        new_role = "STAFF"
    elif metadata_role == "STAFF" and profile.role == "ATTENDEE":
        new_role = "STAFF"

    if new_role != profile.role:
        profile.role = new_role
        db.add(profile)
        await db.commit()
        await db.refresh(profile)

    return profile
