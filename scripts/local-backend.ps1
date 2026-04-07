param(
    [ValidateSet("up", "down", "logs", "ps", "test")]
    [string]$Action = "up"
)

$ErrorActionPreference = "Stop"

$composeArgs = @(
    "--env-file", ".env.local",
    "-f", "docker-compose.local.yml"
)

switch ($Action) {
    "up" {
        docker compose @composeArgs up -d --build
    }
    "down" {
        docker compose @composeArgs down
    }
    "logs" {
        docker compose @composeArgs logs -f --tail 200
    }
    "ps" {
        docker compose @composeArgs ps
    }
}
