package cersei.auth.dto;

import lombok.Data;

import java.util.Map;

@Data
public class LoginOkResponseDto {
    private String message;
    private String token;

    public LoginOkResponseDto(Map<String, String> map) {
        this.message = map.get("message");
        this.token = map.get("token");
    }
}
