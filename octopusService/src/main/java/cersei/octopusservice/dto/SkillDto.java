package cersei.octopusservice.dto;

public record SkillDto(
        Integer id,
        String name,
        String description,
        String elementType,
        Integer cooldownMs,
        Integer manaCost
) {
}