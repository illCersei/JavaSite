package cersei.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegisterDto {
    @NotBlank(message = "Логин обязатален")
    private String username;

    @NotBlank(message = "Пароль обязатален")
    private String password;

    @Email(message = "Email неккоректрый")
    @NotBlank(message = "Email обязателен")
    private String email;
}
