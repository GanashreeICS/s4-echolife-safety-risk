package com.echolife.s4echolifesafetyrisk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SafetyCheckRequest(
        @NotBlank String userId,
        @NotBlank String personaId,
        @NotBlank String sessionId,
        @NotBlank String text,
        @NotNull Enums.SafetyDirection direction
) {}