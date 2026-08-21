ALTER TABLE projects ADD CONSTRAINT uk_projects_owner_account_name UNIQUE (owner_account_id, name);

CREATE TABLE project_members (
    id VARCHAR(36) NOT NULL,
    project_id VARCHAR(36) NOT NULL,
    account_id VARCHAR(36) NOT NULL,
    role VARCHAR(30) NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_project_members PRIMARY KEY (id),
    CONSTRAINT fk_project_members_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_project_members_account FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT uk_project_members_project_account UNIQUE (project_id, account_id)
);

CREATE INDEX idx_project_members_project_id ON project_members (project_id);
CREATE INDEX idx_project_members_account_id ON project_members (account_id);
