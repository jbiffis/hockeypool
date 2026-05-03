ALTER TABLE questions DROP CONSTRAINT IF EXISTS questions_question_type_check;
ALTER TABLE questions ADD CONSTRAINT questions_question_type_check
    CHECK (question_type IN ('multi_select', 'free_form', 'jeopardy', 'number_of_games', 'text_box', 'box', 'best_team_name'));

ALTER TABLE question_options ADD COLUMN participant_id INT;
