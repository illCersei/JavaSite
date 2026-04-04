package cersei.wallet.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WalletCommandResult(
        @JsonProperty("correlationId") String correlationId,
        @JsonProperty("replyInstanceId") String replyInstanceId,
        @JsonProperty("success") boolean success,
        @JsonProperty("balanceMinorAfter") Long balanceMinorAfter,
        @JsonProperty("errorCode") String errorCode,
        @JsonProperty("ledgerEntryId") String ledgerEntryId,
        @JsonProperty("idempotentReplay") boolean idempotentReplay) {}
