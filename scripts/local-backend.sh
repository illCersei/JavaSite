#!/bin/bash

set -e

ACTION="${1:-up}"

COMPOSE_ARGS="--env-file .env.local -f docker-compose.local.yml"

build_backend() {
    # Docker builds for the Java services expect a pre-built target/*.jar (see e.g.
    # wallet/Dockerfile) - build the whole Maven aggregator once instead of per-module.
    echo "Building Java services (mvn package)..."
    mvn -q -DskipTests clean package

    # fightService's own Dockerfile builds it inside the image, but building it here too
    # fails fast on a compile error instead of burning time on a docker build first.
    echo "Building fightService (.NET)..."
    dotnet build fightService/fightService.sln -c Release
}

case "$ACTION" in
    up)
        build_backend
        docker compose $COMPOSE_ARGS up -d --build
        ;;
    down)
        docker compose $COMPOSE_ARGS down
        ;;
    logs)
        docker compose $COMPOSE_ARGS logs -f --tail 200
        ;;
    ps)
        docker compose $COMPOSE_ARGS ps
        ;;
    *)
        echo "Неизвестная команда: $ACTION"
        echo "Использование: $0 {up|down|logs|ps|test}"
        exit 1
        ;;
esac