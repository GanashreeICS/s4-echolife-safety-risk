package com.echolife.s4echolifesafetyrisk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "safety_policies")
public class SafetyPolicy {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "daily_minutes_limit", nullable = false)
    private int dailyMinutesLimit = 20;

    @Column(name = "quiet_hours_start", nullable = false)
    private LocalTime quietHoursStart = LocalTime.of(22, 0);

    @Column(name = "quiet_hours_end", nullable = false)
    private LocalTime quietHoursEnd = LocalTime.of(6, 0);

    @Column(name = "late_night_threshold", nullable = false)
    private int lateNightThreshold = 3;

    @Column(name = "repeated_risk_threshold", nullable = false)
    private int repeatedRiskThreshold = 2;

    @Column(nullable = false)
    private int version = 1;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public SafetyPolicy() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() { return id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public int getDailyMinutesLimit() { return dailyMinutesLimit; }
    public void setDailyMinutesLimit(int dailyMinutesLimit) { this.dailyMinutesLimit = dailyMinutesLimit; }
    public LocalTime getQuietHoursStart() { return quietHoursStart; }
    public void setQuietHoursStart(LocalTime quietHoursStart) { this.quietHoursStart = quietHoursStart; }
    public LocalTime getQuietHoursEnd() { return quietHoursEnd; }
    public void setQuietHoursEnd(LocalTime quietHoursEnd) { this.quietHoursEnd = quietHoursEnd; }
    public int getLateNightThreshold() { return lateNightThreshold; }
    public void setLateNightThreshold(int lateNightThreshold) { this.lateNightThreshold = lateNightThreshold; }
    public int getRepeatedRiskThreshold() { return repeatedRiskThreshold; }
    public void setRepeatedRiskThreshold(int repeatedRiskThreshold) { this.repeatedRiskThreshold = repeatedRiskThreshold; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}