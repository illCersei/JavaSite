package cersei.auth.dto;

import lombok.Data;

@Data
public class LoginOkResponseDto {
    private String message;
    private String token;

    public LoginOkResponseDto(String message, String token) {
        this.message = message;
        this.token = token;
    }
}