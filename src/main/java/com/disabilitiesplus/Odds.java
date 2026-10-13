package com.disabilitiesplus;

final class Odds {
    private Odds() {
    }

    static int audience(int base, int audience) {
        return Math.max(1, base / (1 + Math.max(0, audience)));
    }
}
