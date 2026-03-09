-- V3: Create drivers table
CREATE TABLE drivers (
    id BIGSERIAL PRIMARY KEY,
    external_driver_id INTEGER NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    driver_number INTEGER,
    team_name VARCHAR(255),
    country_code VARCHAR(10)
);

-- Add comment
COMMENT ON TABLE drivers IS 'F1 drivers from OpenF1 API';
