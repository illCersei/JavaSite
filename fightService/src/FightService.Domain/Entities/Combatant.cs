using FightService.Domain.Enums;
using FightService.Domain.ValueObjects;

namespace FightService.Domain.Entities;

public sealed class Combatant
{
    public const int TurnMeterReadyThreshold = 10_000;

    public CombatantId Id { get; }
    public string Name { get; }
    public bool IsPlayerControlled { get; }
    public Stats BaseStats { get; }
    public int MaxHp { get; }
    public int CurrentHp { get; private set; }
    public int ShieldPoints { get; private set; }
    public int TurnMeter { get; private set; }
    public IReadOnlyList<Skill> Skills { get; }
    public IReadOnlyList<ActiveEffect> ActiveEffects => _activeEffects;

    private readonly List<ActiveEffect> _activeEffects = new();
    private readonly Dictionary<int, int> _skillReadyAtMs = new();

    public Combatant(CombatantId id, string name, bool isPlayerControlled, Stats baseStats, IReadOnlyList<Skill> skills)
    {
        Id = id;
        Name = name;
        IsPlayerControlled = isPlayerControlled;
        BaseStats = baseStats;
        MaxHp = baseStats.Hp;
        CurrentHp = baseStats.Hp;
        Skills = skills;
    }

    // Rehydration from a persisted snapshot - restores exact mutable battle state instead of
    // starting a fresh combatant at full HP/zero turn meter.
    public static Combatant Restore(
        CombatantId id,
        string name,
        bool isPlayerControlled,
        Stats baseStats,
        IReadOnlyList<Skill> skills,
        int currentHp,
        int shieldPoints,
        int turnMeter,
        IReadOnlyDictionary<int, int> skillCooldownsReadyAtMs,
        IEnumerable<ActiveEffect> activeEffects)
    {
        var combatant = new Combatant(id, name, isPlayerControlled, baseStats, skills)
        {
            CurrentHp = currentHp,
            ShieldPoints = shieldPoints,
            TurnMeter = turnMeter
        };
        foreach (KeyValuePair<int, int> entry in skillCooldownsReadyAtMs)
        {
            combatant._skillReadyAtMs[entry.Key] = entry.Value;
        }
        combatant._activeEffects.AddRange(activeEffects);
        return combatant;
    }

    public IReadOnlyDictionary<int, int> SkillCooldownsReadyAtMs => _skillReadyAtMs;

    public bool IsDead => CurrentHp <= 0;

    public Stats EffectiveStats()
    {
        int attack = BaseStats.Attack;
        int magicPower = BaseStats.MagicPower;
        int armor = BaseStats.Armor;
        int magicResist = BaseStats.MagicResist;
        int speed = BaseStats.Speed;

        foreach (ActiveEffect effect in _activeEffects)
        {
            if (effect.AffectedStat is null)
            {
                continue;
            }
            int delta = effect.EffectType == SkillEffectType.DEBUFF ? -effect.Magnitude : effect.Magnitude;
            switch (effect.AffectedStat)
            {
                case ScalingStat.ATTACK: attack += delta; break;
                case ScalingStat.MAGIC_POWER: magicPower += delta; break;
                case ScalingStat.ARMOR: armor += delta; break;
                case ScalingStat.MAGIC_RESIST: magicResist += delta; break;
                case ScalingStat.SPEED: speed += delta; break;
            }
        }

        return Stats.Create(
            MaxHp, attack, magicPower, armor, magicResist, speed,
            BaseStats.CritChance, BaseStats.CritDamage, BaseStats.Accuracy,
            BaseStats.Evasion, BaseStats.Tenacity, BaseStats.StatusPower);
    }

    public void GainTurnMeter(int amount) => TurnMeter += amount;

    public bool IsReadyToAct() => !IsDead && TurnMeter >= TurnMeterReadyThreshold;

    public void ConsumeTurnMeter() => TurnMeter = Math.Max(0, TurnMeter - TurnMeterReadyThreshold);

    public bool IsSkillReady(SkillId skillId, int nowMs) =>
        !_skillReadyAtMs.TryGetValue(skillId.Value, out int readyAt) || nowMs >= readyAt;

    public void PutSkillOnCooldown(SkillId skillId, int cooldownMs, int nowMs) =>
        _skillReadyAtMs[skillId.Value] = nowMs + cooldownMs;

    public void ApplyDamage(int amount)
    {
        int absorbed = Math.Min(ShieldPoints, amount);
        ShieldPoints -= absorbed;
        CurrentHp = Math.Max(0, CurrentHp - (amount - absorbed));
    }

    // A dead combatant stays dead until whatever revive/resurrect mechanic the game
    // design eventually adds explicitly overrides this - plain HEAL/HOT must not.
    public void Heal(int amount)
    {
        if (IsDead)
        {
            return;
        }
        CurrentHp = Math.Min(MaxHp, CurrentHp + amount);
    }

    // Shields absorb damage before HP until consumed; a fresh cast overwrites the pool
    // (no independent time-based expiry - simplest model that's still distinct from HEAL).
    public void SetShield(int amount) => ShieldPoints = Math.Max(ShieldPoints, amount);

    public void AddOrRefreshEffect(ActiveEffect incoming, int nowMs)
    {
        if (incoming.Stacking == StackingRule.NO_STACK)
        {
            if (_activeEffects.Any(e => e.EffectType == incoming.EffectType && e.SourceSkillName == incoming.SourceSkillName))
            {
                return;
            }
            _activeEffects.Add(incoming);
            return;
        }

        ActiveEffect? existing = _activeEffects.FirstOrDefault(
            e => e.EffectType == incoming.EffectType && e.SourceSkillName == incoming.SourceSkillName);

        if (existing is null)
        {
            _activeEffects.Add(incoming);
            return;
        }

        switch (incoming.Stacking)
        {
            case StackingRule.REFRESH_DURATION:
                existing.RefreshDuration(incoming.ExpiresAtMs - nowMs, nowMs);
                break;
            case StackingRule.STACK_INTENSITY:
            case StackingRule.STACK_BOTH:
                existing.AddStack(incoming.Magnitude, incoming.ExpiresAtMs - nowMs, nowMs);
                break;
        }
    }

    // Applies due DOT/HOT ticks and drops expired effects. onDotTick/onHotTick let the caller
    // (BattleEngine) log the resulting damage/healing as part of the action's event.
    public void TickEffects(int nowMs, Action<ActiveEffect, int> onDotTick, Action<ActiveEffect, int> onHotTick)
    {
        foreach (ActiveEffect effect in _activeEffects)
        {
            while (effect.HasTickDue(nowMs))
            {
                if (effect.EffectType == SkillEffectType.DOT)
                {
                    ApplyDamage(effect.Magnitude);
                    onDotTick(effect, effect.Magnitude);
                }
                else if (effect.EffectType == SkillEffectType.HEAL)
                {
                    Heal(effect.Magnitude);
                    onHotTick(effect, effect.Magnitude);
                }
                effect.AdvanceTick();
            }
        }
        _activeEffects.RemoveAll(e => e.IsExpired(nowMs));
    }
}
