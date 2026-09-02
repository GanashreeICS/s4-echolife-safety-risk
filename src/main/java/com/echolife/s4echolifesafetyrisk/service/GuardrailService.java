package com.echolife.s4echolifesafetyrisk.service;

import com.echolife.s4echolifesafetyrisk.dto.Enums.SafetyReason;
import com.echolife.s4echolifesafetyrisk.dto.Enums.SafetySeverity;
import com.echolife.s4echolifesafetyrisk.dto.GuardrailCheckRequest;
import com.echolife.s4echolifesafetyrisk.dto.SafetyDecisionResponse;
import com.echolife.s4echolifesafetyrisk.entity.SafetyPolicy;
import com.echolife.s4echolifesafetyrisk.repository.SafetyPolicyRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;

@Service
public class GuardrailService {

    private final SafetyPolicyRepository policyRepository;
    private final StringRedisTemplate redisTemplate;

    public GuardrailService(SafetyPolicyRepository policyRepository, StringRedisTemplate redisTemplate) {
        this.policyRepository = policyRepository;
        this.redisTemplate = redisTemplate;
    }

    public SafetyDecisionResponse checkLimits(GuardrailCheckRequest request) {
        SafetyPolicy policy = policyRepository.findFirstByActiveTrueOrderByVersionDesc()
                .orElseGet(SafetyPolicy::new);

        LocalTime now = LocalTime.now();
        if (isWithinQuietHours(now, policy.getQuietHoursStart(), policy.getQuietHoursEnd())) {
            return new SafetyDecisionResponse(
                    false,
                    SafetyReason.QUIET_HOURS,
                    SafetySeverity.MEDIUM,
                    "Conversations are restricted during quiet hours.",
                    false
            );
        }

        String dailyKey = "usage:" + request.userId() + ":daily_minutes";
        String currentStr = redisTemplate.opsForValue().get(dailyKey);
        int currentMinutes = currentStr != null ? Integer.parseInt(currentStr) : 0;

        if (currentMinutes + request.sessionMinutesRequested() > policy.getDailyMinutesLimit()) {
            return new SafetyDecisionResponse(
                    false,
                    SafetyReason.DAILY_CAP_EXCEEDED,
                    SafetySeverity.HIGH,
                    "Daily usage cap exceeded for this profile.",
                    true
            );
        }

        redisTemplate.opsForValue().increment(dailyKey, request.sessionMinutesRequested());
        redisTemplate.expire(dailyKey, Duration.ofDays(1));

        return new SafetyDecisionResponse(true, SafetyReason.OK, SafetySeverity.LOW, null, false);
    }

    private boolean isWithinQuietHours(LocalTime time, LocalTime start, LocalTime end) {
        if (start.isBefore(end)) {
            return !time.isBefore(start) && time.isBefore(end);
        } else {
            return !time.isBefore(start) || time.isBefore(end);
        }
    }
}