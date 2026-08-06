using System.Text.Json;
using FightService.Application;
using FightService.Domain.Entities;
using FightService.Domain.ValueObjects;
using Microsoft.EntityFrameworkCore;

namespace FightService.Infrastructure.Persistence;

public sealed class BattleSessionRepository(FightDbContext dbContext) : IBattleSessionRepository
{
    private static readonly JsonSerializerOptions JsonOptions = new(JsonSerializerDefaults.Web);

    public async Task<BattleSession?> LoadAsync(BattleId battleId, CancellationToken cancellationToken)
    {
        BattleSessionEntity? entity = await dbContext.BattleSessions
            .AsNoTracking()
            .FirstOrDefaultAsync(e => e.BattleId == battleId.Value, cancellationToken);

        if (entity is null)
        {
            return null;
        }

        BattleSessionSnapshot snapshot = JsonSerializer.Deserialize<BattleSessionSnapshot>(entity.StateJson, JsonOptions)
            ?? throw new InvalidOperationException($"Could not deserialize battle session {battleId.Value}");
        return BattleSnapshotMapper.ToDomain(snapshot);
    }

    public async Task SaveAsync(BattleSession session, CancellationToken cancellationToken)
    {
        BattleSessionSnapshot snapshot = BattleSnapshotMapper.ToSnapshot(session);
        string json = JsonSerializer.Serialize(snapshot, JsonOptions);
        DateTimeOffset now = DateTimeOffset.UtcNow;

        BattleSessionEntity? existing = await dbContext.BattleSessions
            .FirstOrDefaultAsync(e => e.BattleId == session.Id.Value, cancellationToken);

        if (existing is null)
        {
            dbContext.BattleSessions.Add(new BattleSessionEntity
            {
                BattleId = session.Id.Value,
                UserId = session.UserId,
                Status = session.Status.ToString(),
                StateJson = json,
                CreatedAt = now,
                UpdatedAt = now
            });
        }
        else
        {
            existing.Status = session.Status.ToString();
            existing.StateJson = json;
            existing.UpdatedAt = now;
        }

        await dbContext.SaveChangesAsync(cancellationToken);
    }
}
