package cersei.octopusservice.dto.dungeon;

public record DungeonStartFightDto(
        String battleId,
        String fightServiceUrl,
        Long roomId,
        String roomType
) {
}