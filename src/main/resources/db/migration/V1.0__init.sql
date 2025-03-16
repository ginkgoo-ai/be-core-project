-- V1__Initial_Schema.sql

CREATE TABLE project.project (
                         id VARCHAR(255) PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         description VARCHAR(255),
                         plot_line VARCHAR(255),
                         status VARCHAR(255) DEFAULT 'DRAFTING' NOT NULL,
                         last_activity_at TIMESTAMP,
                         workspace_id VARCHAR(255),
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         producer VARCHAR(255),
                         created_by VARCHAR(255)
);

CREATE TABLE project.project_role (
                              id VARCHAR(255) PRIMARY KEY,
                              name VARCHAR(255) NOT NULL,
                              character_description VARCHAR(255),
                              self_tape_instructions VARCHAR(255),
                              audition_notes TEXT,
                              age_range VARCHAR(50),
                              gender VARCHAR(20),
                              is_active BOOLEAN DEFAULT TRUE NOT NULL,
                              project_id VARCHAR(255) NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              sides TEXT[],
                              status SMALLINT CHECK (status >= 0 AND status <= 4)
);

CREATE TABLE project.imdb_movie_item (
                                 id VARCHAR(255) PRIMARY KEY,
                                 cover VARCHAR(255),
                                 media_type VARCHAR(255),
                                 rating VARCHAR(255),
                                 role VARCHAR(255),
                                 title VARCHAR(255),
                                 title_url VARCHAR(255),
                                 year VARCHAR(255)
);

CREATE TABLE project.shortlist (
                           id VARCHAR(255) PRIMARY KEY,
                           created_at TIMESTAMP(6),
                           created_by VARCHAR(255),
                           description VARCHAR(255),
                           name VARCHAR(255),
                           owner_id VARCHAR(255),
                           project_id VARCHAR(255),
                           updated_at TIMESTAMP(6),
                           version BIGINT,
                           workspace_id VARCHAR(255)
);

CREATE TABLE project.talent_profile_meta (
                                     id VARCHAR(255) PRIMARY KEY,
                                     created_at TIMESTAMP(6),
                                     created_by VARCHAR(255),
                                     data JSONB,
                                     source VARCHAR(255),
                                     source_url VARCHAR(255),
                                     updated_at TIMESTAMP(6)
);

CREATE TABLE project.talent (
                        id VARCHAR(255) PRIMARY KEY,
                        agency_name VARCHAR(255),
                        agent_email VARCHAR(255),
                        agent_name VARCHAR(255),
                        attributes JSONB,
                        created_at TIMESTAMP(6),
                        created_by VARCHAR(255),
                        email VARCHAR(255),
                        imdb_profile_url VARCHAR(255),
                        known_for_movie_ids TEXT[],
                        name VARCHAR(255),
                        name_suffix VARCHAR(255),
                        personal_details JSONB,
                        profile_photo_url VARCHAR(255),
                        spotlight_profile_url VARCHAR(255),
                        status VARCHAR(255) CHECK (status IN ('DRAFT', 'PENDING_VERIFICATION', 'ACTIVE', 'UNAVAILABLE', 'EXCLUSIVE', 'ARCHIVED', 'BLACKLISTED')),
                        updated_at TIMESTAMP(6),
                        workspace_id VARCHAR(255),
                        profile_meta_id VARCHAR(255)
);

CREATE TABLE project.application (
                             id VARCHAR(255) PRIMARY KEY,
                             agency_name VARCHAR(255),
                             agent_email VARCHAR(255),
                             agent_name VARCHAR(255),
                             created_at TIMESTAMP(6),
                             created_by VARCHAR(255),
                             review_notes VARCHAR(255),
                             reviewed_at TIMESTAMP(6),
                             reviewed_by VARCHAR(255),
                             status VARCHAR(255) CHECK (status IN ('ADDED', 'REQUESTED', 'DECLINED', 'SUBMITTED', 'REVIEWED', 'RETAPE', 'SHORTLISTED')),
                             updated_at TIMESTAMP(6),
                             version BIGINT,
                             workspace_id VARCHAR(255),
                             project_id VARCHAR(255),
                             role_id VARCHAR(255),
                             talent_id VARCHAR(255) UNIQUE
);

