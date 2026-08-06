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
COPY --from=build /app .

ENTRYPOINT ["dotnet", "FightService.API.dll"]
