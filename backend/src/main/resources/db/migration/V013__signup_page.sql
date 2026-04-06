ALTER TABLE seasons ADD COLUMN signup_content TEXT;

ALTER TABLE participants ALTER COLUMN name DROP NOT NULL;
ALTER TABLE participants ALTER COLUMN team_name DROP NOT NULL;
