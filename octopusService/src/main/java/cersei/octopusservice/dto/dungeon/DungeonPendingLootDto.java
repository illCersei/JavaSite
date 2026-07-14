package cersei.octopusservice.dto.dungeon;

public record DungeonPendingLootDto(
        Long id,
        Integer itemId,
        int quantity,
        long coinsMinor
) {
}