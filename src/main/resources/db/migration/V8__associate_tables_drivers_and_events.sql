-- V8__associate_tables_drivers_and_events.sql

ALTER TABLE drivers DROP COLUMN IF EXISTS external_session_key;

CREATE TABLE event_drivers (
                               event_id bigint NOT NULL,
                               driver_id bigint NOT NULL,
                               PRIMARY KEY (event_id, driver_id),
                               CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
                               CONSTRAINT fk_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE
);

CREATE INDEX idx_event_drivers_event_id ON event_drivers(event_id);
CREATE INDEX idx_event_drivers_driver_id ON event_drivers(driver_id);