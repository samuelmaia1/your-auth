CREATE TABLE auth_config (
    id VARCHAR(36) NOT NULL,
    project_id VARCHAR(36) NOT NULL,

    access_token_expiration_minutes INTEGER NOT NULL,
    refresh_token_expiration_days INTEGER NOT NULL,
    session_mode VARCHAR(40) NOT NULL,
    max_active_sessions INTEGER,

    refresh_token_rotation_enabled BOOLEAN NOT NULL,
    revoke_tokens_on_password_change BOOLEAN NOT NULL,
    failed_login_attempts_limit INTEGER NOT NULL,
    lock_duration_minutes INTEGER NOT NULL,
    require_email_verification BOOLEAN NOT NULL,
    registration_enabled BOOLEAN NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_auth_config
        PRIMARY KEY (id),

    CONSTRAINT uk_auth_config_project
        UNIQUE (project_id),

    CONSTRAINT fk_auth_config_project
        FOREIGN KEY (project_id)
            REFERENCES projects (id)
            ON DELETE CASCADE,

    CONSTRAINT ck_auth_config_access_token_expiration
        CHECK (access_token_expiration_minutes BETWEEN 1 AND 1440),

    CONSTRAINT ck_auth_config_refresh_token_expiration
        CHECK (refresh_token_expiration_days BETWEEN 1 AND 365),

    CONSTRAINT ck_auth_config_session_mode
        CHECK (session_mode IN ('MULTIPLE_DEVICES', 'SINGLE_ACTIVE_SESSION', 'LIMITED_ACTIVE_SESSIONS')),

    CONSTRAINT ck_auth_config_max_active_sessions
        CHECK (max_active_sessions IS NULL OR max_active_sessions BETWEEN 1 AND 100),

    CONSTRAINT ck_auth_config_limited_sessions_requires_limit
        CHECK (session_mode <> 'LIMITED_ACTIVE_SESSIONS' OR max_active_sessions IS NOT NULL),

    CONSTRAINT ck_auth_config_non_limited_sessions_without_limit
        CHECK (session_mode = 'LIMITED_ACTIVE_SESSIONS' OR max_active_sessions IS NULL),

    CONSTRAINT ck_auth_config_failed_login_attempts
        CHECK (failed_login_attempts_limit BETWEEN 1 AND 20),

    CONSTRAINT ck_auth_config_lock_duration
        CHECK (lock_duration_minutes BETWEEN 1 AND 1440)
);

INSERT INTO auth_config (
    id,
    project_id,
    access_token_expiration_minutes,
    refresh_token_expiration_days,
    session_mode,
    max_active_sessions,
    refresh_token_rotation_enabled,
    revoke_tokens_on_password_change,
    failed_login_attempts_limit,
    lock_duration_minutes,
    require_email_verification,
    registration_enabled
)
SELECT
    project.id,
    project.id,
    15,
    7,
    'MULTIPLE_DEVICES',
    NULL,
    TRUE,
    TRUE,
    5,
    15,
    FALSE,
    TRUE
FROM projects AS project;
