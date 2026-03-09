-- V5: Add indexes for foreign keys and frequently queried columns

-- Indexes for bets table foreign keys
CREATE INDEX idx_bets_user_id ON bets(user_id);
CREATE INDEX idx_bets_event_id ON bets(event_id);
CREATE INDEX idx_bets_driver_id ON bets(driver_id);
CREATE INDEX idx_bets_status ON bets(status);
CREATE INDEX idx_bets_created_at ON bets(created_at DESC);

-- Composite index for user bets by event
CREATE INDEX idx_bets_user_event ON bets(user_id, event_id);

-- Index for events table
CREATE INDEX idx_events_year ON events(year);
CREATE INDEX idx_events_country ON events(country);
CREATE INDEX idx_events_session_name ON events(session_name);
CREATE INDEX idx_events_start_time ON events(start_time DESC);

-- Composite index for event filtering
CREATE INDEX idx_events_year_country_session ON events(year, country, session_name);

-- Index for drivers table
CREATE INDEX idx_drivers_team_name ON drivers(team_name);

-- Index for users table
CREATE INDEX idx_users_name ON users(name);
CREATE INDEX idx_users_created_at ON users(created_at DESC);
