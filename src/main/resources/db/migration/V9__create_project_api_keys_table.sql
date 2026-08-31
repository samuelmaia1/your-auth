CREATE TABLE project_api_keys (
    id VARCHAR(36) NOT NULL,
    project_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    key_id VARCHAR(64) NOT NULL,
    prefix VARCHAR(128) NOT NULL,
    secret_hash VARCHAR(64) NOT NULL,
    secret_last_four VARCHAR(4) NOT NULL,
    environment VARCHAR(30) NOT NULL,
    created_by_account_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    revoked_at TIMESTAMP,
    expires_at TIMESTAMP,
    CONSTRAINT pk_project_api_keys PRIMARY KEY (id),
    CONSTRAINT fk_project_api_keys_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_project_api_keys_created_by_account FOREIGN KEY (created_by_account_id) REFERENCES accounts (id),
    CONSTRAINT uk_project_api_keys_key_id UNIQUE (key_id),
    CONSTRAINT uk_project_api_keys_prefix UNIQUE (prefix)
);

CREATE INDEX idx_project_api_keys_project_id ON project_api_keys (project_id);
CREATE INDEX idx_project_api_keys_created_by_account_id ON project_api_keys (created_by_account_id);

CREATE TABLE project_api_key_scopes (
    project_api_key_id VARCHAR(36) NOT NULL,
    scope VARCHAR(50) NOT NULL,
    CONSTRAINT pk_project_api_key_scopes PRIMARY KEY (project_api_key_id, scope),
    CONSTRAINT fk_project_api_key_scopes_api_key FOREIGN KEY (project_api_key_id)
        REFERENCES project_api_keys (id)
        ON DELETE CASCADE
);
