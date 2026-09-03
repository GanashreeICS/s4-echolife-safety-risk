package com.echolife.s4echolifesafetyrisk.dto;

import jakarta.validation.constraints.NotBlank;

public record SafetyInputCheckRequest(
        @NotBlank String tenantId,
        @NotBlank String userId,
        @NotBlank String sessionId,
        @NotBlank String content
) {}