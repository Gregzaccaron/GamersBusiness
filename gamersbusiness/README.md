# GamersBusiness

GamersBusiness is an academic Steam-inspired platform with a React storefront and a Java 17 / Spring Boot REST API for a game catalog and users' libraries, reviews, and achievements. The API uses PostgreSQL 17, with Flyway as the schema migration authority and Hibernate configured to validate (not create or update) the schema at startup. Acquisitions are simulations; there is no real payment or game download.

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

Copy `.env.example` to `.env` and replace all `CHANGE_ME` values with local-only values before starting Compose. The example deliberately contains placeholders, not credentials. Keep `.env` private and do not commit it. Compose reads `.env` automatically for interpolation.

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
| `POSTGRES_PORT` | Loopback-only PostgreSQL host port; defaults to `5433`. |
| `API_PORT` | Loopback-only API host port; defaults to `8081`. |
| `FRONTEND_PORT` | Loopback-only storefront host port; defaults to `8082`. |
| `DEMO_SEED_ENABLED` | Opt-in versioned academic datasets; defaults to `false`, requires profile `local`. |
| `DEMO_USER_PASSWORD` | External password for newly provisioned demonstration USER accounts; never resets an existing password. |

At startup, the API receives `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and `JWT_SECRET`; the Compose file derives the database connection from the variables above. When running through Maven, configure those four names directly. Set `SPRING_PROFILES_ACTIVE=local` to use the local profile.

### Optional local ADMIN bootstrap

Bootstrap is disabled by default. To request a local ADMIN, explicitly set `BOOTSTRAP_ADMIN_ENABLED=true` and provide a unique username, email, and strong password through the three `BOOTSTRAP_ADMIN_*` variables before startup. Do not put a real password in this README or `.env.example`. Bootstrap is local-profile functionality only; it is not a production account-management mechanism. The application must fail startup on incomplete/conflicting bootstrap configuration rather than silently promote an existing USER. Once created, subsequent starts must not replace that account's password.

### Optional demonstration dataset

Set `DEMO_SEED_ENABLED=true` and replace `DEMO_USER_PASSWORD` with a local-only password (at most 72 UTF-8 bytes). The seed requires the `local` profile. A fresh database receives the original dataset and its richer expansion:

- 14 fictional USER accounts, including `player@gamersbusiness.local` and `community@gamersbusiness.local`, using the configured `DEMO_USER_PASSWORD` when first created.
- 48 games (8 free-to-play), 42 developers, 37 categories and 168 catalogue achievements, spanning competitive games, RPGs, strategy, survival, automation, racing, cooperative games and indies.
- 230 acquisitions, 229 reviews with ratings from 1 to 5, and 453 unlocked achievements. Each expansion account owns 18 games; played hours range from unplayed to hundreds of hours, with achievements still available to unlock. New community activity belongs to the 12 expansion accounts; the original players' collections are left untouched.

Try `ana_explora@gamersbusiness.local` for an expanded library. The other fictional usernames are `bruno_rpg`, `carla_indie`, `diego_corridas`, `elisa_estrategia`, `felipe_coop`, `gabi_souls`, `henrique_builder`, `iris_arcade`, `joao_tatico`, `luiza_fazenda` and `marcos_retro`, all with `@gamersbusiness.local` emails. Their initial password is the external `DEMO_USER_PASSWORD`; do not publish the local `.env`.

Counts describe a fresh, unmodified demonstration database, excluding the optional ADMIN from the 14 USER accounts. Prices, reviews and achievements are academic samples. Some developer foundation dates use the first day of the known month/year when an exact day is unavailable.

Enable the separate ADMIN bootstrap to manage the catalogue through the interface. Its email and password come from `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD`; no credential is embedded in frontend files.

The dataset is academic demonstration content, not a live Steam catalogue or current price feed. Each phase is recorded transactionally in `demo_seed_state` (created by Flyway V2): `initial-demo-catalogue-v1` and `rich-demo-catalogue-v2`. An existing original database receives only the missing expansion; no volume reset or manual deletion of seed markers is required. Completed phases never reconcile records, so restarts do not duplicate entries, reset credentials, overwrite edits or recreate deleted samples. Changing `DEMO_USER_PASSWORD` does not change existing passwords; use the profile form instead. Conflicting new account identities or expansion game titles fail atomically instead of claiming or overwriting existing records. Keep demo provisioning disabled outside local development.


## Run with Docker Compose

```sh
cp .env.example .env
# Edit .env and replace every CHANGE_ME placeholder with local values.
docker compose up --build
```

Compose builds the API and the frontend (Node build, Nginx runtime), starts the official `postgres:17` image, waits for the database healthcheck before starting the API, and stores database files in the named `postgres_data` volume. Open the storefront at <http://localhost:8082>; the local-profile API is available at <http://localhost:8081>. All published ports bind only to `127.0.0.1`. Nginx forwards `/api/` to the API on the private Compose network. Stop with `Ctrl-C`; `docker compose down` removes containers and network while preserving data. **`docker compose down -v` deletes the database and all its data.**

To start just PostgreSQL for a host-run API:

```sh
docker compose up -d postgres
```

The database is published on `localhost:${POSTGRES_PORT:-5433}`. Flyway applies pending migrations when the API starts.

## Run with Maven

With PostgreSQL 17 available on the host, create the local database/user values used in `.env`. The following loads that file into the current shell; run it only after replacing its placeholders:

```sh
set -a
. ./.env
set +a
export SPRING_PROFILES_ACTIVE=local
export DATABASE_URL="jdbc:postgresql://localhost:${POSTGRES_PORT:-5433}/${POSTGRES_DB}"
export DATABASE_USERNAME="$POSTGRES_USER"
export DATABASE_PASSWORD="$POSTGRES_PASSWORD"
./mvnw spring-boot:run
```

Alternatively, run a packaged JAR with `./mvnw package` followed by `java -jar target/gamersbusiness-0.0.1-SNAPSHOT.jar`, keeping the same environment variables set. Use `mvn` instead of `./mvnw` if Maven is installed separately. Flyway runs automatically on application startup; Hibernate validates the resulting schema.

## Tests

From this directory, with Java 17 and Docker running:

```sh
./mvnw verify
```

This compiles the application, runs unit and HTTP integration tests, and packages the executable JAR. Integration tests start an isolated PostgreSQL 17 container with Testcontainers, apply Flyway migrations, and clean up the container automatically. No `.env`, running Compose stack, or manually created database is needed. The first run needs network access to download Maven dependencies and container images.

To run only the unit tests without Docker:

```sh
./mvnw -Dtest=PageRequestTest,BCryptPasswordHasherTest test
```

The suite covers authentication and permissions, catalog filters, pagination, ownership and historical records, local administrator bootstrap, malformed HTTP requests, BCrypt password byte limits, and demo seed atomicity, idempotence, account collisions and preservation of later edits/deletions. `DemoSeedIntegrationTest` uses an isolated PostgreSQL 17 database; it does not touch the Compose demonstration database.

## Local API documentation

With Docker, the `local` Spring profile allows unauthenticated access to Swagger UI at <http://localhost:8081/swagger-ui.html> and the OpenAPI document at <http://localhost:8081/v3/api-docs>. A host-run Maven API defaults to port `8080`. This is a documentation-only exception: business routes remain protected, and Swagger's **Authorize** action requires a valid Bearer token. Swagger/OpenAPI should be disabled outside the local profile; do not deploy the local profile on a public server.

## Example API flow

Examples below assume the Docker API at `http://localhost:8081` and follow the `/api/v1` JSON contract. Replace IDs with IDs returned by your own API. All routes other than registration and login require `Authorization: Bearer <accessToken>`.

