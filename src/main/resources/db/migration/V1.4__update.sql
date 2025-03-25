ALTER TABLE project.project ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE project.project_role ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE project.project_role ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

ALTER TABLE project.project_nda ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE project.project_nda ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

ALTER TABLE project.project_member ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE project.project_member ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

ALTER TABLE project.imdb_movie_item ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
ALTER TABLE project.imdb_movie_item ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE project.imdb_movie_item ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE project.imdb_movie_item ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

ALTER TABLE project.shortlist ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE project.talent_profile_meta ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE project.talent ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE project.application ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE project.submission ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE project.application_comment ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE project.application_note ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE project.submission_comment ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE project.shortlist_item ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

ALTER TABLE project.shortlist_item_submission_mapping ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE project.shortlist_item_submission_mapping ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE project.shortlist_item_submission_mapping ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

ALTER TABLE project.submission_view_record ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE project.submission_view_record ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE project.submission_view_record ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

ALTER TABLE submission_view_record ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE submission_view_record ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE submission_view_record ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255); 