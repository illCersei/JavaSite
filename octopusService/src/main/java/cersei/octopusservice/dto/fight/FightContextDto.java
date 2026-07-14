package cersei.octopusservice.dto.fight;

import cersei.octopusservice.model.utils.FightSource;

import java.util.UUID;

public record FightContextDto(
        FightSource source,
        UUID userId,
        UUID dungeonRunId,
        Long dungeonRoomId
) {
}