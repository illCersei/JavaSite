package cersei.octopusservice.dto.fight;

import cersei.octopusservice.dto.CombatStatsDto;
import cersei.octopusservice.dto.SkillDto;

import java.util.List;

public record FightCombatantDto(
        String combatantId,
        String templateId,
        String name,
        CombatStatsDto stats,
        List<SkillDto> skills
) {
}