### Register and log in

Passwords must be nonblank and fit within BCrypt's **72 UTF-8 byte** limit. Accented characters and emoji can occupy multiple bytes. The same limit applies to password changes and administrator provisioning. Oversized new passwords return `400`; login attempts with oversized passwords return `401`. Passwords are never silently truncated.

Registration returns a public user profile (never a password or password hash):

```sh
curl -i http://localhost:8081/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"player1","email":"player1@example.test","password":"replace-with-a-local-password"}'
```

Log in to receive an access token, then set it for the remaining examples:

```sh
curl -i http://localhost:8081/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"player1@example.test","password":"replace-with-a-local-password"}'

USER_TOKEN='<accessToken from player login response>'
```

### Representative ADMIN catalog operations

Use a token belonging to ADMIN. If local bootstrap is enabled, log in with the configured bootstrap credentials to obtain the admin token:

```sh
curl -i http://localhost:8081/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"<BOOTSTRAP_ADMIN_EMAIL>","password":"<BOOTSTRAP_ADMIN_PASSWORD>"}'

ADMIN_TOKEN='<accessToken from admin login response>'

# Create a developer and a category.
curl -i http://localhost:8081/api/v1/developers \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Example Studio","country":"US","foundationDate":"2010-01-01"}'

curl -i http://localhost:8081/api/v1/categories \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Adventure"}'

# Use the created developerId and categoryIds in the game request.
curl -i http://localhost:8081/api/v1/games \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"Example Quest","description":"A local catalog example","price":19.99,"releaseDate":"2025-01-15","developerId":1,"categoryIds":[1]}'

# Use the returned game ID to add a catalog achievement.
curl -i http://localhost:8081/api/v1/achievements \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"gameId":1,"name":"First Steps","description":"Complete the opening level"}'
```

