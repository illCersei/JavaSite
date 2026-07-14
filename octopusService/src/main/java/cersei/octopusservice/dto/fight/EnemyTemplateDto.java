package cersei.octopusservice.dto.fight;

import cersei.octopusservice.dto.CombatStatsDto;
import cersei.octopusservice.dto.SkillDto;

import java.util.List;

public record EnemyTemplateDto(
        String id,
        String name,
        int tier,
        CombatStatsDto stats,
        List<SkillDto> skills
) {
}
