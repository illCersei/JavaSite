ALTER TABLE octopus
    ADD COLUMN starter_skill_id INTEGER;

ALTER TABLE octopus
    ADD CONSTRAINT fk_octopus_on_starter_skill FOREIGN KEY (starter_skill_id) REFERENCES octopus_skill (id);
