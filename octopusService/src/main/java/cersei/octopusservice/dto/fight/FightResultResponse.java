package cersei.octopusservice.dto.fight;

import cersei.octopusservice.model.utils.BattleResult;

import java.util.List;

/**
 * C# → Java: итог боя (poll GET /fight/result/{battleId} или callback).
 * Java проверяет result перед выдачей лута.
 */
public record FightResultResponse(
        String battleId,
        BattleResult result,
        boolean finished,
        int turnsTaken,
        List<FightCombatantStateDto> playerSquadFinal,
        List<FightCombatantStateDto> enemySquadFinal
) {
}

