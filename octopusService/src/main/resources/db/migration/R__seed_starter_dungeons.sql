INSERT INTO dungeon_template (id, name, description, tier, entry_cost_minor, room_count)
VALUES (1, 'Коралловые Пещеры', 'Стартовое подземелье с сундуками и боями.', 1, 0, 3)
ON CONFLICT (id) DO UPDATE SET
    name             = EXCLUDED.name,
    description      = EXCLUDED.description,
    tier             = EXCLUDED.tier,
    entry_cost_minor = EXCLUDED.entry_cost_minor,
    room_count       = EXCLUDED.room_count;

INSERT INTO dungeon_room_template (
    dungeon_template_id, room_index, room_type,
    enemy_template_id, loot_item_id, loot_quantity, loot_coins_minor
)
VALUES
    (1, 0, 'CHEST', NULL, 1, 1, 25),
    (1, 1, 'BATTLE', 'mob_coral_guard', NULL, 0, 50),
    (1, 2, 'BOSS', 'mob_coral_king', 5, 1, 100)
ON CONFLICT (dungeon_template_id, room_index) DO UPDATE SET
    room_type         = EXCLUDED.room_type,
    enemy_template_id = EXCLUDED.enemy_template_id,
    loot_item_id      = EXCLUDED.loot_item_id,
    loot_quantity     = EXCLUDED.loot_quantity,
    loot_coins_minor  = EXCLUDED.loot_coins_minor;
