UPDATE account_subscription_events
SET previous_plan_id = 'pro'
WHERE previous_plan_id = 'business';

UPDATE account_subscription_events
SET new_plan_id = 'pro'
WHERE new_plan_id = 'business';

UPDATE account_subscriptions
SET plan_id = 'pro'
WHERE plan_id = 'business';

DELETE FROM plans
WHERE id = 'business';

ALTER TABLE plans DROP CONSTRAINT ck_plans_code;

ALTER TABLE plans
    ADD CONSTRAINT ck_plans_code CHECK (code IN ('FREE', 'STARTER', 'PRO'));

UPDATE plans
SET description = 'Plano gratuito para testes e projetos iniciais.',
    updated_at = CURRENT_TIMESTAMP
WHERE id = 'free';

UPDATE plans
SET description = 'Plano inicial para pequenos projetos em produção.',
    updated_at = CURRENT_TIMESTAMP
WHERE id = 'starter';

UPDATE plans
SET description = 'Plano para produtos com maior volume de autenticação.',
    updated_at = CURRENT_TIMESTAMP
WHERE id = 'pro';

DELETE FROM plan_features
WHERE plan_id IN ('free', 'starter', 'pro');

INSERT INTO plan_features (id, plan_id, code, description, enabled)
VALUES
    ('free-login-basico', 'free', 'CADASTRO_LOGIN_BASICO', 'Cadastro/login básico', TRUE),
    ('free-verificacao-email', 'free', 'VERIFICACAO_EMAIL', 'Verificação de e-mail', TRUE),
    ('free-refresh-rotation', 'free', 'REFRESH_TOKEN_ROTATION_BASICA', 'Refresh token rotation básica', TRUE),
    ('free-politica-senha-padrao', 'free', 'POLITICA_SENHA_PADRAO', 'Política de senha padrão', TRUE),
    ('free-revoga-sessoes', 'free', 'REVOGACAO_MANUAL_SESSOES', 'Revogação manual de sessões', TRUE),
    ('free-dashboard', 'free', 'DASHBOARD_SIMPLES', 'Dashboard simples', TRUE),
    ('free-logs-24h', 'free', 'LOGS_24H', 'Logs por 24h', TRUE),
    ('starter-tudo-free', 'starter', 'TUDO_DO_FREE', 'Tudo do Free', TRUE),
    ('starter-branches', 'starter', 'CRIACAO_BRANCHES_PROJETOS', 'Criação de branches nos projetos', TRUE),
    ('starter-emails-basicos', 'starter', 'CUSTOMIZACAO_BASICA_EMAILS', 'Customização básica de e-mails', TRUE),
    ('starter-senha-config', 'starter', 'POLITICA_SENHA_CONFIGURAVEL', 'Política de senha configurável', TRUE),
    ('starter-bloqueio', 'starter', 'LIMITE_TENTATIVAS_BLOQUEIO', 'Limite de tentativas e bloqueio', TRUE),
    ('starter-convites', 'starter', 'GESTAO_CONVITES', 'Gestão de convites', TRUE),
    ('starter-papeis', 'starter', 'PAPEIS_ADMIN_DEVELOPER_VIEWER', 'Papéis ADMIN/DEVELOPER/VIEWER', TRUE),
    ('starter-token-exp', 'starter', 'EXPIRACAO_CUSTOMIZADA_TOKENS', 'Expiração customizada de tokens', TRUE),
    ('starter-metricas', 'starter', 'METRICAS_BASICAS', 'Métricas básicas', TRUE),
    ('starter-logs-7d', 'starter', 'LOGS_7_DIAS', 'Logs por 7 dias', TRUE),
    ('pro-tudo-starter', 'pro', 'TUDO_DO_STARTER', 'Tudo do Starter', TRUE),
    ('pro-mfa-totp', 'pro', 'MFA_TOTP', 'MFA/TOTP', TRUE),
    ('pro-sessoes-dispositivo', 'pro', 'SESSOES_POR_DISPOSITIVO', 'Sessões por dispositivo', TRUE),
    ('pro-api-keys-avancadas', 'pro', 'ROTACAO_REVOGACAO_AVANCADA_API_KEYS', 'Rotação/revogação avançada de API keys', TRUE),
    ('pro-webhooks', 'pro', 'WEBHOOKS', 'Webhooks', TRUE),
    ('pro-exportacao-usuarios', 'pro', 'EXPORTACAO_USUARIOS', 'Exportação de usuários', TRUE),
    ('pro-auditoria-30d', 'pro', 'AUDITORIA_30_DIAS', 'Auditoria por 30 dias', TRUE),
    ('pro-rbac-custom', 'pro', 'PERMISSOES_RBAC_CUSTOMIZAVEIS', 'Permissões/RBAC customizáveis', TRUE),
    ('pro-politicas-projeto', 'pro', 'POLITICAS_POR_PROJETO', 'Políticas por projeto', TRUE),
    ('pro-senha-vazada', 'pro', 'DETECCAO_SENHA_VAZADA', 'Detecção de senha vazada', TRUE),
    ('pro-suporte-prioritario', 'pro', 'SUPORTE_PRIORITARIO', 'Suporte prioritário', TRUE);
