CREATE TABLE ledger_entries
(
    id                  UUID         NOT NULL,
    wallet_id           UUID         NOT NULL,
    entry_type          VARCHAR(64)  NOT NULL,
    direction           VARCHAR(16)  NOT NULL,
    amount_minor        BIGINT       NOT NULL,
    balance_after_minor BIGINT       NOT NULL,
    reference_type      VARCHAR(64)  NOT NULL,
    reference_id        VARCHAR(256) NOT NULL,
    metadata_json       VARCHAR(255),
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_ledger_entries PRIMARY KEY (id)
);

CREATE TABLE outbox_events
(
    id           UUID         NOT NULL,
    wallet_id    UUID         NOT NULL,
    user_id      UUID         NOT NULL,
    topic        VARCHAR(256) NOT NULL,
    payload_json OID          NOT NULL,
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    published_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_outbox_events PRIMARY KEY (id)
);

CREATE TABLE wallet
(
    id            UUID        NOT NULL,
    user_id       UUID        NOT NULL,
    currency      VARCHAR(16) NOT NULL,
    balance_minor BIGINT      NOT NULL,
    version       BIGINT      NOT NULL,
    status        VARCHAR(32) NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_wallet PRIMARY KEY (id)
);

ALTER TABLE wallet
    ADD CONSTRAINT uc_wallet_user UNIQUE (user_id);

ALTER TABLE ledger_entries
    ADD CONSTRAINT FK_LEDGER_ENTRIES_ON_WALLET FOREIGN KEY (wallet_id) REFERENCES wallet (id);