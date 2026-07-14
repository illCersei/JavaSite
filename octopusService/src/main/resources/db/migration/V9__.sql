ALTER TABLE user_battle_team_slot
DROP
CONSTRAINT user_battle_team_slot_user_octopus_id_fkey;

CREATE TABLE user_battle_team
(
    user_id          UUID NOT NULL,
    user_octopus_ids INTEGER[]                   NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_user_battle_team PRIMARY KEY (user_id)
);

DROP TABLE user_battle_team_slot CASCADE;