import datetime
import uuid
from typing import Optional
from sqlmodel import SQLModel, Field

class Checkin(SQLModel, table=True):
    __tablename__ = "checkins"

    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    ticket_id: uuid.UUID = Field(foreign_key="tickets.id", unique=True, index=True, nullable=False)
    validated_by: Optional[str] = Field(default=None, foreign_key="profiles.id", index=True)
    checkin_time: datetime.datetime = Field(
        default_factory=datetime.datetime.utcnow,
        nullable=False
    )
