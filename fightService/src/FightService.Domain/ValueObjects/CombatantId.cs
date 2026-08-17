using FightService.Domain.Exceptions;

namespace FightService.Domain.ValueObjects;

public sealed record CombatantId
{
    public string Value { get; }

    private CombatantId(string value) => Value = value;

    public static CombatantId Create(string value)
    {
        if (string.IsNullOrWhiteSpace(value))
            throw new DomainException("CombatantId cannot be empty");
        return new CombatantId(value);
    }

    public override string ToString() => Value;
}
