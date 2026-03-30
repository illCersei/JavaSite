package cersei.wallet.service.utils;

import lombok.Builder;

import java.util.UUID;

@Builder
public record WalletOperationResult(
        UUID ledgerEntryId,
        long balanceMinorAfter,
        boolean idempotentReplay) {}
