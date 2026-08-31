CREATE TABLE users (
    id VARCHAR(36) NOT NULL,
    project_id VARCHAR(36) NOT NULL,
    email VARCHAR(320) NOT NULL,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    last_password_changed_at TIMESTAMP,
    last_failed_login_at TIMESTAMP,
    failed_login_attempts INTEGER,
    locked_until TIMESTAMP,
    last_login_ip_address VARCHAR(45),
    last_login_user_agent VARCHAR(512),
    phone_ddd VARCHAR(5),
    phone_number VARCHAR(20),
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT fk_users_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT uk_users_project_email UNIQUE (project_id, email)
);

CREATE INDEX idx_users_project_id ON users (project_id);
CREATE INDEX idx_users_project_email ON users (project_id, email);
