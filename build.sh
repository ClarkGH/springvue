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

echo "Done doctor-cool. Run the backend with: ./run.sh or ./run.sh -Dspring-boot.run.profiles=dev"
echo "Or manually with two processes: cd backend && ./mvnw spring-boot:run"
echo "and another terminal running cd frontend && pnpm dev"
echo "You can also build the JAR: cd backend && ./mvnw package -DskipTests"
echo "You can run the JAR: java -jar backend/target/backend-0.0.1-SNAPSHOT.jar"
