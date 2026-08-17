using FightService.Domain.Enums.Units;
using FightService.Domain.Exceptions;
using FightService.Domain.ValueObjects;

namespace FightService.Domain.Entities;

public sealed class Skill
{
    public SkillId Id { get; private set; }
    public string Name { get; private set; }
    public string Description { get; private set; }
    public ElementType ElementType { get; private set; }
    public int CooldownMs { get; private set; }   // в миллисекундах
    public int ManaCost { get; private set; }
    public IReadOnlyList<Effect> Effects => _effects.AsReadOnly();

    private readonly List<Effect> _effects = new();

    public Skill(
        SkillId id,
        string name,
        string description,
        ElementType elementType,
        int cooldownMs,
        int manaCost,
        IEnumerable<Effect> effects)
    {
        Id = id ?? throw new ArgumentNullException(nameof(id));
        Name = string.IsNullOrWhiteSpace(name) ? throw new DomainException("Skill name is required") : name;
        Description = description ?? string.Empty;
        ElementType = elementType;
        CooldownMs = cooldownMs >= 0 ? cooldownMs : throw new DomainException("Cooldown cannot be negative");
        ManaCost = manaCost >= 0 ? manaCost : throw new DomainException("ManaCost cannot be negative");
        _effects.AddRange(effects ?? Enumerable.Empty<Effect>());
    }
}