-- Reference data (docs/octopus_db_seeding_best_practices.md): repeatable migration, see
-- R__seed_items.sql for why this isn't a manual/CI-CD step.
-- This catalog never had a seed script before - gacha.max-template-id defaults to 6
-- (see application.yml), so the gacha roll needs exactly ids 1..6 to exist. One tier-1
-- starter per element already established by R__seed_skills.sql, lightly stat-leaned
-- to match its element flavor.

INSERT INTO octopus (
    id, name, element_type, tier,
    attack_stat, magic_power_stat, armor_stat, magic_resist_stat, speed_stat
)
VALUES (1, 'Inkling Prowler', 'POISON', 1, 12, 4, 6, 5, 8),
       (2, 'Glacial Drifter', 'FROST', 1, 6, 8, 12, 9, 4),
       (3, 'Shadow Siphon', 'ABYSS', 1, 5, 13, 6, 8, 6),
       (4, 'Ember Kraken', 'FLAME', 1, 13, 6, 5, 4, 10),
       (5, 'Voltjet', 'STORM', 1, 8, 6, 4, 5, 15),
       (6, 'Current Warden', 'TIDE', 1, 6, 6, 10, 11, 5)
ON CONFLICT (id)
    DO UPDATE SET
                  name              = EXCLUDED.name,
                  element_type      = EXCLUDED.element_type,
                  tier              = EXCLUDED.tier,
                  attack_stat       = EXCLUDED.attack_stat,
                  magic_power_stat  = EXCLUDED.magic_power_stat,
                  armor_stat        = EXCLUDED.armor_stat,
                  magic_resist_stat = EXCLUDED.magic_resist_stat,
                  speed_stat        = EXCLUDED.speed_stat;
