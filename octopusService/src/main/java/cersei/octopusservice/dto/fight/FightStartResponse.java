package cersei.octopusservice.dto.fight;

/**
 * C# → Java сразу после POST /fight/start.
 */
public record FightStartResponse(
        String battleId,
        String status,
        String currentActorId,
        int turnNumber
) {
}