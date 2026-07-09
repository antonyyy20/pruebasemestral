import datetime
import uuid
from typing import Optional
from pydantic import BaseModel, field_validator

class CheckinRequest(BaseModel):
    ticket_id: uuid.UUID
    event_id: uuid.UUID
    qr_signature: str

class CheckinResponse(BaseModel):
    id: uuid.UUID
    ticket_id: uuid.UUID
    validated_by: Optional[str] = None
    checkin_time: datetime.datetime

    @field_validator("validated_by", mode="before")
    @classmethod
    def coerce_validated_by(cls, value: object) -> str | None:
        if value is None:
            return None
        return str(value)

    class Config:
        from_attributes = True
