package com.echolife.s4echolifesafetyrisk.service;

import com.echolife.s4echolifesafetyrisk.entity.OutboxEvent;
import com.echolife.s4echolifesafetyrisk.metrics.SafetyMetrics;
import com.echolife.s4echolifesafetyrisk.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class OutboxPublisherService {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherService.class);
    private static final int MAX_RETRIES = 3;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SafetyMetrics safetyMetrics;
    private final String safetyEventsTopic;
    private final String safetyDlqTopic;

    public OutboxPublisherService(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            SafetyMetrics safetyMetrics,
            @Value("${app.kafka.safety-events-topic:safety-risk-events}") String safetyEventsTopic,
            @Value("${app.kafka.safety-events-dlq-topic:safety-risk-events.DLQ}") String safetyDlqTopic) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.safetyMetrics = safetyMetrics;
        this.safetyEventsTopic = safetyEventsTopic;
        this.safetyDlqTopic = safetyDlqTopic;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING");

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending outbox event(s) to publish", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                kafkaTemplate.send(safetyEventsTopic, event.getAggregateId(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Kafka ack failed for outbox event id: {}", event.getId(), ex);
                            }
                        });

                event.setStatus("PUBLISHED");
                event.setProcessedAt(Instant.now());
                outboxEventRepository.save(event);

                safetyMetrics.incrementPublished();

            } catch (Exception e) {
                log.error("Failed to publish event id: {}. Retrying...", event.getId(), e);
                int updatedRetries = event.getRetryCount() + 1;
                event.setRetryCount(updatedRetries);

                if (updatedRetries >= MAX_RETRIES) {
                    log.error("Event id: {} exceeded max retries. Routing to DLQ...", event.getId());
                    try {
                        kafkaTemplate.send(safetyDlqTopic, event.getAggregateId(), event.getPayload());
                        event.setStatus("DEAD_LETTER");
                        safetyMetrics.incrementDlq();
                    } catch (Exception dlqEx) {
                        log.error("Fatal: Unable to send to DLQ topic for event id: {}", event.getId(), dlqEx);
                        event.setStatus("FAILED");
                    }
                }
                outboxEventRepository.save(event);
            }
        }
    }
}