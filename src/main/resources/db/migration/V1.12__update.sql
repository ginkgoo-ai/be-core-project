ALTER TABLE project.project_role
    ADD COLUMN workspace_id VARCHAR(255);

UPDATE project.project_role pr
SET workspace_id = (SELECT p.workspace_id
                    FROM project.project p
                    WHERE p.id = pr.project_id);

ALTER TABLE project.project_role
    ALTER COLUMN workspace_id SET NOT NULL;

CREATE INDEX idx_project_role_workspace_id ON project.project_role (workspace_id);