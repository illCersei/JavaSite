package cersei.octopusservice.service.useritem;

import cersei.octopusservice.dto.ItemDto;
import cersei.octopusservice.model.Item;
import org.springframework.stereotype.Component;

@Component
public class ItemDtoMapper {

    public ItemDto toDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getSlot(),
                item.getTier(),
                item.getAttackStat(),
                item.getMagicPowerStat(),
                item.getArmorStat(),
                item.getMagicResistStat(),
                item.getSpeedStat()
        );
    }
}
