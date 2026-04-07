#!/usr/bin/env bash
# Локальная инициализация PokeAPI в docker-compose.local.yml:
# - миграции Django
# - (опционально) загрузка справочных данных build_all()
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

ENV_FILE="${ENV_FILE:-.env.local}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.local.yml}"
POKEAPI_REPO="${POKEAPI_REPO:-https://github.com/PokeAPI/pokeapi.git}"

MODE="${1:-seed}" # migrate | seed

compose() {
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"
}

echo "== pokeapi-local-migrate =="
echo "ROOT=$ROOT"
echo "ENV_FILE=$ENV_FILE"
echo "COMPOSE_FILE=$COMPOSE_FILE"
echo "MODE=$MODE"

echo "== ensure containers up =="
compose up -d --pull missing postgres redis pokeapi

echo "== recreate pokeapi (ensure env/ports) =="
compose up -d --no-deps --force-recreate pokeapi

echo "== ensure CSV present =="
mkdir -p "./pokeapi-data/data/v2/csv"
if [[ -z "$(ls -A ./pokeapi-data/data/v2/csv 2>/dev/null || true)" ]]; then
  command -v git >/dev/null 2>&1 || { echo "Нужен git чтобы скачать CSV PokeAPI (см. POKEAPI.md)"; exit 1; }
  rm -rf "./pokeapi-temp"
  echo "CSV папка пустая. Клонирую PokeAPI и копирую data/v2/csv..."
  git clone --depth 1 "$POKEAPI_REPO" "./pokeapi-temp"
  cp -r "./pokeapi-temp/data/v2/csv/"* "./pokeapi-data/data/v2/csv/"
  rm -rf "./pokeapi-temp"

  echo "== recreate pokeapi (to pick up CSV volume) =="
  compose up -d --no-deps --force-recreate pokeapi
fi

echo "== ensure pokeapi database exists =="
set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a
if [[ -z "${DB_USER:-}" || -z "${POKEAPI_DB:-}" ]]; then
  echo "DB_USER/POKEAPI_DB must be set in $ENV_FILE"
  exit 1
fi
if ! docker exec postgres_container psql -U "$DB_USER" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='${POKEAPI_DB}';" | grep -q 1; then
  echo "Creating database: $POKEAPI_DB"
  docker exec postgres_container psql -U "$DB_USER" -d postgres -c "CREATE DATABASE \"${POKEAPI_DB}\";"
fi

echo "== migrate =="
docker exec pokeapi python manage.py migrate --settings=config.docker-compose

if [[ "$MODE" == "seed" ]]; then
  echo "== build_all() seed =="
  docker exec pokeapi sh -c 'echo "from data.v2.build import build_all; build_all()" | python manage.py shell --settings=config.docker-compose'
fi

echo "== cleanup =="
rm -rf "./pokeapi-temp"
rm -rf "./pokeapi-data"

echo "== done =="
