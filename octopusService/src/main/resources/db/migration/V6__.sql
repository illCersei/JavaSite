ALTER TABLE user_octopus_open_skill
    ADD CONSTRAINT fk_useoctopeski_on_octopus_skill FOREIGN KEY (skill_id) REFERENCES octopus_skill (id);

ALTER TABLE user_octopus_open_skill
    ADD CONSTRAINT fk_useoctopeski_on_user_octopus FOREIGN KEY (user_octopus_id) REFERENCES user_octopus (id);
