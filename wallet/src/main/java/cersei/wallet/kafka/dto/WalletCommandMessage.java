package cersei.wallet.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WalletCommandMessage(
        @JsonProperty("command") String command,
        @JsonProperty("userId") String userId,
        @JsonProperty("amountMinor") long amountMinor,
        @JsonProperty("referenceType") String referenceType,
        @JsonProperty("referenceId") String referenceId,
        @JsonProperty("entryType") String entryType,
        @JsonProperty("metadataJson") String metadataJson,
        @JsonProperty("correlationId") String correlationId) {}
