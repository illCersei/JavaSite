ALTER TABLE dungeon_run
    ADD COLUMN IF NOT EXISTS current_room_id BIGINT;

ALTER TABLE dungeon_template
    ADD COLUMN IF NOT EXISTS depth_layers INTEGER NOT NULL DEFAULT 3;

CREATE TABLE dungeon_run_room
(
    id                 BIGSERIAL PRIMARY KEY,
    dungeon_run_id     UUID        NOT NULL REFERENCES dungeon_run (id) ON DELETE CASCADE,
    layer_index        INTEGER     NOT NULL,
    slot_index         INTEGER     NOT NULL,
    room_type          VARCHAR(32) NOT NULL,
    enemy_template_id  VARCHAR(64),
    room_status        VARCHAR(32) NOT NULL DEFAULT 'LOCKED',
    loot_item_id       INTEGER REFERENCES item (id),
    loot_quantity      INTEGER     NOT NULL DEFAULT 0,
    loot_coins_minor   BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT uq_dungeon_run_room_position UNIQUE (dungeon_run_id, layer_index, slot_index)
);

CREATE INDEX idx_dungeon_run_room_run_id ON dungeon_run_room (dungeon_run_id);

CREATE TABLE dungeon_run_room_link
(
    id             BIGSERIAL PRIMARY KEY,
    dungeon_run_id UUID    NOT NULL REFERENCES dungeon_run (id) ON DELETE CASCADE,
    from_room_id   BIGINT  NOT NULL REFERENCES dungeon_run_room (id) ON DELETE CASCADE,
    to_room_id     BIGINT  NOT NULL REFERENCES dungeon_run_room (id) ON DELETE CASCADE,
    CONSTRAINT uq_dungeon_run_room_link UNIQUE (from_room_id, to_room_id)
);

UPDATE dungeon_template SET depth_layers = 3 WHERE id = 1;
