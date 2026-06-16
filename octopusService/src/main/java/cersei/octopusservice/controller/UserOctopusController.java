package cersei.octopusservice.controller;

import cersei.octopusservice.dto.UserOctopusAddedExpDto;
import cersei.octopusservice.dto.UserOctopusAddedStatsDto;
import cersei.octopusservice.dto.UserOctopusDto;
import cersei.octopusservice.service.UserOctopusService;
import cersei.octopusservice.utils.StatsForUpgrade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/octopuses")
@RequiredArgsConstructor
public class UserOctopusController {
    private final UserOctopusService userOctopusService;

    @GetMapping("/get/all")
    List<UserOctopusDto> getUserOctopuses(@AuthenticationPrincipal Jwt jwt){
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userOctopusService.getAllUserOctopuses(userId);
    }

    @GetMapping("/get/{id}")
    UserOctopusDto getUserOctopusById(@AuthenticationPrincipal Jwt jwt,
                                          @PathVariable int id){
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userOctopusService.getUserOctopusById(userId, id);
    }

    @PostMapping("{id}")
    UserOctopusAddedExpDto addExpToOctopus(@AuthenticationPrincipal Jwt jwt,
                                           @PathVariable int id,
                                           @RequestParam int exp){
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userOctopusService.addExpToOctopus(userId, id, exp);
    }

    @PostMapping("{id}/add/{stat}")
    UserOctopusAddedStatsDto addStatsToOctopus(@AuthenticationPrincipal Jwt jwt,
                                               @PathVariable int id,
                                               @PathVariable StatsForUpgrade stat){
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userOctopusService.addStatsToOctopus(userId, id, stat);
    }
}
