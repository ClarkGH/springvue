# Benchmarks — Performance Stipulations (Rust vs Java)

Rules and conditions for comparing performance between the [Rust (Actix)](rust.md) and [Java (Spring)](java.md) backends. Use this doc to keep runs fair and comparable.

---

## Purpose

- Compare throughput and latency of the two backends under comparable load.
- Inform tuning and architecture choices (see [design](design.md) Scale section).
- Keep benchmarks reproducible and documented.

---

## Scope

- **In scope**: Same API contract ([design](design.md)), same schema, same Postgres DB (or identical copies). Compare **backend-only** behavior: HTTP API handling, auth, DB access.
- **Out of scope (for now)**: Full-stack (browser → frontend → API), SSR, SEO, or non-API endpoints. Defer client-side or frontend-only benchmarks.

---

## Conditions (fair comparison)

1. **Same API**: Both backends implement the same [API contract](design.md#api-contract). Benchmarks use the same requests (paths, headers, bodies).
2. **Same schema and data**: Use the same `users` / `todos` tables. For a given run, seed or load data consistently (e.g. same user count, same todo count per user). Document schema version (e.g. migration) and any seed script.
3. **Same database**: Either (a) one Postgres instance, one backend at a time, or (b) two identical DBs (same Postgres version, config, and data) if running backends in parallel. Document which.
4. **Isolated runs**: No other heavy processes on the same machine during a run. Same OS, same hardware for both backends when comparing.
5. **Documented config**: Record Java version, JVM opts (heap, GC), Rust build (`release`), and any tunables (pool size, threads). Use **release** builds for Rust; use production-like JVM settings for Java (e.g. `-Xmx`, `-XX:+UseG1GC`).
6. **Warm-up**: Exclude warm-up phase from reported metrics (e.g. discard first N seconds or first K requests). Document warm-up policy.

---

## What to measure

- **Throughput**: Requests per second (rps) per endpoint or per mixed workload. Define the workload (e.g. ratio of `GET /api/todos`, `POST /api/todos`, `PUT`, `DELETE`, `POST /api/auth/login`).
- **Latency**: Percentiles (e.g. p50, p95, p99) per endpoint or overall. Report in milliseconds.
- **Resource usage** (optional): CPU, memory, DB connections during a run. Helps explain differences.

---

## Workload

- **Endpoints**: At minimum, cover `POST /api/auth/login`, `GET /api/todos`, `POST /api/todos`, `PUT /api/todos/:id`, `DELETE /api/todos/:id`. Mixed workloads should reflect realistic usage (e.g. more reads than writes).
- **Concurrency**: Run at several concurrency levels (e.g. 1, 10, 50, 100, 200) to see how each backend scales. Document concurrent clients and total request count per run.
- **Auth**: Authenticated requests must send a valid `Authorization: Bearer <token>`. Use a small set of seeded users and obtain JWTs before the benchmark, or use a login step in the workload. Don’t benchmark login-only unless that’s explicitly the goal.

---

## Tooling

- Use a single HTTP load tool (e.g. `wrk`, `hey`, `k6`, `ab`) for both backends so methodology is comparable. Document tool, version, and exact command (or config file).
- Script runs (e.g. shell or `Makefile` targets) so others can reproduce. Store any benchmark config (k6 script, wrk Lua) in the repo.

---

## Reporting

- **Per run**: Backend (Java vs Rust), config (see Conditions), tool and command, workload, warm-up, results (throughput, latency percentiles). Optionally, CPU/memory.
- **Comparisons**: When comparing Java vs Rust, use identical workload, DB, and machine. State clearly what differs (only the backend under test).

---

## Stipulations

- **No special-case optimizations for the benchmark**: Both backends should behave as in production (same validation, same DB queries, same auth checks). No “benchmark mode” that skips logic.
- **Connection pooling**: Use production-like pool sizes. Document pool size and Postgres `max_connections` so runs don’t hide pool exhaustion or connection storms.
- **Secrets and config**: Use the same JWT secret and DB credentials for both; avoid config differences that could affect performance (e.g. unnecessarily verbose logging only in one backend).

---

## Optional follow-ups

- **Sweeps**: Vary pool size, thread count, or JVM opts and record impact.
- **Sustained load**: Long-running runs (e.g. 5–10 minutes) to check for degradation or leaks.
- **Rate limiting**: If rate limiting is enabled, document it; it will cap throughput and affect comparability.
