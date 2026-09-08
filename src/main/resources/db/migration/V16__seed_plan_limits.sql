INSERT INTO plan_limits (id, plan_id, code, limit_value, unit, period)
VALUES
    ('free-max-projects', 'free', 'MAX_PROJECTS', 1, 'COUNT', 'NONE'),
    ('free-max-users-total', 'free', 'MAX_USERS_TOTAL', 100, 'COUNT', 'NONE'),
    ('free-max-active-sessions-total', 'free', 'MAX_ACTIVE_SESSIONS_TOTAL', 200, 'COUNT', 'NONE'),
    ('starter-max-projects', 'starter', 'MAX_PROJECTS', 3, 'COUNT', 'NONE'),
    ('starter-max-users-total', 'starter', 'MAX_USERS_TOTAL', 300, 'COUNT', 'NONE'),
    ('starter-max-active-sessions-total', 'starter', 'MAX_ACTIVE_SESSIONS_TOTAL', 600, 'COUNT', 'NONE'),
    ('pro-max-projects', 'pro', 'MAX_PROJECTS', 5, 'COUNT', 'NONE'),
    ('pro-max-users-total', 'pro', 'MAX_USERS_TOTAL', 1500, 'COUNT', 'NONE'),
    ('pro-max-active-sessions-total', 'pro', 'MAX_ACTIVE_SESSIONS_TOTAL', 10000, 'COUNT', 'NONE'),
    ('business-max-projects', 'business', 'MAX_PROJECTS', 10, 'COUNT', 'NONE'),
    ('business-max-users-total', 'business', 'MAX_USERS_TOTAL', 10000, 'COUNT', 'NONE'),
    ('business-max-active-sessions-total', 'business', 'MAX_ACTIVE_SESSIONS_TOTAL', 100000, 'COUNT', 'NONE');
