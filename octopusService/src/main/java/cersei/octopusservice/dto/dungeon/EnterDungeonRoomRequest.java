package cersei.octopusservice.dto.dungeon;

public record EnterDungeonRoomRequest(
        long roomId
) {
    public EnterDungeonRoomRequest {
        if (roomId <= 0) {
            throw new IllegalArgumentException("roomId должен быть больше 0");
        }
    }
}