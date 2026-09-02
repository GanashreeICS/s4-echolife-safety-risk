package com.echolife.s4echolifesafetyrisk.service;

import com.echolife.s4echolifesafetyrisk.dto.Enums.SafetyDirection;
import com.echolife.s4echolifesafetyrisk.dto.Enums.SafetyReason;
import com.echolife.s4echolifesafetyrisk.dto.Enums.SafetySeverity;
import com.echolife.s4echolifesafetyrisk.dto.SafetyCheckRequest;
import com.echolife.s4echolifesafetyrisk.dto.SafetyDecisionResponse;
import com.echolife.s4echolifesafetyrisk.entity.SafetyEvent;
import com.echolife.s4echolifesafetyrisk.repository.SafetyEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
public class SafetyEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(SafetyEvaluationService.class);

    private final SafetyEventRepository eventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public SafetyEvaluationService(SafetyEventRepository eventRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.eventRepository = eventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public SafetyDecisionResponse evaluate(SafetyCheckRequest request) {
        String content = request.text().toLowerCase(Locale.ROOT);

        boolean allowed = true;
        SafetyReason reason = SafetyReason.OK;
        SafetySeverity severity = SafetySeverity.LOW;
        String replacement = null;
        boolean escalate = false;

        if (content.contains("kill myself") || content.contains("suicide") || content.contains("end my life")) {
            allowed = false;
            reason = SafetyReason.SELF_HARM;
            severity = SafetySeverity.CRITICAL;
            replacement = "I cannot fulfill this request. If you are in distress, please contact a crisis support helpline immediately.";
            escalate = true;
        } else if (content.contains("prescribe") || content.contains("medical dosage")) {
            allowed = false;
            reason = SafetyReason.MEDICAL;
            severity = SafetySeverity.HIGH;
            replacement = "I cannot provide medical advice or prescription details. Please consult a qualified healthcare provider.";
        } else if (content.contains("legal lawsuit") || content.contains("sue someone")) {
            allowed = false;
            reason = SafetyReason.LEGAL;
            severity = SafetySeverity.MEDIUM;
            replacement = "I cannot provide formal legal advice. Please speak with an attorney.";
        }

        SafetyEvent event = new SafetyEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setUserId(request.userId());
        event.setPersonaId(request.personaId());
        event.setSessionId(request.sessionId());
        event.setDirection(request.direction().name());
        event.setReason(reason.name());
        event.setSeverity(severity.name());
        event.setAllowed(allowed);
        event.setShouldEscalate(escalate);
        event.setReplacementMessage(replacement);
        event.setPolicyVersion(1);

        eventRepository.save(event);

        if (escalate) {
            try {
                kafkaTemplate.send("safety-events", request.userId(), event);
            } catch (Exception ex) {
                log.error("Failed to publish safety escalation event for user {}: {}", request.userId(), ex.getMessage());
            }
        }

        return new SafetyDecisionResponse(allowed, reason, severity, replacement, escalate);
    }
}