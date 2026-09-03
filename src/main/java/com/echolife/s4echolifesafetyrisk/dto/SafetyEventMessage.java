package com.echolife.s4echolifesafetyrisk.dto;

import java.time.Instant;
import java.util.UUID;

public record SafetyEventMessage(
        UUID eventId,
        String eventType,
        String schemaVersion,
        Instant occurredAt,
        String producer,
        String environment,
        String tenantId,
        String traceId,
        SafetyPayload payload
) {
    public record SafetyPayload(
            String userId,
            String sessionId,
            String category,
            String severity,
            String action,
            String reason
    ) {}
}