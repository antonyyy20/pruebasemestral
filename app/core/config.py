import re
from urllib.parse import quote_plus, urlparse, urlunparse

from pydantic import Field, computed_field
from pydantic_settings import BaseSettings, SettingsConfigDict

_DIRECT_DB_HOST = re.compile(r"^db\.([a-z0-9]+)\.supabase\.co$", re.IGNORECASE)
_POOLER_HOST = re.compile(r"\.pooler\.supabase\.com$", re.IGNORECASE)
_SUPABASE_PROJECT_REF = re.compile(r"https?://([a-z0-9]+)\.supabase\.co", re.IGNORECASE)


class Settings(BaseSettings):
    PROJECT_NAME: str = "Quickvnt API"
    API_V1_STR: str = "/api/v1"

    # Supabase Configuration
    SUPABASE_URL: str = Field(default="https://your-supabase-project.supabase.co")
    SUPABASE_KEY: str = Field(default="your-anon-key")
    SUPABASE_SERVICE_ROLE_KEY: str = Field(default="")
    SUPABASE_JWT_SECRET: str = Field(default="your-jwt-secret")

    # PostgreSQL (Supabase pooler) — DATABASE_URL o variables DB_* por separado
    DATABASE_URL: str | None = Field(default=None)
    DB_HOST: str = Field(default="aws-0-us-east-1.pooler.supabase.com")
    DB_PORT: int = Field(default=6543)
    DB_USER: str = Field(default="postgres.your-project-ref")
    DB_PASSWORD: str = Field(default="your-db-password")
    DB_NAME: str = Field(default="postgres")

    # JWT security
    QR_JWT_SECRET: str = Field(default="qr-signing-secret-change-this-in-production")

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    @property
    def supabase_project_ref(self) -> str | None:
        match = _SUPABASE_PROJECT_REF.match(self.SUPABASE_URL.strip())
        return match.group(1) if match else None

    def _to_asyncpg_scheme(self, url: str) -> str:
        if url.startswith("postgres://"):
            return url.replace("postgres://", "postgresql+asyncpg://", 1)
        if url.startswith("postgresql://"):
            return url.replace("postgresql://", "postgresql+asyncpg://", 1)
        return url

    def _append_pooler_params(self, url: str) -> str:
        if "prepared_statement_cache_size" not in url:
            separator = "&" if "?" in url else "?"
            url = f"{url}{separator}prepared_statement_cache_size=0"
        return url

    def _normalize_database_url(self, url: str) -> str:
        """Convierte conexión directa db.*.supabase.co al pooler (puerto transaccional)."""
        url = self._to_asyncpg_scheme(url.strip())
        parsed = urlparse(url)

        if not parsed.hostname:
            return self._append_pooler_params(url)

        host = parsed.hostname
        port = parsed.port or 5432
        username = parsed.username or ""
        password = parsed.password or ""
        database = (parsed.path or "/postgres").lstrip("/") or "postgres"

        direct_match = _DIRECT_DB_HOST.match(host)
        if direct_match:
            project_ref = direct_match.group(1)
            host = self.DB_HOST
            port = self.DB_PORT
            if not username.startswith("postgres."):
                username = f"postgres.{project_ref}"
        elif _POOLER_HOST.search(host):
            # Session pooler (:5432) → transaction pooler (:6543) recomendado para asyncpg
            if port == 5432:
                port = self.DB_PORT
            if username == "postgres":
                project_ref = self.supabase_project_ref
                if project_ref:
                    username = f"postgres.{project_ref}"

        netloc = f"{quote_plus(username)}:{quote_plus(password)}@{host}:{port}"
        normalized = urlunparse(
            (
                parsed.scheme,
                netloc,
                f"/{database}",
                parsed.params,
                parsed.query,
                parsed.fragment,
            )
        )
        return self._append_pooler_params(normalized)

    @computed_field  # type: ignore[prop-decorator]
    @property
    def async_database_url(self) -> str:
        """URL asyncpg; normaliza DATABASE_URL al pooler de Supabase si hace falta."""
        if self.DATABASE_URL:
            return self._normalize_database_url(self.DATABASE_URL)

        password = quote_plus(self.DB_PASSWORD)
        return self._append_pooler_params(
            f"postgresql+asyncpg://{self.DB_USER}:{password}"
            f"@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"
        )


settings = Settings()
