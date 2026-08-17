using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;

namespace FightService.Infrastructure.Persistence;

// Design-time only - lets `dotnet ef migrations` run without the API project's DI container.
// The runtime connection string always comes from FightService.API's configuration instead.
public sealed class FightDbContextFactory : IDesignTimeDbContextFactory<FightDbContext>
{
    public FightDbContext CreateDbContext(string[] args)
    {
        string connectionString = Environment.GetEnvironmentVariable("FIGHT_DB_CONNECTION_STRING")
            ?? "Host=localhost;Port=5432;Database=fight_db;Username=postgres;Password=postgres";

        DbContextOptionsBuilder<FightDbContext> optionsBuilder = new();
        optionsBuilder.UseNpgsql(connectionString);
        return new FightDbContext(optionsBuilder.Options);
    }
}
