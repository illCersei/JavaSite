namespace FightService.Infrastructure.Persistence;

// Plain, fully-serializable mirror of the BattleSession aggregate - this is what actually
// goes into the `battle_sessions.state` jsonb column. Kept separate from the domain entities
// so those can stay properly encapsulated (private setters, behavior) while this stays a
// dumb data bag System.Text.Json can round-trip without any custom converters.
public sealed class BattleSessionSnapshot
{
    public required string BattleId { get; init; }
    public required Guid UserId { get; init; }
    public required string Source { get; init; }
    public Guid? DungeonRunId { get; init; }
    public long? DungeonRoomId { get; init; }
    public required long RngSeed { get; init; }
    public required int MaxTurns { get; init; }
    public required string Status { get; init; }
    public required int ClockMs { get; init; }
    public string? CurrentActorId { get; init; }
    public string? Result { get; init; }
    public required int TurnNumber { get; init; }
    public required List<CombatantSnapshot> PlayerSquad { get; init; }
    public required List<CombatantSnapshot> EnemySquad { get; init; }
    public required List<BattleEventSnapshot> EventLog { get; init; }
}

public sealed class CombatantSnapshot
{
    public required string Id { get; init; }
    public required string Name { get; init; }
    public required bool IsPlayerControlled { get; init; }
    public required StatsSnapshot BaseStats { get; init; }
    public required int CurrentHp { get; init; }
    public required int ShieldPoints { get; init; }
    public required int TurnMeter { get; init; }
    public required List<SkillSnapshot> Skills { get; init; }
    public required Dictionary<int, int> SkillCooldownsReadyAtMs { get; init; }
    public required List<ActiveEffectSnapshot> ActiveEffects { get; init; }
}

public sealed class StatsSnapshot
{
    public required int Hp { get; init; }
    public required int Attack { get; init; }
    public required int MagicPower { get; init; }
    public required int Armor { get; init; }
    public required int MagicResist { get; init; }
    public required int Speed { get; init; }
    public required int CritChance { get; init; }
    public required int CritDamage { get; init; }
    public required int Accuracy { get; init; }
    public required int Evasion { get; init; }
    public required int Tenacity { get; init; }
    public required int StatusPower { get; init; }
}

public sealed class SkillSnapshot
{
    public required int Id { get; init; }
    public required string Name { get; init; }
    public required string Description { get; init; }
    public required string ElementType { get; init; }
    public required int CooldownMs { get; init; }
    public required int ManaCost { get; init; }
    public required List<EffectSnapshot> Effects { get; init; }
}

public sealed class EffectSnapshot
{
    public required string EffectType { get; init; }
    public required string ElementType { get; init; }
    public required int BaseValue { get; init; }
    public string? ScalingStat { get; init; }
    public required int ScalingRatioBps { get; init; }
    public int? DurationMs { get; init; }
    public int? TickMs { get; init; }
    public string? StackingRule { get; init; }
}

public sealed class ActiveEffectSnapshot
{
    public required string SourceSkillName { get; init; }
    public required string EffectType { get; init; }
    public required string ElementType { get; init; }
    public string? AffectedStat { get; init; }
    public required int Magnitude { get; init; }
    public required int TickMs { get; init; }
    public required string Stacking { get; init; }
    public required int Stacks { get; init; }
    public required int ExpiresAtMs { get; init; }
    public required int NextTickAtMs { get; init; }
}

public sealed class BattleEventSnapshot
{
    public required string ActorId { get; init; }
    public int? SkillId { get; init; }
    public required List<string> TargetIds { get; init; }
    public required bool Missed { get; init; }
    public required bool Crit { get; init; }
    public required Dictionary<string, int> DamageDealt { get; init; }
    public required Dictionary<string, int> HealingDone { get; init; }
}
