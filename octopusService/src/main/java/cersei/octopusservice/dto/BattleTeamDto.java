package cersei.octopusservice.dto;

import java.util.List;

public record BattleTeamDto(
        List<BattleTeamSlotDto> slots
) {
}
