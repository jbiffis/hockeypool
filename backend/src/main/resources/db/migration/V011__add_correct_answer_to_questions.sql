ALTER TABLE questions ADD COLUMN correct_answer_text VARCHAR(500);

-- Round 2 correct answers
UPDATE questions SET correct_answer_text = 'Winnipeg Jets' WHERE id = 2;
UPDATE questions SET correct_answer_text = '7' WHERE id = 3;
UPDATE questions SET correct_answer_text = 'Dallas Stars' WHERE id = 4;
UPDATE questions SET correct_answer_text = '7' WHERE id = 5;
UPDATE questions SET correct_answer_text = 'Vegas Golden Knights' WHERE id = 6;
UPDATE questions SET correct_answer_text = '6' WHERE id = 7;
UPDATE questions SET correct_answer_text = 'Edmonton Oilers' WHERE id = 8;
UPDATE questions SET correct_answer_text = '6' WHERE id = 9;
UPDATE questions SET correct_answer_text = 'Toronto Maple Leafs' WHERE id = 10;
UPDATE questions SET correct_answer_text = '6' WHERE id = 11;
UPDATE questions SET correct_answer_text = 'Florida Panthers' WHERE id = 12;
UPDATE questions SET correct_answer_text = '5' WHERE id = 13;
UPDATE questions SET correct_answer_text = 'Washington Capitals' WHERE id = 14;
UPDATE questions SET correct_answer_text = '5' WHERE id = 15;
UPDATE questions SET correct_answer_text = 'Carolina Hurricanes' WHERE id = 16;
UPDATE questions SET correct_answer_text = '5' WHERE id = 17;
UPDATE questions SET correct_answer_text = '3' WHERE id = 18;
UPDATE questions SET correct_answer_text = 'Team Blue' WHERE id = 19;
UPDATE questions SET correct_answer_text = 'Matthew Tkachuk - FLA' WHERE id = 20;
-- Q21 (Keepin up with the Jones) - no correct answer, stays NULL
