package cersei.wallet.controller;


import cersei.wallet.dto.WalletBalanceResponse;
import cersei.wallet.dto.WalletOperationRequest;
import cersei.wallet.dto.WalletOperationResponse;
import cersei.wallet.service.WalletService;
import cersei.wallet.service.utils.WalletBalanceView;
import cersei.wallet.service.utils.WalletOperationResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/private")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/me")
    public WalletBalanceResponse me(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        WalletBalanceView v = walletService.getBalance(userId);
        return new WalletBalanceResponse(v.walletId(), v.userId(), v.balanceMinor(), v.currency());
    }

    @PostMapping("/me/credits")
    public WalletOperationResponse credit(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody WalletOperationRequest body) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        WalletOperationResult r =
                walletService.credit(
                        userId,
                        body.amountMinor(),
                        body.referenceType(),
                        body.referenceId(),
                        body.entryType(),
                        body.metadataJson(),
                        body.correlationId());
        return new WalletOperationResponse(r.ledgerEntryId(), r.balanceMinorAfter(), r.idempotentReplay());
    }

    @PostMapping("/me/debits")
    public WalletOperationResponse debit(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody WalletOperationRequest body) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        WalletOperationResult r =
                walletService.debit(
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
