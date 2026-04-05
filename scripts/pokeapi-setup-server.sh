#!/usr/bin/env bash
# Выполнять на сервере из корня репозитория (где docker-compose.yml).
# Соответствует POKEAPI.md (Linux): клон → pokeapi-data → migrate → build_all → удаление временных папок.
set -euo pipefail

ROOT="$(pwd)"
echo "== pokeapi-setup-server: ROOT=$ROOT =="

command -v git >/dev/null 2>&1 || { echo "Нужен git"; exit 1; }
command -v docker >/dev/null 2>&1 || { echo "Нужен docker"; exit 1; }

echo "== Redis + PokeAPI =="
docker compose up -d redis pokeapi --no-build --pull never \
  || docker compose up -d redis pokeapi --pull missing

echo "== Клон PokeAPI и CSV (POKEAPI.md) =="
rm -rf "${ROOT}/pokeapi-temp"
git clone --depth 1 https://github.com/PokeAPI/pokeapi.git "${ROOT}/pokeapi-temp"
mkdir -p "${ROOT}/pokeapi-data/data/v2/csv"
cp -r "${ROOT}/pokeapi-temp/data/v2/csv/"* "${ROOT}/pokeapi-data/data/v2/csv/"
ls -la "${ROOT}/pokeapi-data/data/v2/csv" | head -20

echo "== Перезапуск pokeapi с CSV =="
docker compose up -d pokeapi --no-build --force-recreate
sleep 8

echo "== migrate =="
docker exec pokeapi python manage.py migrate --settings=config.docker-compose

echo "== build_all() =="
docker exec pokeapi sh -c 'echo "from data.v2.build import build_all; build_all()" | python manage.py shell --settings=config.docker-compose'

echo "== Удаление pokeapi-temp и pokeapi-data (POKEAPI.md) =="
rm -rf "${ROOT}/pokeapi-temp"
rm -rf "${ROOT}/pokeapi-data"

echo "== Готово =="
