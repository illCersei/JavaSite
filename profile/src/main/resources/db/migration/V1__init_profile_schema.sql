CREATE TABLE user_profile
(
    user_id    UUID NOT NULL,
    nickname   VARCHAR(32),
    avatar_url VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_user_profile PRIMARY KEY (user_id)
);