package com.disabilitiesplus;

import java.util.regex.Pattern;

public final class MindRules {
    private static final double GAZE_COS = Math.cos(Math.toRadians(40.0));
    public static final int ANXIETY_NERVOUS = 30;
    public static final int ANXIETY_PANIC = 60;
    public static final int ANXIETY_FREEZE = 90;
    private static final Pattern LAUGHTER = Pattern.compile(
        "(?i)(\\blo+l+\\b|\\blmf?ao+\\b|\\brofl\\b|\\b(ha){2,}h?\\b|\\b(he){2,}h?\\b|\\bxd+\\b|\\bkek+w?\\b|\\bjaja+\\b|\\bhah+a+h*\\b|😂|🤣)");

    private MindRules() {
    }

    static boolean facing(double vx, double vy, double vz, double dx, double dy, double dz) {
        double lv = Math.sqrt(vx * vx + vy * vy + vz * vz);
        double ld = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (lv < 1.0E-9 || ld < 1.0E-9) {
            return false;
        }

        return (vx * dx + vy * dy + vz * dz) / (lv * ld) >= GAZE_COS;
    }

    static float anxietyNext(float current, int watchers, boolean comfort) {
        float gain = Math.min(Math.max(watchers, 0), 4) * 3.0F * (comfort ? 0.5F : 1.0F);
        float loss = (watchers <= 0 ? 4.0F : 0.0F) + (comfort ? 5.0F : 0.0F);
        return Math.max(0.0F, Math.min(100.0F, current + gain - loss));
    }

    static int anxietyStage(float meter) {
        return meter >= ANXIETY_FREEZE ? 3 : meter >= ANXIETY_PANIC ? 2 : meter >= ANXIETY_NERVOUS ? 1 : 0;
    }

    static boolean isLaughter(String text) {
        return text != null && LAUGHTER.matcher(text).find();
    }

    static int woundAfter(int current, float taken) {
        return taken >= 3.0F ? Math.min(600, current + 200) : current;
    }

    static float nonLethal(float want, float health, float floor) {
        return Math.max(0.0F, Math.min(want, health - floor));
    }
}
