import datetime
import uuid

from pydantic import BaseModel, field_validator

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

    @field_validator("id", mode="before")
    @classmethod
    def coerce_id(cls, value: object) -> str:
        return str(value)

    class Config:
        from_attributes = True
