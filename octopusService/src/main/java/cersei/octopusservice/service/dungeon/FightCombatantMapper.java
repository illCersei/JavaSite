package cersei.octopusservice.service.dungeon;

import cersei.octopusservice.dto.CombatSnapshotDto;
import cersei.octopusservice.dto.SkillDto;
import cersei.octopusservice.dto.SkillSlotDto;
import cersei.octopusservice.dto.fight.EnemyTemplateDto;
import cersei.octopusservice.dto.fight.FightCombatantDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class FightCombatantMapper {

    public FightCombatantDto fromPlayerSnapshot(CombatSnapshotDto snapshot) {
        List<SkillDto> skills = snapshot.skillSlots().stream()
                .map(SkillSlotDto::skill)
                .filter(Objects::nonNull)
                .toList();
        if (skills.isEmpty()) {
            skills = List.copyOf(snapshot.openSkills());
        }
        return new FightCombatantDto(
                String.valueOf(snapshot.userOctopusId()),
                String.valueOf(snapshot.baseOctopusId()),
                snapshot.nickname(),
                snapshot.stats(),
                skills
        );
    }

    public FightCombatantDto fromEnemy(EnemyTemplateDto enemy) {
        return new FightCombatantDto(
                enemy.id(),
                enemy.id(),
                enemy.name(),
                enemy.stats(),
                enemy.skills()
        );
    }
}