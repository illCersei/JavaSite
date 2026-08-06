package cersei.octopusservice.dto;

public record SummonOctopusRequest(
        Integer octopusId
) {
    public SummonOctopusRequest {
        if (octopusId == null || octopusId <= 0) {
            throw new IllegalArgumentException("octopusId должен быть больше 0");
        }
    }
}
