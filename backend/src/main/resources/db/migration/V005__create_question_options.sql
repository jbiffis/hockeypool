CREATE TABLE question_options (
    id              SERIAL PRIMARY KEY,
    question_id     INT NOT NULL REFERENCES questions(id),
    option_text     TEXT NOT NULL,
    display_order   INT NOT NULL,
    points          INT
);

CREATE INDEX idx_question_options_question_id ON question_options(question_id);