CREATE TABLE project.submission (
                            id VARCHAR(255) PRIMARY KEY,
                            created_at TIMESTAMP(6),
                            created_by VARCHAR(255),
                            file_size BIGINT,
                            metadata JSONB,
                            mime_type VARCHAR(255),
                            original_filename VARCHAR(255),
                            processing_error VARCHAR(255),
                            processing_status VARCHAR(255) CHECK (processing_status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
                            updated_at TIMESTAMP(6),
                            video_duration BIGINT,
                            video_resolution VARCHAR(255),
                            video_thumbnail_url VARCHAR(255),
                            video_url VARCHAR(255),
                            workspace_id VARCHAR(255),
                            application_id VARCHAR(255)
);

CREATE TABLE project.application_comment (
                                     id VARCHAR(255) PRIMARY KEY,
                                     content TEXT,
                                     created_at TIMESTAMP(6),
                                     created_by VARCHAR(255),
                                     updated_at TIMESTAMP(6),
                                     application_id VARCHAR(255),
                                     parent_comment_id VARCHAR(255)
);

CREATE TABLE project.application_note (
                                  id VARCHAR(255) PRIMARY KEY,
                                  content TEXT,
                                  created_at TIMESTAMP(6),
                                  created_by VARCHAR(255),
                                  updated_at TIMESTAMP(6),
                                  application_id VARCHAR(255)
);


CREATE TABLE project.submission_comment (
                                    id VARCHAR(255) PRIMARY KEY,
                                    content TEXT,
                                    created_at TIMESTAMP(6),
                                    created_by VARCHAR(255),
                                    updated_at TIMESTAMP(6),
                                    parent_comment_id VARCHAR(255),
                                    submission_id VARCHAR(255),
                                    type VARCHAR(255) NOT NULL CHECK (type IN ('PUBLIC', 'INTERNAL')),
                                    workspace_id VARCHAR(255)
);

CREATE TABLE project.shortlist_item (
                                id VARCHAR(255) PRIMARY KEY,
                                added_at TIMESTAMP(6),
                                added_by VARCHAR(255),
                                notes VARCHAR(255),
                                sort_order INTEGER,
                                shortlist_id VARCHAR(255),
                                submission_id VARCHAR(255)
);

CREATE TABLE project.shortlist_item_submission_mapping (
                                                   shortlist_item_id VARCHAR(36) NOT NULL,
                                                   submission_id VARCHAR(36) NOT NULL,
                                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                   PRIMARY KEY (shortlist_item_id, submission_id)
);

CREATE INDEX idx_shortlist_item_submission_mapping_submission_id ON project.shortlist_item_submission_mapping (submission_id);
CREATE INDEX idx_shortlist_item_submission_mapping_shortlist_item_id ON project.shortlist_item_submission_mapping (shortlist_item_id);

CREATE INDEX idx_project_name ON project.project (name);
CREATE INDEX idx_project_status ON project.project (status);
CREATE INDEX idx_project_workspace_id ON project.project (workspace_id);
CREATE INDEX idx_project_created_at ON project.project (created_at);
CREATE INDEX idx_project_updated_at ON project.project (updated_at);

CREATE INDEX idx_project_role_project_id ON project.project_role (project_id);
CREATE INDEX idx_project_role_name ON project.project_role (name);
CREATE INDEX idx_project_role_created_at ON project.project_role (created_at);
CREATE INDEX idx_project_role_updated_at ON project.project_role (updated_at);

CREATE INDEX idx_talent_name ON project.talent (name);
CREATE INDEX idx_talent_status ON project.talent (status);
CREATE INDEX idx_talent_workspace_id ON project.talent (workspace_id);
CREATE INDEX idx_talent_created_at ON project.talent (created_at);
CREATE INDEX idx_talent_updated_at ON project.talent (updated_at);

CREATE INDEX idx_application_project_id ON project.application (project_id);
CREATE INDEX idx_application_role_id ON project.application (role_id);
CREATE INDEX idx_application_talent_id ON project.application (talent_id);
CREATE INDEX idx_application_status ON project.application (status);
CREATE INDEX idx_application_workspace_id ON project.application (workspace_id);
CREATE INDEX idx_application_created_at ON project.application (created_at);
CREATE INDEX idx_application_updated_at ON project.application (updated_at);

CREATE INDEX idx_submission_application_id ON project.submission (application_id);
CREATE INDEX idx_submission_processing_status ON project.submission (processing_status);
CREATE INDEX idx_submission_workspace_id ON project.submission (workspace_id);
CREATE INDEX idx_submission_created_at ON project.submission (created_at);
CREATE INDEX idx_submission_updated_at ON project.submission (updated_at);

CREATE INDEX idx_application_comment_application_id ON project.application_comment (application_id);
CREATE INDEX idx_application_note_application_id ON project.application_note (application_id);
CREATE INDEX idx_submission_comment_submission_id ON project.submission_comment (submission_id);
CREATE INDEX idx_shortlist_item_shortlist_id ON project.shortlist_item (shortlist_id);
CREATE INDEX idx_shortlist_item_submission_id ON project.shortlist_item (submission_id);
