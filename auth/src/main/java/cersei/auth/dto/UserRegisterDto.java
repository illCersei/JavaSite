package cersei.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDto {
    @NotBlank(message = "Логин обязатален")
    private String username;

    @NotBlank(message = "Пароль обязатален")
    @Size(min = 8, message = "Пароль должен состоянть минимум из 8 символов")
    @Size(max = 32, message = "Пароль должен состоять максимум из 32 символов")
    private String password;

    @Email(message = "Email неккоректрый")
    @NotBlank(message = "Email обязателен")
    private String email;
}
