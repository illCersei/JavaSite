package cersei.octopusservice.dto.fight;

/**
 * Java → C#: старт боя. C# владеет всем turn-based циклом (ходы игрока и мобов).
 */
public record FightStartRequest(
        String battleId,
        FightContextDto context,
        long rngSeed,
        FightSquadDto playerSquad,
        FightSquadDto enemySquad,
        FightRulesDto rules
) {
}