package cersei.auth.dto;

import lombok.Data;

@Data
public class RegisterBadDto {
    String message = "Пользователь с таким логином существует";
}
