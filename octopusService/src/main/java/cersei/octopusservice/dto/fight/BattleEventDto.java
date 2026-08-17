package cersei.octopusservice.dto.fight;

import java.util.List;
import java.util.Map;

/**
 * C# → Java: один разрешённый ход (свой или авто-резолвленный ход ИИ между ходами игрока).
 */
public record BattleEventDto(
        String actorId,
        Integer skillId,
        List<String> targetIds,
        boolean missed,
        boolean crit,
        Map<String, Integer> damageDealt,
        Map<String, Integer> healingDone
) {
}
