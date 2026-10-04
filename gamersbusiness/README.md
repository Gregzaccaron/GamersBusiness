# GamersBusiness API

GamersBusiness is a Java 17 / Spring Boot REST API for a game catalog and users' libraries, reviews, and achievements. The API uses a PostgreSQL 17 database, with Flyway as the schema migration authority and Hibernate configured to validate (not create or update) the schema at startup.

## Architecture

The application is organized under `br.com.gregfabio.gamersbusiness` using Clean Architecture boundaries:

- `domain`: business concepts and rules.
- `application`: use cases and ports.
- `infrastructure`: persistence adapters, configuration, and integrations.
- `presentation`: HTTP controllers and request/response DTOs.

HTTP endpoints use the versioned `/api/v1` contract. Authentication uses Bearer JWTs. Database structure is maintained by versioned SQL migrations in `src/main/resources/db/migration`; do not edit an applied migration to change a deployed schema—add another migration instead.

## Requirements

- Java 17 and Maven (or the included Maven Wrapper) for running on the host.
- Docker with the Docker Compose plugin for the containerized setup.
- PostgreSQL 17 when running the application directly with Maven.

## Configuration

Copy `.env.example` to `.env` and replace all `CHANGE_ME` values with local-only values before starting Compose. The example deliberately contains placeholders, not credentials. Keep `.env` private and do not commit it. Compose reads `.env` automatically for interpolation; `POSTGRES_PORT` and `API_PORT` are optional host port overrides.

| Variable | Purpose |
| --- | --- |
| `POSTGRES_DB` | Local database name. |
| `POSTGRES_USER` | PostgreSQL user and API database username. |
| `POSTGRES_PASSWORD` | PostgreSQL password and API database password. |
| `JWT_SECRET` | Secret used to sign JWTs; use a long, randomly generated local value and never reuse a production secret. |
| `BOOTSTRAP_ADMIN_ENABLED` | Optional local administrator bootstrap switch; defaults to `false`. |
| `BOOTSTRAP_ADMIN_USERNAME` | Username for the optional bootstrap administrator. |
| `BOOTSTRAP_ADMIN_EMAIL` | Email for the optional bootstrap administrator. |
| `BOOTSTRAP_ADMIN_PASSWORD` | Password for the optional bootstrap administrator. |
| `POSTGRES_PORT` | Optional host port for PostgreSQL; defaults to `5432`. |
| `API_PORT` | Optional host port for the API; defaults to `8080`. |

At startup, the API receives `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and `JWT_SECRET`; the Compose file derives the database connection from the variables above. When running through Maven, configure those four names directly. Set `SPRING_PROFILES_ACTIVE=local` to use the local profile.

### Optional local ADMIN bootstrap

Bootstrap is disabled by default. To request a local ADMIN, explicitly set `BOOTSTRAP_ADMIN_ENABLED=true` and provide a unique username, email, and strong password through the three `BOOTSTRAP_ADMIN_*` variables before startup. Do not put a real password in this README or `.env.example`. Bootstrap is local-profile functionality only; it is not a production account-management mechanism. The application must fail startup on incomplete/conflicting bootstrap configuration rather than silently promote an existing USER. Once created, subsequent starts must not replace that account's password.

## Run with Docker Compose

```sh
cp .env.example .env
# Edit .env and replace every CHANGE_ME placeholder with local values.
docker compose up --build
```

Compose builds the API image from the included multi-stage Java 17 Dockerfile, starts the official `postgres:17` image, waits for its healthcheck before starting the API, and stores database files in the named `postgres_data` volume. The API runs with `SPRING_PROFILES_ACTIVE=local` on port 8080 by default. Stop the services with `Ctrl-C`; use `docker compose down` to remove containers and network while preserving database data. `docker compose down -v` also deletes the database volume and all its data.

To start just PostgreSQL for a host-run API:

```sh
docker compose up -d postgres
```

The database is published on `localhost:${POSTGRES_PORT:-5432}`. Flyway applies pending migrations when the API starts.

## Run with Maven

With PostgreSQL 17 available on the host, create the local database/user values used in `.env`. The following loads that file into the current shell; run it only after replacing its placeholders:

```sh
set -a
. ./.env
set +a
export SPRING_PROFILES_ACTIVE=local
export DATABASE_URL="jdbc:postgresql://localhost:${POSTGRES_PORT:-5432}/${POSTGRES_DB}"
export DATABASE_USERNAME="$POSTGRES_USER"
export DATABASE_PASSWORD="$POSTGRES_PASSWORD"
./mvnw spring-boot:run
```

Alternatively, run a packaged JAR with `./mvnw package` followed by `java -jar target/gamersbusiness-0.0.1-SNAPSHOT.jar`, keeping the same environment variables set. Use `mvn` instead of `./mvnw` if Maven is installed separately. Flyway runs automatically on application startup; Hibernate validates the resulting schema.

## Local API documentation

The `local` Spring profile allows unauthenticated access to Swagger UI at <http://localhost:8080/swagger-ui.html> and the OpenAPI document at <http://localhost:8080/v3/api-docs>. This is a documentation-only exception: business routes remain protected, and Swagger's **Authorize** action requires a valid Bearer token for protected operations. Swagger/OpenAPI should be disabled outside the local profile; do not deploy the local profile on a public server.

## Example API flow

Examples below assume the API is reachable at `http://localhost:8080` and follow the `/api/v1` JSON contract. Replace IDs with IDs returned by your own API. All routes other than registration and login require `Authorization: Bearer <accessToken>`.

