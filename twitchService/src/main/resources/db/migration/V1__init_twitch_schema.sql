CREATE TABLE games
(
    game_id   BIGINT NOT NULL,
    game_name VARCHAR(255),
    CONSTRAINT pk_games PRIMARY KEY (game_id)
);

CREATE TABLE viewer
(
    id        BIGINT NOT NULL,
    game_id   BIGINT NOT NULL,
    date_time TIMESTAMP WITHOUT TIME ZONE,
    viewers   BIGINT NOT NULL,
    CONSTRAINT pk_viewer PRIMARY KEY (id)
);