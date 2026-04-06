# JavaSite

Репозиторий пет проекта backend-микросервисов на **Spring Boot 3.5** и **Java 17** для изучения backend разработки,
микросервисной архитектуры, java-spring фреймворка.

---

## Реализовано:

| Возможность                                                                            | Сервисы |
|----------------------------------------------------------------------------------------|---------|
| Регистрация, логин, JWT, refresh                                                       | `auth` |
| Профиль пользователя, реакция на события                                               | `profile` |
| Данные Twitch, кэш Redis                                                               | `twitchService` |
| Кошелёк, периодическая награда, Kafka-команды                                          | `wallet` |
| Каталог покемонов (PokeAPI), возможность роллять покемнов за игровую валюту, инвентарь | `pokemonService` |
| Тестовый API (RabbitMQ)                                                                | `testApi` |
| Общие security/error утилиты                                                           | `common` |

---

## Карта проекта

```
JavaSite/
├── auth/
├── profile/
├── wallet/
├── twitchService/
├── pokemonService/
├── testApi/
├── common/            
├── docker-compose.yml
├── POKEAPI.md - это сторонний сервис, взятый с просторов гитхаба и запущенный на моей сервере
└── pom.xml
```

TODO: Добавить подробности по каждому сервису — в **README соответствующей папки модуля**.

---

## Стек

| Категория | Технологии                                                                     |
|--------|--------------------------------------------------------------------------------|
| Язык | Java 17, Maven                                                                 |
| Framework | Spring Boot 3.5 (Web, Data JPA, Security OAuth2 Resource Server, Kafka, Redis) |
| БД | PostgreSQL                                                                     |
| Инфраструктура | Docker Compose: Redis, RabbitMQ, Kafka, PokeAPI (image) - НЕ МОЙ               |
| CI/CD | GitHub Actions (`deploy-backend.yml`, `pokeapi-setup.yml`(не работает))        |

---

## Быстрый старт (локально)

1. **PostgreSQL** на хосте (или в Docker), заполнить `.env`:
   - `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`
   - `JWT_SECRET`, порты Kafka/RabbitMQ/Redis по необходимости
2. Запуск:
   ```bash
   docker compose up -d --build
   ```
3. Базовые URL (порты из `.env`, примеры типичны для `SERVER_PORT` на каждом сервисе):

| Сервис | Base URL (пример) |
|--------|-------------------|
| auth | `http://localhost:8001/api/v1/auth` |
| twitchService | `http://localhost:8002/api/v1/twitch` |
| testApi | `http://localhost:8003/api/v1/test` |
| profile | `http://localhost:8004/api/v1/profile` |
| wallet | `http://localhost:8005/api/v1/wallet` |
| pokemonService | `http://localhost:8006/api/v1/pokemon` |

**Swagger UI** (если включён springdoc в модуле): `/swagger-ui/index.html` относительно base path сервиса.

## CI/CD

| Workflow                                                                     | Когда | Описание |
|------------------------------------------------------------------------------|-------|----------|
| `.github/workflows/deploy-backend.yml`                                       | push `main` / вручную | Сборка JAR, rsync на сервер, `docker compose up` для сервисов приложений; PokeAPI только старт из образа |
| `.github/workflows/pokeapi-setup.yml` !!!ПОКА ЧТО НЕ РАБОТАЕТ ДЕЛАЕМ ВРУЧНУЮ | вручную | Полная инициализация данных PokeAPI по [POKEAPI.md](POKEAPI.md) |

**Secrets:** `SERVER_HOST`, `SERVER_PORT`, `SERVER_USER`, `SERVER_SSH_KEY`, `SERVER_KNOWN_HOSTS`, `BACKEND_REMOTE_PATH`, `BACKEND_ENV_FILE`.

---

## Логи

Каталоги на хосте: `./logs/...` (см. `docker-compose.yml`).

---

## TODO: СКРИНЫ



---

