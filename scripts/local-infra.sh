#!/bin/bash
# Локальная инфраструктура для запуска сервисов из IDE: Postgres/Redis/RabbitMQ/Kafka.
# Базы сервисов Postgres создаёт сам при первом запуске (sql/00-create-databases.sql).

set -euo pipefail

cd "$(dirname "$0")/.."

ENV_FILE=".env.local-infra"
COMPOSE_ARGS="--env-file $ENV_FILE -f docker-compose.local-infra.yml"
SEED_DIR="sql/seed"

DB_USER="$(grep -E '^DB_USER=' "$ENV_FILE" | cut -d= -f2- | tr -d '\r')"

psql_in() {
    local db="$1"
    shift
    docker compose $COMPOSE_ARGS exec -T postgres psql -U "$DB_USER" -d "$db" -v ON_ERROR_STOP=1 "$@"
}

# seed <логин|uuid> [quick|full|grant-all] - тестовые данные octopusService для игрока.
seed() {
    local user="${1:?использование: $0 seed <логин|uuid> [quick|full|grant-all]}"
    local mode="${2:-quick}"
    local file

    case "$mode" in
        quick) file="$SEED_DIR/bootstrap_octopus_test_data_for_user.sql" ;;
        full) file="$SEED_DIR/octopus_seed_all_test_tables.sql" ;;
        grant-all) file="$SEED_DIR/grant_all_octopus_meta_to_user.sql" ;;
        *) echo "Неизвестный режим: $mode (quick|full|grant-all)"; exit 1 ;;
    esac

    local uuid="$user"
    if ! [[ "$user" =~ ^[0-9a-fA-F-]{36}$ ]]; then
        uuid="$(psql_in auth_db -tA -v name="$user" <<< "SELECT user_id FROM users WHERE username = :'name'")"
        [ -n "$uuid" ] || { echo "Пользователь $user не найден в auth_db"; exit 1; }
    fi

    echo "Сид '$mode' для $user ($uuid)..."
    if [ "$mode" = "grant-all" ]; then
        psql_in octopus_db -q -v user_id="'$uuid'" < "$file"
    else
        # В этих скриптах UUID зашит константой demo_user.
        sed -E "s/(demo_user CONSTANT uuid := ')[^']*'/\1$uuid'/" "$file" | psql_in octopus_db -q
    fi
}

ACTION="${1:-up}"

case "$ACTION" in
    up)
        docker compose $COMPOSE_ARGS up -d --wait
        ;;
    down)
        docker compose $COMPOSE_ARGS down
        ;;
    ps)
        docker compose $COMPOSE_ARGS ps
        ;;
    logs)
        docker compose $COMPOSE_ARGS logs -f --tail 200
        ;;
    reset)
        # Удаляет тома: при следующем up базы создадутся заново пустыми.
        read -r -p "Удалить все данные инфры? (yes): " answer
        [ "$answer" = "yes" ] && docker compose $COMPOSE_ARGS down -v
        ;;
    seed)
        shift
        seed "$@"
        ;;
    *)
        echo "Неизвестная команда: $ACTION"
        echo "Использование: $0 {up|down|ps|logs|reset|seed <логин|uuid> [quick|full|grant-all]}"
        exit 1
        ;;
esac
