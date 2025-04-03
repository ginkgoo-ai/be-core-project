ALTER TABLE project_role
    DROP COLUMN status;

ALTER TABLE project_role
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFTING';

ALTER TABLE project.talent
    ADD COLUMN IF NOT EXISTS contacts JSONB DEFAULT '[]';

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