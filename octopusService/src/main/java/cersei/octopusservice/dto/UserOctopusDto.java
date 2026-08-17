package cersei.octopusservice.dto;

import cersei.octopusservice.model.utils.CombatRole;

import java.util.List;
import java.util.Set;

public record UserOctopusDto(
        Integer id,
        Integer baseOctopusId,
        String nickname,
        Integer level,
        Integer currentTier,
        Integer stars,
        CombatRole role,
        Integer exp,
        Integer currentAttackStat,
        Integer currentMagicPowerStat,
        Integer currentArmorStat,
        Integer currentMagicResistStat,
        Integer currentSpeedStat,
        Integer currentFreeSkillPoints,
        Set<SkillDto> openSkills,
        List<SkillSlotDto> skillSlots,
        List<EquipmentDto> equipment
) {
}