#!/usr/bin/env bash
set -e

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

(cd backend && ./mvnw spring-boot:run "$@")

echo "Done doctor-cool. Open http://localhost:8080 in your browser after the build completes."
