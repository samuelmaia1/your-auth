ALTER TABLE account_refresh_tokens RENAME COLUMN family_id TO session_id;
DROP INDEX idx_refresh_tokens_family_id;
CREATE INDEX idx_account_refresh_tokens_session_id ON account_refresh_tokens (session_id);

ALTER TABLE user_refresh_tokens RENAME COLUMN family_id TO session_id;
DROP INDEX idx_user_refresh_tokens_family_id;
CREATE INDEX idx_user_refresh_tokens_session_id ON user_refresh_tokens (session_id);
