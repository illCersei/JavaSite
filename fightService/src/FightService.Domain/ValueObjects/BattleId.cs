using FightService.Domain.Exceptions;

namespace FightService.Domain.ValueObjects;

public sealed record BattleId
{
    public string Value { get; }

    private BattleId(string value) => Value = value;

    public static BattleId Create(string value)
    {
        if (string.IsNullOrWhiteSpace(value))
            throw new DomainException("BattleId cannot be empty");
        return new BattleId(value);
    }

    public static BattleId New() => new(Guid.NewGuid().ToString());
}