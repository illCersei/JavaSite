package cersei.octopusservice.dto;

public record SkillEffectDto(
        Long id,
        String effectType,
        String elementType,
        Integer baseValue,
        String scalingStat,
        Integer scalingRatioBps,
        Integer durationMs,
        Integer tickMs,
        String stackingRule
) {
}
