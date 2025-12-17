package cersei.newyearservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GiftRequest {
    @NotBlank(message = "Описание подарка обязательно")
    private String description;
    
    private String link;
}

