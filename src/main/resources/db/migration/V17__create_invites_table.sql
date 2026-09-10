CREATE TABLE invites (
    id VARCHAR(36) NOT NULL,
    sender_account_id VARCHAR(36) NOT NULL,
    recipient_account_id VARCHAR(36) NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    project_id VARCHAR(36) NOT NULL,
    project_name VARCHAR(100) NOT NULL,
    project_description VARCHAR(255),
    CONSTRAINT pk_invites PRIMARY KEY (id),
    CONSTRAINT fk_invites_sender_account FOREIGN KEY (sender_account_id) REFERENCES accounts (id),
    CONSTRAINT fk_invites_recipient_account FOREIGN KEY (recipient_account_id) REFERENCES accounts (id),
    CONSTRAINT fk_invites_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT ck_invites_role CHECK (role IN ('ADMIN', 'DEVELOPER', 'VIEWER')),
    CONSTRAINT ck_invites_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REFUSED'))
);

CREATE INDEX idx_invites_recipient_account_id ON invites (recipient_account_id);
CREATE INDEX idx_invites_project_recipient_status ON invites (project_id, recipient_account_id, status);
