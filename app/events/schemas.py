import datetime
import uuid
from typing import Any
from pydantic import BaseModel, Field

class EventBase(BaseModel):
    title: str
    description: str
    category: str
    location: str
    date_start: datetime.datetime
    date_end: datetime.datetime
    capacity: int = Field(gt=0)
    banner_url: str | None = None
    custom_form_schema: dict[str, Any] = Field(default_factory=dict)

class EventCreate(EventBase):
    pass

class EventUpdate(BaseModel):
    title: str | None = None
    description: str | None = None
    category: str | None = None
    location: str | None = None
    date_start: datetime.datetime | None = None
    date_end: datetime.datetime | None = None
    capacity: int | None = Field(None, gt=0)
    banner_url: str | None = None
    status: str | None = None # DRAFT | PUBLISHED | CLOSED | CANCELLED
    custom_form_schema: dict[str, Any] | None = None

class EventResponse(EventBase):
    id: uuid.UUID
    organizer_id: str
    status: str
    created_at: datetime.datetime

    class Config:
        from_attributes = True

class StaffAssignmentBase(BaseModel):
    user_id: str

class StaffAssignmentResponse(BaseModel):
    id: uuid.UUID
    event_id: uuid.UUID
    user_id: str
    assigned_at: datetime.datetime

    class Config:
        from_attributes = True
