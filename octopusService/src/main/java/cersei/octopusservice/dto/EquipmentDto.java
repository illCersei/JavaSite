package cersei.octopusservice.dto;

import cersei.octopusservice.model.utils.ItemSlot;

public record EquipmentDto(
        Integer id,
        ItemSlot slot,
        ItemDto item
) {
}