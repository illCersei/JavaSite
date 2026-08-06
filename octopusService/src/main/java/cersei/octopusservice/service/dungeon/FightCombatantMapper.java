package cersei.octopusservice.service.dungeon;

import cersei.octopusservice.dto.CombatSnapshotDto;
import cersei.octopusservice.dto.CombatStatsDto;
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
        return fromEnemy(enemy, 1.0);
    }

    // statMultiplier scales the core combat stats (hp/attack/magicPower/armor/magicResist) -
    // used to make ELITE_COMBAT rooms noticeably tougher than a plain BATTLE with the same mob.
    public FightCombatantDto fromEnemy(EnemyTemplateDto enemy, double statMultiplier) {
        return new FightCombatantDto(
                enemy.id(),
                enemy.id(),
                enemy.name(),
                scale(enemy.stats(), statMultiplier),
                enemy.skills()
        );
    }

    private CombatStatsDto scale(CombatStatsDto stats, double multiplier) {
        if (multiplier == 1.0) {
            return stats;
        }
        return new CombatStatsDto(
                (int) Math.round(stats.hp() * multiplier),
                (int) Math.round(stats.attack() * multiplier),
                (int) Math.round(stats.magicPower() * multiplier),
                (int) Math.round(stats.armor() * multiplier),
                (int) Math.round(stats.magicResist() * multiplier),
                stats.speed(),
                stats.critChance(),
                stats.critDamage(),
                stats.accuracy(),
                stats.evasion(),
                stats.tenacity(),
                stats.statusPower()
        );
    }
}