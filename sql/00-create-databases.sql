-- Базы сервисов. Postgres выполняет этот файл сам при первом запуске на пустом томе
-- (sql/ смонтирована в /docker-entrypoint-initdb.d в docker-compose.local*.yml).
SELECT format('CREATE DATABASE %I', db)
FROM unnest(ARRAY['auth_db', 'profile_db', 'wallet_db', 'octopus_db', 'fight_db', 'twitch_monitor']) AS db
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = db)
\gexec
