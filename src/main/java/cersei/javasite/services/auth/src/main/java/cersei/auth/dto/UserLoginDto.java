package cersei.auth.dto;

import lombok.Data;

@Data
public class UserLoginDto {
    private String username;
    private String password;
}
