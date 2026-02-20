package cersei.profile.service;

import cersei.profile.dto.UserProfileChangeDto;
import cersei.profile.dto.UserProfileDto;

import java.util.UUID;

public interface UserProfile {
    UserProfileDto getProfile(UUID userId);
    UserProfileDto changeProfile(UUID userId, UserProfileChangeDto dto);
}
