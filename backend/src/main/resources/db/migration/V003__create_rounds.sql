CREATE TABLE rounds (
    id                      SERIAL PRIMARY KEY,
    name                    VARCHAR(255) NOT NULL,
    description             TEXT,
    deadline                TIMESTAMP NOT NULL,
    status                  VARCHAR(20) DEFAULT 'draft' CHECK (status IN ('draft', 'open', 'closed', 'scored')),
    display_order           INT NOT NULL,
    display_with_round_id   INT REFERENCES rounds(id),
    created_at              TIMESTAMP DEFAULT NOW()
);
