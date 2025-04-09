ALTER TABLE project.talent
    ADD COLUMN IF NOT EXISTS first_name VARCHAR (255),
    ADD COLUMN IF NOT EXISTS last_name VARCHAR (255);

UPDATE project.talent
SET first_name = CASE
                     WHEN position(' ' in name) > 0 THEN substring(name from 1 for position(' ' in name) - 1)
                     ELSE name
    END,
    last_name  = CASE
                     WHEN position(' ' in name) > 0 THEN substring(name from position(' ' in name) + 1)
                     ELSE ''
        END
WHERE name IS NOT NULL;

UPDATE project.talent
SET first_name = ''
WHERE first_name IS NULL;

UPDATE project.talent
SET last_name = ''
WHERE last_name IS NULL;

ALTER TABLE project.talent
    ALTER COLUMN first_name SET NOT NULL,
    ALTER COLUMN last_name SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_talent_first_name ON project.talent (first_name);
CREATE INDEX IF NOT EXISTS idx_talent_last_name ON project.talent (last_name);