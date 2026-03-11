package cersei.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Data
public class UserProfileDto {
    private UUID userId;

    private String nickname;

    private String avatarUrl;

    private LocalDateTime createdAt;
}
