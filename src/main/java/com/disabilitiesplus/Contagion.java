package com.disabilitiesplus;

final class Contagion {
    private Contagion() {
    }

    static boolean shouldInfect(double distanceSq, double radius, boolean alreadySick, boolean immune, double roll, double chance) {
        return !alreadySick && !immune && distanceSq <= radius * radius && roll < chance;
    }
}
