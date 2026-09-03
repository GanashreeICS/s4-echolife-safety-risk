package com.echolife.s4echolifesafetyrisk.dto;

public record SafetyDecisionResponse(
        boolean allowed,
        String severity,
        String action,
        String reason,
        String replacementMessage
) {}