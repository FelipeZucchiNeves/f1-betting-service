-- V7__add_external_session_key_to_drivers.sql

ALTER TABLE drivers
    ADD COLUMN external_session_key INTEGER NULL;

CREATE INDEX idx_drivers_external_session_key ON drivers(external_session_key);

COMMENT ON COLUMN drivers.external_session_key IS 'Foreign key linking driver to external session key from the external API';