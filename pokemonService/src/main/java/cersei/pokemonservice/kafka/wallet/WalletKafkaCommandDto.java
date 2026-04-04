package cersei.pokemonservice.kafka.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WalletKafkaCommandDto(
        @JsonProperty("command") String command,
        @JsonProperty("userId") String userId,
        @JsonProperty("amountMinor") long amountMinor,
        @JsonProperty("referenceType") String referenceType,
        @JsonProperty("referenceId") String referenceId,
        @JsonProperty("entryType") String entryType,
        @JsonProperty("metadataJson") String metadataJson,
        @JsonProperty("correlationId") String correlationId,
        @JsonProperty("replyInstanceId") String replyInstanceId,
        @JsonProperty("accessToken") String accessToken) {}
