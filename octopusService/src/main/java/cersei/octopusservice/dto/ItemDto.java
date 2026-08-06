package cersei.octopusservice.dto;

import cersei.octopusservice.model.utils.ItemSlot;

public record ItemDto(
        Integer id,
        String name,
        String description,
        ItemSlot slot,
        Integer tier,
        Integer attackStat,
        Integer magicPowerStat,
        Integer armorStat,
        Integer magicResistStat,
        Integer speedStat,
        Integer critChance,
        Integer critDamage,
        Integer accuracy,
        Integer evasion,
        Integer tenacity,
        Integer statusPower
) {
}