package cersei.octopusservice.dto.fight;

import java.util.List;

public record FightSquadDto(
        List<FightCombatantDto> fighters
) {
}