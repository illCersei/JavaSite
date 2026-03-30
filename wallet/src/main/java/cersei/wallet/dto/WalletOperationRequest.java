package cersei.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WalletOperationRequest(
        @NotNull @Positive Long amountMinor,
        @NotBlank String referenceType,
        @NotBlank String referenceId,
        @NotBlank String entryType,
        String metadataJson,
        String correlationId) {}
