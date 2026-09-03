package com.echolife.s4echolifesafetyrisk.rules;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class MedicalAdviceRule implements SafetyRule {

    private static final List<String> TRIGGERS = List.of(
            "medical dosage", "prescribe medicine", "how many pills to cure", "substitute prescription"
    );

    @Override
    public Optional<RuleViolation> evaluate(String content) {
        if (content == null || content.isBlank()) return Optional.empty();
        String normalized = content.toLowerCase();
        for (String trigger : TRIGGERS) {
            if (normalized.contains(trigger)) {
                return Optional.of(new RuleViolation(
                        "MEDICAL",
                        "HIGH",
                        "REDIRECT_SAFE",
                        "Medical dosage or direct clinical treatment guidance requested"
                ));
            }
        }
        return Optional.empty();
    }
}