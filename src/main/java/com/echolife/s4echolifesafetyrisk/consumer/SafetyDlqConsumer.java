package com.echolife.s4echolifesafetyrisk.consumer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class SafetyDlqConsumer {

    private static final Logger log = LoggerFactory.getLogger(SafetyDlqConsumer.class);
    private final Counter dlqCounter;

    public SafetyDlqConsumer(MeterRegistry meterRegistry) {
        this.dlqCounter = meterRegistry.counter("safety_outbox_dlq_events_total");
    }

    @KafkaListener(
            topics = "safety-risk-events.DLT",
            groupId = "safety-risk-dlq-monitoring-group"
    )
    public void consumeDlqEvent(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(KafkaHeaders.OFFSET) Long offset) {

        dlqCounter.increment();
        log.error("CRITICAL DLQ EVENT: Poison pill captured from [{}] | Key: {} | Offset: {} | Payload: {}",
                topic, key, offset, payload);
    }
}