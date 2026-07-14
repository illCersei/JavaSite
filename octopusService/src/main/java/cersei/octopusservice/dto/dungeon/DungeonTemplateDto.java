package cersei.octopusservice.dto.dungeon;

public record DungeonTemplateDto(
        int id,
        String name,
        String description,
        int tier,
        long entryCostMinor,
        int depthLayers
) {
}