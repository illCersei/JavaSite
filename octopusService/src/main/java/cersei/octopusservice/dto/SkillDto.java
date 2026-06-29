package cersei.octopusservice.dto;

import java.util.List;

public record SkillDto(
        Integer id,
        String name,
        String description,
        String elementType,
        Integer cooldownMs,
        Integer manaCost,
        List<SkillEffectDto> effects
) {
}
