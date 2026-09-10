ALTER TABLE invites ADD COLUMN sender_account_email VARCHAR(255);
ALTER TABLE invites ADD COLUMN recipient_account_email VARCHAR(255);

UPDATE invites
SET sender_account_email = (
        SELECT email
        FROM accounts
        WHERE accounts.id = invites.sender_account_id
    ),
    recipient_account_email = (
        SELECT email
        FROM accounts
        WHERE accounts.id = invites.recipient_account_id
    );

ALTER TABLE invites ALTER COLUMN sender_account_email SET NOT NULL;
ALTER TABLE invites ALTER COLUMN recipient_account_email SET NOT NULL;

CREATE INDEX idx_invites_recipient_status ON invites (recipient_account_id, status);
CREATE INDEX idx_invites_project_status ON invites (project_id, status);
