package cersei.profile.controller;

import cersei.profile.dto.UserProfileChangeDto;
import cersei.profile.dto.UserProfileCreateDto;
import cersei.profile.dto.UserProfileDto;
import cersei.profile.service.UserProfileImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileImpl userProfileService;

    // TODO: Нужно сделать пабликПрофильДто + оно 401 всегда, подумать как это реализовать
    @GetMapping("/public/get/{userId}")
    UserProfileDto getPublicProfile(@PathVariable UUID userId){
        return userProfileService.getProfile(userId);
    }

    @GetMapping("/private/get/me")
    UserProfileDto getProfile(@AuthenticationPrincipal Jwt jwt){
        UUID uuid = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userProfileService.getProfile(uuid);
    }

    @PatchMapping("/private/update/me")
    UserProfileDto changeProfile(@AuthenticationPrincipal Jwt jwt,
                                 @RequestBody UserProfileChangeDto dto){
        UUID uuid = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userProfileService.changeProfile(uuid, dto);
    }

    @PostMapping("/private/create/me")
    UserProfileDto createProfile(@AuthenticationPrincipal Jwt jwt,
                                 @RequestBody UserProfileCreateDto dto){
        UUID uuid = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userProfileService.createProfile(uuid, dto);
    }

    // TODO: Доработать она сейчас ничего даже не выкидывает
    @DeleteMapping("/private/delete/me")
    void deleteProfile(@AuthenticationPrincipal Jwt jwt){
        UUID uuid = UUID.fromString(jwt.getClaimAsString("uuid"));
        userProfileService.deleteProfile(uuid);
    }
}
