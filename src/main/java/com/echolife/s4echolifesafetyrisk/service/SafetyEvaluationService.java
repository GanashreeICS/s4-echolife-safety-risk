package com.echolife.s4echolifesafetyrisk.service;

import com.echolife.s4echolifesafetyrisk.dto.SafetyDecisionResponse;
import com.echolife.s4echolifesafetyrisk.dto.SafetyInputCheckRequest;
import com.echolife.s4echolifesafetyrisk.dto.SafetyOutputCheckRequest;
import com.echolife.s4echolifesafetyrisk.entity.OutboxEvent;
import com.echolife.s4echolifesafetyrisk.entity.SafetyEvent;
import com.echolife.s4echolifesafetyrisk.metrics.SafetyMetrics;
import com.echolife.s4echolifesafetyrisk.repository.OutboxEventRepository;
import com.echolife.s4echolifesafetyrisk.repository.SafetyEventRepository;
import com.echolife.s4echolifesafetyrisk.rules.SafetyRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SafetyEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(SafetyEvaluationService.class);

    private final List<SafetyRule> safetyRules;
    private final SafetyEventRepository eventRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final Tracer tracer;
    private final SafetyMetrics safetyMetrics;

    public SafetyEvaluationService(List<SafetyRule> safetyRules,
                                   SafetyEventRepository eventRepository,
                                   OutboxEventRepository outboxEventRepository,
                                   ObjectMapper objectMapper,
                                   Tracer tracer,
                                   SafetyMetrics safetyMetrics) {
        this.safetyRules = safetyRules;
        this.eventRepository = eventRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.tracer = tracer;
        this.safetyMetrics = safetyMetrics;
    }

    @Transactional
    public SafetyDecisionResponse evaluateInput(SafetyInputCheckRequest request) {
        long startTime = System.currentTimeMillis();
        String traceId = getTraceId();
        log.info("Evaluating safety input for tenant: {}, user: {}, traceId: {}",
                request.tenantId(), request.userId(), traceId);

        for (SafetyRule rule : safetyRules) {
            Optional<SafetyRule.RuleViolation> violation = rule.evaluate(request.content());
            if (violation.isPresent()) {
                SafetyRule.RuleViolation v = violation.get();
                log.warn("Input safety rule violation caught [{}]: {}", v.severity(), v.reason());

                safetyMetrics.incrementViolation(v.category(), v.severity());

                persistSafetyEventAndOutbox(request.tenantId(), request.userId(), request.sessionId(),
                        v.category(), v.severity(), v.action(), v.reason(), traceId);

                safetyMetrics.recordEvaluationLatency(System.currentTimeMillis() - startTime);

                return new SafetyDecisionResponse(
                        false,
                        v.severity(),
                        v.action(),
                        v.reason(),
                        "Your request was blocked due to safety guidelines: " + v.reason()
                );
            }
        }

        safetyMetrics.recordEvaluationLatency(System.currentTimeMillis() - startTime);
        return new SafetyDecisionResponse(true, "LOW", "ALLOW", "Input passed safety evaluation", null);
    }

    @Transactional
    public SafetyDecisionResponse evaluateOutput(SafetyOutputCheckRequest request) {
        long startTime = System.currentTimeMillis();
        String traceId = getTraceId();
        log.info("Evaluating safety output for tenant: {}, user: {}, traceId: {}",
                request.tenantId(), request.userId(), traceId);

        for (SafetyRule rule : safetyRules) {
            Optional<SafetyRule.RuleViolation> violation = rule.evaluate(request.generatedContent());
            if (violation.isPresent()) {
                SafetyRule.RuleViolation v = violation.get();
                log.warn("Output safety rule violation caught [{}]: {}", v.severity(), v.reason());

                safetyMetrics.incrementViolation(v.category(), v.severity());

                persistSafetyEventAndOutbox(request.tenantId(), request.userId(), request.sessionId(),
                        v.category(), v.severity(), v.action(), "Unsafe model output: " + v.reason(), traceId);

                safetyMetrics.recordEvaluationLatency(System.currentTimeMillis() - startTime);

                return new SafetyDecisionResponse(
                        false,
                        v.severity(),
                        v.action(),
                        "Unsafe output generated",
                        "I cannot complete this response as it violates safety guidelines."
                );
            }
        }

        safetyMetrics.recordEvaluationLatency(System.currentTimeMillis() - startTime);
        return new SafetyDecisionResponse(true, "LOW", "ALLOW", "Output passed safety evaluation", null);
    }

    private void persistSafetyEventAndOutbox(String tenantId, String userId, String sessionId,
                                             String category, String severity, String action,
                                             String reason, String traceId) {
        // 1. Persist the safety event audit log
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
        SafetyEvent savedEvent = eventRepository.save(event);

        // 2. Persist outbox event inside the same transaction
        try {
            String payloadJson = objectMapper.writeValueAsString(savedEvent);
            OutboxEvent outboxEvent = new OutboxEvent(
                    UUID.randomUUID(),
                    "SAFETY_EVENT",
                    userId != null ? userId : tenantId,
                    "SAFETY_VIOLATION_OCCURRED",
                    payloadJson,
                    traceId,
                    "PENDING",
                    0,
                    Instant.now(),
                    null
            );
            outboxEventRepository.save(outboxEvent);
            log.info("Outbox event saved successfully with ID: {}", outboxEvent.getId());
        } catch (Exception e) {
            log.error("Failed to serialize or save outbox event", e);
            throw new RuntimeException("Failed to save outbox event", e);
        }
    }

    public String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return UUID.randomUUID().toString();
    }
}