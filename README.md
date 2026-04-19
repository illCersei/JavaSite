# JavaSite

Пет-проект: **набор связанных backend-сервисов** на **Spring Boot** и **Java 17** в одном Maven-монорепозитории.

Реализация проекта с фронтендом:   [Фронтенд написан на vue](https://gorelov.net/)
---

## Сервисы

| Область      | Суть                                                                                          |
|--------------|-----------------------------------------------------------------------------------------------|
| **auth**     | Регистрация и вход, **JWT** (access) и **refresh**-токены; общий секрет для resource servers. |
| **profile**  | Профиль пользователя и реакция на события приложения.                                         |
| **twitch**   | Интеграция с данными Twitch, собранными мною, **кэш в Redis**.                                |
| **wallet**   | Баланс, периодическая награда, **Kafka**: команды/ответы, журнал проводок с идемпотентностью  |
| **pokemon**  | Сервис покемонов, инвентаря покемонов                                                         |
| **test API** | Демонстрация **RabbitMQ**.                                                                    |
| **common**   | Общие коспоненты для сервисов                                                                 |
---
## Структура репозитория

Детали по API и конфигурации модулей — в **README соответствующей папки** (будет). //TODO

---

## Стек

| Категория      | Технологии                                                                     |
|----------------|--------------------------------------------------------------------------------|
| Язык           | Java 17, Maven                                                                 |
| Framework      | Spring Boot 3.5 (Web, Data JPA, Security OAuth2 Resource Server, Kafka, Redis) |
| Данные         | PostgreSQL, Redis                                                              |
| Инфраструктура | Docker Compose: Redis, RabbitMQ, Kafka                                         |
| CI             | GitHub Actions (сборка и деплой сервисов на разные сервера                     |
| Мониторинг     | Prometheus + Grafana                                                           |

---

## Мониторинг Prometheus + Grafana который мониторит сервисы на всех серверах, а так же сами сервера

![image alt](https://github.com/illCersei/JavaSite/blob/c9f92d3b152130d8f2bad0896d22c3e0621e3945/screenshots/monitoring.png)

---

## Скриншот коллекции

![image alt](https://github.com/illCersei/JavaSite/blob/c9f92d3b152130d8f2bad0896d22c3e0621e3945/screenshots/pokemonCollectionExample.png)

---

