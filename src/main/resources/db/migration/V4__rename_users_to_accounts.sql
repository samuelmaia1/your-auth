DROP INDEX idx_refresh_tokens_user_id;

ALTER TABLE refresh_tokens DROP CONSTRAINT fk_refresh_tokens_user;

ALTER TABLE users RENAME TO accounts;

ALTER TABLE accounts RENAME CONSTRAINT pk_users TO pk_accounts;
ALTER TABLE accounts RENAME CONSTRAINT uk_users_email TO uk_accounts_email;
ALTER TABLE accounts RENAME CONSTRAINT uk_users_cpf TO uk_accounts_cpf;

ALTER TABLE refresh_tokens RENAME COLUMN user_id TO account_id;

ALTER TABLE refresh_tokens ADD CONSTRAINT fk_refresh_tokens_account FOREIGN KEY (account_id) REFERENCES accounts (id);

CREATE INDEX idx_refresh_tokens_account_id ON refresh_tokens (account_id);
