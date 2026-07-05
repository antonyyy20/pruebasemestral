# Quickvnt Backend — MVP SaaS de Gestión de Eventos MICE

Quickvnt es un MVP SaaS (Software as a Service) para la gestión integral de eventos MICE (Meetings, Incentives, Conferences, and Exhibitions). Este repositorio contiene la API del backend implementada bajo un enfoque de **monolito modular**.

El sistema interactúa con una aplicación móvil Android (Kotlin) como frontend y utiliza **Supabase** como base de datos administrada y proveedor de autenticación.

---

## 🚀 Información del Proyecto

* **Nombre del Equipo**: *[Ingresa el nombre de tu equipo aquí]*
* **Integrantes**:
  * *[Nombre del Integrante 1]*
  * *[Nombre del Integrante 2]*
  * *[Nombre del Integrante 3]*

---

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Python 3.10+
* **Framework Web:** [FastAPI](https://fastapi.tiangolo.com/) (rápido, tipado y asíncrono)
* **Servidor ASGI:** Uvicorn
* **Base de Datos & Auth:** [Supabase](https://supabase.com/) (PostgreSQL + Supabase Auth para la gestión de usuarios y JWT)
* **ORM:** [SQLModel](https://sqlmodel.tiangolo.com/) / [SQLAlchemy](https://www.sqlalchemy.org/) (Modelado de base de datos asíncrono con `asyncpg`)
* **Validación de Datos:** Pydantic v2
* **Seguridad:** `python-jose` (para la decodificación y validación de tokens JWT de Supabase y de códigos QR)
* **Generación de QRs:** `qrcode` & `pillow`

---

## 📂 Estructura de la API

La aplicación está diseñada como un monolito modular con separación de responsabilidades:

```text
├── app/
│   ├── main.py                  # Punto de entrada de la aplicación y middlewares
│   ├── core/                    # Configuración global, seguridad y base de datos
│   │   ├── config.py            # Configuración con variables de entorno (Pydantic Settings)
│   │   ├── database.py          # Conexión asíncrona a PostgreSQL/Supabase
│   │   └── security.py          # Dependencias de validación de roles y JWT de Supabase
│   │
│   ├── auth/                    # Puente/Bridge hacia Supabase Auth para login/registro
│   ├── users/                   # Manejo de perfiles (Profiles: ATTENDEE / ORGANIZER)
│   ├── events/                  # Gestión de eventos (creación, edición, listado)
│   ├── tickets/                 # Registro a eventos y generación de códigos QR firmados
│   ├── checkin/                 # Validación de accesos y escaneo de códigos QR
│   └── analytics/               # Endpoints de estadísticas y reportes para organizadores
│
├── schema.sql                   # Esquema oficial de base de datos relacional para PostgreSQL
├── .env.example                 # Plantilla para variables de entorno locales
└── requirements.txt             # Dependencias del proyecto
```

Cada módulo sigue la arquitectura de capas recomendada:
`Router (HTTP) ➡️ Service (Lógica) ➡️ Model/Repository (Datos)`

---

## ⚙️ Requisitos Previos

* Python 3.10 o superior instalado.
* Una cuenta y un proyecto creado en [Supabase](https://supabase.com/).
* PostgreSQL (a través de la instancia de Supabase).

---

## 💻 Instalación y Configuración Local

Sigue estos pasos para levantar el entorno de desarrollo:

### 1. Clonar el repositorio
```bash
git clone https://github.com/ElRulios/Proyecto-final-ultimate-movil-BE.git
cd Proyecto-final-ultimate-movil-BE
```

### 2. Crear y activar el entorno virtual
En Windows (PowerShell):
```powershell
python -m venv .venv
.venv\Scripts\Activate.ps1
```
En macOS/Linux:
```bash
python3 -m venv .venv
source .venv/bin/activate
```

### 3. Instalar las dependencias
```bash
pip install -r requirements.txt
```

### 4. Configurar las variables de entorno
Crea una copia de `.env.example` y nómbrala `.env`:
```bash
cp .env.example .env
```
Abre el archivo `.env` e ingresa las credenciales obtenidas de tu panel de **Supabase** (Configuración del proyecto -> API y Database):
```env
PROJECT_NAME="Quickvnt API"
API_V1_STR="/api/v1"

SUPABASE_URL="https://tu-proyecto.supabase.co"
SUPABASE_KEY="tu-anon-public-key"
SUPABASE_JWT_SECRET="tu-jwt-secret-de-supabase"

DATABASE_URL="postgresql+asyncpg://postgres:contraseña@db.tu-proyecto.supabase.co:6543/postgres"

QR_JWT_SECRET="clave-hexadecimal-aleatoria-para-firmar-codigos-qr"
```

### 5. Configurar la Base de Datos
Copia el contenido de [`schema.sql`](file:///c:/Users/joako/Documents/proyecto-final-dev-be/schema.sql) y ejecútalo en el **SQL Editor** de tu panel de control de Supabase para inicializar las tablas, índices y políticas de seguridad RLS.

### 6. Ejecutar el servidor de desarrollo
Levanta el servidor con Uvicorn:
```bash
uvicorn app.main:app --reload
```

---

## 🔍 Verificación y Documentación

Una vez encendido el servidor, puedes interactuar y validar el proyecto a través de:

* **Endpoint de prueba (sin Auth):** [http://127.0.0.1:8000/db-test-events](http://127.0.0.1:8000/db-test-events)
* **Documentación interactiva de la API (Swagger UI):** [http://127.0.0.1:8000/docs](http://127.0.0.1:8000/docs)
* **Esquema de especificación:** [http://127.0.0.1:8000/api/v1/openapi.json](http://127.0.0.1:8000/api/v1/openapi.json)
