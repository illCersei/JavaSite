package cersei.octopusservice.dto;

public record LootGrantRequest(
        int itemId,
        int quantity,
        Long coinsMinor
) {
}
