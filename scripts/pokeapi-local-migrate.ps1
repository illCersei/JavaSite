param(
    [ValidateSet("migrate", "seed")]
    [string]$Mode = "seed"
)

$ErrorActionPreference = "Stop"

$envFile = ".env.local"
$composeFile = "docker-compose.local.yml"
$pokeapiRepo = "https://github.com/PokeAPI/pokeapi.git"
$tempDir = "pokeapi-temp"
$csvSrc = Join-Path $tempDir "data/v2/csv"
$csvDst = "pokeapi-data/data/v2/csv"

function Read-DotEnv {
    param([string]$Path)
    $map = @{}
    if (!(Test-Path $Path)) { throw "Env file not found: $Path" }
    foreach ($line in Get-Content $Path) {
        $trim = $line.Trim()
        if ($trim.Length -eq 0) { continue }
        if ($trim.StartsWith("#")) { continue }
        $idx = $trim.IndexOf("=")
        if ($idx -lt 1) { continue }
        $k = $trim.Substring(0, $idx).Trim()
        $v = $trim.Substring($idx + 1).Trim()
        $map[$k] = $v
    }
    return $map
}

function Compose {
    param([Parameter(ValueFromRemainingArguments=$true)][string[]]$Args)
    docker compose --env-file $envFile -f $composeFile @Args
    if ($LASTEXITCODE -ne 0) { throw "docker compose failed (exit=$LASTEXITCODE)" }
}

Write-Host "== pokeapi-local-migrate =="
Write-Host "env: $envFile"
Write-Host "compose: $composeFile"
Write-Host "mode: $Mode"

Write-Host "== ensure containers up =="
$upArgs = @("up", "-d", "--pull", "missing", "postgres", "redis", "pokeapi")
Compose @upArgs

Write-Host "== recreate pokeapi (ensure env/ports) =="
$recreatePokeapiArgs = @("up", "-d", "--no-deps", "--force-recreate", "pokeapi")
Compose @recreatePokeapiArgs

Write-Host "== ensure CSV present =="
if (!(Test-Path $csvDst)) {
    New-Item -ItemType Directory -Force -Path $csvDst | Out-Null
}
$csvFiles = Get-ChildItem -Path $csvDst -File -ErrorAction SilentlyContinue
if ($null -eq $csvFiles -or $csvFiles.Count -eq 0) {
    if (!(Get-Command git -ErrorAction SilentlyContinue)) {
        throw "git не найден. Нужен git чтобы скачать CSV PokeAPI (см. POKEAPI.md)."
    }
    if (Test-Path $tempDir) { Remove-Item $tempDir -Recurse -Force }
    Write-Host "CSV папка пустая. Клонирую PokeAPI и копирую data/v2/csv..."
    git clone --depth 1 $pokeapiRepo $tempDir | Out-Host
    Copy-Item -Path (Join-Path $csvSrc "*") -Destination $csvDst -Recurse -Force
    Remove-Item $tempDir -Recurse -Force

    Write-Host "== recreate pokeapi (to pick up CSV volume) =="
    $recreateArgs = @("up", "-d", "--no-deps", "--force-recreate", "pokeapi")
    Compose @recreateArgs
}

Write-Host "== ensure pokeapi database exists =="
$envMap = Read-DotEnv $envFile
$dbUser = ($envMap["DB_USER"] | ForEach-Object { $_.Trim() })
$pokeDb = ($envMap["POKEAPI_DB"] | ForEach-Object { $_.Trim() })
if ([string]::IsNullOrWhiteSpace($dbUser) -or [string]::IsNullOrWhiteSpace($pokeDb)) {
    throw "DB_USER/POKEAPI_DB must be set in $envFile"
}
$exists = (docker exec postgres_container psql -U $dbUser -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$pokeDb';" | Out-String)
if ($LASTEXITCODE -ne 0) { throw "psql check database failed (exit=$LASTEXITCODE)" }
if ($exists.Trim() -ne "1") {
    Write-Host "Creating database: $pokeDb"
    $safeDb = $pokeDb.Replace('"', '""')
    docker exec postgres_container psql -U $dbUser -d postgres -c "CREATE DATABASE ""$safeDb"";"
    if ($LASTEXITCODE -ne 0) { throw "psql create database failed (exit=$LASTEXITCODE)" }
}

Write-Host "== migrate =="
docker exec pokeapi python manage.py migrate --settings=config.docker-compose
if ($LASTEXITCODE -ne 0) { throw "pokeapi migrate failed (exit=$LASTEXITCODE)" }

if ($Mode -eq "seed") {
    Write-Host "== build_all() seed =="
    docker exec pokeapi sh -c "echo 'from data.v2.build import build_all; build_all()' | python manage.py shell --settings=config.docker-compose"
    if ($LASTEXITCODE -ne 0) { throw "pokeapi seed build_all failed (exit=$LASTEXITCODE)" }
}

echo "== cleanup =="
Remove-Item -Recurse -Force "./pokeapi-temp" -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force "./pokeapi-data" -ErrorAction SilentlyContinue

Write-Host "== done =="
