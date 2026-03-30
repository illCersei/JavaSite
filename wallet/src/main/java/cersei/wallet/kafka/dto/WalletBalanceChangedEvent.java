package cersei.wallet.kafka.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WalletBalanceChangedEvent(
        @JsonProperty("userId") String userId,
        @JsonProperty("walletId") String walletId,
        @JsonProperty("currency") String currency,
        @JsonProperty("balanceMinor") long balanceMinor,
        @JsonProperty("entryType") String entryType,
        @JsonProperty("correlationId") String correlationId,
        @JsonProperty("occurredAt") String occurredAt) {}
