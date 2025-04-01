ALTER TABLE project_role
    DROP COLUMN status;

ALTER TABLE project_role
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFTING';

ALTER TABLE project.talent
    ADD COLUMN IF NOT EXISTS first_name VARCHAR (255),
    ADD COLUMN IF NOT EXISTS last_name VARCHAR (255),
    ADD COLUMN IF NOT EXISTS contacts JSONB DEFAULT '[]';

UPDATE project.talent
SET first_name = SPLIT_PART(name, ' ', 1),
    last_name  = SUBSTRING(name FROM POSITION(' ' IN name) + 1)
WHERE name IS NOT NULL;

UPDATE project.talent
SET contacts = jsonb_build_array(
        jsonb_build_object(
                'fullName', agent_name,
                'email', agent_email,
                'role', 'Agent',
                'countryCode', '',
                'phoneNumber', ''
        ))
WHERE agent_name IS NOT NULL
   OR agent_email IS NOT NULL;