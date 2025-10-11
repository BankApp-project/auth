ALTER TABLE passkey
    ADD id_bytes BYTEA;

UPDATE passkey
SET id_bytes = uuid_send(id);

ALTER TABLE passkey
    DROP COLUMN id;

ALTER TABLE passkey
    RENAME id_bytes TO id;

ALTER TABLE passkey
    ALTER COLUMN id SET NOT NULL;

ALTER TABLE passkey
    ADD PRIMARY KEY (id);