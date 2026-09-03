package com.echolife.s4echolifesafetyrisk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "safety_policies")
public class SafetyPolicy {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "daily_minutes_limit")
    private Integer dailyMinutesLimit;

    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;

    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;

    @Column(name = "late_night_threshold")
    private Integer lateNightThreshold;

    @Column(name = "repeated_risk_threshold")
    private Integer repeatedRiskThreshold;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public SafetyPolicy() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Integer getDailyMinutesLimit() { return dailyMinutesLimit; }
    public void setDailyMinutesLimit(Integer dailyMinutesLimit) { this.dailyMinutesLimit = dailyMinutesLimit; }

    public LocalTime getQuietHoursStart() { return quietHoursStart; }
    public void setQuietHoursStart(LocalTime quietHoursStart) { this.quietHoursStart = quietHoursStart; }

    public LocalTime getQuietHoursEnd() { return quietHoursEnd; }
    public void setQuietHoursEnd(LocalTime quietHoursEnd) { this.quietHoursEnd = quietHoursEnd; }

    public Integer getLateNightThreshold() { return lateNightThreshold; }
    public void setLateNightThreshold(Integer lateNightThreshold) { this.lateNightThreshold = lateNightThreshold; }

    public Integer getRepeatedRiskThreshold() { return repeatedRiskThreshold; }
    public void setRepeatedRiskThreshold(Integer repeatedRiskThreshold) { this.repeatedRiskThreshold = repeatedRiskThreshold; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}