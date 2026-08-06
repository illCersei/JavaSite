namespace FightService.Domain.ValueObjects;

// Balance caps from docs/OCTOPUS_MINIGAME_PLAN.md §16.7, enforced once here so every
// downstream calculation (ATB, damage, crit, hit chance) already works with capped numbers.
public sealed record Stats
{
    public const int MaxCritChance = 75;
    public const int MaxEvasion = 45;
    public const int MaxSpeed = 300;

    public int Hp { get; }
    public int Attack { get; }
    public int MagicPower { get; }
    public int Armor { get; }
    public int MagicResist { get; }
    public int Speed { get; }
    public int CritChance { get; }
    public int CritDamage { get; }
    public int Accuracy { get; }
    public int Evasion { get; }
    public int Tenacity { get; }
    public int StatusPower { get; }

    private Stats(
        int hp, int attack, int magicPower, int armor, int magicResist, int speed,
        int critChance, int critDamage, int accuracy, int evasion, int tenacity, int statusPower)
    {
        Hp = Math.Max(0, hp);
        Attack = Math.Max(0, attack);
        MagicPower = Math.Max(0, magicPower);
        Armor = Math.Max(0, armor);
        MagicResist = Math.Max(0, magicResist);
        Speed = Math.Clamp(speed, 0, MaxSpeed);
        CritChance = Math.Clamp(critChance, 0, MaxCritChance);
        CritDamage = Math.Max(0, critDamage);
        Accuracy = Math.Max(0, accuracy);
        Evasion = Math.Clamp(evasion, 0, MaxEvasion);
        Tenacity = Math.Max(0, tenacity);
        StatusPower = Math.Max(0, statusPower);
    }

    public static Stats Create(
        int hp, int attack, int magicPower, int armor, int magicResist, int speed,
        int critChance, int critDamage, int accuracy, int evasion, int tenacity, int statusPower) =>
        new(hp, attack, magicPower, armor, magicResist, speed,
            critChance, critDamage, accuracy, evasion, tenacity, statusPower);

    public int Get(Enums.ScalingStat stat) => stat switch
    {
        Enums.ScalingStat.ATTACK => Attack,
        Enums.ScalingStat.MAGIC_POWER => MagicPower,
        Enums.ScalingStat.ARMOR => Armor,
        Enums.ScalingStat.MAGIC_RESIST => MagicResist,
        Enums.ScalingStat.SPEED => Speed,
        _ => 0
    };
}
