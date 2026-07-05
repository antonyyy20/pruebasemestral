import datetime
from pydantic import BaseModel

class ProfileBase(BaseModel):
    name: str
    role: str

class ProfileCreate(ProfileBase):
    id: str # Obtained from Supabase Auth after register

class ProfileUpdate(BaseModel):
    name: str | None = None

class ProfileResponse(ProfileBase):
    id: str
    created_at: datetime.datetime

    class Config:
        from_attributes = True
