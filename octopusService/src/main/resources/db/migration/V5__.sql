CREATE TABLE user_octopus_open_skill
(
    skill_id        INTEGER NOT NULL,
    user_octopus_id INTEGER NOT NULL,
    CONSTRAINT pk_user_octopus_open_skill PRIMARY KEY (skill_id, user_octopus_id)
);

ALTER TABLE item
    ADD armor_stat INTEGER;

ALTER TABLE item
    ADD attack_stat INTEGER;

ALTER TABLE item
    ADD magic_power_stat INTEGER;

ALTER TABLE item
    ADD magic_resist_stat INTEGER;

ALTER TABLE item
    ADD speed_stat INTEGER;

ALTER TABLE item
    ADD tier INTEGER;

ALTER TABLE item
    ALTER COLUMN armor_stat SET NOT NULL;

ALTER TABLE item
    ALTER COLUMN attack_stat SET NOT NULL;

ALTER TABLE item
    ALTER COLUMN magic_power_stat SET NOT NULL;

ALTER TABLE item
    ALTER COLUMN magic_resist_stat SET NOT NULL;

ALTER TABLE item
    ALTER COLUMN speed_stat SET NOT NULL;

ALTER TABLE item
    ALTER COLUMN tier SET NOT NULL;

ALTER TABLE user_octopus_equipment
    ADD CONSTRAINT uq_user_octopus_equipment_slot UNIQUE (user_octopus_id, slot);

ALTER TABLE user_octopus_equipment
ALTER
COLUMN slot TYPE VARCHAR(64) USING (slot::VARCHAR(64));