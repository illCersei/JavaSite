FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

COPY fightService/fightService.sln ./
COPY fightService/src/FightService.Domain/FightService.Domain.csproj src/FightService.Domain/
COPY fightService/src/FightService.Application/FightService.Application.csproj src/FightService.Application/
COPY fightService/src/FightService.Infrastructure/FightService.Infrastructure.csproj src/FightService.Infrastructure/
COPY fightService/src/FightService.API/FightService.API.csproj src/FightService.API/
RUN dotnet restore fightService.sln

COPY fightService/src src
RUN dotnet publish src/FightService.API/FightService.API.csproj -c Release -o /app --no-restore

FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS runtime
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && chown -R app:app /app
COPY --from=build --chown=app:app /app .

USER app
EXPOSE 8007

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl --fail --silent --show-error -o /dev/null http://127.0.0.1:8007/health || exit 1

ENTRYPOINT ["dotnet", "FightService.API.dll"]
