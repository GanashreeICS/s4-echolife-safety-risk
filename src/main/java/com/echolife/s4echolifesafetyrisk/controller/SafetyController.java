package com.echolife.s4echolifesafetyrisk.controller;

import com.echolife.s4echolifesafetyrisk.dto.GuardrailCheckRequest;
import com.echolife.s4echolifesafetyrisk.dto.SafetyCheckRequest;
import com.echolife.s4echolifesafetyrisk.dto.SafetyDecisionResponse;
import com.echolife.s4echolifesafetyrisk.service.GuardrailService;
import com.echolife.s4echolifesafetyrisk.service.SafetyEvaluationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/safety")
public class SafetyController {

    private final SafetyEvaluationService evaluationService;
    private final GuardrailService guardrailService;

    public SafetyController(SafetyEvaluationService evaluationService, GuardrailService guardrailService) {
        this.evaluationService = evaluationService;
        this.guardrailService = guardrailService;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<SafetyDecisionResponse> evaluate(@Valid @RequestBody SafetyCheckRequest request) {
        SafetyDecisionResponse response = evaluationService.evaluate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/guardrails/check")
    public ResponseEntity<SafetyDecisionResponse> checkGuardrails(@Valid @RequestBody GuardrailCheckRequest request) {
        SafetyDecisionResponse response = guardrailService.checkLimits(request);
        return ResponseEntity.ok(response);
    }
}