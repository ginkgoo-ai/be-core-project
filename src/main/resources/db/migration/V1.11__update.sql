CREATE TABLE IF NOT EXISTS project.talent_comment
(
    id           VARCHAR(255) PRIMARY KEY,
    workspace_id VARCHAR(255) NOT NULL,
    talent_id    VARCHAR(255) NOT NULL,
    content      TEXT         NOT NULL,
    parent_id    VARCHAR(255),
    created_by   VARCHAR(255),
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(255),
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_talent_comment_talent FOREIGN KEY (talent_id) REFERENCES project.talent (id) ON DELETE CASCADE
);

ALTER TABLE project.talent_comment
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR (255);

CREATE INDEX IF NOT EXISTS idx_talent_comment_talent_id ON project.talent_comment (talent_id);
CREATE INDEX IF NOT EXISTS idx_talent_comment_workspace_id ON project.talent_comment (workspace_id);
CREATE INDEX IF NOT EXISTS idx_talent_comment_parent_id ON project.talent_comment (parent_id);
CREATE INDEX IF NOT EXISTS idx_talent_comment_created_at ON project.talent_comment (created_at);