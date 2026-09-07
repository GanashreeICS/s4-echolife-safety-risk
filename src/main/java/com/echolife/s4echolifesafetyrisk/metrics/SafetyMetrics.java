package com.echolife.s4echolifesafetyrisk.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SafetyMetrics {

    private final MeterRegistry registry;
    private final Counter outboxDlqCounter;
    private final Counter outboxPublishedCounter;

    public SafetyMetrics(MeterRegistry registry) {
        this.registry = registry;

        this.outboxDlqCounter = Counter.builder("safety.outbox.dlq.events.total")
                .description("Total number of outbox events routed to DLQ")
                .register(registry);

        this.outboxPublishedCounter = Counter.builder("safety.outbox.published.events.total")
                .description("Total number of outbox events successfully published to Kafka")
                .register(registry);
    }

    public void incrementViolation(String category, String severity) {
        Counter.builder("safety.violations.total")
                .description("Total count of safety rule violations caught")
                .tag("category", category)
                .tag("severity", severity)
                .register(registry)
                .increment();
    }

    public void incrementPublished() {
        outboxPublishedCounter.increment();
    }

    public void incrementDlq() {
        outboxDlqCounter.increment();
    }

    public void recordEvaluationLatency(long durationMs) {
        Timer.builder("safety.evaluation.duration")
                .description("Evaluation latency in ms")
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }
}