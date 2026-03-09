-- V2: Create events table
CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    external_session_key INTEGER NOT NULL UNIQUE,
    year INTEGER NOT NULL,
    country VARCHAR(255) NOT NULL,
    session_name VARCHAR(255) NOT NULL,
    start_time TIMESTAMP,
    circuit_short_name VARCHAR(255)
);

-- Add comment
COMMENT ON TABLE events IS 'F1 race sessions/events from OpenF1 API';
