import datetime


def to_db_datetime(value: datetime.datetime) -> datetime.datetime:
    """Normalize datetimes for asyncpg + PostgreSQL timestamptz (naive UTC)."""
    if value.tzinfo is None:
        return value
    return value.astimezone(datetime.timezone.utc).replace(tzinfo=None)


def utcnow_naive() -> datetime.datetime:
    return datetime.datetime.now(datetime.timezone.utc).replace(tzinfo=None)
