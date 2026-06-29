package cersei.octopusservice.dto;

public record LootGrantResponse(
        String grantId,
        ItemDto item,
        int quantityAfter,
        long coinsMinor,
        Long balanceMinorAfter,
        boolean walletIdempotentReplay
) {
}
