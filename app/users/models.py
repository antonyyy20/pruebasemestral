import datetime
import uuid

from sqlalchemy import Column, DateTime
from sqlmodel import Field, SQLModel


def utc_now() -> datetime.datetime:
    return datetime.datetime.now(datetime.timezone.utc)


class Profile(SQLModel, table=True):
    __tablename__ = "profiles"

    id: uuid.UUID = Field(primary_key=True, description="Corresponds to auth.users.id UUID")
    name: str
    role: str = Field(default="ATTENDEE", description="ATTENDEE, ORGANIZER or STAFF")
    created_at: datetime.datetime = Field(
        default_factory=utc_now,
        sa_column=Column(DateTime(timezone=True), nullable=False),
    )
