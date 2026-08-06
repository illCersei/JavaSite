using FightService.Application.Contracts;
using FightService.Domain.Entities;
using FightService.Domain.Enums;
using FightService.Domain.Enums.Units;
using FightService.Domain.Exceptions;
using FightService.Domain.ValueObjects;
using DomainSkill = FightService.Domain.Entities.Skill;

namespace FightService.Application;

public static class BattleMapper
{
    public static Stats ToStats(CombatStatsDto dto) => Stats.Create(
        dto.Hp, dto.Attack, dto.MagicPower, dto.Armor, dto.MagicResist, dto.Speed,
        dto.CritChance, dto.CritDamage, dto.Accuracy, dto.Evasion, dto.Tenacity, dto.StatusPower);

    public static Effect ToEffect(SkillEffectDto dto) => new(
        ParseEnum<SkillEffectType>(dto.EffectType),
        ParseEnum<ElementType>(dto.ElementType),
        dto.BaseValue,
        dto.ScalingStat is { } s ? ParseEnum<ScalingStat>(s) : null,
        dto.ScalingRatioBps ?? 0,
        dto.DurationMs,
        dto.TickMs,
        dto.StackingRule is { } r ? ParseEnum<StackingRule>(r) : null);

    public static DomainSkill ToSkill(SkillDto dto) => new(
        new SkillId(dto.Id),
        dto.Name,
        dto.Description ?? string.Empty,
        ParseEnum<ElementType>(dto.ElementType),
        dto.CooldownMs,
        dto.ManaCost,
        dto.Effects.Select(ToEffect));

    public static Combatant ToCombatant(FightCombatantDto dto, bool isPlayerControlled) => new(
        CombatantId.Create(dto.CombatantId),
        dto.Name,
        isPlayerControlled,
        ToStats(dto.Stats),
        dto.Skills.Select(ToSkill).ToList());

    public static BattleSession ToBattleSession(FightStartRequest request)
    {
        BattleId battleId = BattleId.Create(request.BattleId);
        FightSource source = ParseEnum<FightSource>(request.Context.Source);

        List<Combatant> playerSquad = request.PlayerSquad.Fighters.Select(f => ToCombatant(f, true)).ToList();
        List<Combatant> enemySquad = request.EnemySquad.Fighters.Select(f => ToCombatant(f, false)).ToList();

        return new BattleSession(
            battleId,
            request.Context.UserId,
            source,
            request.Context.DungeonRunId,
            request.Context.DungeonRoomId,
            request.RngSeed,
            request.Rules.MaxTurns,
            playerSquad,
            enemySquad);
    }

    public static FightStateDto ToStateDto(BattleSession session)
    {
        IReadOnlyList<CombatantStateDto> playerSquad = session.PlayerSquad.Select(ToCombatantStateDto).ToList();
        IReadOnlyList<CombatantStateDto> enemySquad = session.EnemySquad.Select(ToCombatantStateDto).ToList();
        IReadOnlyList<BattleEventDto> events = session.EventLog.Select(ToEventDto).ToList();

        return new FightStateDto(
            session.Id.Value,
            session.Status.ToString(),
            session.CurrentActorId?.Value,
            session.TurnNumber,
            session.Status == BattleStatus.FINISHED,
            session.Result?.ToString(),
            playerSquad,
            enemySquad,
            events);
    }

    private static CombatantStateDto ToCombatantStateDto(Combatant combatant) => new(
        combatant.Id.Value,
        combatant.CurrentHp,
        combatant.IsDead,
        combatant.TurnMeter);

    private static BattleEventDto ToEventDto(BattleEvent battleEvent) => new(
        battleEvent.ActorId,
        battleEvent.SkillId,
        battleEvent.TargetIds,
        battleEvent.Missed,
        battleEvent.Crit,
        battleEvent.DamageDealt,
        battleEvent.HealingDone);

    private static TEnum ParseEnum<TEnum>(string value) where TEnum : struct, Enum
    {
        if (Enum.TryParse(value, ignoreCase: false, out TEnum result))
        {
            return result;
        }
        throw new DomainException($"Unknown {typeof(TEnum).Name} value: '{value}'");
    }
}
