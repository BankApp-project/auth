CREATE TABLE passkey
(
    id                      UUID         NOT NULL,
    user_handle             UUID         NOT NULL,
    type                    VARCHAR(255) NOT NULL,
    public_key              BYTEA        NOT NULL,
    sign_count              BIGINT       NOT NULL,
    uv_initialized          BOOLEAN      NOT NULL,
    backup_eligible         BOOLEAN      NOT NULL,
    backup_state            BOOLEAN      NOT NULL,
    transports              VARCHAR,
    client_extensions       JSON,
    attestation             BYTEA        NOT NULL,
    attestation_client_data BYTEA,
    CONSTRAINT pk_passkey PRIMARY KEY (id)
);

CREATE TABLE users
(
    id      UUID         NOT NULL,
    email   VARCHAR(255) NOT NULL,
    enabled BOOLEAN      NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE passkey
    ADD CONSTRAINT uc_passkey_public_key UNIQUE (public_key);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

CREATE INDEX idx_user_email_activated ON users (email, enabled);

CREATE INDEX idx_user_userHandle ON passkey (user_handle);