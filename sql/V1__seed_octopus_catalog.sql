INSERT INTO octopus (id, name, element_type, tier, attack_stat, magic_power_stat, armor_stat, magic_resist_stat, speed_stat)
VALUES
  (1, 'Toxic Inkling', 'POISON', 1, 18, 10,  8,  7, 12),
  (2, 'Frozen Pearl',  'FROST',  1, 12, 18,  7, 10, 11),
  (3, 'Ember Tentacle','FLAME',  1, 20,  9,  7,  7, 13),
  (4, 'Storm Current', 'STORM',  1, 14, 16,  8,  8, 15),
  (5, 'Abyss Watcher', 'ABYSS',  1, 16, 14, 10, 11, 10),
  (6, 'Tidal Guard',   'TIDE',   1, 12, 12, 14, 13,  9)
ON CONFLICT (id) DO NOTHING;
