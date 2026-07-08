import uuid
from typing import Annotated
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlmodel import select, func

from app.core.database import get_db
from app.core.security import get_current_user
from app.users.models import Profile
from app.events.models import Event
from app.tickets.models import Ticket
from app.tickets.schemas import TicketCreate, TicketResponse
from app.tickets.qr import sign_ticket

router = APIRouter(prefix="/tickets", tags=["Tickets"])

@router.post("/register/{event_id}", response_model=TicketResponse, status_code=status.HTTP_201_CREATED)
async def register_to_event(
    event_id: uuid.UUID,
    ticket_data: TicketCreate,
    current_user: Annotated[Profile, Depends(get_current_user)],
    db: Annotated[AsyncSession, Depends(get_db)]
):
    """
    Register the current user to an event with pessimistic locking to ensure
    we do not exceed the event's capacity.
    """
    # 1. Start a transaction using db.begin() if not already in one, but usually
    # FastAPI Depends(get_db) session handles transactions. Let's do pessimistic lock.
    try:
        # Check if user already has a ticket for this event
        existing_query = select(Ticket).where(
            Ticket.event_id == event_id,
            Ticket.user_id == current_user.id,
            Ticket.status != "CANCELLED"
        )
        existing_result = await db.execute(existing_query)
        if existing_result.scalar_one_or_none():
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Ya estás registrado en este evento"
            )

        # SELECT FOR UPDATE to lock the event row
        event_query = select(Event).where(Event.id == event_id).with_for_update()
        event_result = await db.execute(event_query)
        event = event_result.scalar_one_or_none()

        if not event:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Evento no encontrado"
            )
        
        if event.status != "PUBLISHED":
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="No puedes registrarte en un evento que no está publicado"
            )

        # Count current registrations
        count_query = select(func.count(Ticket.id)).where(
            Ticket.event_id == event_id,
            Ticket.status != "CANCELLED"
        )
        count_result = await db.execute(count_query)
        current_registrations = count_result.scalar() or 0

        if current_registrations >= event.capacity:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="El evento ya alcanzó su capacidad máxima"
            )

        # Generate ticket ID first to sign it
        ticket_id = uuid.uuid4()
        qr_sig = sign_ticket(ticket_id, event_id, current_user.id)

        new_ticket = Ticket(
            id=ticket_id,
            event_id=event_id,
            user_id=current_user.id,
            form_response=ticket_data.form_response,
            qr_signature=qr_sig,
            status="REGISTERED"
        )

        db.add(new_ticket)
        await db.commit()
        await db.refresh(new_ticket)
        return new_ticket

    except HTTPException:
        await db.rollback()
        raise
    except Exception as e:
        await db.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Ocurrió un error durante el registro: {str(e)}"
        )

@router.get("/me", response_model=list[TicketResponse])
async def get_my_tickets(
    current_user: Annotated[Profile, Depends(get_current_user)],
    db: Annotated[AsyncSession, Depends(get_db)]
):
    """Retrieve all tickets belonging to the authenticated user (Bearer token required)."""
    query = select(Ticket).where(Ticket.user_id == current_user.id)
    result = await db.execute(query)
    return result.scalars().all()

@router.get("/{id}", response_model=TicketResponse)
async def get_ticket(
    id: uuid.UUID,
    current_user: Annotated[Profile, Depends(get_current_user)],
    db: Annotated[AsyncSession, Depends(get_db)]
):
    """Retrieve details of a specific ticket. Accessible by ticket owner or event organizer."""
    query = select(Ticket).where(Ticket.id == id)
    result = await db.execute(query)
    ticket = result.scalar_one_or_none()

    if not ticket:
        raise HTTPException(status_code=404, detail="Boleto no encontrado")

    # Authorize: ticket owner or event organizer
    if ticket.user_id != current_user.id:
        # Check if current user is the organizer of the event
        event_query = select(Event).where(Event.id == ticket.event_id)
        event_result = await db.execute(event_query)
        event = event_result.scalar_one_or_none()
        if not event or event.organizer_id != current_user.id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="No tienes permiso para ver este boleto"
            )

    return ticket
