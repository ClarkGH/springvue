# Design

Rules, guidelines, architecture, and implementation details for the TODO app live under **[/docs](docs/)**:

| Doc | Description |
|-----|-------------|
| **[docs/design.md](docs/design.md)** | General design: scope, data model, API contract, frontend, security stipulations, architecture, scale |
| **[docs/java.md](docs/java.md)** | Java (Spring Boot) backend: Flyway, JPA, Security, delivery, ops |
| **[docs/rust.md](docs/rust.md)** | Rust (Actix) backend: sqlx, handlers, auth, config, delivery |
| **[docs/benchmarks.md](docs/benchmarks.md)** | Performance stipulations: comparing Rust vs Java (conditions, workload, tooling, reporting) |

Start with [docs/design.md](docs/design.md) for shared concepts; use the backend-specific docs when working on Spring or Actix. See [docs/benchmarks.md](docs/benchmarks.md) for benchmarking Rust vs Java.
