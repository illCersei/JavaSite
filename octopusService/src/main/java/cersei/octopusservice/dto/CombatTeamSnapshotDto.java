package cersei.octopusservice.dto;

import java.util.List;

public record CombatTeamSnapshotDto(
        List<CombatSnapshotDto> fighters
) {
}
