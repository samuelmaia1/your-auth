CREATE TABLE user_refresh_tokens (
    id VARCHAR(36) NOT NULL,
    project_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    hash VARCHAR(255) NOT NULL,
    family_id VARCHAR(36) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_agent VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_user_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uk_user_refresh_tokens_hash UNIQUE (hash),
    CONSTRAINT fk_user_refresh_tokens_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_user_refresh_tokens_project_user_id ON user_refresh_tokens (project_id, user_id);
CREATE INDEX idx_user_refresh_tokens_family_id ON user_refresh_tokens (family_id);
