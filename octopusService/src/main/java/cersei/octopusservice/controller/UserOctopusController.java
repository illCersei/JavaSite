package cersei.octopusservice.controller;

import cersei.octopusservice.config.OctopusSecurityProperties;
import cersei.octopusservice.dto.CombatSnapshotDto;
import cersei.octopusservice.dto.SummonOctopusRequest;
import cersei.octopusservice.dto.UserOctopusAddedExpDto;
import cersei.octopusservice.dto.UserOctopusAddedStatsDto;
import cersei.octopusservice.dto.UserOctopusDto;
import cersei.octopusservice.exception.ForbiddenActionException;
import cersei.octopusservice.service.UserOctopusStashService;
import cersei.octopusservice.service.useroctopus.UserOctopusProgressionService;
import cersei.octopusservice.service.useroctopus.UserOctopusQueryService;
import cersei.octopusservice.utils.StatsForUpgrade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/octopuses")
@RequiredArgsConstructor
public class UserOctopusController {
    private final UserOctopusQueryService userOctopusQueryService;
    private final UserOctopusProgressionService userOctopusProgressionService;
    private final UserOctopusStashService userOctopusStashService;
    private final OctopusSecurityProperties octopusSecurityProperties;

    @PostMapping("/summon")
    UserOctopusDto summon(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SummonOctopusRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userOctopusStashService.summon(userId, request.octopusId(), idempotencyKey);
    }

    @GetMapping("/get/all")
    List<UserOctopusDto> getUserOctopuses(@AuthenticationPrincipal Jwt jwt){
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userOctopusQueryService.getAllUserOctopuses(userId);
    }

    @GetMapping("/get/{id}")
    UserOctopusDto getUserOctopusById(@AuthenticationPrincipal Jwt jwt,
                                          @PathVariable int id){
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userOctopusQueryService.getUserOctopusById(userId, id);
    }

    @GetMapping("/get/{id}/combat-snapshot")
    CombatSnapshotDto getCombatSnapshot(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable int id
    ) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userOctopusQueryService.getCombatSnapshot(userId, id);
    }

    // Not wired to any real game event (fight/dungeon completion) yet - a client-supplied
    // exp amount would let a player max out an octopus for free, so this is GM-only until
    // exp is granted server-side from an actual battle result.
    @PostMapping("{id}")
    UserOctopusAddedExpDto addExpToOctopus(@AuthenticationPrincipal Jwt jwt,
                                           @PathVariable int id,
                                           @RequestParam int exp){
        String role = jwt.getClaimAsString("role");
        if (role == null || !octopusSecurityProperties.privilegedRolesSet().contains(role.toUpperCase(Locale.ROOT))) {
            throw new ForbiddenActionException(
                    "Manually adding exp is restricted to privileged roles. Exp should come from battle outcomes.");
        }
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userOctopusProgressionService.addExpToOctopus(userId, id, exp);
    }

    @PostMapping("{id}/add/{stat}")
    UserOctopusAddedStatsDto addStatsToOctopus(@AuthenticationPrincipal Jwt jwt,
                                               @PathVariable int id,
                                               @PathVariable StatsForUpgrade stat){
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userOctopusProgressionService.addStatsToOctopus(userId, id, stat);
    }
}
