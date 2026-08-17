using FightService.Domain.Entities;
using FightService.Domain.Enums;
using FightService.Domain.ValueObjects;

namespace FightService.Application;

// Executes one resolved skill-use (actor + skill + targets) end to end: hit roll per target,
// then each of the skill's effects against that target, cooldown update, and a BattleEvent
// summarizing what happened. Used identically for player-submitted and AI-chosen actions.
public sealed class ActionResolver
{
    private readonly Random _rng;

    public ActionResolver(Random rng) => _rng = rng;

    public BattleEvent Resolve(Combatant actor, Skill skill, IReadOnlyList<Combatant> targets, int nowMs)
    {
        var damageDealt = new Dictionary<string, int>();
        var healingDone = new Dictionary<string, int>();
        bool anyCrit = false;
        bool anyHit = false;

        Stats attackerStats = actor.EffectiveStats();

        foreach (Combatant target in targets)
        {
            Stats targetStats = target.EffectiveStats();
            if (!CombatMath.RollHit(attackerStats, targetStats, _rng))
            {
                continue;
            }
            anyHit = true;

            foreach (Effect effect in skill.Effects)
            {
                ApplyEffect(skill, actor, attackerStats, target, targetStats, effect, nowMs, damageDealt, healingDone, ref anyCrit);
            }
        }

        actor.PutSkillOnCooldown(skill.Id, skill.CooldownMs, nowMs);

        return new BattleEvent(
            actor.Id.Value,
            skill.Id.Value,
            targets.Select(t => t.Id.Value).ToList(),
            Missed: !anyHit,
            Crit: anyCrit,
            damageDealt,
            healingDone);
    }

    private void ApplyEffect(
        Skill skill,
        Combatant actor,
        Stats attackerStats,
        Combatant target,
        Stats targetStats,
        Effect effect,
        int nowMs,
        Dictionary<string, int> damageDealt,
        Dictionary<string, int> healingDone,
        ref bool anyCrit)
    {
        switch (effect.EffectType)
        {
            case SkillEffectType.DAMAGE:
            {
                (int damage, bool crit) = CombatMath.ResolveDamage(attackerStats, targetStats, effect, _rng);
                target.ApplyDamage(damage);
                Accumulate(damageDealt, target.Id.Value, damage);
                anyCrit = anyCrit || crit;
                break;
            }
            case SkillEffectType.HEAL when effect.DurationMs is null:
            {
                int heal = CombatMath.ResolveHeal(attackerStats, effect);
                target.Heal(heal);
                Accumulate(healingDone, target.Id.Value, heal);
                break;
            }
            case SkillEffectType.HEAL:
            {
                int magnitude = CombatMath.ResolveHeal(attackerStats, effect);
                target.AddOrRefreshEffect(NewActiveEffect(skill, effect, magnitude, nowMs), nowMs);
                break;
            }
            case SkillEffectType.SHIELD:
            {
                int amount = CombatMath.ResolveHeal(attackerStats, effect);
                target.SetShield(amount);
                break;
            }
            case SkillEffectType.DOT:
            {
                if (!CombatMath.RollStatusApply(attackerStats, targetStats, _rng))
                {
                    break;
                }
                int raw = CombatMath.RawEffectValue(attackerStats, effect);
                int magnitude = CombatMath.ApplyMitigation(raw, effect.ElementType, targetStats);
                target.AddOrRefreshEffect(NewActiveEffect(skill, effect, magnitude, nowMs), nowMs);
                break;
            }
            case SkillEffectType.BUFF:
            {
                int magnitude = CombatMath.RawEffectValue(attackerStats, effect);
                target.AddOrRefreshEffect(NewActiveEffect(skill, effect, magnitude, nowMs), nowMs);
                break;
            }
            case SkillEffectType.DEBUFF:
            {
                if (!CombatMath.RollStatusApply(attackerStats, targetStats, _rng))
                {
                    break;
                }
                int magnitude = CombatMath.RawEffectValue(attackerStats, effect);
                target.AddOrRefreshEffect(NewActiveEffect(skill, effect, magnitude, nowMs), nowMs);
                break;
            }
        }
    }

    private static ActiveEffect NewActiveEffect(Skill skill, Effect effect, int magnitude, int nowMs) => ActiveEffect.Create(
        skill.Name,
        effect.EffectType,
        effect.ElementType,
        effect.ScalingStat,
        magnitude,
        effect.DurationMs ?? 0,
        effect.TickMs ?? effect.DurationMs ?? 0,
        effect.StackingRule ?? StackingRule.REFRESH_DURATION,
        nowMs);

    private static void Accumulate(Dictionary<string, int> map, string key, int amount) =>
        map[key] = map.GetValueOrDefault(key) + amount;
}
