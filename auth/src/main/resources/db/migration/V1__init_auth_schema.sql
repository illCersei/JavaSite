CREATE TABLE refresh_token
(
    id         UUID NOT NULL,
    user_id    UUID,
    token      VARCHAR(255),
    expires_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_refreshtoken PRIMARY KEY (id)
);

CREATE TABLE users
(
    user_id  UUID         NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email    VARCHAR(255),
    role     VARCHAR(255),
    CONSTRAINT pk_users PRIMARY KEY (user_id)
);

ALTER TABLE refresh_token
    ADD CONSTRAINT uc_refreshtoken_token UNIQUE (token);

ALTER TABLE users
    ADD CONSTRAINT uk_user_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uk_user_username UNIQUE (username);