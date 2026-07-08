from typing import Annotated, Any
import uuid
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlmodel import select, and_, or_

from app.core.database import get_db
from app.core.security import get_current_user, require_role
from app.users.models import Profile
from app.events.models import Event, StaffAssignment
from app.events.schemas import (
    EventCreate, EventUpdate, EventResponse, 
    StaffAssignmentBase, StaffAssignmentResponse
)

router = APIRouter(prefix="/events", tags=["Events"])

@router.get("", response_model=list[EventResponse])
async def list_events(
    db: Annotated[AsyncSession, Depends(get_db)],
    category: str | None = None,
    status_filter: str = Query(default="PUBLISHED", description="Filter events by status (default: PUBLISHED)"),
    skip: int = Query(default=0, ge=0),
    limit: int = Query(default=20, ge=1, le=100)
):
    """
    Retrieve list of events (marketplace).
    Publicly accessible endpoint.
    """
    conditions = []
    if status_filter:
        conditions.append(Event.status == status_filter)
    if category:
        conditions.append(Event.category == category)
        
    query = select(Event)
    if conditions:
        query = query.where(and_(*conditions))
        
    query = query.offset(skip).limit(limit)
    result = await db.execute(query)
    return result.scalars().all()

@router.get("/{id}", response_model=EventResponse)
async def get_event(
    id: uuid.UUID,
    db: Annotated[AsyncSession, Depends(get_db)]
):
    """Get details of a specific event."""
    result = await db.execute(select(Event).where(Event.id == id))
    event = result.scalar_one_or_none()
    if not event:
        raise HTTPException(status_code=404, detail="Evento no encontrado")
    return event

@router.post("", response_model=EventResponse, status_code=status.HTTP_201_CREATED)
async def create_event(
    event_data: EventCreate,
    current_user: Annotated[Profile, Depends(require_role(["ORGANIZER"]))],
    db: Annotated[AsyncSession, Depends(get_db)]
):
    """Create a new event. Restricted to ORGANIZER role."""
    event = Event(
        organizer_id=current_user.id,
        title=event_data.title,
        description=event_data.description,
        category=event_data.category,
        location=event_data.location,
        date_start=event_data.date_start,
        date_end=event_data.date_end,
        capacity=event_data.capacity,
        banner_url=event_data.banner_url,
        custom_form_schema=event_data.custom_form_schema,
        status="DRAFT" # Starts as draft
    )
    db.add(event)
    await db.commit()
    await db.refresh(event)
    return event

@router.put("/{id}", response_model=EventResponse)
async def update_event(
    id: uuid.UUID,
    event_update: EventUpdate,
    current_user: Annotated[Profile, Depends(require_role(["ORGANIZER"]))],
    db: Annotated[AsyncSession, Depends(get_db)]
):
    """Update event details. Only the owner (organizer) can update."""
    result = await db.execute(select(Event).where(Event.id == id))
    event = result.scalar_one_or_none()
    if not event:
        raise HTTPException(status_code=404, detail="Evento no encontrado")
    
    if event.organizer_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="No tienes permiso para actualizar este evento"
        )
        
    for key, value in event_update.model_dump(exclude_unset=True).items():
        setattr(event, key, value)
        
    db.add(event)
    await db.commit()
    await db.refresh(event)
    return event

@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_event(
    id: uuid.UUID,
    current_user: Annotated[Profile, Depends(require_role(["ORGANIZER"]))],
    db: Annotated[AsyncSession, Depends(get_db)]
):
    """Delete event. Only the owner (organizer) can delete."""
    result = await db.execute(select(Event).where(Event.id == id))
    event = result.scalar_one_or_none()
    if not event:
        raise HTTPException(status_code=404, detail="Evento no encontrado")
        
    if event.organizer_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="No tienes permiso para eliminar este evento"
        )
        
    await db.delete(event)
    await db.commit()
    return None

@router.post("/{id}/staff", response_model=StaffAssignmentResponse, status_code=status.HTTP_201_CREATED)
async def assign_staff(
    id: uuid.UUID,
    assignment: StaffAssignmentBase,
    current_user: Annotated[Profile, Depends(require_role(["ORGANIZER"]))],
    db: Annotated[AsyncSession, Depends(get_db)]
):
    """Assign staff to an event. Restricted to the event organizer."""
    # Verify event exists and is owned by current_user
    result = await db.execute(select(Event).where(Event.id == id))
    event = result.scalar_one_or_none()
    if not event:
        raise HTTPException(status_code=404, detail="Evento no encontrado")
        
    if event.organizer_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="No eres el organizador de este evento"
        )
    
    # Verify user being assigned exists
    user_result = await db.execute(
        select(Profile).where(Profile.id == uuid.UUID(assignment.user_id))
    )
    staff_profile = user_result.scalar_one_or_none()
    if not staff_profile:
        raise HTTPException(status_code=404, detail="Usuario a asignar como staff no encontrado")
        
    # Check if already assigned
    existing_result = await db.execute(
        select(StaffAssignment).where(
            and_(
                StaffAssignment.event_id == id,
                StaffAssignment.user_id == uuid.UUID(assignment.user_id),
            )
        )
    )
    if existing_result.scalar_one_or_none():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="El usuario ya está asignado al staff de este evento"
        )
        
    staff_assign = StaffAssignment(
        event_id=id,
        user_id=uuid.UUID(assignment.user_id),
    )
    db.add(staff_assign)
    await db.commit()
    await db.refresh(staff_assign)
    return staff_assign
