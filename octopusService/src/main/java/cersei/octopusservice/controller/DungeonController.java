package cersei.octopusservice.controller;

import cersei.octopusservice.dto.dungeon.*;
import cersei.octopusservice.service.dungeon.DungeonService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dungeon")
@RequiredArgsConstructor
@Tag(name = "Dungeon", description = "Данжи: забег, карта, бои через fight-сервис")
public class DungeonController {

    private final DungeonService dungeonService;

    @GetMapping("/templates")
    public List<DungeonTemplateDto> listTemplates() {
        return dungeonService.listTemplates();
    }

    @PostMapping("/runs")
    public DungeonRunStateDto startRun(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody StartDungeonRunRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return dungeonService.startRun(jwt.getTokenValue(), userId, request.templateId());
    }

    @GetMapping("/runs/{runId}")
    public DungeonRunStateDto getRun(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID runId
    ) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return dungeonService.getRun(userId, runId);
    }

    // Lets the frontend recover an in-progress run's id after a reload/new device
    // without needing to remember it client-side - startRun() 400s if one already exists.
    @GetMapping("/runs/active")
    public ResponseEntity<DungeonRunStateDto> getActiveRun(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return dungeonService.getActiveRun(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/runs/{runId}/enter-room")
    public DungeonRunStateDto enterRoom(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID runId,
            @RequestBody EnterDungeonRoomRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return dungeonService.enterRoom(userId, runId, request.roomId());
    }

        @PostMapping("/runs/{runId}/rooms/{roomId}/start-fight")
    public DungeonStartFightDto startFight(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID runId,
            @PathVariable long roomId
    ) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return dungeonService.startFight(jwt.getTokenValue(), userId, runId, roomId);
    }

    @PostMapping("/runs/{runId}/fights/{battleId}/complete")
    public DungeonRunStateDto completeFight(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID runId,
            @PathVariable String battleId
    ) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return dungeonService.completeFight(jwt.getTokenValue(), userId, runId, battleId);
    }

    @PostMapping("/runs/{runId}/extract")
    public DungeonRunStateDto extract(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID runId,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        requireIdempotencyKey(idempotencyKey);
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return dungeonService.extract(jwt.getTokenValue(), userId, runId, idempotencyKey);
    }

    private static void requireIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }
    }
}