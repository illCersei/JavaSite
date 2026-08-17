using FightService.Domain.Enums;
using FightService.Domain.Enums.Units;

namespace FightService.Domain.Entities;

// A resolved instance of a skill's DOT/HOT/BUFF/DEBUFF/SHIELD effect, ticking against the
// battle clock. Magnitude is computed once at application time (caster stats at cast time),
// not recomputed per tick.
public sealed class ActiveEffect
{
    public string SourceSkillName { get; }
    public SkillEffectType EffectType { get; }
    public ElementType ElementType { get; }
    public ScalingStat? AffectedStat { get; }
    public int Magnitude { get; private set; }
    public int TickMs { get; }
    public StackingRule Stacking { get; }
    public int Stacks { get; private set; }
    public int ExpiresAtMs { get; private set; }
    public int NextTickAtMs { get; private set; }

    private ActiveEffect(
        string sourceSkillName,
        SkillEffectType effectType,
        ElementType elementType,
        ScalingStat? affectedStat,
        int magnitude,
        int tickMs,
        StackingRule stacking,
        int stacks,
        int expiresAtMs,
        int nextTickAtMs)
    {
        SourceSkillName = sourceSkillName;
        EffectType = effectType;
        ElementType = elementType;
        AffectedStat = affectedStat;
        Magnitude = magnitude;
        TickMs = tickMs;
        Stacking = stacking;
        Stacks = stacks;
        ExpiresAtMs = expiresAtMs;
        NextTickAtMs = nextTickAtMs;
    }

    // Fresh effect application, e.g. from a skill cast this turn.
    public static ActiveEffect Create(
        string sourceSkillName,
        SkillEffectType effectType,
        ElementType elementType,
        ScalingStat? affectedStat,
        int magnitude,
        int durationMs,
        int tickMs,
        StackingRule stacking,
        int nowMs)
    {
        int resolvedTickMs = tickMs > 0 ? tickMs : durationMs;
        return new ActiveEffect(
            sourceSkillName, effectType, elementType, affectedStat, magnitude, resolvedTickMs,
            stacking, stacks: 1, expiresAtMs: nowMs + durationMs, nextTickAtMs: nowMs + resolvedTickMs);
    }

    // Rehydration from a persisted snapshot - restores exact absolute clock values instead of
    // recomputing them from "now".
    public static ActiveEffect Restore(
        string sourceSkillName,
        SkillEffectType effectType,
        ElementType elementType,
        ScalingStat? affectedStat,
        int magnitude,
        int tickMs,
        StackingRule stacking,
        int stacks,
        int expiresAtMs,
        int nextTickAtMs) =>
        new(sourceSkillName, effectType, elementType, affectedStat, magnitude, tickMs, stacking, stacks, expiresAtMs, nextTickAtMs);

    public bool IsExpired(int nowMs) => nowMs >= ExpiresAtMs;

    public bool HasTickDue(int nowMs) => TickMs > 0 && nowMs >= NextTickAtMs && !IsExpired(nowMs);

    public void AdvanceTick() => NextTickAtMs += TickMs;

    public void RefreshDuration(int durationMs, int nowMs) => ExpiresAtMs = nowMs + durationMs;

    public void AddStack(int magnitude, int durationMs, int nowMs)
    {
        Stacks++;
        Magnitude += magnitude;
        RefreshDuration(durationMs, nowMs);
    }
}
