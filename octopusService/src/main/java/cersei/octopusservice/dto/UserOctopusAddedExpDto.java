package cersei.octopusservice.dto;

public record UserOctopusAddedExpDto(
        UserOctopusDto userOctopusDto,
        Integer startLevel,
        Integer newLevel) {
}
