ALTER TABLE passkey
    ADD id_bytes BYTEA;

UPDATE passkey
SET id_bytes = id::bytea
WHERE id_bytes IS NULL;