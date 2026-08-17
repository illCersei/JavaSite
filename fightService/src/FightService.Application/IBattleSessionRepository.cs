using FightService.Domain.Entities;
using FightService.Domain.ValueObjects;

namespace FightService.Application;

public interface IBattleSessionRepository
{
    Task<BattleSession?> LoadAsync(BattleId battleId, CancellationToken cancellationToken);

    /// <summary>
    /// Like <see cref="LoadAsync"/>, but locks the row (DB-level, held until the matching
    /// <see cref="SaveAsync"/> call commits) so two concurrent actions against the same battle
    /// serialize instead of both processing the same snapshot and one clobbering the other's
    /// result. Use for any load that will be followed by a mutation + save in the same request.
    /// </summary>
    Task<BattleSession?> LoadForUpdateAsync(BattleId battleId, CancellationToken cancellationToken);

    Task SaveAsync(BattleSession session, CancellationToken cancellationToken);
}
