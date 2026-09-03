package com.echolife.s4echolifesafetyrisk.dto;

import jakarta.validation.constraints.NotBlank;

public record SafetyOutputCheckRequest(
        @NotBlank String tenantId,
        @NotBlank String userId,
        @NotBlank String sessionId,
        @NotBlank String generatedContent,
        String originalPrompt
) {}