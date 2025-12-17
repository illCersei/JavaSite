package cersei.newyearservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PersonRequest {
    @NotBlank(message = "Имя обязательно")
    private String name;
}

