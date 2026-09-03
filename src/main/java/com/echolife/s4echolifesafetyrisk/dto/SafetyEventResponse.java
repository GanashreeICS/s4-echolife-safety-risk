package com.echolife.s4echolifesafetyrisk.dto;

import java.time.Instant;
import java.util.UUID;

public record SafetyEventResponse(
        UUID id,
        String tenantId,
        String userId,
        String sessionId,
        String category,
        String severity,
        String action,
        String reason,
        Instant createdAt
) {}