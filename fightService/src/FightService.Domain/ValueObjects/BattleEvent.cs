namespace FightService.Domain.ValueObjects;

public sealed record BattleEvent(
    string ActorId,
    int? SkillId,
    IReadOnlyList<string> TargetIds,
    bool Missed,
    bool Crit,
    IReadOnlyDictionary<string, int> DamageDealt,
    IReadOnlyDictionary<string, int> HealingDone
);
