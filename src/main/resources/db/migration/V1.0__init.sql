CREATE TABLE project
(
    id               VARCHAR(36) PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    description      TEXT,
    plot_line        TEXT,
    status           VARCHAR(20)  NOT NULL DEFAULT 'DRAFTING',
    owner_id         VARCHAR(36)  NOT NULL,
    last_activity_at TIMESTAMP,
    workspace_id     VARCHAR(36) ,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_project_workspace_id ON project (workspace_id);
CREATE INDEX idx_project_name ON project (name);
CREATE INDEX idx_project_status ON project (status);
CREATE INDEX idx_project_owner_id ON project (owner_id);
CREATE INDEX idx_project_updated_at ON project (updated_at);
CREATE INDEX idx_project_created_at ON project (created_at);

CREATE TABLE project_role
(
    id                     VARCHAR(36) PRIMARY KEY,
    name                   VARCHAR(100) NOT NULL,
    character_description  TEXT,
    self_tape_instructions TEXT,
    audition_notes         TEXT,
    age_range              VARCHAR(50),
    gender                 VARCHAR(20),
    is_active              BOOLEAN      NOT NULL DEFAULT TRUE,
    project_id             VARCHAR(36)  NOT NULL,
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP             DEFAULT CURRENT_TIMESTAMP
);


CREATE INDEX idx_project_role_project_id ON project_role (project_id);
CREATE INDEX idx_project_role_name ON project_role (name);
CREATE INDEX idx_project_role_updated_at ON project_role (updated_at);
CREATE INDEX idx_project_role_created_at ON project_role (created_at);

CREATE TABLE project_nda
(
    id            VARCHAR(36) PRIMARY KEY,
    requires_nda  BOOLEAN     NOT NULL,
    apply_to_all  BOOLEAN     NOT NULL,
    version       VARCHAR(50) NOT NULL,
    full_name     TEXT,
    title         TEXT,
    company       TEXT,
    signature_url TEXT,
    project_id    VARCHAR(36) NOT NULL,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP            DEFAULT CURRENT_TIMESTAMP
);


CREATE INDEX idx_project_nda_project_id ON project_nda (project_id);
CREATE INDEX idx_project_nda_updated_at ON project_nda (updated_at);
CREATE INDEX idx_project_nda_created_at ON project_nda (created_at);

CREATE TABLE project_member
(
    id         VARCHAR(36) PRIMARY KEY,
    user_id    VARCHAR(36) NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    project_id VARCHAR(36) NOT NULL,
    role_id    VARCHAR(36),
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP            DEFAULT CURRENT_TIMESTAMP
);


CREATE INDEX idx_project_member_project_id ON project_member (project_id);
CREATE INDEX idx_project_member_user_id ON project_member (user_id);
CREATE INDEX idx_project_member_status ON project_member (status);
CREATE INDEX idx_project_member_role_id ON project_member (role_id);
CREATE INDEX idx_project_member_updated_at ON project_member (updated_at);
CREATE INDEX idx_project_member_created_at ON project_member (created_at);

CREATE TABLE project_activity
(
    id            VARCHAR(36) PRIMARY KEY,
    activity_type VARCHAR(50) NOT NULL,
    status        VARCHAR(50) NOT NULL,
    description   TEXT,
    project_id    VARCHAR(36) NOT NULL,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP            DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_project_activity_project_id ON project_activity (project_id);
CREATE INDEX idx_project_activity_created_at ON project_activity (created_at);
CREATE INDEX idx_project_activity_status ON project_activity (status);
CREATE INDEX idx_project_activity_type ON project_activity (activity_type);
CREATE INDEX idx_project_activity_updated_at ON project_activity (updated_at);
