import uuid
from typing import Annotated, Any
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlmodel import select, func

from app.core.database import get_db
from app.core.security import get_current_user
from app.users.models import Profile
from app.events.models import Event
from app.tickets.models import Ticket
from app.checkin.models import Checkin

router = APIRouter(prefix="/analytics", tags=["Analytics"])

@router.get("/events/{event_id}", response_model=dict[str, Any])
async def get_event_analytics(
    event_id: uuid.UUID,
    current_user: Annotated[Profile, Depends(get_current_user)],
    db: Annotated[AsyncSession, Depends(get_db)]
):
    """
    Retrieve aggregated analytics/KPIs for a specific event.
    Only accessible by the event organizer.
    """
    # 1. Verify event exists and current user is the organizer
    event_query = select(Event).where(Event.id == event_id)
    event_result = await db.execute(event_query)
    event = event_result.scalar_one_or_none()

    if not event:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Evento no encontrado"
        )

    if event.organizer_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Solo el organizador del evento puede ver las analíticas"
        )

    # 2. Total capacity
    capacity = event.capacity

    # 3. Total registered tickets (excluding cancelled)
    registered_query = select(func.count(Ticket.id)).where(
        Ticket.event_id == event_id,
        Ticket.status != "CANCELLED"
    )
    registered_result = await db.execute(registered_query)
    total_registered = registered_result.scalar() or 0

    # 4. Total checked-in attendees
    checkin_query = select(func.count(Checkin.id)).join(
        Ticket, Ticket.id == Checkin.ticket_id
    ).where(
        Ticket.event_id == event_id
    )
    checkin_result = await db.execute(checkin_query)
    total_checked_in = checkin_result.scalar() or 0

    # 5. Calculation rates
    occupancy_rate = (total_registered / capacity) * 100 if capacity > 0 else 0
    attendance_rate = (total_checked_in / total_registered) * 100 if total_registered > 0 else 0

    return {
        "event_id": event_id,
        "capacity": capacity,
        "total_registered": total_registered,
        "total_checked_in": total_checked_in,
        "occupancy_rate_percent": round(occupancy_rate, 2),
        "attendance_rate_percent": round(attendance_rate, 2),
        "status": event.status
    }
