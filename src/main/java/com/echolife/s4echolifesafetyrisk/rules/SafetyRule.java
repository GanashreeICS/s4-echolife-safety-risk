package com.echolife.s4echolifesafetyrisk.rules;

import java.util.Optional;

public interface SafetyRule {
    Optional<RuleViolation> evaluate(String content);

    record RuleViolation(String category, String severity, String action, String reason) {}
}