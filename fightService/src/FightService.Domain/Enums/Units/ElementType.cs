namespace FightService.Domain.Enums.Units;

// Member names mirror the Java enum's wire format (Jackson serializes enums as their ALL_CAPS name).
public enum ElementType
{
    POISON,
    FROST,
    FLAME,
    STORM,
    ABYSS,
    TIDE,
    ARCANE,
    PHYSICAL
}
