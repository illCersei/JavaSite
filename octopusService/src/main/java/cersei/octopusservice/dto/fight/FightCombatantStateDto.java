package cersei.octopusservice.dto.fight;

public record FightCombatantStateDto(
        String combatantId,
        int hpRemaining,
        boolean dead,
        int turnMeterBps
) {
}