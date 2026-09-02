package com.echolife.s4echolifesafetyrisk.dto;

public class Enums {
    public enum SafetyDirection { INPUT, OUTPUT }
    public enum SafetySeverity { LOW, MEDIUM, HIGH, CRITICAL }
    public enum SafetyReason {
        OK, SELF_HARM, MEDICAL, LEGAL, FINANCIAL, UNSAFE_OUTPUT, POLICY, DAILY_CAP_EXCEEDED, QUIET_HOURS
    }
}
