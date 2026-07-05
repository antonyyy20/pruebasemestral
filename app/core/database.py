from collections.abc import AsyncGenerator
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import sessionmaker
from sqlmodel import SQLModel
from app.core.config import settings

# Create async engine
engine = create_async_engine(
    settings.DATABASE_URL,
    echo=False,
    future=True,
    pool_pre_ping=True
)

# Create async session factory
AsyncSessionLocal = sessionmaker(
    bind=engine,
    class_=AsyncSession,
    expire_on_commit=False
)

async def get_db() -> AsyncGenerator[AsyncSession, None]:
    """Dependency to get async database session."""
    async with AsyncSessionLocal() as session:
        yield session

async def init_db() -> None:
    """Initialize database tables. Note: In production, Alembic is preferred."""
    async with engine.begin() as conn:
        # Import models here to register them with SQLModel's metadata
        from app.users.models import Profile
        from app.events.models import Event, StaffAssignment
        from app.tickets.models import Ticket
        from app.checkin.models import Checkin
        
        await conn.run_sync(SQLModel.metadata.create_all)
