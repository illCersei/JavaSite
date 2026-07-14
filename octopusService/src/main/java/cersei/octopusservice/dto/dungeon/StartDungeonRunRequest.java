package cersei.octopusservice.dto.dungeon;

public record StartDungeonRunRequest(
        int templateId
) {
    public StartDungeonRunRequest {
        if (templateId <= 0) {
            throw new IllegalArgumentException("templateId должен быть больше 0");
        }
    }
}