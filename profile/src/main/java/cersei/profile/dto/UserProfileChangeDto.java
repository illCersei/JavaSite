package cersei.profile.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileChangeDto {
    @Size(min = 3, max = 32)
    @Pattern(regexp = "^[A-Za-z0-9_]+$")
    private String nickname;

    @Pattern(regexp = "^https://.+$")
    private String avatarUrl;
}
