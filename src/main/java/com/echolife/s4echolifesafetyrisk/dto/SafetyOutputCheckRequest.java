package com.echolife.s4echolifesafetyrisk.dto;

import jakarta.validation.constraints.NotBlank;

public record SafetyOutputCheckRequest(
        @NotBlank(message = "tenantId is required")
        String tenantId,

        @NotBlank(message = "userId is required")
        String userId,

        String sessionId,

        @NotBlank(message = "generatedContent is required")
        String generatedContent
) {}