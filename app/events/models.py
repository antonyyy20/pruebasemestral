import datetime
import uuid
from typing import Any

from sqlalchemy.dialects.postgresql import JSONB
from sqlmodel import SQLModel, Field, Column


class Event(SQLModel, table=True):
    __tablename__ = "events"
    
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    organizer_id: uuid.UUID = Field(foreign_key="profiles.id", nullable=False)
    title: str = Field(index=True)
    description: str
    category: str
    location: str
    date_start: datetime.datetime
    date_end: datetime.datetime
    capacity: int = Field(description="Max capacity of the event")
    banner_url: str | None = None
    status: str = Field(default="DRAFT", description="DRAFT, PUBLISHED, CLOSED, CANCELLED")
    
    custom_form_schema: dict[str, Any] = Field(
        default_factory=dict,
        sa_column=Column(JSONB, nullable=False, server_default="{}")
    )
    
    created_at: datetime.datetime = Field(
        default_factory=lambda: datetime.datetime.now(datetime.timezone.utc),
        nullable=False
    )

class StaffAssignment(SQLModel, table=True):
    __tablename__ = "staff_assignments"
    
    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    event_id: uuid.UUID = Field(foreign_key="events.id", nullable=False)
    user_id: uuid.UUID = Field(foreign_key="profiles.id", nullable=False)
    assigned_at: datetime.datetime = Field(
        default_factory=datetime.datetime.utcnow,
        nullable=False
    )
