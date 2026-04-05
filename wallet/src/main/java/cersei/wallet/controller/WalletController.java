package cersei.wallet.controller;


import cersei.wallet.config.WalletSecurityProperties;
import cersei.wallet.dto.WalletBalanceResponse;
import cersei.wallet.dto.WalletOperationRequest;
import cersei.wallet.dto.WalletOperationResponse;
import cersei.wallet.exception.WalletAccessDeniedException;
import cersei.wallet.service.WalletService;
import cersei.wallet.service.utils.WalletBalanceView;
import cersei.wallet.service.utils.WalletOperationResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/private")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletSecurityProperties walletSecurityProperties;

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
        requireArbitraryWalletMutationAllowed(jwt);
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
        requireArbitraryWalletMutationAllowed(jwt);
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

    private void requireArbitraryWalletMutationAllowed(Jwt jwt) {
        if (walletSecurityProperties.isAllowArbitraryCreditDebit()) {
            return;
        }
        String role = jwt.getClaimAsString("role");
        if (role != null
                && walletSecurityProperties
                        .privilegedRolesSet()
                        .contains(role.toUpperCase(Locale.ROOT))) {
            return;
        }
        throw new WalletAccessDeniedException(
                "Direct credit/debit is disabled. Use POST /private/me/rewards/claim or game flows (e.g. gacha).");
    }
}
