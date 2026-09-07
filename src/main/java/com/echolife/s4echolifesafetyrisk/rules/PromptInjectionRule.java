package com.echolife.s4echolifesafetyrisk.rules;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class PromptInjectionRule implements SafetyRule {

    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("(?i)ignore\\s+all\\s+(previous|prior)\\s+instructions"),
            Pattern.compile("(?i)system\\s+prompt\\s+override"),
            Pattern.compile("(?i)you\\s+are\\s+now\\s+in\\s+(dan|developer|god)\\s+mode"),
            Pattern.compile("(?i)disregard\\s+(all\\s+rules|safety\\s+guidelines)")
    );

    @Override
    public Optional<RuleViolation> evaluate(String content) {
        if (content == null || content.isBlank()) {
            return Optional.empty();
        }

        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(content).find()) {
                return Optional.of(new RuleViolation(
                        "PROMPT_INJECTION",
                        "HIGH",
                        "BLOCK_AND_FLAG",
                        "Detected attempt to override system instructions"
                ));
            }
        }
        return Optional.empty();
    }
}