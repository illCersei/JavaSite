package cersei.octopusservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EquipItemRequest(
        @JsonProperty("userOctopusId") int userOctopusId,
        @JsonProperty("itemId") int itemId
) {
    public EquipItemRequest {
        if (userOctopusId <= 0) {
            throw new IllegalArgumentException("userOctopusId должен быть больше 0");
        }
        if (itemId <= 0) {
            throw new IllegalArgumentException(
                    "itemId должен быть больше 0. Проверь JSON: поле \"itemId\" без пробелов в имени"
            );
        }
    }
}
