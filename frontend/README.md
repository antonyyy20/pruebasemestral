# Quickvnt Android — Frontend MVP

App móvil en **Kotlin + Jetpack Compose + Retrofit** para la plataforma Quickvnt (gestión de eventos MICE).

## Requisitos

- Android Studio Ladybug o superior
- JDK 11+
- Backend Quickvnt corriendo en `http://127.0.0.1:8000`

## Configuración

1. Levanta el backend:
   ```bash
   cd ..
   uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
   ```

2. Abre este proyecto (`jhdkasjhd/`) en Android Studio.

3. La URL base de la API está en `app/build.gradle.kts`:
   - Emulador: `http://10.0.2.2:8000/api/v1/`
   - Dispositivo físico: cambia a la IP de tu PC, por ejemplo `http://192.168.1.10:8000/api/v1/`

4. Ejecuta la app en emulador o dispositivo.

## Arquitectura

```text
app/src/main/java/com/example/jhdkasjhd/
├── core/
│   ├── network/     # Retrofit, OkHttp, AuthInterceptor
│   ├── data/        # TokenStore (DataStore)
│   └── util/        # QR + formularios dinámicos
├── data/
│   ├── dto/         # Modelos API
│   └── repository/  # Auth, Events, Tickets
├── navigation/      # NavHost + rutas
└── ui/
    ├── auth/        # Login y registro
    ├── marketplace/ # Marketplace de eventos
    ├── tickets/     # Registro, mis tickets, QR
    ├── organizer/   # CRUD eventos (organizador)
    ├── checkin/     # Scanner QR staff
    ├── analytics/   # Dashboard KPIs
    └── profile/     # Perfil y logout
```

## Funcionalidades MVP

| Feature | Pantalla | API |
|---------|----------|-----|
| Login / Registro | `LoginScreen`, `RegisterScreen` | `/auth/*` |
| Marketplace | `MarketplaceScreen` | `GET /events` |
| Registro a evento | `RegisterEventScreen` | `POST /tickets/register/{id}` |
| Mis tickets + QR | `MyTicketsScreen`, `TicketDetailScreen` | `GET /tickets/me` |
| CRUD eventos | `MyEventsScreen`, `CreateEventScreen` | `POST/PUT/DELETE /events` |
| Check-in QR | `QrScannerScreen` | `POST /checkin/validate` |
| Analytics | `AnalyticsScreen` | `GET /analytics/events/{id}` |

## Roles

- **ATTENDEE**: Marketplace → Tickets → Perfil
- **ORGANIZER**: Mis Eventos → Marketplace → Perfil (+ analytics y scanner)

## Stack

- Kotlin, Jetpack Compose, Material 3
- Retrofit 2 + OkHttp + Moshi
- MVVM + AppContainer (DI manual)
- Navigation Compose
- DataStore (sesión JWT)
- ZXing (QR generate + scan)
