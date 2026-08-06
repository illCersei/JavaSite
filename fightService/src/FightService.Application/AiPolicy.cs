using FightService.Domain.Entities;
using FightService.Domain.Enums;

namespace FightService.Application;

// Simple rule-based mob AI (per plan): no metadata beyond what Java already sends in the
// skill/effect list. Priority order per ready enemy turn:
//   1. Heal/shield an ally (including self) below ~50% HP, if a ready skill can.
//   2. Apply a debuff/DOT to an enemy that doesn't already have one from this skill.
//   3. Otherwise, the hardest-hitting ready damage skill, focus-firing the lowest-HP target.
//   4. Nothing ready -> pass.
public sealed class AiPolicy
{
    private const int HealThresholdPercent = 50;
    private readonly Random _rng;

    public AiPolicy(Random rng) => _rng = rng;

    public (Skill Skill, IReadOnlyList<Combatant> Targets)? ChooseAction(BattleSession session, Combatant actor, int nowMs)
    {
        List<Combatant> allies = session.AlliesOf(actor).Where(c => !c.IsDead).ToList();
        List<Combatant> enemies = session.OpponentsOf(actor).Where(c => !c.IsDead).ToList();
        if (enemies.Count == 0)
        {
            return null;
        }

        List<Skill> readySkills = actor.Skills.Where(s => actor.IsSkillReady(s.Id, nowMs)).ToList();
        if (readySkills.Count == 0)
        {
            return null;
        }

        Skill? healSkill = readySkills.FirstOrDefault(
            s => s.Effects.Any(e => e.EffectType is SkillEffectType.HEAL or SkillEffectType.SHIELD));
        if (healSkill is not null)
        {
            Combatant? hurtAlly = allies
                .Where(a => a.CurrentHp * 100 / Math.Max(1, a.MaxHp) < HealThresholdPercent)
                .OrderBy(a => a.CurrentHp)
                .FirstOrDefault();
            if (hurtAlly is not null)
            {
                return (healSkill, new[] { hurtAlly });
            }
        }

        foreach (Skill skill in readySkills.Where(
                     s => s.Effects.Any(e => e.EffectType is SkillEffectType.DEBUFF or SkillEffectType.DOT)))
        {
            Combatant? target = enemies
                .Where(e => !HasEffectFrom(e, skill))
                .OrderBy(e => e.CurrentHp)
                .FirstOrDefault();
            if (target is not null)
            {
                return (skill, new[] { target });
            }
        }

        Skill? damageSkill = readySkills
            .Where(s => s.Effects.Any(e => e.EffectType == SkillEffectType.DAMAGE))
            .OrderByDescending(s => s.Effects
                .Where(e => e.EffectType == SkillEffectType.DAMAGE)
                .Max(e => e.ScalingRatioBps))
            .FirstOrDefault();
        if (damageSkill is not null)
        {
            Combatant focusTarget = enemies.OrderBy(e => e.CurrentHp).ThenBy(_ => _rng.Next()).First();
            return (damageSkill, new[] { focusTarget });
        }

        return null;
    }

    private static bool HasEffectFrom(Combatant target, Skill skill) =>
        target.ActiveEffects.Any(e => e.SourceSkillName == skill.Name);
}
