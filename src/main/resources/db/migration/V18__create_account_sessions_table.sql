CREATE TABLE account_sessions (
    id VARCHAR(36) NOT NULL,
    account_id VARCHAR(36) NOT NULL,
    device_name VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    revoked_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_account_sessions PRIMARY KEY (id),
    CONSTRAINT fk_account_sessions_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE
);

CREATE INDEX idx_account_sessions_account_id ON account_sessions (account_id);
CREATE INDEX idx_account_sessions_revoked_at ON account_sessions (revoked_at);

INSERT INTO account_sessions (
    id,
    account_id,
    user_agent,
    created_at,
    last_used_at,
    revoked_at,
    version
)
SELECT
    session_id,
    MIN(account_id),
    MIN(user_agent),
    MIN(created_at),
    MAX(created_at),
    CASE
        WHEN SUM(CASE WHEN revoked_at IS NULL THEN 1 ELSE 0 END) = 0 THEN MAX(revoked_at)
        ELSE NULL
    END,
    0
FROM account_refresh_tokens
GROUP BY session_id;

ALTER TABLE account_refresh_tokens
    ADD CONSTRAINT fk_account_refresh_tokens_session
    FOREIGN KEY (session_id) REFERENCES account_sessions (id) ON DELETE CASCADE;
