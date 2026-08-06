using FightService.Application.Contracts;
using FightService.Domain.Entities;
using FightService.Domain.Enums;
using FightService.Domain.Exceptions;
using FightService.Domain.ValueObjects;

namespace FightService.Application;

// ATB scheduler + battle loop. The same resolution loop backs both StartBattle and
// SubmitAction: advance the clock, let ready AI combatants act immediately, and stop the
// instant a player-controlled combatant becomes ready (or the battle ends). SubmitAction
// resolves that one player action first, then re-enters the same loop.
public sealed class BattleEngine
{
    private const int TickMs = 100;
    // Belt-and-suspenders guard: with speed>=1 always granted (see GainTurnMeter below) this
    // should never trip, but a stalled battle should end as a loss, not hang the request.
    private const int MaxTicksPerCall = 200_000;

    public BattleSession StartBattle(FightStartRequest request)
    {
        BattleSession session = BattleMapper.ToBattleSession(request);
        RunUntilPlayerActionOrFinished(session);
        return session;
    }

    public void SubmitAction(BattleSession session, string actorId, int skillId, IReadOnlyList<string> targetIds)
    {
        if (session.Status != BattleStatus.WAITING_PLAYER_ACTION)
        {
            throw new DomainException("Battle is not waiting for a player action");
        }
        if (session.CurrentActorId is null || session.CurrentActorId.Value != actorId)
        {
            throw new DomainException($"It is not combatant '{actorId}'s turn");
        }

        Combatant actor = session.Require(CombatantId.Create(actorId));
        Skill skill = actor.Skills.FirstOrDefault(s => s.Id.Value == skillId)
            ?? throw new DomainException($"Combatant '{actorId}' has no skill {skillId}");
        if (!actor.IsSkillReady(skill.Id, session.ClockMs))
        {
            throw new DomainException($"Skill {skillId} is on cooldown");
        }
        if (targetIds.Count == 0)
        {
            throw new DomainException("At least one target is required");
        }

        List<Combatant> targets = targetIds.Select(id => session.Require(CombatantId.Create(id))).ToList();

        var resolver = new ActionResolver(CreateRng(session));
        BattleEvent battleEvent = resolver.Resolve(actor, skill, targets, session.ClockMs);
        session.AppendEvent(battleEvent);
        session.IncrementTurn();

        if (CheckBattleEnd(session) || EnforceTurnLimit(session))
        {
            return;
        }

        RunUntilPlayerActionOrFinished(session);
    }

    private void RunUntilPlayerActionOrFinished(BattleSession session)
    {
        if (session.Status == BattleStatus.FINISHED)
        {
            return;
        }

        for (int ticks = 0; ticks < MaxTicksPerCall; ticks++)
        {
            if (CheckBattleEnd(session))
            {
                return;
            }

            session.AdvanceClock(TickMs);

            foreach (Combatant combatant in session.AllCombatants().Where(c => !c.IsDead))
            {
                combatant.GainTurnMeter(Math.Max(1, combatant.EffectiveStats().Speed));
                combatant.TickEffects(
                    session.ClockMs,
                    (_, dmg) => session.AppendEvent(TickEvent(combatant, damage: dmg)),
                    (_, heal) => session.AppendEvent(TickEvent(combatant, heal: heal)));
            }

            if (CheckBattleEnd(session))
            {
                return;
            }

            List<Combatant> ready = session.AllCombatants()
                .Where(c => c.IsReadyToAct())
                .OrderByDescending(c => c.EffectiveStats().Speed)
                .ToList();

            foreach (Combatant actor in ready)
            {
                if (actor.IsDead || !actor.IsReadyToAct())
                {
                    continue;
                }

                actor.ConsumeTurnMeter();

                if (actor.IsPlayerControlled)
                {
                    session.SetWaitingOn(actor.Id);
                    return;
                }

                ResolveAiTurn(session, actor);
                session.IncrementTurn();

                if (CheckBattleEnd(session) || EnforceTurnLimit(session))
                {
                    return;
                }
            }
        }

        // Exhausted the tick budget without anyone becoming ready (shouldn't happen given the
        // speed>=1 floor) - treat as a loss rather than hang the caller.
        session.Finish(BattleResult.LOSS);
    }

    private void ResolveAiTurn(BattleSession session, Combatant actor)
    {
        var rng = CreateRng(session);
        var decision = new AiPolicy(rng).ChooseAction(session, actor, session.ClockMs);
        if (decision is null)
        {
            session.AppendEvent(new BattleEvent(
                actor.Id.Value, null, Array.Empty<string>(), Missed: true, Crit: false,
                new Dictionary<string, int>(), new Dictionary<string, int>()));
            return;
        }

        var resolver = new ActionResolver(rng);
        BattleEvent battleEvent = resolver.Resolve(actor, decision.Value.Skill, decision.Value.Targets, session.ClockMs);
        session.AppendEvent(battleEvent);
    }

    private static bool CheckBattleEnd(BattleSession session)
    {
        if (session.Status == BattleStatus.FINISHED)
        {
            return true;
        }
        if (session.IsEnemyWipeout())
        {
            session.Finish(BattleResult.WIN);
            return true;
        }
        if (session.IsPlayerWipeout())
        {
            session.Finish(BattleResult.LOSS);
            return true;
        }
        return false;
    }

    // A timed-out battle (hit MaxTurns without a winner) resolves as a loss - an indefinite
    // stalemate shouldn't reward the player.
    private static bool EnforceTurnLimit(BattleSession session)
    {
        if (session.TurnNumber < session.MaxTurns)
        {
            return false;
        }
        session.Finish(BattleResult.LOSS);
        return true;
    }

    private static BattleEvent TickEvent(Combatant owner, int damage = 0, int heal = 0) => new(
        owner.Id.Value,
        null,
        new[] { owner.Id.Value },
        Missed: false,
        Crit: false,
        damage > 0 ? new Dictionary<string, int> { [owner.Id.Value] = damage } : new Dictionary<string, int>(),
        heal > 0 ? new Dictionary<string, int> { [owner.Id.Value] = heal } : new Dictionary<string, int>());

    private static Random CreateRng(BattleSession session) =>
        new(unchecked((int)(session.RngSeed ^ session.TurnNumber ^ session.ClockMs)));
}
