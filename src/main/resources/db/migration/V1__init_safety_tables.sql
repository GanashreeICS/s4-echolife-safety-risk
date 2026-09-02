CREATE TABLE safety_policies (
                                 id UUID PRIMARY KEY,
                                 tenant_id VARCHAR(64) NOT NULL,
                                 daily_minutes_limit INT NOT NULL DEFAULT 20,
                                 quiet_hours_start TIME NOT NULL DEFAULT '22:00:00',
                                 quiet_hours_end TIME NOT NULL DEFAULT '06:00:00',
                                 late_night_threshold INT NOT NULL DEFAULT 3,
                                 repeated_risk_threshold INT NOT NULL DEFAULT 2,
                                 version INT NOT NULL DEFAULT 1,
                                 active BOOLEAN NOT NULL DEFAULT TRUE,
                                 created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE safety_events (
                               id UUID PRIMARY KEY,
                               event_id VARCHAR(64) UNIQUE NOT NULL,
                               user_id VARCHAR(64) NOT NULL,
                               persona_id VARCHAR(64) NOT NULL,
                               session_id VARCHAR(64) NOT NULL,
                               direction VARCHAR(16) NOT NULL,
                               reason VARCHAR(32) NOT NULL,
                               severity VARCHAR(16) NOT NULL,
                               allowed BOOLEAN NOT NULL,
                               should_escalate BOOLEAN NOT NULL,
                               replacement_message TEXT,
                               policy_version INT NOT NULL,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_safety_events_user_id ON safety_events(user_id);