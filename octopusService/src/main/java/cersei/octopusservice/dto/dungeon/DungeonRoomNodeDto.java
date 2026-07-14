package cersei.octopusservice.dto.dungeon;

import cersei.octopusservice.model.utils.DungeonRoomStatus;
import cersei.octopusservice.model.utils.DungeonRoomType;

import java.util.List;

public record DungeonRoomNodeDto(
        Long id,
        int layerIndex,
        int slotIndex,
        DungeonRoomType roomType,
        DungeonRoomStatus roomStatus,
        String enemyTemplateId,
        Integer lootItemId,
        int lootQuantity,
        long lootCoinsMinor,
        List<Long> linkedRoomIds
) {
}