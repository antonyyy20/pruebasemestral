from typing import Optional
from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import Field

class Settings(BaseSettings):
    PROJECT_NAME: str = "Quickvnt API"
    API_V1_STR: str = "/api/v1"
    
    # Supabase Configuration
    SUPABASE_URL: str = Field(default="https://your-supabase-project.supabase.co")
    SUPABASE_KEY: str = Field(default="your-anon-key")
    SUPABASE_JWT_SECRET: str = Field(default="your-jwt-secret")
    
    # Database Configuration (PostgreSQL Async)
    DATABASE_URL: str = Field(default="postgresql+asyncpg://postgres:password@localhost:5432/postgres")
    
    # JWT security
    QR_JWT_SECRET: str = Field(default="qr-signing-secret-change-this-in-production")
    
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore"
    )

settings = Settings()
