# Design — Java (Spring) Backend

Implementation details for the Spring Boot backend. See [General design](design.md) for scope, data model, API contract, and shared conventions.

---

## Stack

| Layer   | Tech |
|---------|------|
| Backend | Spring Boot 3 (Java 17+), Spring Data JPA, Spring Security, Flyway |
| Database | PostgreSQL |

---

## Schema / Flyway

**Stipulation:** Database schema is managed by **Flyway**, not Hibernate.

- Migrations live under `backend/src/main/resources/db/migration/` (e.g. `V1__create_tables.sql`). Flyway runs them on startup in version order.
- JPA uses `spring.jpa.hibernate.ddl-auto: validate`. Hibernate does **not** create or alter tables; it only checks that entity mappings match the existing schema.
- **Any schema change** (new table, column, index, FK) **must** be done via a new Flyway migration (e.g. `V2__add_foo.sql`). Do not rely on `ddl-auto: update` or hand-edit the DB.
- Keep migrations and JPA entities in sync: migrations define the source of truth, entities must match.

### Querying the database (psql)

Connect with `psql` using the same host, port, database, and credentials as the app (see `application.yml`; defaults: host `localhost`, port `5432`, database `springvue`, user `springvue`, password `springvue`):

```bash
psql -h localhost -p 5432 -U springvue -d springvue
```

Non-interactively:

```bash
PGPASSWORD=springvue psql -h localhost -p 5432 -U springvue -d springvue
```

Useful commands: `\dt`, `\d users`, `\d todos`, `\q`. Example queries:

- List users: `SELECT id, username, created_at FROM users;`
- List todos with owner: `SELECT t.id, t.title, t.completed, t.created_at, u.username FROM todos t JOIN users u ON t.user_id = u.id ORDER BY t.created_at DESC;`
- One-off: `PGPASSWORD=springvue psql -h localhost -p 5432 -U springvue -d springvue -c "SELECT * FROM users;"`

### Clearing the database

**Wipe data, keep schema** (dev reset):

```bash
PGPASSWORD=springvue psql -h localhost -p 5432 -U springvue -d springvue -c "TRUNCATE todos CASCADE; TRUNCATE users CASCADE;"
```

If truncation fails due to FK order, run `TRUNCATE todos;` then `TRUNCATE users;` in separate `-c` invocations.

**Full reset** (drop and recreate DB): requires superuser, no active connections.

```bash
sudo -u postgres psql -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'springvue' AND pid <> pg_backend_pid();"
sudo -u postgres psql -c "DROP DATABASE springvue;"
sudo -u postgres psql -c "CREATE DATABASE springvue OWNER springvue;"
```

Re-run README → Database grants on `public` for the `springvue` user. On next app start, Flyway recreates tables.

### Freeing ports

- **Port 8080 (Spring Boot)**: Stop the process (Ctrl+C in `./run.sh` terminal), or `lsof -i :8080` / `ss -tlnp | grep 8080` then `kill <PID>`, or `pkill -f "spring-boot:run"`.
- **Port 5432 (PostgreSQL)**: `systemctl stop postgresql`, `brew services stop postgresql@15`, or `pg_ctl stop` as appropriate.

---

## Entities

JPA entities in `dev.springvue.entity` map to the tables defined by Flyway. Schema is the source of truth.

- **User** (`entity/User.java`): table `users`. Fields: `id` (PK, identity), `username` (unique, non-null), `passwordHash` (non-null), `createdAt` (non-null, updatable = false). `@PrePersist` sets `createdAt` to `Instant.now()`.
- **Todo** (`entity/Todo.java`): table `todos`. Fields: `id` (PK, identity), `title` (non-null), `completed` (non-null, default false), `createdAt` (non-null, updatable = false), `userId` (non-null, column `user_id`). `@PrePersist` sets `createdAt` to `Instant.now()`.

---

## Repositories

Spring Data JPA repositories in `dev.springvue.repository`. Todo access is always scoped by the authenticated user.

- **UserRepository** (`repository/UserRepository.java`): `Optional<User> findByUsername(String username)`. Used by login and by controllers to resolve the current user.
- **TodoRepository** (`repository/TodoRepository.java`): `List<Todo> findByUserIdOrderByCreatedAtDesc(Long userId)`. Returns a user’s todos newest-first.

---

## Web

REST controllers and DTOs in `dev.springvue.web`. All `/api/**` endpoints are authenticated except `POST /api/auth/login`.

### Controllers

