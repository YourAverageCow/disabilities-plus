package com.disabilitiesplus;

/** Small pure rules about chances, kept apart from the game classes so they can be tested on their own. */
final class Odds {
   private Odds() {
   }

   /**
    * Erectile Dysfunction: the 1-in-N chance per tick that using an item cuts out. Performance anxiety: it gets more
    * likely the more eyes are on you ({@code audience} living things nearby). Never less than 1.
    */
   static int audience(int base, int audience) {
      return Math.max(1, base / (1 + Math.max(0, audience)));
   }
}
