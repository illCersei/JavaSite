package cersei.octopusservice.dto.fight;

public record FightRulesDto(
        int maxTurns,
        boolean autoPlayEnemy
) {
}