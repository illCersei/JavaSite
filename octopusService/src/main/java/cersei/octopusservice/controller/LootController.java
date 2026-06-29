package cersei.octopusservice.controller;

import cersei.octopusservice.dto.LootGrantRequest;
import cersei.octopusservice.dto.LootGrantResponse;
import cersei.octopusservice.dto.LootRollResponse;
import cersei.octopusservice.service.LootService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/loot")
@RequiredArgsConstructor
@Tag(name = "Loot", description = "Случайный и фиксированный лут: предметы + коины")
public class LootController {

    private final LootService lootService;

    @PostMapping("/roll")
    public LootRollResponse roll(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
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

    private static void requireIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }
    }
}
