ALTER TABLE project.project
    DROP COLUMN IF EXISTS last_activity_at;

ALTER TABLE project.application
    ADD COLUMN IF NOT EXISTS created_by VARCHAR (255);
ALTER TABLE project.application_comment
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR (255);
ALTER TABLE project.application_note
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR (255);
ALTER TABLE project.shortlist
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR (255);
ALTER TABLE project.shortlist_item
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR (255);
ALTER TABLE project.submission
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR (255);
ALTER TABLE project.submission_comment
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR (255);

ALTER TABLE project.submission_view_record
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;
ALTER TABLE project.submission_view_record
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE project.submission_view_record
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR (255);
