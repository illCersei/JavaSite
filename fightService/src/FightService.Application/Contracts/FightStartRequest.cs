namespace FightService.Application.Contracts;

// Mirrors cersei.octopusservice.dto.fight.FightCombatantDto.
public sealed record FightCombatantDto(
    string CombatantId,
    string TemplateId,
    string Name,
    CombatStatsDto Stats,
    IReadOnlyList<SkillDto> Skills);

// Mirrors cersei.octopusservice.dto.fight.FightSquadDto.
public sealed record FightSquadDto(IReadOnlyList<FightCombatantDto> Fighters);

// Mirrors cersei.octopusservice.dto.fight.FightContextDto. Source/dungeonRunId/dungeonRoomId
// stay nullable/string-typed the same way the Java record allows them to be.
public sealed record FightContextDto(
    string Source,
    Guid UserId,
    Guid? DungeonRunId,
    long? DungeonRoomId);

// Mirrors cersei.octopusservice.dto.fight.FightRulesDto. autoPlayEnemy is accepted for wire
// compatibility but not branched on - every non-player combatant is always AI-controlled.
public sealed record FightRulesDto(int MaxTurns, bool AutoPlayEnemy);

// Mirrors cersei.octopusservice.dto.fight.FightStartRequest - shape is unchanged from the
// original design, only the response type changes (FightStateDto instead of FightStartResponse).
public sealed record FightStartRequest(
    string BattleId,
    FightContextDto Context,
    long RngSeed,
    FightSquadDto PlayerSquad,
    FightSquadDto EnemySquad,
    FightRulesDto Rules);
