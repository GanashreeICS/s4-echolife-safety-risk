package com.echolife.s4echolifesafetyrisk.controller;

import com.echolife.s4echolifesafetyrisk.dto.GuardrailCheckRequest;
import com.echolife.s4echolifesafetyrisk.dto.SafetyDecisionResponse;
import com.echolife.s4echolifesafetyrisk.dto.SafetyEventResponse;
import com.echolife.s4echolifesafetyrisk.dto.SafetyInputCheckRequest;
import com.echolife.s4echolifesafetyrisk.dto.SafetyOutputCheckRequest;
import com.echolife.s4echolifesafetyrisk.entity.SafetyEvent;
import com.echolife.s4echolifesafetyrisk.repository.SafetyEventRepository;
import com.echolife.s4echolifesafetyrisk.service.GuardrailService;
import com.echolife.s4echolifesafetyrisk.service.SafetyEvaluationService;
import jakarta.validation.Valid;
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

    public SafetyController(SafetyEvaluationService safetyService,
                            GuardrailService guardrailService,
                            SafetyEventRepository eventRepository) {
        this.safetyService = safetyService;
        this.guardrailService = guardrailService;
        this.eventRepository = eventRepository;
    }

    @PostMapping("/internal/safety/input-check")
    public ResponseEntity<SafetyDecisionResponse> checkInput(@Valid @RequestBody SafetyInputCheckRequest request) {
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