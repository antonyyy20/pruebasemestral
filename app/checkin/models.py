import datetime
import uuid

from sqlmodel import Field, SQLModel

class Checkin(SQLModel, table=True):
    __tablename__ = "checkins"

    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    ticket_id: uuid.UUID = Field(foreign_key="tickets.id", unique=True, index=True, nullable=False)
    validated_by: uuid.UUID | None = Field(default=None, foreign_key="profiles.id", index=True)
    checkin_time: datetime.datetime = Field(
        default_factory=lambda: datetime.datetime.now(datetime.timezone.utc),
        nullable=False
    )
