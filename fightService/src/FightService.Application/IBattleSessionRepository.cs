using FightService.Domain.Entities;
using FightService.Domain.ValueObjects;

namespace FightService.Application;

public interface IBattleSessionRepository
{
    Task<BattleSession?> LoadAsync(BattleId battleId, CancellationToken cancellationToken);

    Task SaveAsync(BattleSession session, CancellationToken cancellationToken);
}
