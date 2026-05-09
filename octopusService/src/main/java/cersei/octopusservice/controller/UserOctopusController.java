package cersei.octopusservice.controller;

import cersei.octopusservice.dto.UserOctopusDto;
import cersei.octopusservice.service.UserOctopusService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/collection")
@RequiredArgsConstructor
public class UserOctopusController {
    private final UserOctopusService userOctopusService;

    @GetMapping("/get")
    List<UserOctopusDto> getUserOctopuses(@AuthenticationPrincipal Jwt jwt){
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userOctopusService.getUserOctopuses(userId);
    }
}
