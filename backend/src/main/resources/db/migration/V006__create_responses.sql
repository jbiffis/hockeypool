CREATE TABLE responses (
    id              SERIAL PRIMARY KEY,
    participant_id  INT NOT NULL REFERENCES participants(id),
    round_id        INT NOT NULL REFERENCES rounds(id),
    submitted_at    TIMESTAMP DEFAULT NOW(),
    UNIQUE (participant_id, round_id)
);

CREATE INDEX idx_responses_participant_id ON responses(participant_id);
CREATE INDEX idx_responses_round_id ON responses(round_id);
