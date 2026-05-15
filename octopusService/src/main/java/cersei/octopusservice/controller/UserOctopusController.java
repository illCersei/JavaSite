package cersei.octopusservice.controller;

import cersei.octopusservice.dto.UserOctopusDto;
import cersei.octopusservice.service.UserOctopusService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
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
}
