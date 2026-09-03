package com.echolife.s4echolifesafetyrisk.rules;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class LegalAdviceRule implements SafetyRule {

    private static final List<String> TRIGGERS = List.of(
            "how to forge a legal document", "evade law enforcement", "illegal contract draft"
    );

    @Override
    public Optional<RuleViolation> evaluate(String content) {
        if (content == null || content.isBlank()) return Optional.empty();
        String normalized = content.toLowerCase();
        for (String trigger : TRIGGERS) {
            if (normalized.contains(trigger)) {
                return Optional.of(new RuleViolation(
                        "LEGAL",
                        "HIGH",
                        "BLOCK",
                        "Unlawful legal guidance or illegal assistance requested"
                ));
            }
        }
        return Optional.empty();
    }
}