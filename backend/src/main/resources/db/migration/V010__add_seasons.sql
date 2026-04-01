-- Create seasons table
CREATE TABLE seasons (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    year        INT NOT NULL,
    status      VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'archived')),
    created_at  TIMESTAMP DEFAULT NOW()
);

-- Insert default season for existing data
INSERT INTO seasons (name, year, status) VALUES ('2024/2025', 2025, 'active');

-- Add season_id to rounds
ALTER TABLE rounds ADD COLUMN season_id INT REFERENCES seasons(id);
UPDATE rounds SET season_id = 1;
ALTER TABLE rounds ALTER COLUMN season_id SET NOT NULL;
CREATE INDEX idx_rounds_season_id ON rounds(season_id);

-- Add season_id to participants
ALTER TABLE participants ADD COLUMN season_id INT REFERENCES seasons(id);
UPDATE participants SET season_id = 1;
ALTER TABLE participants ALTER COLUMN season_id SET NOT NULL;
ALTER TABLE participants DROP CONSTRAINT IF EXISTS participants_email_key;
ALTER TABLE participants ADD CONSTRAINT participants_email_season_unique UNIQUE (email, season_id);
CREATE INDEX idx_participants_season_id ON participants(season_id);
