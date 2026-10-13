package com.disabilitiesplus;

/** The rule for catching an illness from a neighbour. Kept apart from the game classes so it can be tested on its own. */
final class Contagion {
   private Contagion() {
   }

   /** True when a player standing {@code distanceSq} (squared blocks) away catches it on this {@code roll} in [0, 1). */
   static boolean shouldInfect(double distanceSq, double radius, boolean alreadySick, boolean immune, double roll, double chance) {
      return !alreadySick && !immune && distanceSq <= radius * radius && roll < chance;
   }
}
