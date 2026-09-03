-- Widen varchar columns
ALTER TABLE safety_events ALTER COLUMN action TYPE VARCHAR(64);
ALTER TABLE safety_events ALTER COLUMN severity TYPE VARCHAR(64);
ALTER TABLE safety_events ALTER COLUMN category TYPE VARCHAR(64);
ALTER TABLE safety_events ALTER COLUMN tenant_id TYPE VARCHAR(64);
ALTER TABLE safety_events ALTER COLUMN user_id TYPE VARCHAR(64);
ALTER TABLE safety_events ALTER COLUMN session_id TYPE VARCHAR(64);
ALTER TABLE safety_events ALTER COLUMN reason TYPE TEXT;

-- Drop legacy NOT NULL constraints
ALTER TABLE safety_events ALTER COLUMN persona_id DROP NOT NULL;
ALTER TABLE safety_events ALTER COLUMN event_id DROP NOT NULL;
ALTER TABLE safety_events ALTER COLUMN direction DROP NOT NULL;
ALTER TABLE safety_events ALTER COLUMN allowed DROP NOT NULL;
ALTER TABLE safety_events ALTER COLUMN should_escalate DROP NOT NULL;
ALTER TABLE safety_events ALTER COLUMN policy_version DROP NOT NULL;