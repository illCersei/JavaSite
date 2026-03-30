package cersei.wallet.dto;

import java.util.UUID;

public record WalletBalanceResponse(UUID walletId, UUID userId, long balanceMinor, String currency) {}