- **AuthController** (`web/AuthController.java`): `@RequestMapping("/api/auth")`. `POST /login` accepts `LoginRequest` (username, password); returns 400 if either null/blank. Looks up user by username, checks password with `PasswordEncoder`, issues JWT via `JwtService`, returns `LoginResponse` (token, username). Returns 401 if user not found or password does not match.
- **TodoController** (`web/TodoController.java`): `@RequestMapping("/api/todos")`. Resolves current user from `Authentication` via `UserRepository.findByUsername(auth.getName())`; unauthenticated or unknown user → 401 or empty list. `GET /` → list todos (newest-first). `POST /` → create from `CreateTodoRequest.title`, 400 if null/blank. `GET /{id}`, `PUT /{id}`, `DELETE /{id}` → get/update/delete only if todo belongs to current user; 404 otherwise. Uses `TodoDto` for responses.

### DTOs

- **LoginRequest**: `username`, `password`. Jackson `@JsonCreator` / `@JsonProperty`.
- **LoginResponse**: `token`, `username`.
- **CreateTodoRequest**: `title`. Body for `POST /api/todos`.
- **UpdateTodoRequest**: `title`, `completed` (both optional). Body for `PUT /api/todos/{id}`; only non-null fields applied, blank title ignored.
- **TodoDto**: `id`, `title`, `completed`, `createdAt`. `TodoDto.from(Todo)` builds from entity.

---

## Security (implementation)

- **SecurityConfig** (`config/SecurityConfig.java`): Two filter chains. (1) `/api/**`: stateless; `POST /api/auth/login` permitted anonymously; all other `/api/**` require authentication; CSRF disabled; `JwtAuthFilter` before `UsernamePasswordAuthenticationFilter`. (2) `/**`: permit all (SPA static and fallback). Provides `PasswordEncoder` (BCrypt).
- **JwtAuthFilter** (`security/JwtAuthFilter.java`): `OncePerRequestFilter`. Reads `Authorization: Bearer <token>`, validates via `JwtService`, sets `SecurityContextHolder` with `UsernamePasswordAuthenticationToken` (principal = username). Invalid/missing token → context unauthenticated; filter always continues.
- **JwtService** (`security/JwtService.java`): HMAC key from `JwtProperties.getSecret()`; `generateToken(username)`; `getUsernameFromToken(token)`. Subject, iat, exp. Does not enforce secret length or reject default dev secret (see [design](design.md) stipulations).
- **JwtProperties** (`config/JwtProperties.java`): `@ConfigurationProperties(prefix = "app.jwt")`. Binds `app.jwt.secret`, `app.jwt.expiration-ms` (default 86400000).

---

## Config

- **application.yml**: Datasource URL/user/password, JPA, server port, `app.jwt.secret` / `app.jwt.expiration-ms`. Sensitive values overridden by env vars (`SPRING_DATASOURCE_*`, `APP_JWT_SECRET`, `APP_JWT_EXPIRATION_MS`). For local overrides without env vars, copy `application-local.yml.example` to `application-local.yml` and run with `--spring.profiles.active=local`.
- **Dev profile**: `DevDataLoader` creates user `user` / password `password` on first run if no user exists. Use for local development only.

---

## Delivery

- **Production**: `./build.sh` then `./run.sh`. Spring serves built Vue app from `/` and API under `/api`. Same origin. Open <http://localhost:8080>.
- **Development**:
  - **Monolith**: `./run.sh -Dspring-boot.run.profiles=dev`. App at <http://localhost:8080>. Frontend must be built at least once (`./build.sh`); rebuild when frontend changes. No hot reload.
  - **Two-process**: Backend `./run.sh -Dspring-boot.run.profiles=dev`, frontend `cd frontend && pnpm dev`. Vite proxies `/api` to backend (`vite.config.ts`). Use Vite URL (e.g. <http://localhost:5173>) for hot reload.

**Scripts**: `./run.sh [args]` runs the backend. `./build.sh` runs frontend install + build and copies `frontend/dist/*` into `backend/src/main/resources/static/`. You can also run Maven from `backend/` and build/copy manually.

---

## Architecture (Java)

The browser loads the Vue SPA. The SPA calls the Spring backend under `/api`. Spring handles auth (JWT), talks to Postgres, returns JSON. Spring serves the SPA’s `index.html` and static assets; non-API paths fall back to `index.html` (see `SpaConfig`).

---

## Scale (Java-specific)

- **Connection pooling**: HikariCP (default). Tune `maximum-pool-size` so (instances × pool size) &lt; Postgres `max_connections`. Use PgBouncer when many instances share the DB.
- **Virtual threads**: Prefer virtual threads (Java 21+) for higher concurrency per instance before introducing WebFlux. Keep Spring MVC; enable virtual threads and align pool/thread usage.
- **Rate limiting**: Bucket4j, Resilience4j, or reverse-proxy. Document the approach.
- **Input validation**: Bean Validation (JSR 380) on DTOs; `@Valid` on request bodies. Keep JPA column lengths in sync (e.g. `@Column(length = 500)`).
