package cersei.octopusservice.dto;

public record GachaSpinResponse(
        String spinId,
        OctopusSummaryDto octopus,
        long balanceMinorAfter,
        boolean walletIdempotentReplay
) {
}
