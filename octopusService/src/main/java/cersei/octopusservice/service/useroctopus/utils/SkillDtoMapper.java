package cersei.octopusservice.service.useroctopus.utils;

import cersei.octopusservice.dto.SkillDto;
import cersei.octopusservice.dto.SkillEffectDto;
import cersei.octopusservice.model.OctopusSkill;
import cersei.octopusservice.model.OctopusSkillEffect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SkillDtoMapper {

    @Value("${octopus.skill-icon-url-template}")
    private String skillIconUrlTemplate;

    public SkillDto toDto(OctopusSkill skill) {
        if (skill == null) {
            return null;
        }
        return new SkillDto(
                skill.getId(),
                skill.getName(),
                skill.getDescription(),
                skillIconUrlTemplate.formatted(skill.getId()),
                skill.getElementType().name(),
                skill.getCooldownMs(),
                skill.getManaCost(),
                skill.getEffects().stream().map(this::toEffectDto).toList()
        );
    }

    private SkillEffectDto toEffectDto(OctopusSkillEffect effect) {
        return new SkillEffectDto(
                effect.getId(),
                effect.getEffectType().name(),
                effect.getElementType().name(),
                effect.getBaseValue(),
                effect.getScalingStat() != null ? effect.getScalingStat().name() : null,
                effect.getScalingRatioBps(),
                effect.getDurationMs(),
                effect.getTickMs(),
                effect.getStackingRule() != null ? effect.getStackingRule().name() : null
        );
    }
}
