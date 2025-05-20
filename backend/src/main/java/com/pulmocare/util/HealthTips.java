package com.pulmocare.util;

import java.time.LocalDate;

public class HealthTips {
    private static final String[] tips = {
            "🚭 Avoid tobacco smoke...",
            "💧 Stay hydrated...",
            "🏃‍♂️ Exercise...",
            "😷 Wear a mask...",
            "💉 Get vaccinated...",
            "🌬️ Use air purifiers...",
            "🫁 Practice breathing...",
            "🪟 Ventilate your home..."
    };

    public static String getTipOfTheDay() {
        int day = LocalDate.now().getDayOfMonth();
        return tips[day % tips.length];
    }
}
