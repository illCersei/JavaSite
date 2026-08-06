namespace FightService.Infrastructure.Persistence;

public sealed class BattleSessionEntity
{
    public required string BattleId { get; set; }
    public required Guid UserId { get; set; }
    public required string Status { get; set; }
    public required string StateJson { get; set; }
    public required DateTimeOffset CreatedAt { get; set; }
    public required DateTimeOffset UpdatedAt { get; set; }
}
