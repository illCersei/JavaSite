package cersei.wallet.service.utils;

import java.util.UUID;

public record WalletBalanceView(UUID walletId, UUID userId, long balanceMinor, String currency) {}
