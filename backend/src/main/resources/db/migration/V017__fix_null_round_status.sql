UPDATE rounds SET status = 'draft' WHERE status IS NULL;
ALTER TABLE rounds ALTER COLUMN status SET NOT NULL;
