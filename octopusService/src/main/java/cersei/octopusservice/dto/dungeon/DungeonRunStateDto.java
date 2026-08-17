package cersei.octopusservice.dto.dungeon;

import java.util.List;

public record DungeonRunStateDto(
        java.util.UUID runId,
        int templateId,
        String templateName,
        String status,
        Long currentRoomId,
        String currentFightId,
        List<DungeonRoomNodeDto> map,
        List<DungeonPendingLootDto> pendingLoot
) {
}