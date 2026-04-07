#!/bin/bash

set -e

ACTION="${1:-up}"

COMPOSE_ARGS="--env-file .env.local -f docker-compose.local.yml"

case "$ACTION" in
    up)
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