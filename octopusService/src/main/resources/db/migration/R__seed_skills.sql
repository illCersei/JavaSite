-- Reference data (docs/octopus_db_seeding_best_practices.md): repeatable migration, see
-- R__seed_items.sql for why this isn't a manual/CI-CD step.
-- effect_type: DAMAGE, DOT, HEAL, SHIELD, BUFF, DEBUFF. element_type: see ElementType enum.
-- scaling_stat: see ScalingStat enum. stacking_rule: see StackingRule enum.

INSERT INTO octopus_skill (id, name, description, element_type, cooldown_ms, mana_cost)
VALUES
  (1, 'Ядовитые Чернила', 'Ядовитый удар с длительным отравлением.', 'POISON', 6000, 15),
  (2, 'Ледяной Пузырь', 'Ледяной удар стихией мороза.', 'FROST', 8000, 20),
  (3, 'Иссушение Бездны', 'Магический урон с самоисцелением.', 'ABYSS', 10000, 30),
  (4, 'Огненный Рывок', 'Мощный огненный удар по одной цели.', 'FLAME', 7000, 18),
  (5, 'Штормовой Разряд', 'Быстрый удар стихией шторма.', 'STORM', 6500, 16),
  (6, 'Стража Прилива', 'Небольшое исцеление и поддержка союзника.', 'TIDE', 9000, 22)
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name, description = EXCLUDED.description, element_type = EXCLUDED.element_type,
  cooldown_ms = EXCLUDED.cooldown_ms, mana_cost = EXCLUDED.mana_cost;

-- Effects for Toxic Ink: direct damage + DOT
INSERT INTO octopus_skill_effect (id, skill_id, effect_type, element_type, base_value, scaling_stat, scaling_ratio_bps, duration_ms, tick_ms, stacking_rule)
VALUES
  (1001, 1, 'DAMAGE', 'POISON', 12, 'ATTACK', 8000, NULL, NULL, NULL),
  (1002, 1, 'DOT',    'POISON',  4, 'MAGIC_POWER', 5000, 6000, 1000, 'REFRESH_DURATION')
ON CONFLICT (id) DO NOTHING;

-- Effects for Frost Bubble: damage only for now (debuff later)
INSERT INTO octopus_skill_effect (id, skill_id, effect_type, element_type, base_value, scaling_stat, scaling_ratio_bps, duration_ms, tick_ms, stacking_rule)
VALUES
  (2001, 2, 'DAMAGE', 'FROST', 14, 'MAGIC_POWER', 9000, NULL, NULL, NULL)
ON CONFLICT (id) DO NOTHING;

-- Effects for Abyss Drain: damage + heal (heal uses baseValue, scaling optional)
INSERT INTO octopus_skill_effect (id, skill_id, effect_type, element_type, base_value, scaling_stat, scaling_ratio_bps, duration_ms, tick_ms, stacking_rule)
VALUES
  (3001, 3, 'DAMAGE', 'ABYSS', 16, 'MAGIC_POWER', 10000, NULL, NULL, NULL),
  (3002, 3, 'HEAL',   'ABYSS',  8, NULL, NULL, NULL, NULL, NULL)
ON CONFLICT (id) DO NOTHING;

-- Effects for Flame Rush: burst
INSERT INTO octopus_skill_effect (id, skill_id, effect_type, element_type, base_value, scaling_stat, scaling_ratio_bps, duration_ms, tick_ms, stacking_rule)
VALUES
  (4001, 4, 'DAMAGE', 'FLAME', 20, 'ATTACK', 11000, NULL, NULL, NULL)
ON CONFLICT (id) DO NOTHING;

-- Effects for Storm Jolt: fast damage profile
INSERT INTO octopus_skill_effect (id, skill_id, effect_type, element_type, base_value, scaling_stat, scaling_ratio_bps, duration_ms, tick_ms, stacking_rule)
VALUES
  (5001, 5, 'DAMAGE', 'STORM', 13, 'SPEED', 7000, NULL, NULL, NULL)
ON CONFLICT (id) DO NOTHING;

-- Effects for Tidal Guard: shield-ish stand-in via heal for now
INSERT INTO octopus_skill_effect (id, skill_id, effect_type, element_type, base_value, scaling_stat, scaling_ratio_bps, duration_ms, tick_ms, stacking_rule)
VALUES
  (6001, 6, 'HEAL', 'TIDE', 10, 'MAGIC_POWER', 6000, NULL, NULL, NULL)
ON CONFLICT (id) DO NOTHING;

-- Each octopus's own themed skill (same id, since both catalogs were seeded 1:1 by
-- element from the start) - UserOctopusStashService grants and equips this automatically
-- on summon, so every octopus starts with a skill matching its element instead of an
-- empty loadout. Runs after both octopus and octopus_skill rows exist (repeatable
-- migrations apply in alphabetical order: seed_octopus_catalog, then this one).
UPDATE octopus SET starter_skill_id = id WHERE id BETWEEN 1 AND 6;
