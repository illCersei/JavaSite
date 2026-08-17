namespace FightService.Application.Contracts;

// Compact per-action log entry so the client can animate what happened between the player's
// click and their next turn (their own action plus every auto-resolved AI turn in between).
public sealed record BattleEventDto(
    string ActorId,
    int? SkillId,
    IReadOnlyList<string> TargetIds,
    bool Missed,
    bool Crit,
    IReadOnlyDictionary<string, int> DamageDealt,
    IReadOnlyDictionary<string, int> HealingDone);

// Extends the original FightCombatantStateDto (combatantId, hpRemaining, dead) with turnMeterBps.
public sealed record CombatantStateDto(string CombatantId, int HpRemaining, bool Dead, int TurnMeterBps);

// Replaces FightStartResponse and FightResultResponse: returned by /fight/start,
// /fight/{battleId}/action and /fight/{battleId}/state alike.
public sealed record FightStateDto(
    string BattleId,
    string Status,
    string? CurrentActorId,
    int TurnNumber,
    bool Finished,
    string? Result,
    IReadOnlyList<CombatantStateDto> PlayerSquad,
    IReadOnlyList<CombatantStateDto> EnemySquad,
    IReadOnlyList<BattleEventDto> Events);
