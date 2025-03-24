ALTER TABLE submission ADD COLUMN view_count BIGINT DEFAULT 0;

CREATE TABLE submission_view_record (
    id VARCHAR(36) PRIMARY KEY,
    workspace_id VARCHAR(36) NOT NULL,
    submission_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36),
    ip_address VARCHAR(45),
    user_agent TEXT,
    viewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES submission(id) ON DELETE CASCADE
);

CREATE INDEX idx_submission_view_record_submission_id ON submission_view_record(submission_id);
CREATE INDEX idx_submission_view_record_user_id ON submission_view_record(user_id);
CREATE INDEX idx_submission_view_record_ip_address ON submission_view_record(ip_address); 