### Register and log in

Registration returns a public user profile (never a password or password hash):

```sh
curl -i http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"player1","email":"player1@example.test","password":"replace-with-a-local-password"}'
```

Log in to receive an access token, then set it for the remaining examples:

```sh
curl -i http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"player1@example.test","password":"replace-with-a-local-password"}'

USER_TOKEN='<accessToken from player login response>'
```

### Representative ADMIN catalog operations

Use a token belonging to ADMIN. If local bootstrap is enabled, log in with the configured bootstrap credentials to obtain the admin token:

```sh
curl -i http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"<BOOTSTRAP_ADMIN_EMAIL>","password":"<BOOTSTRAP_ADMIN_PASSWORD>"}'

ADMIN_TOKEN='<accessToken from admin login response>'

# Create a developer and a category.
curl -i http://localhost:8080/api/v1/developers \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Example Studio","country":"US","foundationDate":"2010-01-01"}'

curl -i http://localhost:8080/api/v1/categories \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Adventure"}'

# Use the created developerId and categoryIds in the game request.
curl -i http://localhost:8080/api/v1/games \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"Example Quest","description":"A local catalog example","price":19.99,"releaseDate":"2025-01-15","developerId":1,"categoryIds":[1]}'

# Use the returned game ID to add a catalog achievement.
curl -i http://localhost:8080/api/v1/achievements \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"gameId":1,"name":"First Steps","description":"Complete the opening level"}'
```

The numeric IDs above are illustrative: use the IDs returned by the corresponding create responses. Catalog reads, such as `GET /api/v1/games?page=0&size=20`, are available to authenticated users; catalog create/update/delete operations require ADMIN.

### Library, review, and personal achievement operations

With the registered player's token, acquire the game, update played hours, post a review, and unlock an achievement for a game currently in the player's library:

```sh
curl -i http://localhost:8080/api/v1/me/library \
  -H "Authorization: Bearer $USER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"gameId":1}'

# Use the acquisition ID returned above.
curl -i -X PATCH http://localhost:8080/api/v1/me/library/1 \
  -H "Authorization: Bearer $USER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"hoursPlayed":12}'

curl -i http://localhost:8080/api/v1/games/1/reviews \
  -H "Authorization: Bearer $USER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"rating":5,"comment":"A great adventure."}'

# Use the achievement ID returned by the catalog create response.
curl -i http://localhost:8080/api/v1/me/achievements \
  -H "Authorization: Bearer $USER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"achievementId":1}'

curl -i 'http://localhost:8080/api/v1/me/achievements?gameId=1' \
  -H "Authorization: Bearer $USER_TOKEN"

# Removing an acquisition preserves review and achievement history.
curl -i -X DELETE http://localhost:8080/api/v1/me/library/1 \
  -H "Authorization: Bearer $USER_TOKEN"

# A later acquisition is a new entry and snapshots the current catalog price.
curl -i http://localhost:8080/api/v1/me/library \
  -H "Authorization: Bearer $USER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"gameId":1}'
```

The API derives the acting user from the token. Acquisition is required before posting a review or unlocking an achievement. Collection endpoints use `page` (zero-based, default `0`) and `size` (default `20`, maximum `100`); for example, `GET /api/v1/games/1/reviews?page=0&size=20` returns the page plus the aggregate review count and average. A removed library entry does not erase review or achievement history.
