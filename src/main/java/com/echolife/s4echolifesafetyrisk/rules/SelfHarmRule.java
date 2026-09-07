package com.echolife.s4echolifesafetyrisk.rules;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class SelfHarmRule implements SafetyRule {

    private static final List<String> TRIGGERS = List.of(
            "kill myself",
            "suicide",
            "end my life",
            "want to die",
            "harm myself",
            "hurt myself",
            "cutting myself",
            "cut myself",
            "bleed out",
            "hang myself",
            "overdose",
            "take my own life"
    );

    @Override
    public Optional<RuleViolation> evaluate(String content) {
        if (content == null || content.isBlank()) {
            return Optional.empty();
        }

        String normalized = content.toLowerCase();
        for (String trigger : TRIGGERS) {
            if (normalized.contains(trigger)) {
                return Optional.of(new RuleViolation(
                        "SELF_HARM",
                        "CRITICAL",
                        "BLOCK_AND_ESCALATE",
                        "Detected severe self-harm or suicidal ideation prompt"
                ));
            }
        }

        return Optional.empty();
    }
}