package cersei.wallet.controller;

import cersei.wallet.dto.PeriodicRewardClaimResponse;
import cersei.wallet.service.PeriodicRewardClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/private/me/rewards")
@RequiredArgsConstructor
public class RewardClaimController {

    private final PeriodicRewardClaimService periodicRewardClaimService;

    /**
     * Периодическая награда для фронта: сумма и интервал задаются только конфигом сервера.
     * Идемпотентность по периоду через ledger ({@code referenceType} + {@code referenceId}).
     */
    @PostMapping("/claim")
    public PeriodicRewardClaimResponse claim(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return periodicRewardClaimService.claim(userId);
    }
}
