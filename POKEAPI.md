# README — загрузка данных в PokeAPI

Ниже последовательные шаги, как были загружены данные в базу

ВАЖНО!! СНАЧАЛА НУЖНО СДЕЛАТЬ МАНИПУЛЯЦИИ С ПАПКАМИ ПОТОМ ПОДНИМАТЬ КОНТЕЙНЕР

## Что было сделано

### Windows PowerShell

```powershell
ls
git clone https://github.com/PokeAPI/pokeapi.git .\pokeapi-temp
New-Item -ItemType Directory -Force -Path .\pokeapi-data\data\v2\csv
Copy-Item .\pokeapi-temp\data\v2\csv\* .\pokeapi-data\data\v2\csv\ -Recurse -Force
Get-ChildItem .\pokeapi-data\data\v2\csv
```

### Команды внутри Docker

```powershell
docker exec pokeapi python manage.py migrate --settings=config.docker-compose
docker exec -it pokeapi python manage.py shell --settings=config.docker-compose
```

### Внутри Django shell

```python
from data.v2.build import build_all
build_all()
```

## Команды для удаления папок

### Windows PowerShell

Удалить только временный клон:

```powershell
Remove-Item .\pokeapi-temp -Recurse -Force
```

Удалить всю папку `pokeapi-data`:

```powershell
Remove-Item .\pokeapi-data -Recurse -Force
```


## Аналогичные команды для Linux

### Linux shell

```bash
ls
git clone https://github.com/PokeAPI/pokeapi.git ./pokeapi-temp
mkdir -p ./pokeapi-data/data/v2/csv
cp -r ./pokeapi-temp/data/v2/csv/* ./pokeapi-data/data/v2/csv/
ls ./pokeapi-data/data/v2/csv
```

### Команды внутри Docker

```bash
docker exec pokeapi python manage.py migrate --settings=config.docker-compose
docker exec -it pokeapi python manage.py shell --settings=config.docker-compose
```

### Внутри Django shell

```python
from data.v2.build import build_all
build_all()
```

### Linux shell

Удалить только временный клон:

```bash
rm -rf ./pokeapi-temp
```

Удалить всю папку `pokeapi-data`:

```bash
rm -rf ./pokeapi-data
```

## Автоматизация (сервер, CI)

Тот же порядок шагов вынесен в скрипт репозитория:

```bash
chmod +x scripts/pokeapi-setup-server.sh
./scripts/pokeapi-setup-server.sh
```

Запуск из корня проекта (рядом с `docker-compose.yml`). На сервере нужны `git` и `docker compose`.

В GitHub Actions: workflow **`pokeapi-setup`** (`workflow_dispatch`) копирует этот скрипт на сервер (secret `BACKEND_REMOTE_PATH`) и выполняет его.

