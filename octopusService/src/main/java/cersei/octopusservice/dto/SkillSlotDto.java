package cersei.octopusservice.dto;

public record SkillSlotDto(
        Long id,
        Integer slotIndex,
        SkillDto skill
) {
}