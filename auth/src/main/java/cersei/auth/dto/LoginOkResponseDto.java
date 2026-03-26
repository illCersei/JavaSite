package cersei.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginOkResponseDto {
    private String message;
    private String username;
    private String email;
    private String token;
    private String refreshToken;
}