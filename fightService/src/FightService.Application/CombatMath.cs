using FightService.Domain.Enums;
using FightService.Domain.Enums.Units;
using FightService.Domain.ValueObjects;

namespace FightService.Application;

// Formulas from docs/OCTOPUS_MINIGAME_PLAN.md §16.5. Caps (crit/evasion/speed) are already
// enforced by Stats itself (§16.7); everything here works with already-capped numbers.
// Thresholds without an exact figure in the doc (hit-chance floor, status-apply formula) are
// explicit, tunable defaults - not derived from a spec.
public static class CombatMath
{
    private const int MinHitChance = 5;
    private const int MaxHitChance = 100;
    private const int MinStatusChance = 10;
    private const int MaxStatusChance = 95;
    private const int BaseStatusChance = 50;

    public static bool RollHit(Stats attacker, Stats defender, Random rng)
    {
        int hitChance = Math.Clamp(100 + attacker.Accuracy - defender.Evasion, MinHitChance, MaxHitChance);
        return rng.Next(1, 101) <= hitChance;
    }

    public static bool RollCrit(Stats attacker, Random rng) => rng.Next(1, 101) <= attacker.CritChance;

    public static bool RollStatusApply(Stats attacker, Stats defender, Random rng)
    {
        int chance = Math.Clamp(
            BaseStatusChance + (attacker.StatusPower - defender.Tenacity) / 2,
            MinStatusChance,
            MaxStatusChance);
        return rng.Next(1, 101) <= chance;
    }

    public static int RawEffectValue(Stats casterStats, Effect effect)
    {
        int scaling = effect.ScalingStat is { } stat ? casterStats.Get(stat) : 0;
        return effect.BaseValue + scaling * effect.ScalingRatioBps / 10_000;
    }

    public static bool IsPhysical(ElementType element) => element == ElementType.PHYSICAL;

    public static int ApplyMitigation(int rawAmount, ElementType element, Stats targetStats)
    {
        int mitigationStat = IsPhysical(element) ? targetStats.Armor : targetStats.MagicResist;
        return rawAmount * 100 / (100 + mitigationStat);
    }

    // Resolves a DAMAGE effect end to end: raw value, mitigation, crit. Returns the final
    // damage amount and whether it crit.
    public static (int Damage, bool Crit) ResolveDamage(Stats attacker, Stats target, Effect effect, Random rng)
    {
        int raw = RawEffectValue(attacker, effect);
        int mitigated = ApplyMitigation(raw, effect.ElementType, target);
        bool crit = RollCrit(attacker, rng);
        int finalDamage = crit ? mitigated * attacker.CritDamage / 100 : mitigated;
        return (Math.Max(0, finalDamage), crit);
    }

    // Resolves a HEAL/SHIELD effect - no mitigation, no crit, just caster-stat scaling.
    public static int ResolveHeal(Stats caster, Effect effect) => Math.Max(0, RawEffectValue(caster, effect));
}
