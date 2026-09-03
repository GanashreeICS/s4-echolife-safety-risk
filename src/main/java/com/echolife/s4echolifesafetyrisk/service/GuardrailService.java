package com.echolife.s4echolifesafetyrisk.service;

import com.echolife.s4echolifesafetyrisk.dto.GuardrailCheckRequest;
import com.echolife.s4echolifesafetyrisk.dto.SafetyDecisionResponse;
import com.echolife.s4echolifesafetyrisk.entity.SafetyPolicy;
import com.echolife.s4echolifesafetyrisk.redis.SafetyCounterService;
import com.echolife.s4echolifesafetyrisk.repository.SafetyPolicyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class GuardrailService {

    private final SafetyPolicyRepository policyRepository;
    private final SafetyCounterService counterService;

    public GuardrailService(SafetyPolicyRepository policyRepository, SafetyCounterService counterService) {
        this.policyRepository = policyRepository;
        this.counterService = counterService;
    }

    public SafetyDecisionResponse checkGuardrails(GuardrailCheckRequest request) {
        SafetyPolicy policy = policyRepository
                .findFirstByTenantIdAndActiveTrueOrderByVersionDesc(request.tenantId())
                .orElse(null);

        if (policy == null) {
            return new SafetyDecisionResponse(
                    true,
                    "LOW",
                    "ALLOW",
                    "No active safety policy found for tenant; default allow",
                    null
            );
        }

        LocalTime now = LocalTime.now();

        // 1. Quiet Hours Enforcement
        if (policy.getQuietHoursStart() != null && policy.getQuietHoursEnd() != null) {
            boolean inQuietHours = isTimeWithinRange(now, policy.getQuietHoursStart(), policy.getQuietHoursEnd());
            if (inQuietHours) {
                return new SafetyDecisionResponse(
                        false,
                        "MEDIUM",
                        "BLOCK",
                        "Operation blocked: active quiet hours window",
                        "Quiet hours are currently active. Please try again later."
                );
            }
        }

        // 2. Late-Night Fatigue / Continuous Session Threshold
        if (policy.getLateNightThreshold() != null && policy.getLateNightThreshold() > 0) {
            LocalTime lateNightStart = LocalTime.of(23, 0);
            LocalTime lateNightEnd = LocalTime.of(5, 0);
            if (isTimeWithinRange(now, lateNightStart, lateNightEnd)) {
                long currentUsage = counterService.getDailyMinutes(request.tenantId(), request.userId());
                if (currentUsage >= policy.getLateNightThreshold()) {
                    return new SafetyDecisionResponse(
                            false,
                            "HIGH",
                            "MANDATORY_BREAK",
                            "Excessive late-night continuous session detected",
                            "You have reached the late-night continuous usage threshold. Please take a rest."
                    );
                }
            }
        }

        // 3. Atomic Daily Minute Cap
        if (policy.getDailyMinutesLimit() != null && policy.getDailyMinutesLimit() > 0) {
            int sessionIncrement = request.requestedMinutes() > 0 ? request.requestedMinutes() : 1;
            long totalMinutesToday = counterService.incrementAndGetDailyMinutes(
                    request.tenantId(), request.userId(), sessionIncrement
            );

            if (totalMinutesToday > policy.getDailyMinutesLimit()) {
                return new SafetyDecisionResponse(
                        false,
                        "HIGH",
                        "BLOCK",
                        "Daily limit of " + policy.getDailyMinutesLimit() + " minutes exceeded",
                        "You have reached your daily usage quota."
                );
            }
        }

        return new SafetyDecisionResponse(
                true,
                "LOW",
                "ALLOW",
                "All guardrails passed successfully",
                null
        );
    }

    private boolean isTimeWithinRange(LocalTime current, LocalTime start, LocalTime end) {
        if (start.isBefore(end)) {
            return !current.isBefore(start) && current.isBefore(end);
        } else {
            return !current.isBefore(start) || current.isBefore(end);
        }
    }
}