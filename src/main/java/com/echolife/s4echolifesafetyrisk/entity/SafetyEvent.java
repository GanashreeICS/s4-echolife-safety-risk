package com.echolife.s4echolifesafetyrisk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "safety_events")
public class SafetyEvent {

    @Id
    private UUID id;

    @Column(name = "event_id", unique = true, nullable = false, length = 64)
    private String eventId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "persona_id", nullable = false, length = 64)
    private String personaId;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Column(nullable = false, length = 16)
    private String direction;

    @Column(nullable = false, length = 32)
    private String reason;

    @Column(nullable = false, length = 16)
    private String severity;

    @Column(nullable = false)
    private boolean allowed;

    @Column(name = "should_escalate", nullable = false)
    private boolean shouldEscalate;

    @Column(name = "replacement_message", columnDefinition = "TEXT")
    private String replacementMessage;

    @Column(name = "policy_version", nullable = false)
    private int policyVersion;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public SafetyEvent() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() { return id; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPersonaId() { return personaId; }
    public void setPersonaId(String personaId) { this.personaId = personaId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public boolean isAllowed() { return allowed; }
    public void setAllowed(boolean allowed) { this.allowed = allowed; }
    public boolean isShouldEscalate() { return shouldEscalate; }
    public void setShouldEscalate(boolean shouldEscalate) { this.shouldEscalate = shouldEscalate; }
    public String getReplacementMessage() { return replacementMessage; }
    public void setReplacementMessage(String replacementMessage) { this.replacementMessage = replacementMessage; }
    public int getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(int policyVersion) { this.policyVersion = policyVersion; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}