package com.echolife.s4echolifesafetyrisk.dto;

public record SafetyDecisionResponse(
        boolean allowed,
        Enums.SafetyReason reason,
        Enums.SafetySeverity severity,
        String replacementMessage,
        boolean shouldEscalate
) {}