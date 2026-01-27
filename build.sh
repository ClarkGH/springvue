#!/usr/bin/env bash
set -e

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

echo "Building frontend..."
(cd frontend && pnpm install --frozen-lockfile && pnpm build)

echo "Copying frontend dist into backend static..."
STATIC_DIR="backend/src/main/resources/static"
rm -rf "$STATIC_DIR"
mkdir -p "$STATIC_DIR"
cp -r frontend/dist/* "$STATIC_DIR/"

echo "Done. Run the backend with: cd backend && ./mvnw spring-boot:run"
echo "Or build the JAR: cd backend && ./mvnw package -DskipTests"
