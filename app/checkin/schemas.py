import datetime
import uuid
from typing import Optional
from pydantic import BaseModel

class CheckinRequest(BaseModel):
    ticket_id: uuid.UUID
    event_id: uuid.UUID
    qr_signature: str

class CheckinResponse(BaseModel):
    id: uuid.UUID
    ticket_id: uuid.UUID
    validated_by: Optional[str]
    checkin_time: datetime.datetime

    class Config:
        from_attributes = True
