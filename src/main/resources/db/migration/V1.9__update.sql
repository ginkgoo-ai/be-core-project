ALTER TABLE project.project
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR (255);

ALTER TABLE project.project_role
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR (255);

ALTER TABLE project.application
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR (255);

ALTER TABLE project.submission
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR (255);

ALTER TABLE project.talent
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR (255);

ALTER TABLE project.shortlist
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR (255);

ALTER TABLE project.application_comment
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR (255);

ALTER TABLE project.application_note
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR (255);

ALTER TABLE project.submission_comment
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR (255);

ALTER TABLE project.shortlist_item
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR (255);

CREATE INDEX idx_project_deleted ON project.project (deleted);
CREATE INDEX idx_project_role_deleted ON project.project_role (deleted);
CREATE INDEX idx_application_deleted ON project.application (deleted);
CREATE INDEX idx_submission_deleted ON project.submission (deleted);
CREATE INDEX idx_talent_deleted ON project.talent (deleted);
CREATE INDEX idx_shortlist_deleted ON project.shortlist (deleted);
CREATE INDEX idx_application_comment_deleted ON project.application_comment (deleted);
CREATE INDEX idx_application_note_deleted ON project.application_note (deleted);
CREATE INDEX idx_submission_comment_deleted ON project.submission_comment (deleted);
CREATE INDEX idx_shortlist_item_deleted ON project.shortlist_item (deleted);