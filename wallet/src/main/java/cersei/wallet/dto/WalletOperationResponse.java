package cersei.wallet.dto;

import java.util.UUID;

public record WalletOperationResponse(
        UUID ledgerEntryId, long balanceMinorAfter, boolean idempotentReplay) {}
