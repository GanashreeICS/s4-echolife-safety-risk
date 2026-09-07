package com.echolife.s4echolifesafetyrisk.rules;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class PiiLeakageRule implements SafetyRule {

    private static final List<Pattern> PII_PATTERNS = List.of(
            // Credit card pattern (13-16 digits)
            Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b"),
            // API key / JWT / Bearer patterns
            Pattern.compile("(?i)\\b(eyJh[a-zA-Z0-9_-]+\\.[a-zA-Z0-9_-]+\\.[a-zA-Z0-9_-]+|sk-[a-zA-Z0-9]{20,})\\b")
    );

    @Override
    public Optional<RuleViolation> evaluate(String content) {
        if (content == null || content.isBlank()) {
            return Optional.empty();
        }

        for (Pattern pattern : PII_PATTERNS) {
            if (pattern.matcher(content).find()) {
                return Optional.of(new RuleViolation(
                        "PII_LEAKAGE",
                        "HIGH",
                        "BLOCK_AND_FLAG",
                        "Detected potential PII or secret exposure"
                ));
            }
        }
        return Optional.empty();
    }
}