ALTER TABLE participant_scores
    ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now();

CREATE OR REPLACE FUNCTION participant_scores_touch_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_participant_scores_updated_at
BEFORE UPDATE ON participant_scores
FOR EACH ROW EXECUTE FUNCTION participant_scores_touch_updated_at();
