package cersei.profile.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserProfileDto {
    private UUID userId;

    private String nickname;

    private String avatarUrl;

    private LocalDateTime createdAt;
}
