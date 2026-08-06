using FightService.Domain.Entities;
using FightService.Domain.Enums;
using FightService.Domain.Enums.Units;
using FightService.Domain.ValueObjects;
using DomainSkill = FightService.Domain.Entities.Skill;

namespace FightService.Infrastructure.Persistence;

public static class BattleSnapshotMapper
{
    public static BattleSessionSnapshot ToSnapshot(BattleSession session) => new()
    {
        BattleId = session.Id.Value,
        UserId = session.UserId,
        Source = session.Source.ToString(),
        DungeonRunId = session.DungeonRunId,
        DungeonRoomId = session.DungeonRoomId,
        RngSeed = session.RngSeed,
        MaxTurns = session.MaxTurns,
        Status = session.Status.ToString(),
        ClockMs = session.ClockMs,
        CurrentActorId = session.CurrentActorId?.Value,
        Result = session.Result?.ToString(),
        TurnNumber = session.TurnNumber,
        PlayerSquad = session.PlayerSquad.Select(ToCombatantSnapshot).ToList(),
        EnemySquad = session.EnemySquad.Select(ToCombatantSnapshot).ToList(),
        EventLog = session.EventLog.Select(ToEventSnapshot).ToList()
    };

    public static BattleSession ToDomain(BattleSessionSnapshot snapshot) => BattleSession.Restore(
        BattleId.Create(snapshot.BattleId),
        snapshot.UserId,
        ParseEnum<FightSource>(snapshot.Source),
        snapshot.DungeonRunId,
        snapshot.DungeonRoomId,
        snapshot.RngSeed,
        snapshot.MaxTurns,
        snapshot.PlayerSquad.Select(ToCombatant).ToList(),
        snapshot.EnemySquad.Select(ToCombatant).ToList(),
        ParseEnum<BattleStatus>(snapshot.Status),
        snapshot.ClockMs,
        snapshot.CurrentActorId is { } actorId ? CombatantId.Create(actorId) : null,
        snapshot.Result is { } result ? ParseEnum<BattleResult>(result) : null,
        snapshot.TurnNumber,
        snapshot.EventLog.Select(ToBattleEvent));

    private static CombatantSnapshot ToCombatantSnapshot(Combatant combatant) => new()
    {
        Id = combatant.Id.Value,
        Name = combatant.Name,
        IsPlayerControlled = combatant.IsPlayerControlled,
        BaseStats = ToStatsSnapshot(combatant.BaseStats),
        CurrentHp = combatant.CurrentHp,
        ShieldPoints = combatant.ShieldPoints,
        TurnMeter = combatant.TurnMeter,
        Skills = combatant.Skills.Select(ToSkillSnapshot).ToList(),
        SkillCooldownsReadyAtMs = new Dictionary<int, int>(combatant.SkillCooldownsReadyAtMs),
        ActiveEffects = combatant.ActiveEffects.Select(ToActiveEffectSnapshot).ToList()
    };

    private static Combatant ToCombatant(CombatantSnapshot snapshot) => Combatant.Restore(
        CombatantId.Create(snapshot.Id),
        snapshot.Name,
        snapshot.IsPlayerControlled,
        ToStats(snapshot.BaseStats),
        snapshot.Skills.Select(ToSkill).ToList(),
        snapshot.CurrentHp,
        snapshot.ShieldPoints,
        snapshot.TurnMeter,
        snapshot.SkillCooldownsReadyAtMs,
        snapshot.ActiveEffects.Select(ToActiveEffect));

    private static StatsSnapshot ToStatsSnapshot(Stats stats) => new()
    {
        Hp = stats.Hp,
        Attack = stats.Attack,
        MagicPower = stats.MagicPower,
        Armor = stats.Armor,
        MagicResist = stats.MagicResist,
        Speed = stats.Speed,
        CritChance = stats.CritChance,
        CritDamage = stats.CritDamage,
        Accuracy = stats.Accuracy,
        Evasion = stats.Evasion,
        Tenacity = stats.Tenacity,
        StatusPower = stats.StatusPower
    };

