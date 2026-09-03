CREATE TABLE IF NOT EXISTS outbox_events (
                                             id UUID PRIMARY KEY,
                                             aggregate_type VARCHAR(64) NOT NULL,
                                             aggregate_id VARCHAR(64) NOT NULL,
                                             event_type VARCHAR(64) NOT NULL,
                                             payload TEXT NOT NULL,
                                             trace_id VARCHAR(64),
                                             status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
                                             retry_count INT NOT NULL DEFAULT 0,
                                             created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                             processed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_outbox_status_created ON outbox_events(status, created_at);

ALTER TABLE safety_events ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_safety_events_user_tenant_created ON safety_events(tenant_id, user_id, created_at DESC);

