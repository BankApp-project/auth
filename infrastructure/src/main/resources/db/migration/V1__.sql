CREATE TABLE users
(
    id      UUID         NOT NULL,
    email   VARCHAR(255) NOT NULL,
    enabled BOOLEAN      NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

CREATE INDEX idx_user_email_activated ON users (email, enabled);