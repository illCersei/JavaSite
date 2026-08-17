package cersei.octopusservice.dto.fight;

import java.util.List;

/**
 * C# → Java: returned by /fight/start, /fight/{battleId}/action and /fight/{battleId}/state
 * alike. Replaces the old FightStartResponse/FightResultResponse pair now that fights are
 * action-based rather than a single autonomous simulation Java polled for a final result.
 * Java only calls /start and /state (the frontend calls /action directly against
 * fightServiceUrl), so this DTO is mainly consumed here to read finished/result off /state.
 */
public record FightStateDto(
        String battleId,
        String status,
        String currentActorId,
        int turnNumber,
        boolean finished,
        String result,
        List<FightCombatantStateDto> playerSquad,
        List<FightCombatantStateDto> enemySquad,
        List<BattleEventDto> events
) {
}
