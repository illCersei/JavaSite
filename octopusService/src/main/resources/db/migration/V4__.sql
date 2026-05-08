ALTER TABLE user_octopus
    ADD current_tier INTEGER;

ALTER TABLE user_octopus
    ADD nickname VARCHAR(255);

ALTER TABLE user_octopus
    ADD role VARCHAR(32);

ALTER TABLE user_octopus
    ADD stars INTEGER;

ALTER TABLE user_octopus
    ALTER COLUMN current_tier SET NOT NULL;

ALTER TABLE user_octopus
    ALTER COLUMN role SET NOT NULL;

ALTER TABLE user_octopus
    ALTER COLUMN stars SET NOT NULL;