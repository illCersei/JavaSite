package cersei.octopusservice.controller;

import cersei.octopusservice.config.OctopusSecurityProperties;
import cersei.octopusservice.dto.LootGrantRequest;
import cersei.octopusservice.dto.LootGrantResponse;
import cersei.octopusservice.dto.LootRollResponse;
import cersei.octopusservice.exception.ForbiddenActionException;
import cersei.octopusservice.service.LootService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.UUID;

/**
 * Свободная выдача лута/коинов вне контекста реального игрового события
 * (данжа/боя) - не завязана ни на какую стоимость, поэтому доступна только
 * привилегированным ролям (GM/поддержка), а не рядовым игрокам.
 */
@RestController
@RequestMapping("/loot")
@RequiredArgsConstructor
@Tag(name = "Loot", description = "Случайный и фиксированный лут: предметы + коины (GM-инструмент)")
public class LootController {

    private final LootService lootService;
    private final OctopusSecurityProperties octopusSecurityProperties;

    @PostMapping("/roll")
    public LootRollResponse roll(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        requirePrivilegedRole(jwt);
        requireIdempotencyKey(idempotencyKey);
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return lootService.rollRandom(jwt.getTokenValue(), userId, idempotencyKey);
    }

    @PostMapping("/grant")
    public LootGrantResponse grant(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody LootGrantRequest request
    ) {
        requirePrivilegedRole(jwt);
        requireIdempotencyKey(idempotencyKey);
        if (request.quantity() <= 0) {
            throw new IllegalArgumentException("quantity должен быть больше 0");
        }
        long coinsMinor = request.coinsMinor() != null ? request.coinsMinor() : 0L;
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return lootService.grantReward(
                jwt.getTokenValue(),
                userId,
                idempotencyKey,
                request.itemId(),
                request.quantity(),
                coinsMinor
        );
    }

    private void requirePrivilegedRole(Jwt jwt) {
        String role = jwt.getClaimAsString("role");
        if (role == null || !octopusSecurityProperties.privilegedRolesSet().contains(role.toUpperCase(Locale.ROOT))) {
            throw new ForbiddenActionException(
                    "Free-form loot roll/grant is restricted to privileged roles. "
                            + "Player-facing rewards come from dungeon/fight completion.");
        }
    }

    private static void requireIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }
    }
}
