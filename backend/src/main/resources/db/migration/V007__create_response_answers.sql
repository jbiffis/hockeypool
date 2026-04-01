CREATE TABLE response_answers (
    id                  SERIAL PRIMARY KEY,
    response_id         INT NOT NULL REFERENCES responses(id),
    question_id         INT NOT NULL REFERENCES questions(id),
    selected_option_id  INT REFERENCES question_options(id),
    free_form_value     TEXT,
    UNIQUE (response_id, question_id, selected_option_id)
);

CREATE INDEX idx_response_answers_response_id ON response_answers(response_id);
CREATE INDEX idx_response_answers_question_id ON response_answers(question_id);
