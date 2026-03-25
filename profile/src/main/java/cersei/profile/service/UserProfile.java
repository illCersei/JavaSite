package cersei.profile.service;

import cersei.profile.dto.UserProfileChangeDto;
import cersei.profile.dto.UserProfileCreateDto;
import cersei.profile.dto.UserProfileDto;

import java.util.UUID;

public interface UserProfile {
    UserProfileDto getProfile(UUID userId);

    UserProfileDto changeProfile(UUID userId, UserProfileChangeDto dto);

    UserProfileDto createProfile(UUID userId, UserProfileCreateDto dto);

    //boolean profileExists(UUID userId);

    void deleteProfile(UUID userId);

    void createEmptyProfile(UUID userId);
}
