# Quarkus Agenda App (dev)

This is a minimal Quarkus scaffold for the Agenda project.


Run the full stack with Docker Compose (builds the dev image and runs Quarkus in dev mode):

```bash
# copy example env to .env and customize if needed
cp ../example.env .env

docker-compose up --build
```

Access Quarkus health endpoint:


http://localhost:${QUARKUS_HOST_PORT:-8080}/api/health/ping

Adminer UI: http://localhost:${ADMINER_PORT:-8081}

Keycloak (admin console): http://localhost:${KEYCLOAK_PORT:-8082}

Default Keycloak admin (from example.env): `admin` / `admin`

Notes:
- Source folder is mounted into the container for hot-reload using `mvn quarkus:dev`.
- Database connection is configured via environment variables. See `example.env` for values.
- It's good practice to keep a private `.env` (local overrides) and commit only `example.env`.

