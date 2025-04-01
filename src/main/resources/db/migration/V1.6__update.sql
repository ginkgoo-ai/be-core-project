CREATE TABLE IF NOT EXISTS project.shortlist_share
(
    id              VARCHAR(36) PRIMARY KEY,
    shortlist_id    VARCHAR(36)   NOT NULL,
    recipient_id    VARCHAR(36)   NOT NULL,
    recipient_email VARCHAR(255)  NOT NULL,
    recipient_name  VARCHAR(255),
    share_link      VARCHAR(1024) NOT NULL,
    share_code      VARCHAR(255)  NOT NULL,
    expires_at      TIMESTAMP     NOT NULL,
    active          BOOLEAN                DEFAULT TRUE,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(255),
    CONSTRAINT fk_shortlist_share_shortlist FOREIGN KEY (shortlist_id) REFERENCES project.shortlist (id) ON DELETE CASCADE
);

CREATE INDEX idx_shortlist_share_shortlist_id ON project.shortlist_share (shortlist_id);
CREATE INDEX idx_shortlist_share_share_code ON project.shortlist_share (share_code);
CREATE INDEX idx_shortlist_share_recipient_email ON project.shortlist_share (recipient_email);
CREATE INDEX idx_shortlist_share_recipient_name ON project.shortlist_share (recipient_name);
CREATE INDEX idx_shortlist_share_recipient_id ON project.shortlist_share (recipient_id);
CREATE INDEX idx_shortlist_share_active ON project.shortlist_share (active);
