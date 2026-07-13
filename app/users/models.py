import datetime
import uuid

from sqlmodel import Field, SQLModel


class Profile(SQLModel, table=True):
    __tablename__ = "profiles"

    id: uuid.UUID = Field(primary_key=True, description="Corresponds to auth.users.id UUID")
    name: str
    role: str = Field(default="ATTENDEE", description="ATTENDEE, ORGANIZER or STAFF")
    created_at: datetime.datetime = Field(
        default_factory=datetime.datetime.utcnow,
        nullable=False,
    )
