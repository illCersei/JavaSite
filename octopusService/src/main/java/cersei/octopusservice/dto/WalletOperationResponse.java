package cersei.octopusservice.dto;

import java.util.UUID;

public record WalletOperationResponse(
        UUID ledgerEntryId,
        Long balanceMinorAfter,
        boolean idempotentReplay
) {
}
