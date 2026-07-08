import datetime
import uuid
from typing import Any, Optional
from sqlmodel import SQLModel, Field, Relationship, Column
from sqlalchemy.dialects.postgresql import JSONB

class Ticket(SQLModel, table=True):
    __tablename__ = "tickets"

    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    event_id: uuid.UUID = Field(foreign_key="events.id", index=True, nullable=False)
    user_id: uuid.UUID = Field(foreign_key="profiles.id", index=True, nullable=False)
    qr_signature: str = Field(nullable=False)
    status: str = Field(default="REGISTERED", nullable=False)
    
    # Respuestas al custom_form_schema del organizador
    form_response: dict[str, Any] = Field(
        default_factory=dict,
        sa_column=Column(JSONB, nullable=False, server_default="{}")
    )
    registered_at: datetime.datetime = Field(
        default_factory=datetime.datetime.utcnow,
        nullable=False
    )
