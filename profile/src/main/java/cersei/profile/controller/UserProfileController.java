package cersei.profile.controller;

import cersei.profile.dto.UserProfileChangeDto;
import cersei.profile.dto.UserProfileCreateDto;
import cersei.profile.dto.UserProfileDto;
import cersei.profile.service.UserProfileImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileImpl userProfileService;

    @GetMapping("/get/{userId}")
    UserProfileDto getProfile(@PathVariable UUID userId){
        return userProfileService.getProfile(userId);
    }

    @PatchMapping("/update/{userId}")
    UserProfileDto changeProfile(@RequestBody UserProfileChangeDto dto,
                                 @PathVariable UUID userId){
        return userProfileService.changeProfile(userId, dto);
    }

    @PostMapping("/create/{userId}")
    UserProfileDto createProfile(@RequestBody UserProfileCreateDto dto,
                                 @PathVariable UUID userId){
        return userProfileService.createProfile(userId, dto);
    }

    @DeleteMapping("/delete/{userId}")
    void deleteProfile(@PathVariable UUID userId){
        userProfileService.deleteProfile(userId);
    }
}
