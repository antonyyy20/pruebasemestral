import datetime
import uuid
from typing import Any
from pydantic import BaseModel, Field, field_validator

class TicketBase(BaseModel):
    event_id: uuid.UUID
    form_response: dict[str, Any] = Field(default_factory=dict)

class TicketCreate(TicketBase):
    pass

class TicketResponse(BaseModel):
    id: uuid.UUID
    event_id: uuid.UUID
    user_id: str
    qr_signature: str
    status: str
    form_response: dict[str, Any]
    registered_at: datetime.datetime

    @field_validator("user_id", mode="before")
    @classmethod
    def coerce_user_id(cls, value: object) -> str:
        return str(value)

    class Config:
        from_attributes = True
