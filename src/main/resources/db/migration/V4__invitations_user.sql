-- Ensure unique invitation per (project_id, email_invited)
ALTER TABLE invitations
    ADD CONSTRAINT uk_invitation_project_email
        UNIQUE (project_id, email_invited);

-- Helpful indexes (fast lookups)
CREATE INDEX idx_inv_project ON invitations(project_id);
CREATE INDEX idx_inv_email   ON invitations(email_invited);
