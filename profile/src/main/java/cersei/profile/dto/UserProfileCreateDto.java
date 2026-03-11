package cersei.profile.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class UserProfileCreateDto {
    private UUID userId;

    @Size(min = 3, max = 32)
    @Pattern(regexp = "^[A-Za-z0-9_]+$")
    private String nickname;

    @Pattern(regexp = "^https://.+$")
    private String avatarUrl;
}
