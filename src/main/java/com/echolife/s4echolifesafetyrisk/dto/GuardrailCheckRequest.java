package com.echolife.s4echolifesafetyrisk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record GuardrailCheckRequest(
        @NotBlank String userId,
        @NotBlank String sessionId,
        @Positive int sessionMinutesRequested
) {}