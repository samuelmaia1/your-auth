ALTER TABLE accounts ADD COLUMN avatar_url VARCHAR(2048);

ALTER TABLE accounts ALTER COLUMN name DROP NOT NULL;
ALTER TABLE accounts ALTER COLUMN last_name DROP NOT NULL;
ALTER TABLE accounts ALTER COLUMN password DROP NOT NULL;
ALTER TABLE accounts ALTER COLUMN cpf DROP NOT NULL;

CREATE TABLE social_identities (
    id VARCHAR(36) NOT NULL,
    account_id VARCHAR(36) NOT NULL,
    provider VARCHAR(30) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    provider_email VARCHAR(320) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_social_identities PRIMARY KEY (id),
    CONSTRAINT fk_social_identities_account
        FOREIGN KEY (account_id)
            REFERENCES accounts (id)
            ON DELETE CASCADE,
    CONSTRAINT uk_social_identities_provider_user
        UNIQUE (provider, provider_user_id),
    CONSTRAINT uk_social_identities_account_provider
        UNIQUE (account_id, provider),
    CONSTRAINT ck_social_identities_provider
        CHECK (provider IN ('GOOGLE', 'GITHUB'))
);

CREATE INDEX idx_social_identities_account_id
    ON social_identities (account_id);

CREATE TABLE social_login_codes (
    id VARCHAR(36) NOT NULL,
    account_id VARCHAR(36) NOT NULL,
    hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_social_login_codes PRIMARY KEY (id),
    CONSTRAINT fk_social_login_codes_account
        FOREIGN KEY (account_id)
            REFERENCES accounts (id)
            ON DELETE CASCADE,
    CONSTRAINT uk_social_login_codes_hash
        UNIQUE (hash)
);

CREATE INDEX idx_social_login_codes_account_id
    ON social_login_codes (account_id);
CREATE INDEX idx_social_login_codes_expires_at
    ON social_login_codes (expires_at);
