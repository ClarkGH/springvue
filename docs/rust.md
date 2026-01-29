# Design — Rust (Actix) Backend

Implementation details for the Actix-web backend. See [General design](design.md) for scope, data model, API contract, and shared conventions.

---

## Stack

| Concern       | Crate           | Notes                                  |
|---------------|-----------------|----------------------------------------|
| HTTP server   | **actix-web**   | Extractor-based handlers, middleware   |
| Async runtime | **tokio**       | `rt-multi-thread` + `macros`           |
| DB            | **sqlx**        | Async Postgres, compile-time checked   |
| Migrations    | **sqlx-cli**    | `sqlx migrate`                         |
| JSON          | **serde**, **serde_json** | DTOs, request/response bodies |
| JWT           | **jsonwebtoken**| HS256 sign/verify                      |
| Passwords     | **bcrypt**      | Hash (seed), verify (login)            |
| Config        | **dotenvy**     | Load `.env`                            |

---

## Schema

Use the same tables as [General design](design.md). Migrations live in `rust-backend/migrations/` (e.g. `20250129000001_create_tables.sql`). Run `sqlx migrate run` at startup (or via `sqlx::migrate!()`) or manually with `sqlx-cli`. The schema SQL matches `backend/src/main/resources/db/migration/V1__create_tables.sql` (users + todos + index).

**PostgreSQL 15+**: Same `GRANT` requirements as general design. Use the same DB user (e.g. `springvue`) or a dedicated one; keep credentials in `.env`.

---

## Project layout (WIP)

```text
rust-backend/
├── Cargo.toml
├── .env.example
├── migrations/
│   └── ..._create_tables.sql
└── src/
    ├── main.rs          # Build app, mount routes, run server
    ├── config.rs        # DB URL, JWT secret, etc. from env
    ├── error.rs         # Shared error → HTTP status (optional)
    ├── auth/
    │   ├── mod.rs
    │   ├── jwt.rs       # Generate / validate JWT
    │   └── middleware.rs # Bearer extract, validate, inject user
    ├── db/
    │   ├── mod.rs       # Pool, migrate
    │   └── user.rs      # User lookup, dev seed
    ├── handlers/
    │   ├── mod.rs
    │   ├── auth.rs      # POST /api/auth/login
    │   └── todos.rs     # CRUD /api/todos
    └── models.rs        # User, Todo, DTOs (serde)
```

---

## Models and DTOs

- **User**: `id`, `username`, `password_hash`, `created_at`. Use `sqlx::FromRow` for DB mapping.
- **Todo**: `id`, `title`, `completed`, `created_at`, `user_id`. Same.
- **DTOs**: Request/response structs with `serde::Serialize` / `Deserialize`. Use `#[serde(rename = "camelCase")]` (e.g. `createdAt`) so JSON matches the [API contract](design.md#api-contract). `LoginRequest` / `LoginResponse`, `CreateTodoRequest`, `UpdateTodoRequest` (`title`, `completed` optional), and a todo response DTO.

---

## DB layer

- Create a `PgPool` from `DATABASE_URL`, run migrations on startup, and inject the pool via `web::Data`.
- **User**: “Find by username” query; used at login and to resolve current user from JWT subject.
- **Dev seed**: If `DEV_SEED=true` (or similar) and no user `user` exists, insert one with `bcrypt::hash("password", ...)` and username `user`. Gate on dev-only config.

---

## Auth

- **Login** (`POST /api/auth/login`): Parse `{ username, password }`. 400 if missing/blank. Look up user, `bcrypt::verify`; 401 if not found or mismatch. Issue JWT (subject = username, iat, exp) via `jsonwebtoken`, return `{ token, username }`.
- **JWT**: HS256, same claim shape as Java `JwtService`. Use `APP_JWT_SECRET` (min 32 bytes), `APP_JWT_EXPIRATION_MS` (default 86400000). Fail startup if secret too short in prod.
- **Middleware / extractor**: Read `Authorization: Bearer <token>`. Validate JWT, extract username, attach to request (or extractor). Protected handlers use “current username” and resolve `user_id` when needed. Missing/invalid token → 401.

---

## Handlers

- **Auth**: `POST /api/auth/login` only (see Auth above).
- **Todos**: All require auth. Scope by current user’s id.
  - `GET /api/todos` → list for user, newest-first; 401 if unauthenticated.
  - `POST /api/todos` → create from `{ title }`; 400 if blank, 401 if unauthenticated.
  - `GET /api/todos/:id` → 404 if not found or not owner.
  - `PUT /api/todos/:id` → body `{ title?, completed? }`; apply only provided fields; 404 if not found or not owner.
  - `DELETE /api/todos/:id` → 204; 404 if not found or not owner.

Map errors to 400, 401, 404 as in the [API contract](design.md#api-contract).

---

## Config

- **dotenvy**: Load `.env`. Use `DATABASE_URL`, `APP_JWT_SECRET`, `APP_JWT_EXPIRATION_MS`, optional `DEV_SEED` or `RUST_ENV=dev`. Provide `.env.example` with safe defaults and document in README.

---

## Delivery

- **Run**: `cargo run` from `rust-backend/`. Server binds to configurable host/port (e.g. `0.0.0.0:8080`).
- **With frontend**: Use the same Postgres DB (or a copy) as Java. For dev, run Vue via `cd frontend && pnpm dev` and point the Vite proxy at the Rust server (e.g. `http://localhost:8080`). Ensure CORS is configured if frontend and API differ by origin.
- **Smoke-test**: Log in, list todos, create/update/delete via the Vue app to validate the API contract and config.

---

## Scale (Rust-specific)

- **Connection pooling**: sqlx pool (default). Tune pool size so (instances × pool size) &lt; Postgres `max_connections`. Use PgBouncer when many instances share the DB.
- **Stateless + horizontal scale**: JWT and stateless auth; no session affinity.
- **Caching**: Cache “username → userId” or embed `userId` in JWT to avoid a DB lookup per request. Use Redis (or similar) when multiple instances share cache.
- **Rate limiting**: Throttle `POST /api/auth/login` (e.g. per IP); document the approach. Optionally add actix-web middleware or a reverse-proxy.
- **Observability**: Add `tracing` + `tracing-actix-web` and metrics when operating under load.
