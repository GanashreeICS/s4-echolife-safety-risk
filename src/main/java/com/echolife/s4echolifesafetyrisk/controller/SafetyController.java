package com.echolife.s4echolifesafetyrisk.controller;

import com.echolife.s4echolifesafetyrisk.dto.GuardrailCheckRequest;
import com.echolife.s4echolifesafetyrisk.dto.SafetyDecisionResponse;
import com.echolife.s4echolifesafetyrisk.dto.SafetyEventResponse;
import com.echolife.s4echolifesafetyrisk.dto.SafetyInputCheckRequest;
import com.echolife.s4echolifesafetyrisk.dto.SafetyOutputCheckRequest;
import com.echolife.s4echolifesafetyrisk.entity.SafetyEvent;
import com.echolife.s4echolifesafetyrisk.repository.SafetyEventRepository;
import com.echolife.s4echolifesafetyrisk.service.GuardrailService;
import com.echolife.s4echolifesafetyrisk.service.RedisRateLimiterService;
import com.echolife.s4echolifesafetyrisk.service.SafetyEvaluationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class SafetyController {

    private final SafetyEvaluationService safetyService;
    private final GuardrailService guardrailService;
    private final SafetyEventRepository eventRepository;
    private final RedisRateLimiterService rateLimiterService;

    // Default rate limit quota: 60 requests per 60 seconds per tenant:user
    private static final long DEFAULT_RATE_LIMIT = 60;
    private static final long RATE_WINDOW_SECONDS = 60;

    public SafetyController(SafetyEvaluationService safetyService,
                            GuardrailService guardrailService,
                            SafetyEventRepository eventRepository,
                            RedisRateLimiterService rateLimiterService) {
        this.safetyService = safetyService;
        this.guardrailService = guardrailService;
        this.eventRepository = eventRepository;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/internal/safety/input-check")
    public ResponseEntity<SafetyDecisionResponse> checkInput(@Valid @RequestBody SafetyInputCheckRequest request) {
        String rateLimitKey = request.tenantId() + ":" + request.userId();
        boolean allowed = rateLimiterService.isAllowed(
                "rate:safety:input",
                rateLimitKey,
                DEFAULT_RATE_LIMIT,
                RATE_WINDOW_SECONDS
        );

        if (!allowed) {
            SafetyDecisionResponse rateLimitResponse = new SafetyDecisionResponse(
                    false,
                    "MEDIUM",
                    "RATE_LIMITED",
                    "Rate limit exceeded. Maximum " + DEFAULT_RATE_LIMIT + " requests per minute allowed.",
                    "Too many requests. Please slow down and try again shortly."
            );
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(rateLimitResponse);
        }

        SafetyDecisionResponse response = safetyService.evaluateInput(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/internal/safety/output-check")
    public ResponseEntity<SafetyDecisionResponse> checkOutput(@Valid @RequestBody SafetyOutputCheckRequest request) {
        SafetyDecisionResponse response = safetyService.evaluateOutput(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/internal/safety/guardrails/check")
    public ResponseEntity<SafetyDecisionResponse> checkGuardrails(@Valid @RequestBody GuardrailCheckRequest request) {
        SafetyDecisionResponse response = guardrailService.checkGuardrails(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/safety/events/{eventId}")
    public ResponseEntity<SafetyEventResponse> getSafetyEvent(@PathVariable UUID eventId) {
        return eventRepository.findById(eventId)
                .map(event -> new SafetyEventResponse(
                        event.getId(),
                        event.getTenantId(),
                        event.getUserId(),
                        event.getSessionId(),
                        event.getCategory(),
                        event.getSeverity(),
                        event.getAction(),
                        event.getReason(),
                        event.getCreatedAt()
                ))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}