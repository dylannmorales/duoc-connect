# Duoc Connect

Plataforma web de colaboración académica para estudiantes de Duoc UC.

## Stack

| Capa | Tecnología |
|------|------------|
| Frontend | React, TypeScript, TailwindCSS, Vite |
| Backend | Spring Boot 3, Java 17 |
| Base de datos | PostgreSQL 16 |
| Seguridad | Spring Security, JWT |

## Requisitos

- **Java 17** o superior (JDK)
- Maven 3.9+ (o usar `mvnw.cmd` tras generar el wrapper)
- Node.js 20+
- Docker y Docker Compose (recomendado para PostgreSQL)

> Necesitas al menos JDK 17. Si tienes Java 8 u otra versión antigua, instala JDK 17+ desde [Adoptium](https://adoptium.net/).

## Inicio rápido

### Opción A — Con Docker (recomendado)

```bash
docker compose up -d postgres
cd backend
mvn spring-boot:run
```

En otra terminal:

```bash
cd frontend
npm install
npm run dev
```

### Opción B — PostgreSQL local

1. Crea la base de datos `duoc_connect` en PostgreSQL.
2. Copia `.env.example` a `.env` y ajusta credenciales.
3. Ejecuta backend y frontend como arriba.

## Estructura del proyecto

```
Duoc Connect/
├── backend/          # API REST Spring Boot
├── frontend/         # SPA React
├── docker-compose.yml
└── README.md
```

## Fases de desarrollo

- [x] **Fase 0** — Setup, Docker, Flyway, Swagger, health check
- [x] **Fase 1** — Autenticación y perfiles
- [x] **Fase 2** — Grupos de estudio
- [x] **Fase 3** — Banco de apuntes
- [x] **Fase 4** — Foro académico
- [x] **Fase 5** — Búsqueda de compañeros
- [x] **Fase 6** — Admin y pulido

## Usuario administrador (demo)

Al iniciar el backend por primera vez, se crea automáticamente un administrador si no existe:

| Campo | Valor |
|-------|-------|
| Email | `admin@duocuc.cl` |
| Contraseña | `Admin1234` |

Accede a `/admin` en el frontend con esa cuenta para gestionar usuarios y moderar contenido.

## Panel de administración

| Endpoint | Descripción |
|----------|-------------|
| `GET /api/v1/admin/stats` | Estadísticas generales |
| `GET /api/v1/admin/usuarios` | Listar usuarios |
| `PATCH /api/v1/admin/usuarios/{id}/rol` | Cambiar rol |
| `PATCH /api/v1/admin/usuarios/{id}/estado` | Activar/desactivar |
| `GET /api/v1/admin/apuntes` | Listar apuntes |
| `DELETE /api/v1/admin/apuntes/{id}` | Eliminar apunte |
| `GET /api/v1/admin/foro/preguntas` | Listar preguntas |
| `DELETE /api/v1/admin/foro/preguntas/{id}` | Eliminar pregunta |

Todas las rutas `/admin/**` requieren JWT con rol `ADMINISTRADOR`.

## Chat en tiempo real (WebSocket)

El chat de grupos usa **STOMP sobre SockJS** en lugar de polling:

| Recurso | Ruta |
|---------|------|
| Endpoint WS | `/ws` |
| Suscribirse | `/topic/grupos/{grupoId}` |
| Enviar mensaje | `/app/grupos/{grupoId}/mensajes` |

La autenticación se realiza con el JWT en el header `Authorization` al conectar. Solo los miembros del grupo pueden suscribirse al canal.

## Variables de entorno

Copia `.env.example` a `.env` y ajusta los valores según tu entorno.

## URLs de desarrollo

| Servicio | URL |
|----------|-----|
| Frontend | http://localhost:5173 |
| API | http://localhost:8080/api/v1 |
| Swagger | http://localhost:8080/swagger-ui.html |
| Health check | http://localhost:8080/actuator/health |
| Status app | http://localhost:8080/api/v1/status |
