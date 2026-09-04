CREATE TABLE plans (
    id VARCHAR(36) NOT NULL,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_plans PRIMARY KEY (id),
    CONSTRAINT uk_plans_code UNIQUE (code),
    CONSTRAINT ck_plans_code CHECK (code IN ('FREE', 'STARTER', 'PRO', 'BUSINESS'))
);

CREATE TABLE plan_features (
    id VARCHAR(36) NOT NULL,
    plan_id VARCHAR(36) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_plan_features PRIMARY KEY (id),
    CONSTRAINT fk_plan_features_plan FOREIGN KEY (plan_id) REFERENCES plans (id) ON DELETE CASCADE,
    CONSTRAINT uk_plan_features_plan_code UNIQUE (plan_id, code)
);

CREATE TABLE plan_limits (
    id VARCHAR(36) NOT NULL,
    plan_id VARCHAR(36) NOT NULL,
    code VARCHAR(100) NOT NULL,
    limit_value BIGINT,
    unit VARCHAR(30),
    period VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_plan_limits PRIMARY KEY (id),
    CONSTRAINT fk_plan_limits_plan FOREIGN KEY (plan_id) REFERENCES plans (id) ON DELETE CASCADE,
    CONSTRAINT uk_plan_limits_plan_code UNIQUE (plan_id, code),
    CONSTRAINT ck_plan_limits_period CHECK (period IN ('NONE', 'DAILY', 'MONTHLY', 'YEARLY')),
    CONSTRAINT ck_plan_limits_value CHECK (limit_value IS NULL OR limit_value >= 0)
);

CREATE TABLE account_subscriptions (
    id VARCHAR(36) NOT NULL,
    account_id VARCHAR(36) NOT NULL,
    plan_id VARCHAR(36) NOT NULL,
    status VARCHAR(30) NOT NULL,
    billing_cycle VARCHAR(30) NOT NULL,
    current_period_start TIMESTAMP,
    current_period_end TIMESTAMP,
    trial_ends_at TIMESTAMP,
    canceled_at TIMESTAMP,
    external_customer_id VARCHAR(120),
    external_subscription_id VARCHAR(120),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_account_subscriptions PRIMARY KEY (id),
    CONSTRAINT fk_account_subscriptions_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE,
    CONSTRAINT fk_account_subscriptions_plan FOREIGN KEY (plan_id) REFERENCES plans (id),
    CONSTRAINT ck_account_subscriptions_status
        CHECK (status IN ('ACTIVE', 'TRIALING', 'PAST_DUE', 'SUSPENDED', 'CANCELED')),
    CONSTRAINT ck_account_subscriptions_billing_cycle
        CHECK (billing_cycle IN ('NONE', 'MONTHLY', 'YEARLY'))
);

CREATE TABLE account_current_subscriptions (
    account_id VARCHAR(36) NOT NULL,
    subscription_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_account_current_subscriptions PRIMARY KEY (account_id),
    CONSTRAINT fk_account_current_subscriptions_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE,
    CONSTRAINT fk_account_current_subscriptions_subscription FOREIGN KEY (subscription_id)
        REFERENCES account_subscriptions (id) ON DELETE CASCADE,
    CONSTRAINT uk_account_current_subscriptions_subscription UNIQUE (subscription_id)
);

CREATE TABLE account_subscription_events (
    id VARCHAR(36) NOT NULL,
    account_id VARCHAR(36) NOT NULL,
    subscription_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    previous_plan_id VARCHAR(36),
    new_plan_id VARCHAR(36),
    occurred_at TIMESTAMP NOT NULL,
    description VARCHAR(255),
    created_by_account_id VARCHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_account_subscription_events PRIMARY KEY (id),
    CONSTRAINT fk_account_subscription_events_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE,
    CONSTRAINT fk_account_subscription_events_subscription FOREIGN KEY (subscription_id)
        REFERENCES account_subscriptions (id),
    CONSTRAINT fk_account_subscription_events_previous_plan FOREIGN KEY (previous_plan_id) REFERENCES plans (id),
    CONSTRAINT fk_account_subscription_events_new_plan FOREIGN KEY (new_plan_id) REFERENCES plans (id),
    CONSTRAINT fk_account_subscription_events_created_by_account FOREIGN KEY (created_by_account_id) REFERENCES accounts (id),
    CONSTRAINT ck_account_subscription_events_type
        CHECK (event_type IN ('CREATED', 'PLAN_CHANGED', 'STATUS_CHANGED', 'CANCELED', 'SUSPENDED', 'REACTIVATED', 'PAYMENT_SYNCED'))
);

CREATE INDEX idx_plan_features_plan_id ON plan_features (plan_id);
CREATE INDEX idx_plan_limits_plan_id ON plan_limits (plan_id);
CREATE INDEX idx_account_subscriptions_account_id ON account_subscriptions (account_id);
CREATE INDEX idx_account_subscriptions_plan_id ON account_subscriptions (plan_id);
CREATE INDEX idx_account_subscription_events_account_id ON account_subscription_events (account_id);
CREATE INDEX idx_account_subscription_events_subscription_id ON account_subscription_events (subscription_id);
CREATE INDEX idx_account_subscription_events_occurred_at ON account_subscription_events (occurred_at);

INSERT INTO plans (
    id,
    code,
    name,
    description,
    active,
    display_order
) VALUES
    ('free', 'FREE', 'Free', 'Plano gratuito para testes e projetos iniciais.', TRUE, 1),
    ('starter', 'STARTER', 'Starter', 'Plano inicial para pequenos projetos em producao.', TRUE, 2),
    ('pro', 'PRO', 'Pro', 'Plano para produtos com maior volume de autenticacao.', TRUE, 3),
    ('business', 'BUSINESS', 'Business', 'Plano para operacoes maiores e necessidades avancadas.', TRUE, 4);

INSERT INTO account_subscriptions (
    id,
    account_id,
    plan_id,
    status,
    billing_cycle,
    current_period_start
)
SELECT
    account.id,
    account.id,
    'free',
    'ACTIVE',
    'NONE',
    CURRENT_TIMESTAMP
FROM accounts AS account;

INSERT INTO account_current_subscriptions (
    account_id,
    subscription_id
)
SELECT
    account.id,
    account.id
FROM accounts AS account;

INSERT INTO account_subscription_events (
    id,
    account_id,
    subscription_id,
    event_type,
    new_plan_id,
    occurred_at,
    description
)
SELECT
    account.id,
    account.id,
    account.id,
    'CREATED',
    'free',
    CURRENT_TIMESTAMP,
    'Assinatura FREE criada automaticamente para conta existente.'
FROM accounts AS account;
