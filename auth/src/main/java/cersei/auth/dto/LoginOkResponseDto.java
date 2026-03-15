package cersei.auth.dto;

import lombok.Data;

@Data
public class LoginOkResponseDto {
    private String message;
    private String token;
    private String refreshToken;

    public LoginOkResponseDto(String message, String token, String refreshToken) {
        this.message = message;
        this.token = token;
        this.refreshToken = refreshToken;
    }
}