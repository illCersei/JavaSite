using FightService.Domain.Enums;
using FightService.Domain.Exceptions;
using FightService.Domain.ValueObjects;

namespace FightService.Domain.Entities;

public sealed class BattleSession
{
    public BattleId Id { get; }
    public Guid UserId { get; }
    public FightSource Source { get; }
    public Guid? DungeonRunId { get; }
    public long? DungeonRoomId { get; }
    public long RngSeed { get; }
    public int MaxTurns { get; }
    public BattleStatus Status { get; private set; }
    public int ClockMs { get; private set; }
    public IReadOnlyList<Combatant> PlayerSquad { get; }
    public IReadOnlyList<Combatant> EnemySquad { get; }
    public CombatantId? CurrentActorId { get; private set; }
    public BattleResult? Result { get; private set; }
    public int TurnNumber { get; private set; }
    public List<BattleEvent> EventLog { get; } = new();

    public BattleSession(
        BattleId id,
        Guid userId,
        FightSource source,
        Guid? dungeonRunId,
        long? dungeonRoomId,
        long rngSeed,
        int maxTurns,
        IReadOnlyList<Combatant> playerSquad,
        IReadOnlyList<Combatant> enemySquad)
    {
        Id = id;
        UserId = userId;
        Source = source;
        DungeonRunId = dungeonRunId;
        DungeonRoomId = dungeonRoomId;
        RngSeed = rngSeed;
        MaxTurns = maxTurns;
        PlayerSquad = playerSquad;
        EnemySquad = enemySquad;
        Status = BattleStatus.WAITING_PLAYER_ACTION;
    }

    // Rehydration from a persisted snapshot.
    public static BattleSession Restore(
        BattleId id,
        Guid userId,
        FightSource source,
        Guid? dungeonRunId,
        long? dungeonRoomId,
        long rngSeed,
        int maxTurns,
        IReadOnlyList<Combatant> playerSquad,
        IReadOnlyList<Combatant> enemySquad,
        BattleStatus status,
        int clockMs,
        CombatantId? currentActorId,
        BattleResult? result,
        int turnNumber,
        IEnumerable<BattleEvent> eventLog)
    {
        var session = new BattleSession(id, userId, source, dungeonRunId, dungeonRoomId, rngSeed, maxTurns, playerSquad, enemySquad)
        {
            Status = status,
            ClockMs = clockMs,
            CurrentActorId = currentActorId,
            Result = result,
            TurnNumber = turnNumber
        };
        session.EventLog.AddRange(eventLog);
        return session;
    }

    public IEnumerable<Combatant> AllCombatants() => PlayerSquad.Concat(EnemySquad);

    public IReadOnlyList<Combatant> AlliesOf(Combatant combatant) =>
        PlayerSquad.Contains(combatant) ? PlayerSquad : EnemySquad;

    public IReadOnlyList<Combatant> OpponentsOf(Combatant combatant) =>
        PlayerSquad.Contains(combatant) ? EnemySquad : PlayerSquad;

    public Combatant Require(CombatantId id) =>
        AllCombatants().FirstOrDefault(c => c.Id == id)
        ?? throw new DomainException($"Unknown combatant: {id}");

    public bool IsPlayerWipeout() => PlayerSquad.All(c => c.IsDead);

    public bool IsEnemyWipeout() => EnemySquad.All(c => c.IsDead);

    public void AdvanceClock(int deltaMs) => ClockMs += deltaMs;

    public void SetWaitingOn(CombatantId combatantId)
    {
        Status = BattleStatus.WAITING_PLAYER_ACTION;
        CurrentActorId = combatantId;
    }

    public void Finish(BattleResult result)
    {
        Status = BattleStatus.FINISHED;
        Result = result;
        CurrentActorId = null;
    }

    public void IncrementTurn() => TurnNumber++;

    public void AppendEvent(BattleEvent battleEvent) => EventLog.Add(battleEvent);
}
