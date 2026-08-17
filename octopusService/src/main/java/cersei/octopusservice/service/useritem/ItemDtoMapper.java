package cersei.octopusservice.service.useritem;

import cersei.octopusservice.dto.ItemDto;
import cersei.octopusservice.model.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ItemDtoMapper {

    @Value("${octopus.item-icon-url-template}")
    private String itemIconUrlTemplate;

    public ItemDto toDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                itemIconUrlTemplate.formatted(item.getId()),
                item.getSlot(),
                item.getTier(),
                item.getAttackStat(),
                item.getMagicPowerStat(),
                item.getArmorStat(),
                item.getMagicResistStat(),
                item.getSpeedStat(),
                item.getCritChance(),
                item.getCritDamage(),
                item.getAccuracy(),
                item.getEvasion(),
                item.getTenacity(),
                item.getStatusPower()
        );
    }
}
