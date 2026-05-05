package cersei.wallet.controller;

import cersei.wallet.dto.WalletOperationRequest;
import cersei.wallet.dto.WalletOperationResponse;
import cersei.wallet.service.WalletService;
import cersei.wallet.service.utils.WalletOperationResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/private/me/game")
@RequiredArgsConstructor
public class GameWalletController {

    private final WalletService walletService;

    @PostMapping("/credits")
    public WalletOperationResponse credit(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody WalletOperationRequest body
    ) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        WalletOperationResult r = walletService.credit(
                userId,
                body.amountMinor(),
                body.referenceType(),
                body.referenceId(),
                body.entryType(),
                body.metadataJson(),
                body.correlationId());
        return new WalletOperationResponse(r.ledgerEntryId(), r.balanceMinorAfter(), r.idempotentReplay());
    }

    @PostMapping("/debits")
    public WalletOperationResponse debit(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody WalletOperationRequest body
    ) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        WalletOperationResult r = walletService.debit(
                userId,
                body.amountMinor(),
                body.referenceType(),
                body.referenceId(),
                body.entryType(),
                body.metadataJson(),
                body.correlationId());
        return new WalletOperationResponse(r.ledgerEntryId(), r.balanceMinorAfter(), r.idempotentReplay());
    }
}
