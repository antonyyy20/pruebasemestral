import contextlib
import logging

from fastapi import FastAPI, Depends, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.sql import text
from typing import Annotated

from app.core.config import settings
from app.core.database import init_db, get_db

logger = logging.getLogger(__name__)

# Routers
from app.auth.router import router as auth_router
from app.users.router import router as users_router
from app.events.router import router as events_router
from app.tickets.router import router as tickets_router
from app.checkin.router import router as checkin_router
from app.analytics.router import router as analytics_router

@contextlib.asynccontextmanager
async def lifespan(app: FastAPI):
    # Initialize database tables
    await init_db()
    yield

app = FastAPI(
    title=settings.PROJECT_NAME,
    openapi_url=f"{settings.API_V1_STR}/openapi.json",
    lifespan=lifespan
)


@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception):
    logger.exception("Unhandled error on %s %s", request.method, request.url.path)
    return JSONResponse(
        status_code=500,
        content={"detail": "Error interno del servidor. Intenta de nuevo."},
    )


# Set all CORS enabled origins
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include Routers
app.include_router(auth_router, prefix=settings.API_V1_STR)
app.include_router(users_router, prefix=settings.API_V1_STR)
app.include_router(events_router, prefix=settings.API_V1_STR)
app.include_router(tickets_router, prefix=settings.API_V1_STR)
app.include_router(checkin_router, prefix=settings.API_V1_STR)
app.include_router(analytics_router, prefix=settings.API_V1_STR)

@app.get("/")
async def root():
    return {"message": "Bienvenido a la API de Quickvnt"}

@app.get("/db-test-events")
async def db_test_events(db: Annotated[AsyncSession, Depends(get_db)]):
    try:
        result = await db.execute(text("SELECT id, title, status FROM public.events"))
        events = [{"id": str(row[0]), "title": row[1], "status": row[2]} for row in result.fetchall()]
        return {"status": "ok", "database": "conectada", "events": events}
    except Exception as e:
        return {"status": "error", "message": str(e)}
