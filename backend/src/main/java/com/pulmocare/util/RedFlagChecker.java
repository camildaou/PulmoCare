package com.pulmocare.util;

import java.util.List;

public class RedFlagChecker {
    private static final List<String> redFlags = List.of(
            "chest pain", "shortness of breath", "difficulty breathing",
            "blue lips", "cyanosis", "coughing blood", "blood in cough",
            "severe wheezing", "can't breathe", "struggling to breathe"
    );

    public static boolean contains(String input) {
        return redFlags.stream().anyMatch(flag -> input.toLowerCase().contains(flag));
    }
}
