CREATE TABLE divisions (
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    season_id INTEGER NOT NULL REFERENCES seasons(id) ON DELETE CASCADE,
    UNIQUE (name, season_id)
);

CREATE TABLE participant_divisions (
    division_id    INTEGER NOT NULL REFERENCES divisions(id) ON DELETE CASCADE,
    participant_id INTEGER NOT NULL REFERENCES participants(id) ON DELETE CASCADE,
    PRIMARY KEY (division_id, participant_id)
);
