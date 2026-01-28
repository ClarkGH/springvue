# springvue

POC Spring Monolith with a Vue 3 SPA.

![Image of todo app](https://github.com/user-attachments/assets/4b8c2000-3aad-4f41-a775-18768e18dd62)

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

## Stack?

1. Vite / Vue 3 / Pinia - Frontend
2. Maven / Spring - Backend
3. Typescript / Java / CSS / XML / HTML - Jargon

---

## Design

Rules, architecture, and diagrams live in **[DESIGN.md](DESIGN.md)**. Start there for scope, stack, conventions, and Mermaid diagrams.

---

## How to run

**Requirements:** Java 17+, Node 18+, pnpm, PostgreSQL.

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

### Build and run

From the project root:

```bash
./build.sh
./run.sh
```

`build.sh` installs frontend deps, builds the Vue app, and copies it into the backend static folder. `run.sh` starts the Spring Boot server. Open the app at the backend URL (e.g. `http://localhost:8080`).