    private static Stats ToStats(StatsSnapshot snapshot) => Stats.Create(
        snapshot.Hp, snapshot.Attack, snapshot.MagicPower, snapshot.Armor, snapshot.MagicResist, snapshot.Speed,
        snapshot.CritChance, snapshot.CritDamage, snapshot.Accuracy, snapshot.Evasion, snapshot.Tenacity, snapshot.StatusPower);

    private static SkillSnapshot ToSkillSnapshot(DomainSkill skill) => new()
    {
        Id = skill.Id.Value,
        Name = skill.Name,
        Description = skill.Description,
        ElementType = skill.ElementType.ToString(),
        CooldownMs = skill.CooldownMs,
        ManaCost = skill.ManaCost,
        Effects = skill.Effects.Select(ToEffectSnapshot).ToList()
    };

    private static DomainSkill ToSkill(SkillSnapshot snapshot) => new(
        new SkillId(snapshot.Id),
        snapshot.Name,
        snapshot.Description,
        ParseEnum<ElementType>(snapshot.ElementType),
        snapshot.CooldownMs,
        snapshot.ManaCost,
        snapshot.Effects.Select(ToEffect));

    private static EffectSnapshot ToEffectSnapshot(Effect effect) => new()
    {
        EffectType = effect.EffectType.ToString(),
        ElementType = effect.ElementType.ToString(),
        BaseValue = effect.BaseValue,
        ScalingStat = effect.ScalingStat?.ToString(),
        ScalingRatioBps = effect.ScalingRatioBps,
        DurationMs = effect.DurationMs,
        TickMs = effect.TickMs,
        StackingRule = effect.StackingRule?.ToString()
    };

    private static Effect ToEffect(EffectSnapshot snapshot) => new(
        ParseEnum<SkillEffectType>(snapshot.EffectType),
        ParseEnum<ElementType>(snapshot.ElementType),
        snapshot.BaseValue,
        snapshot.ScalingStat is { } s ? ParseEnum<ScalingStat>(s) : null,
        snapshot.ScalingRatioBps,
        snapshot.DurationMs,
        snapshot.TickMs,
        snapshot.StackingRule is { } r ? ParseEnum<StackingRule>(r) : null);

    private static ActiveEffectSnapshot ToActiveEffectSnapshot(ActiveEffect effect) => new()
    {
        SourceSkillName = effect.SourceSkillName,
        EffectType = effect.EffectType.ToString(),
        ElementType = effect.ElementType.ToString(),
        AffectedStat = effect.AffectedStat?.ToString(),
        Magnitude = effect.Magnitude,
        TickMs = effect.TickMs,
        Stacking = effect.Stacking.ToString(),
        Stacks = effect.Stacks,
        ExpiresAtMs = effect.ExpiresAtMs,
        NextTickAtMs = effect.NextTickAtMs
    };

    private static ActiveEffect ToActiveEffect(ActiveEffectSnapshot snapshot) => ActiveEffect.Restore(
        snapshot.SourceSkillName,
        ParseEnum<SkillEffectType>(snapshot.EffectType),
        ParseEnum<ElementType>(snapshot.ElementType),
        snapshot.AffectedStat is { } s ? ParseEnum<ScalingStat>(s) : null,
        snapshot.Magnitude,
        snapshot.TickMs,
        ParseEnum<StackingRule>(snapshot.Stacking),
        snapshot.Stacks,
        snapshot.ExpiresAtMs,
        snapshot.NextTickAtMs);

    private static BattleEventSnapshot ToEventSnapshot(BattleEvent battleEvent) => new()
    {
        ActorId = battleEvent.ActorId,
        SkillId = battleEvent.SkillId,
        TargetIds = battleEvent.TargetIds.ToList(),
        Missed = battleEvent.Missed,
        Crit = battleEvent.Crit,
        DamageDealt = new Dictionary<string, int>(battleEvent.DamageDealt),
        HealingDone = new Dictionary<string, int>(battleEvent.HealingDone)
    };

    private static BattleEvent ToBattleEvent(BattleEventSnapshot snapshot) => new(
        snapshot.ActorId,
        snapshot.SkillId,
        snapshot.TargetIds,
        snapshot.Missed,
        snapshot.Crit,
        snapshot.DamageDealt,
        snapshot.HealingDone);

    private static TEnum ParseEnum<TEnum>(string value) where TEnum : struct, Enum => Enum.Parse<TEnum>(value);
}
