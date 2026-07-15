import datetime
import uuid
from typing import Any
from pydantic import BaseModel, EmailStr, Field, field_validator

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

    @field_validator("date_start", "date_end", mode="after")
    @classmethod
    def ensure_utc(cls, value: datetime.datetime) -> datetime.datetime:
        if value.tzinfo is None:
            return value.replace(tzinfo=datetime.timezone.utc)
        return value.astimezone(datetime.timezone.utc)

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

    @field_validator("organizer_id", mode="before")
    @classmethod
    def coerce_organizer_id(cls, value: object) -> str:
        return str(value)

    class Config:
        from_attributes = True

class StaffCreateRequest(BaseModel):
    email: EmailStr
    password: str = Field(min_length=6)
    name: str = Field(min_length=2)


class StaffMemberResponse(BaseModel):
    id: uuid.UUID
    event_id: uuid.UUID
    user_id: str
    name: str
    role: str
    assigned_at: datetime.datetime

    class Config:
        from_attributes = True


class StaffAssignmentResponse(BaseModel):
    id: uuid.UUID
    event_id: uuid.UUID
    user_id: str
    assigned_at: datetime.datetime

    class Config:
        from_attributes = True
