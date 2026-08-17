package cersei.wallet.controller;

import cersei.wallet.config.WalletSecurityProperties;
import cersei.wallet.dto.WalletOperationRequest;
import cersei.wallet.dto.WalletOperationResponse;
import cersei.wallet.exception.WalletAccessDeniedException;
import cersei.wallet.service.WalletService;
import cersei.wallet.service.utils.WalletOperationResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Кредит/дебет "от лица игры" (гача, лут, стоимость входа в данж и т.п.),
 * вызывается только octopusService. Т.к. вызывающий сервис форвардит JWT
 * самого игрока, одного JWT недостаточно, чтобы отличить легитимный вызов
 * от игрока, бьющего в этот путь напрямую — поэтому дополнительно требуется
 * общий межсервисный секрет в заголовке {@value #SERVICE_TOKEN_HEADER}.
 */
@RestController
@RequestMapping("/private/me/game")
@RequiredArgsConstructor
public class GameWalletController {

    private static final String SERVICE_TOKEN_HEADER = "X-Internal-Service-Token";

    private final WalletService walletService;
    private final WalletSecurityProperties walletSecurityProperties;

    @PostMapping("/credits")
    public WalletOperationResponse credit(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = SERVICE_TOKEN_HEADER, required = false) String serviceToken,
            @Valid @RequestBody WalletOperationRequest body
    ) {
        requireServiceToken(serviceToken);
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
            @RequestHeader(value = SERVICE_TOKEN_HEADER, required = false) String serviceToken,
            @Valid @RequestBody WalletOperationRequest body
    ) {
        requireServiceToken(serviceToken);
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

    private void requireServiceToken(String presentedToken) {
        String expected = walletSecurityProperties.getServiceToken();
        if (expected == null || expected.isBlank()) {
            throw new WalletAccessDeniedException(
                    "Game wallet endpoints are disabled: wallet.security.service-token is not configured.");
        }
        if (presentedToken == null || !presentedToken.equals(expected)) {
            throw new WalletAccessDeniedException("Invalid or missing " + SERVICE_TOKEN_HEADER + " header.");
        }
    }
}
