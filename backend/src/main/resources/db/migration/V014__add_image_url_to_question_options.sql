-- Add image_url column to question_options for displaying team logos, player photos, etc.
ALTER TABLE question_options ADD COLUMN image_url TEXT;
