CREATE TABLE projects (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    owner_account_id VARCHAR(36) NOT NULL,
    status VARCHAR(30) NOT NULL,
    environment VARCHAR(30) NOT NULL,
    token_audience VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_projects PRIMARY KEY (id),
    CONSTRAINT fk_projects_owner_account FOREIGN KEY (owner_account_id) REFERENCES accounts (id)
);

CREATE INDEX idx_projects_owner_account_id ON projects (owner_account_id);
