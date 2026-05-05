package cersei.octopusservice.dto;

public record WalletOperationRequest(
        Long amountMinor,
        String referenceType,
        String referenceId,
        String entryType,
        String metadataJson,
        String correlationId
) {
}
