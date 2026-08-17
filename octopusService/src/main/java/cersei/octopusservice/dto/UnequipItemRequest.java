package cersei.octopusservice.dto;

import cersei.octopusservice.model.utils.ItemSlot;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UnequipItemRequest(
        @JsonProperty("userOctopusId") int userOctopusId,
        @JsonProperty("slot") ItemSlot slot
) {
    public UnequipItemRequest {
        if (userOctopusId <= 0) {
            throw new IllegalArgumentException("userOctopusId должен быть больше 0");
        }
        if (slot == null) {
            throw new IllegalArgumentException("slot обязателен");
        }
    }
}
