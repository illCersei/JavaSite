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

        return entity is null ? null : Deserialize(battleId, entity);
    }

    public async Task<BattleSession?> LoadForUpdateAsync(BattleId battleId, CancellationToken cancellationToken)
    {
        await dbContext.Database.BeginTransactionAsync(cancellationToken);

        // Raw SQL FOR UPDATE (EF Core has no LINQ equivalent) - blocks any other transaction
        // trying to lock/update this same row until this one commits or rolls back, which is
        // what actually serializes concurrent /action calls for the same battle. The query
        // stays tracked (no AsNoTracking) so the entity SaveAsync mutates below is this same
        // instance, not a duplicate.
        BattleSessionEntity? entity = await dbContext.BattleSessions
            .FromSqlInterpolated($"SELECT * FROM battle_sessions WHERE battle_id = {battleId.Value} FOR UPDATE")
            .FirstOrDefaultAsync(cancellationToken);

        return entity is null ? null : Deserialize(battleId, entity);
    }

    public async Task SaveAsync(BattleSession session, CancellationToken cancellationToken)
    {
        BattleSessionSnapshot snapshot = BattleSnapshotMapper.ToSnapshot(session);
        string json = JsonSerializer.Serialize(snapshot, JsonOptions);
        DateTimeOffset now = DateTimeOffset.UtcNow;

        // If LoadForUpdateAsync already loaded (and locked) this row in the current
        // transaction, EF's change tracker identity map returns that same tracked instance
        // here instead of issuing a duplicate query.
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

        if (dbContext.Database.CurrentTransaction is not null)
        {
            await dbContext.Database.CommitTransactionAsync(cancellationToken);
        }
    }

    private static BattleSession Deserialize(BattleId battleId, BattleSessionEntity entity)
    {
        BattleSessionSnapshot snapshot = JsonSerializer.Deserialize<BattleSessionSnapshot>(entity.StateJson, JsonOptions)
            ?? throw new InvalidOperationException($"Could not deserialize battle session {battleId.Value}");
        return BattleSnapshotMapper.ToDomain(snapshot);
    }
}
