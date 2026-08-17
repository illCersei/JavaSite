package cersei.octopusservice.dto;

public record OctopusSummaryDto(
        int id,
        String name,
        String elementType,
        int tier,
        String imageUrl,
        int attack,
        int magicPower,
        int armor,
        int magicResist,
        int speed,
        int quantity
) {
}
