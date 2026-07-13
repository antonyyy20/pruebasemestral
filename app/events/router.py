from typing import Annotated
import uuid
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.exc import IntegrityError, SQLAlchemyError
from sqlalchemy.ext.asyncio import AsyncSession
from sqlmodel import select, and_

from app.core.database import get_db
from app.core.security import get_current_user, require_role
from app.users.models import Profile
from app.events.models import Event, StaffAssignment
from app.events.schemas import (
    EventCreate, EventUpdate, EventResponse,
    StaffCreateRequest, StaffMemberResponse,
)
from app.events.staff_service import resolve_or_create_staff_profile
from app.users.profile_service import sync_profile_role

router = APIRouter(prefix="/events", tags=["Events"])


@router.get("/mine", response_model=list[EventResponse])
async def list_my_events(
    current_user: Annotated[Profile, Depends(require_role(["ORGANIZER"]))],
    db: Annotated[AsyncSession, Depends(get_db)],
    skip: int = Query(default=0, ge=0),
    limit: int = Query(default=50, ge=1, le=100),
):
    """List events created by the authenticated organizer."""
    query = (
        select(Event)
        .where(Event.organizer_id == current_user.id)
        .order_by(Event.date_start.desc())
        .offset(skip)
        .limit(limit)
    )
    result = await db.execute(query)
    return result.scalars().all()


@router.get("/staff/mine", response_model=list[EventResponse])
async def list_staff_events(
    current_user: Annotated[Profile, Depends(get_current_user)],
    db: Annotated[AsyncSession, Depends(get_db)],
    skip: int = Query(default=0, ge=0),
    limit: int = Query(default=50, ge=1, le=100),
):
    """List events where the authenticated staff member is assigned."""
    current_user = await sync_profile_role(db, current_user)

    query = (
        select(Event)
        .join(StaffAssignment, StaffAssignment.event_id == Event.id)
        .where(StaffAssignment.user_id == current_user.id)
        .order_by(Event.date_start.desc())
        .offset(skip)
        .limit(limit)
    )
    result = await db.execute(query)
    return result.scalars().all()


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
    if event_data.date_end < event_data.date_start:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="La fecha de fin debe ser igual o posterior a la de inicio",
        )

    event = Event(
        organizer_id=current_user.id,
        title=event_data.title.strip(),
        description=event_data.description.strip(),
        category=event_data.category.strip(),
        location=event_data.location.strip(),
        date_start=event_data.date_start,
        date_end=event_data.date_end,
        capacity=event_data.capacity,
        banner_url=event_data.banner_url,
        custom_form_schema=event_data.custom_form_schema or {},
        status="DRAFT",
    )

    try:
        db.add(event)
        await db.commit()
        await db.refresh(event)
        return event
    except HTTPException:
        await db.rollback()
        raise
    except IntegrityError as exc:
        await db.rollback()
        detail = str(getattr(exc, "orig", exc))
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"No se pudo crear el evento: {detail}",
        ) from exc
    except SQLAlchemyError as exc:
        await db.rollback()
        detail = str(getattr(exc, "orig", exc))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error al guardar el evento: {detail}",
        ) from exc
    except Exception as exc:
        await db.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error inesperado al crear el evento: {str(exc)}",
        ) from exc

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

@router.get("/{id}/staff", response_model=list[StaffMemberResponse])
async def list_event_staff(
    id: uuid.UUID,
    current_user: Annotated[Profile, Depends(require_role(["ORGANIZER"]))],
    db: Annotated[AsyncSession, Depends(get_db)],
):
    """List staff assigned to an event. Restricted to the event organizer."""
    result = await db.execute(select(Event).where(Event.id == id))
    event = result.scalar_one_or_none()
    if not event:
        raise HTTPException(status_code=404, detail="Evento no encontrado")

    if event.organizer_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="No eres el organizador de este evento",
        )

    query = (
        select(StaffAssignment, Profile)
        .join(Profile, Profile.id == StaffAssignment.user_id)
        .where(StaffAssignment.event_id == id)
        .order_by(StaffAssignment.assigned_at.desc())
    )
    rows = await db.execute(query)
    members: list[StaffMemberResponse] = []
    for assignment, profile in rows.all():
        members.append(
            StaffMemberResponse(
                id=assignment.id,
                event_id=assignment.event_id,
                user_id=str(assignment.user_id),
                name=profile.name,
                role=profile.role,
                assigned_at=assignment.assigned_at,
            )
        )
    return members


@router.post("/{id}/staff", response_model=StaffMemberResponse, status_code=status.HTTP_201_CREATED)
async def create_and_assign_staff(
    id: uuid.UUID,
    staff_data: StaffCreateRequest,
    current_user: Annotated[Profile, Depends(require_role(["ORGANIZER"]))],
    db: Annotated[AsyncSession, Depends(get_db)],
):
    """Create a staff account (email + password) and assign it to an event."""
    result = await db.execute(select(Event).where(Event.id == id))
    event = result.scalar_one_or_none()
    if not event:
        raise HTTPException(status_code=404, detail="Evento no encontrado")

    if event.organizer_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="No eres el organizador de este evento",
        )

    try:
        staff_profile = await resolve_or_create_staff_profile(
            db=db,
            email=str(staff_data.email),
            password=staff_data.password,
            name=staff_data.name,
        )

        existing_result = await db.execute(
            select(StaffAssignment).where(
                and_(
                    StaffAssignment.event_id == id,
                    StaffAssignment.user_id == staff_profile.id,
                )
            )
        )
        if existing_result.scalar_one_or_none():
            await db.rollback()
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Este usuario ya está asignado al staff de este evento",
            )

        staff_assign = StaffAssignment(
            event_id=id,
            user_id=staff_profile.id,
        )
        db.add(staff_assign)
        await db.commit()
        await db.refresh(staff_assign)
        await db.refresh(staff_profile)

        return StaffMemberResponse(
            id=staff_assign.id,
            event_id=staff_assign.event_id,
            user_id=str(staff_assign.user_id),
            name=staff_profile.name,
            role=staff_profile.role,
            assigned_at=staff_assign.assigned_at,
        )
    except HTTPException:
        await db.rollback()
        raise
    except IntegrityError as exc:
        await db.rollback()
        detail = str(getattr(exc, "orig", exc))
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"No se pudo asignar el staff: {detail}",
        ) from exc
    except SQLAlchemyError as exc:
        await db.rollback()
        detail = str(getattr(exc, "orig", exc))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error al guardar el staff: {detail}",
        ) from exc


@router.delete("/{id}/staff/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
async def remove_staff_assignment(
    id: uuid.UUID,
    user_id: uuid.UUID,
    current_user: Annotated[Profile, Depends(require_role(["ORGANIZER"]))],
    db: Annotated[AsyncSession, Depends(get_db)],
):
    """Remove a staff assignment from an event."""
    result = await db.execute(select(Event).where(Event.id == id))
    event = result.scalar_one_or_none()
    if not event:
        raise HTTPException(status_code=404, detail="Evento no encontrado")

    if event.organizer_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="No eres el organizador de este evento",
        )

    assignment_result = await db.execute(
        select(StaffAssignment).where(
            and_(
                StaffAssignment.event_id == id,
                StaffAssignment.user_id == user_id,
            )
        )
    )
    assignment = assignment_result.scalar_one_or_none()
    if not assignment:
        raise HTTPException(status_code=404, detail="Asignación de staff no encontrada")

    await db.delete(assignment)
    await db.commit()
    return None
