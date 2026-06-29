package cersei.octopusservice.dto;

public record LootRollResponse(
        String grantId,
        ItemDto item,
        int quantityAfter,
        int rolledTier,
        long coinsMinor,
        Long balanceMinorAfter,
        boolean walletIdempotentReplay
) {
}
