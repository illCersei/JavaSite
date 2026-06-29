package cersei.octopusservice.controller;

import cersei.octopusservice.dto.BattleTeamDto;
import cersei.octopusservice.dto.CombatTeamSnapshotDto;
import cersei.octopusservice.dto.SaveBattleTeamRequest;
import cersei.octopusservice.service.UserBattleTeamService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
@Tag(name = "Battle team", description = "Боевая команда из 3 осьминогов")
public class BattleTeamController {

    private final UserBattleTeamService userBattleTeamService;

    @GetMapping
    public BattleTeamDto getTeam(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userBattleTeamService.getTeam(userId);
    }

    @PutMapping
    public BattleTeamDto saveTeam(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SaveBattleTeamRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userBattleTeamService.saveTeam(userId, request.userOctopusIds());
    }

    @GetMapping("/combat-snapshots")
    public CombatTeamSnapshotDto getCombatSnapshots(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userBattleTeamService.getTeamCombatSnapshots(userId);
    }
}
