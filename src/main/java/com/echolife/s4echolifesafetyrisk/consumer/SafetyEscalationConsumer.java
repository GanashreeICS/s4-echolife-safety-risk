package com.echolife.s4echolifesafetyrisk.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class SafetyEscalationConsumer {

    private static final Logger log = LoggerFactory.getLogger(SafetyEscalationConsumer.class);

    private final ObjectMapper objectMapper;

    public SafetyEscalationConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${app.kafka.safety-events-topic:safety-risk-events}",
            groupId = "safety-escalation-service-group"
    )
    public void handleSafetyViolation(
            @Payload String rawPayload,
            @Header(KafkaHeaders.RECEIVED_KEY) String partitionKey,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received Kafka message on partition [{}] at offset [{}] with key [{}]",
                partition, offset, partitionKey);

        try {
            JsonNode event = objectMapper.readTree(rawPayload);

            String eventId = event.path("id").asText();
            String tenantId = event.path("tenantId").asText();
            String userId = event.path("userId").asText();
            String category = event.path("category").asText();
            String severity = event.path("severity").asText();
            String action = event.path("action").asText();
            String reason = event.path("reason").asText();

            log.info("Parsed safety event [{}] for Tenant [{}] / User [{}]: Category={}, Action={}",
                    eventId, tenantId, userId, category, action);

            if ("CRITICAL".equalsIgnoreCase(severity) || "BLOCK_AND_ESCALATE".equalsIgnoreCase(action)) {
                dispatchUrgentEscalation(userId, tenantId, category, reason);
            }

        } catch (Exception ex) {
            log.error("Failed to parse and process safety violation event payload: {}", rawPayload, ex);
        }
    }

    private void dispatchUrgentEscalation(String userId, String tenantId, String category, String reason) {
        log.warn("🚨 [CRITICAL ESCALATION TRIGGERED] User [{}] in Tenant [{}] violated policy [{}]. Reason: '{}'",
                userId, tenantId, category, reason);
    }
}