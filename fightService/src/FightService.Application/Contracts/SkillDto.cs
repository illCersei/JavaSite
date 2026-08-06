namespace FightService.Application.Contracts;

// Mirrors cersei.octopusservice.dto.SkillEffectDto. Enum-ish fields stay strings on the wire,
// same as the Java DTO - they're parsed into domain enums when building the Combatant.
public sealed record SkillEffectDto(
    long? Id,
    string EffectType,
    string ElementType,
    int BaseValue,
    string? ScalingStat,
    int? ScalingRatioBps,
    int? DurationMs,
    int? TickMs,
    string? StackingRule);

// Mirrors cersei.octopusservice.dto.SkillDto.
public sealed record SkillDto(
    int Id,
    string Name,
    string? Description,
    string ElementType,
    int CooldownMs,
    int ManaCost,
    IReadOnlyList<SkillEffectDto> Effects);
