ALTER TABLE octopus
    ADD COLUMN free_skill_points INTEGER NOT NULL DEFAULT 1;

ALTER TABLE user_octopus
    ADD COLUMN free_skill_points INTEGER NOT NULL DEFAULT 1;