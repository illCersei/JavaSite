package cersei.octopusservice.dto;

import cersei.octopusservice.model.utils.CombatRole;

import java.util.List;
import java.util.Set;

public record CombatSnapshotDto(
        Integer userOctopusId,
        Integer baseOctopusId,
        String nickname,
        Integer level,
        Integer currentTier,
        Integer stars,
        CombatRole role,
        CombatStatsDto stats,
        Set<SkillDto> openSkills,
        List<SkillSlotDto> skillSlots,
        List<EquipmentDto> equipment
) {
}
