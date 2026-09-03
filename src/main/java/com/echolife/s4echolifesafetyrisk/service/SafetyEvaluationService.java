package com.echolife.s4echolifesafetyrisk.service;

import com.echolife.s4echolifesafetyrisk.dto.SafetyDecisionResponse;
import com.echolife.s4echolifesafetyrisk.dto.SafetyInputCheckRequest;
import com.echolife.s4echolifesafetyrisk.dto.SafetyOutputCheckRequest;
import com.echolife.s4echolifesafetyrisk.entity.SafetyEvent;
import com.echolife.s4echolifesafetyrisk.repository.SafetyEventRepository;
import com.echolife.s4echolifesafetyrisk.rules.SafetyRule;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SafetyEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(SafetyEvaluationService.class);

    private final List<SafetyRule> safetyRules;
    private final SafetyEventRepository eventRepository;
    private final Tracer tracer;

    public SafetyEvaluationService(List<SafetyRule> safetyRules,
                                   SafetyEventRepository eventRepository,
                                   Tracer tracer) {
        this.safetyRules = safetyRules;
        this.eventRepository = eventRepository;
        this.tracer = tracer;
    }

    public SafetyDecisionResponse evaluateInput(SafetyInputCheckRequest request) {
        log.info("Evaluating safety input for tenant: {}, user: {}, traceId: {}",
                request.tenantId(), request.userId(), getTraceId());

        for (SafetyRule rule : safetyRules) {
            Optional<SafetyRule.RuleViolation> violation = rule.evaluate(request.content());
            if (violation.isPresent()) {
                SafetyRule.RuleViolation v = violation.get();
                log.warn("Input safety rule violation caught [{}]: {}", v.severity(), v.reason());

                persistSafetyEvent(request.tenantId(), request.userId(), request.sessionId(),
                        v.category(), v.severity(), v.action(), v.reason());

                return new SafetyDecisionResponse(
                        false,
                        v.severity(),
                        v.action(),
                        v.reason(),
                        "Your request was blocked due to safety guidelines: " + v.reason()
                );
            }
        }

        return new SafetyDecisionResponse(true, "LOW", "ALLOW", "Input passed safety evaluation", null);
    }

    public SafetyDecisionResponse evaluateOutput(SafetyOutputCheckRequest request) {
        log.info("Evaluating safety output for tenant: {}, user: {}, traceId: {}",
                request.tenantId(), request.userId(), getTraceId());

        for (SafetyRule rule : safetyRules) {
            Optional<SafetyRule.RuleViolation> violation = rule.evaluate(request.generatedContent());
            if (violation.isPresent()) {
                SafetyRule.RuleViolation v = violation.get();
                log.warn("Output safety rule violation caught [{}]: {}", v.severity(), v.reason());

                persistSafetyEvent(request.tenantId(), request.userId(), request.sessionId(),
                        v.category(), v.severity(), v.action(), "Unsafe model output: " + v.reason());

                return new SafetyDecisionResponse(
                        false,
                        v.severity(),
                        v.action(),
                        "Unsafe output generated",
                        "I cannot complete this response as it violates safety guidelines."
                );
            }
        }

        return new SafetyDecisionResponse(true, "LOW", "ALLOW", "Output passed safety evaluation", null);
    }

    private void persistSafetyEvent(String tenantId, String userId, String sessionId,
                                    String category, String severity, String action, String reason) {
        SafetyEvent event = new SafetyEvent();
        event.setId(UUID.randomUUID());
        event.setTenantId(tenantId);
        event.setUserId(userId);
        event.setSessionId(sessionId);
        event.setCategory(category);
        event.setSeverity(severity);
        event.setAction(action);
        event.setReason(reason);
        event.setCreatedAt(Instant.now());
        eventRepository.save(event);
    }

    public String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return UUID.randomUUID().toString();
    }
}