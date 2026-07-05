import datetime
from sqlmodel import SQLModel, Field

class Profile(SQLModel, table=True):
    __tablename__ = "profiles"
    
    id: str = Field(primary_key=True, description="Corresponds to auth.users.id UUID")
    name: str
    role: str = Field(default="ATTENDEE", description="ATTENDEE or ORGANIZER")
    created_at: datetime.datetime = Field(
        default_factory=datetime.datetime.utcnow,
        nullable=False
    )
