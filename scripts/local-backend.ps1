param(
    [ValidateSet("up", "down", "logs", "ps", "test")]
    [string]$Action = "up"
)

$ErrorActionPreference = "Stop"

$composeArgs = @(
    "--env-file", ".env.local",
    "-f", "docker-compose.local.yml"
)

function Build-Backend {
    # Docker builds for the Java services expect a pre-built target/*.jar (see e.g.
    # wallet/Dockerfile) - build the whole Maven aggregator once instead of per-module.
    Write-Host "Building Java services (mvn package)..."
    mvn -q -DskipTests clean package
    if ($LASTEXITCODE -ne 0) { throw "Maven build failed" }

    # fightService's own Dockerfile builds it inside the image, but building it here too
    # fails fast on a compile error instead of burning time on a docker build first.
    Write-Host "Building fightService (.NET)..."
    dotnet build fightService/fightService.sln -c Release
    if ($LASTEXITCODE -ne 0) { throw "dotnet build failed" }
}

switch ($Action) {
    "up" {
        Build-Backend
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
