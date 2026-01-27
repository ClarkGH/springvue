# springvue
POC Spring Monolith with a Vue 3 SPA

## Why?

Another "dOn'T wOrRy AbOuT iT bRo", personal project.

Strengthening web architecture skills. I can work with spring java controllers and am solid in javascript.

This means:

- Pretty pictures and diagrams (me no write, me draw!).
- A design document (wowsers!).
- Cobbled together code (hurray!).
- Half awake dad humor (word.).

## What?

YAGNI'ing up a spring & vue SPA monolith proof of concept. Doesn't need to be pretty. Just need endpoints to get/post/put/delete to. Not doing server side rendering, SEO is irrelevant.

Might throw up some junk code and never return. 3 side-projects in-progress. Many discarded along the wayside. Few completed. Not sorry.

## Stack?

1. Vite / Vue 3 / Pinia - Frontend
2. Maven / Spring - Backend
3. Typescript / Java / CSS / XML / HTML - Jargon

---

## Design

Rules, architecture, and diagrams live in **[DESIGN.md](DESIGN.md)**. Start there for scope, stack, conventions, and Mermaid diagrams.

---

## How to run

**Requirements:** Java 17+ (Spring Boot 3), Node 18+, pnpm, PostgreSQL. If you have multiple JDKs, set `JAVA_HOME` to a 17+ installation before running Maven.

### Database

Create a DB and user (or adjust config per "Config & secrets" below). Flyway needs schema permissions (PostgreSQL 15+ restricts `public` by default):

```sql
CREATE DATABASE springvue;
CREATE USER springvue WITH PASSWORD 'springvue';
GRANT ALL PRIVILEGES ON DATABASE springvue TO springvue;
\c springvue
GRANT ALL ON SCHEMA public TO springvue;
GRANT CREATE ON SCHEMA public TO springvue;
```

### Config & secrets

Sensitive values (DB URL/user/password, JWT secret) are set via env vars or a local config file. Defaults in `application.yml` are for local dev only.

- **Env vars** (recommended for prod):  
  `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `APP_JWT_SECRET`, optional `APP_JWT_EXPIRATION_MS`.

- **Local file** (gitignored):  
  Copy `backend/src/main/resources/application-local.yml.example` to `application-local.yml`, fill in values, then run with `--spring.profiles.active=local` (e.g. `./run.sh -Dspring-boot.run.profiles=dev,local`).

### Development

1. **Backend**

   From project root:

   ```bash
   ./run.sh -Dspring-boot.run.profiles=dev
   ```

   Or from the backend directory: `cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`.

   Uses `dev` profile: seeds user `user` / password `password` if none exist. Defaults: Postgres at `localhost:5432`, DB `springvue`, user `springvue`.

2. **Frontend**

   ```bash
   cd frontend
   pnpm install
   pnpm dev
   ```

   Vite runs the SPA and **proxies `/api` to the backend** (see [DESIGN.md](DESIGN.md)). Use the app at the Vite URL (e.g. `http://localhost:5173`). Log in with `user` / `password` in dev.

### Production (single artifact)

From the project root, run the build script:

```bash
./build.sh
```

This installs frontend deps, builds the Vue app, and copies `frontend/dist/*` into `backend/src/main/resources/static/`. Then run the backend from root:

```bash
./run.sh
```

(Or `cd backend && ./mvnw spring-boot:run`, or build the JAR with `./mvnw package` and run the JAR.) The app and API are served from the same origin; open the backend URL (e.g. `http://localhost:8080`).

To do it manually: build in `frontend` with `pnpm build`, then `cp -r frontend/dist/* backend/src/main/resources/static/`.

## Known issues

- Unenforced JWT key length
  - Fix: Require at least 32 bytes in JwtService or throw
- Not rejecting Default Secret
  - Fix: Fail startup when default secret is used in production
- Not rate limiting
  - Fix: Bucket4j/filter on login (optionally API), or do in proxy
- CORS?
  - Fix: CORS should be specified when APIs are discerned
- Input validation
  - Fix: Bean validation. @Size on DTOs. @Column (length) on entities
- Constant-time login
  - Fix: Always run BCrypt, use dummy hash when user missing
  