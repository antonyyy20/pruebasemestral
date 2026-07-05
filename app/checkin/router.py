import uuid
from typing import Annotated
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlmodel import select, and_, or_

from app.core.database import get_db
from app.core.security import get_current_user
from app.users.models import Profile
from app.events.models import Event, StaffAssignment
from app.tickets.models import Ticket
from app.tickets.qr import verify_ticket_signature
from app.checkin.models import Checkin
from app.checkin.schemas import CheckinRequest, CheckinResponse

router = APIRouter(prefix="/checkin", tags=["Check-in"])

@router.post("/validate", response_model=CheckinResponse, status_code=status.HTTP_201_CREATED)
async def validate_checkin(
    payload: CheckinRequest,
    current_user: Annotated[Profile, Depends(get_current_user)],
    db: Annotated[AsyncSession, Depends(get_db)]
):
    """
    Validate a ticket via QR code and check in the attendee.
    Accessible only by the event organizer or assigned event staff.
    """
    # 1. Fetch the ticket and event to authorize validation
    ticket_query = select(Ticket).where(Ticket.id == payload.ticket_id)
    ticket_result = await db.execute(ticket_query)
    ticket = ticket_result.scalar_one_or_none()

    if not ticket:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Ticket not found"
        )

    if ticket.event_id != payload.event_id:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Ticket does not belong to this event"
        )

    # 2. Check permission: must be Organizer of the event or assigned Staff
    event_query = select(Event).where(Event.id == ticket.event_id)
    event_result = await db.execute(event_query)
    event = event_result.scalar_one_or_none()

    if not event:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Event not found"
        )

    is_organizer = event.organizer_id == current_user.id
    
    # Check if staff
    staff_query = select(StaffAssignment).where(
        StaffAssignment.event_id == event.id,
        StaffAssignment.user_id == current_user.id
    )
    staff_result = await db.execute(staff_query)
    is_staff = staff_result.scalar_one_or_none() is not None

    if not (is_organizer or is_staff):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only event organizers or assigned staff can validate check-ins"
        )

    # 3. Verify cryptographic QR signature
    is_valid_sig = verify_ticket_signature(
        ticket_id=ticket.id,
        event_id=ticket.event_id,
        user_id=ticket.user_id,
        signature=payload.qr_signature
    )
    
    if not is_valid_sig:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid QR code signature"
        )

    # 4. Check if ticket has been cancelled
    if ticket.status == "CANCELLED":
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Ticket has been cancelled"
        )

    # 5. Check if already checked in (Idempotency)
    checkin_query = select(Checkin).where(Checkin.ticket_id == ticket.id)
    checkin_result = await db.execute(checkin_query)
    existing_checkin = checkin_result.scalar_one_or_none()

    if existing_checkin:
        return existing_checkin

    # 6. Create Checkin and update Ticket status
    new_checkin = Checkin(
        ticket_id=ticket.id,
        validated_by=current_user.id
    )
    
    ticket.status = "CHECKED_IN"
    
    db.add(new_checkin)
    db.add(ticket)
    await db.commit()
    await db.refresh(new_checkin)
    
    return new_checkin
