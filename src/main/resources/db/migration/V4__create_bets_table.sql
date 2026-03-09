-- V4: Create bets table
CREATE TABLE bets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    driver_id BIGINT NOT NULL,
    stake DECIMAL(10, 2) NOT NULL,
    odds DECIMAL(4, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_bets_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_bets_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_bets_driver FOREIGN KEY (driver_id) REFERENCES drivers(id),
    CONSTRAINT chk_bet_status CHECK (status IN ('PENDING', 'WON', 'LOST', 'CANCELLED'))
);

-- Add comment
COMMENT ON TABLE bets IS 'User bets on F1 race events';
