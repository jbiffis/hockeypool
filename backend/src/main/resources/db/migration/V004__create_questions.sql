CREATE TABLE questions (
    id                  SERIAL PRIMARY KEY,
    round_id            INT NOT NULL REFERENCES rounds(id),
    title               VARCHAR(500) NOT NULL,
    description         TEXT,
    image_url           TEXT,
    question_type       VARCHAR(20) NOT NULL CHECK (question_type IN ('single_select', 'multi_select', 'free_form', 'jeopardy')),
    is_mandatory        BOOLEAN DEFAULT TRUE,
    display_order       INT NOT NULL,
    max_wager           INT,
    parent_question_id  INT REFERENCES questions(id),
    created_at          TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_questions_round_id ON questions(round_id);
