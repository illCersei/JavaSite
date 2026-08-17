CREATE TABLE user_battle_team_slot (
       id          BIGSERIAL PRIMARY KEY,
       user_id     UUID         NOT NULL,
       slot_index  SMALLINT     NOT NULL CHECK (slot_index BETWEEN 0 AND 2),
       user_octopus_id INTEGER  NOT NULL REFERENCES user_octopus (id) ON DELETE CASCADE,
       updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
       CONSTRAINT uq_user_battle_team_slot UNIQUE (user_id, slot_index),
       CONSTRAINT uq_user_battle_team_octopus UNIQUE (user_id, user_octopus_id)
);

CREATE INDEX idx_user_battle_team_user_id ON user_battle_team_slot (user_id);

ALTER TABLE user_battle_team_slot
DROP
COLUMN slot_index;

ALTER TABLE user_battle_team_slot
    ADD slot_index INTEGER NOT NULL;

ALTER TABLE user_battle_team_slot
    ADD CONSTRAINT uq_user_battle_team_slot UNIQUE (slot_index);