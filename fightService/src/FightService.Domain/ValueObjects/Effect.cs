using FightService.Domain.Enums;
using FightService.Domain.Enums.Units;

namespace FightService.Domain.ValueObjects;

public sealed record Effect(
    SkillEffectType EffectType,
    ElementType ElementType,
    int BaseValue,
    ScalingStat? ScalingStat,
    int ScalingRatioBps,
    int? DurationMs,
    int? TickMs,
    StackingRule? StackingRule
);
