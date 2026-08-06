namespace FightService.Application.Contracts;

// Mirrors cersei.octopusservice.dto.CombatStatsDto exactly (shared by both squads and enemy
// templates on the Java side, so this one shape covers every combatant on the wire).
public sealed record CombatStatsDto(
    int Hp,
    int Attack,
    int MagicPower,
    int Armor,
    int MagicResist,
    int Speed,
    int CritChance,
    int CritDamage,
    int Accuracy,
    int Evasion,
    int Tenacity,
    int StatusPower);
