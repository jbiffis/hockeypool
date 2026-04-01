CREATE TABLE participant_scores (
    id              SERIAL PRIMARY KEY,
    participant_id  INT NOT NULL REFERENCES participants(id),
    question_id     INT NOT NULL REFERENCES questions(id),
    round_id        INT NOT NULL REFERENCES rounds(id),
    points_earned   INT NOT NULL DEFAULT 0,
    UNIQUE (participant_id, question_id)
);

CREATE INDEX idx_participant_scores_participant_id ON participant_scores(participant_id);
CREATE INDEX idx_participant_scores_round_id ON participant_scores(round_id);
