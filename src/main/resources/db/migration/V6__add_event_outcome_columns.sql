-- Add columns for event outcome/settlement

ALTER TABLE events 
ADD COLUMN winner_driver_id BIGINT NULL,
ADD COLUMN settled BOOLEAN NOT NULL DEFAULT FALSE;

-- Add foreign key for winner_driver_id
ALTER TABLE events
ADD CONSTRAINT fk_events_winner_driver 
FOREIGN KEY (winner_driver_id) REFERENCES drivers(id);

-- Add index for settled status for efficient queries
CREATE INDEX idx_events_settled ON events(settled);

COMMENT ON COLUMN events.winner_driver_id IS 'The driver who won this event (null until outcome is simulated)';
COMMENT ON COLUMN events.settled IS 'Whether this event outcome has been settled and bets processed';