The numeric IDs above are illustrative: use the IDs returned by the corresponding create responses. Catalog reads, such as `GET /api/v1/games?page=0&size=20`, are available to authenticated users; catalog create/update/delete operations require ADMIN.

The `title` filter performs a case-insensitive substring search. Characters such as `%` and `_` are treated literally, not as SQL wildcards. Encode query parameters when constructing URLs; for example, search for a literal percent sign with `curl --get --data-urlencode 'title=%' -H "Authorization: Bearer $USER_TOKEN" http://localhost:8081/api/v1/games`.

### Library, review, and personal achievement operations

With the registered player's token, acquire the game, update played hours, post a review, and unlock an achievement for a game currently in the player's library:

```sh
curl -i http://localhost:8081/api/v1/me/library \
  -H "Authorization: Bearer $USER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"gameId":1}'

# Use the acquisition ID returned above.
curl -i -X PATCH http://localhost:8081/api/v1/me/library/1 \
  -H "Authorization: Bearer $USER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"hoursPlayed":12}'

curl -i http://localhost:8081/api/v1/games/1/reviews \
  -H "Authorization: Bearer $USER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"rating":5,"comment":"A great adventure."}'

# Use the achievement ID returned by the catalog create response.
curl -i http://localhost:8081/api/v1/me/achievements \
  -H "Authorization: Bearer $USER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"achievementId":1}'

curl -i 'http://localhost:8081/api/v1/me/achievements?gameId=1' \
  -H "Authorization: Bearer $USER_TOKEN"

# Removing an acquisition preserves review and achievement history.
curl -i -X DELETE http://localhost:8081/api/v1/me/library/1 \
  -H "Authorization: Bearer $USER_TOKEN"

# A later acquisition is a new entry and snapshots the current catalog price.
curl -i http://localhost:8081/api/v1/me/library \
  -H "Authorization: Bearer $USER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"gameId":1}'
```

The API derives the acting user from the token. Acquisition is required before posting a review or unlocking an achievement. Collection endpoints use `page` (zero-based, default `0`) and `size` (default `20`, maximum `100`); for example, `GET /api/v1/games/1/reviews?page=0&size=20` returns the page plus the aggregate review count and average. A removed library entry does not erase review or achievement history.

## Error responses

Errors use the JSON fields `timestamp`, `status`, `message`, `path`, and `errors` (field validation details). For authenticated requests, unknown API routes return `404`, unsupported HTTP methods return `405` with an `Allow` header, and unsupported request content types return `415`. Invalid input returns `400`, authentication failures `401`, insufficient permissions `403`, and conflicts with stored records `409`. Unexpected server failures return a generic `500` response and are logged server-side for diagnosis.

## Frontend development

The React/Vite project lives in `../frontend` relative to this README. With the Docker API available on `localhost:8081`:

```sh
cd ../frontend
npm ci
npm run dev
```

Vite forwards `/api` to the local API. `npm run build` generates `dist/`; Docker serves that output with Nginx. No Node installation is needed to run the complete Compose setup.

The storefront supports registration/login, catalogue discovery, simulated acquisition, libraries, played hours, reviews, achievements and profile management. ADMIN accounts also manage games, developers, categories, achievements and users. Business API routes remain authenticated; the public welcome screen does not expose the protected catalogue. JWTs are stored in the current tab's `sessionStorage`, not embedded in the bundle.

Game artwork is stored locally under `frontend/public/images` and originates from Steam's public game-image CDN. Titles, artwork and trademarks belong to their respective owners; this is an independent academic project, not an official Steam product. Review usage rights before publishing beyond the academic demonstration.

For a browser smoke check, create a temporary account, search/filter the catalogue, acquire a game, update hours, publish/edit/delete a review, unlock an achievement and update the profile. Check catalogue CRUD with ADMIN and verify that deleting referenced records shows an error instead of removing them. Also check the mobile navigation and session expiry while an acquisition confirmation is open: both dialogs must close and page scrolling must resume. The interface targets modern browsers with native `<dialog>` and CSS `:has()` support.
