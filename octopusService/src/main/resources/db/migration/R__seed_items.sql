-- Reference data (docs/octopus_db_seeding_best_practices.md): repeatable migration so a fresh
-- database always has the starter item catalog, without a separate manual/CI-CD step to forget.
-- Base items for octopuses (columns tier/stats from V5/V6; crit/accuracy/evasion/tenacity/
-- statusPower added in V12 default to 0 for these - starter gear has no bonus on those yet).

INSERT INTO item (
    id, name, description, slot,
    tier,
    attack_stat, magic_power_stat, armor_stat, magic_resist_stat, speed_stat
)
VALUES (1, 'Coral Spear', 'Starter weapon. +ATK', 'WEAPON', 1,
        6, 0, 2, 1, 1),
       (2, 'Kelp Mail', 'Starter armor.', 'ARMOR', 1,
        2, 0, 12, 4, 0),
       (3, 'Shell Helm', 'Starter helmet.', 'HELMET', 1,
        2, 0, 8, 6, 2),
       (4, 'Tide Boots', 'Starter boots.', 'BOOTS', 1,
        3, 0, 6, 3, 6),
       (5, 'Abyss Sigil', 'Starter artifact.', 'ARTIFACT', 1,
        0, 8, 0, 8, 0),
       (6, 'Venom Fang', 'Rare weapon.', 'WEAPON', 2,
        10, 2, 0, 2, 0),
       (7, 'Guardian Carapace', 'Rare armor.', 'ARMOR', 2,
        4, 0, 16, 6, 0),
       (8, 'Frost Crown', 'Rare helmet.', 'HELMET', 2,
        4, 0, 10, 8, 3),
       (9, 'Raider Fins', 'Rare boots.', 'BOOTS', 2,
        4, 0, 8, 4, 8)
ON CONFLICT (id)
    DO UPDATE SET
                  name              = EXCLUDED.name,
                  description       = EXCLUDED.description,
                  slot              = EXCLUDED.slot,
                  tier              = EXCLUDED.tier,
                  attack_stat       = EXCLUDED.attack_stat,
                  magic_power_stat  = EXCLUDED.magic_power_stat,
                  armor_stat        = EXCLUDED.armor_stat,
                  magic_resist_stat = EXCLUDED.magic_resist_stat,
                  speed_stat        = EXCLUDED.speed_stat;
