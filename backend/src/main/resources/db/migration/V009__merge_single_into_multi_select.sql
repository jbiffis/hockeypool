-- Add max_selections column (NULL means unlimited)
ALTER TABLE questions ADD COLUMN max_selections INT;

-- Convert existing single_select questions to multi_select with max_selections = 1
UPDATE questions SET question_type = 'multi_select', max_selections = 1 WHERE question_type = 'single_select';

-- Update the CHECK constraint to remove single_select
ALTER TABLE questions DROP CONSTRAINT IF EXISTS questions_question_type_check;
ALTER TABLE questions ADD CONSTRAINT questions_question_type_check
    CHECK (question_type IN ('multi_select', 'free_form', 'jeopardy'));

-- Add subtext column to question_options
ALTER TABLE question_options ADD COLUMN subtext TEXT;
