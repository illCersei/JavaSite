package cersei.octopusservice.dto;

public record CombatStatsDto(
        int hp,
        int attack,
        int magicPower,
        int armor,
        int magicResist,
        int speed
) {
}
