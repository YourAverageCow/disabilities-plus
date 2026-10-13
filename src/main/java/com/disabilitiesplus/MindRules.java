package com.disabilitiesplus;

import java.util.regex.Pattern;

/** The rules behind the conditions about other people and the body, kept free of game classes so they can be tested alone. */
public final class MindRules {
   /** Someone is watching you if you are within this angle of where they are looking. */
   private static final double GAZE_COS = Math.cos(Math.toRadians(40.0));
   public static final int ANXIETY_NERVOUS = 30;
   public static final int ANXIETY_PANIC = 60;
   public static final int ANXIETY_FREEZE = 90;
   private static final Pattern LAUGHTER = Pattern.compile(
      "(?i)(\\blo+l+\\b|\\blmf?ao+\\b|\\brofl\\b|\\b(ha){2,}h?\\b|\\b(he){2,}h?\\b|\\bxd+\\b|\\bkek+w?\\b|\\bjaja+\\b|\\bhah+a+h*\\b|😂|🤣)");

   private MindRules() {
   }

   /** True when the vector (dx, dy, dz) from the watcher to you is within 40 degrees of their view vector. */
   static boolean facing(double vx, double vy, double vz, double dx, double dy, double dz) {
      double lv = Math.sqrt(vx * vx + vy * vy + vz * vz);
      double ld = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (lv < 1.0E-9 || ld < 1.0E-9) {
         return false;
      }

      return (vx * dx + vy * dy + vz * dz) / (lv * ld) >= GAZE_COS;
   }

   /**
    * One second of Social Anxiety: each watcher (up to four) adds 3, halved with a friend close by; with nobody
    * watching it drains 4, and a close friend drains a further 5. Kept between 0 and 100.
    */
   static float anxietyNext(float current, int watchers, boolean comfort) {
      float gain = Math.min(Math.max(watchers, 0), 4) * 3.0F * (comfort ? 0.5F : 1.0F);
      float loss = (watchers <= 0 ? 4.0F : 0.0F) + (comfort ? 5.0F : 0.0F);
      return Math.max(0.0F, Math.min(100.0F, current + gain - loss));
   }

   /** 0 = calm, 1 = nervous, 2 = panicking, 3 = frozen. */
   static int anxietyStage(float meter) {
      return meter >= ANXIETY_FREEZE ? 3 : meter >= ANXIETY_PANIC ? 2 : meter >= ANXIETY_NERVOUS ? 1 : 0;
   }

   /** Whether a chat message has someone laughing in it (the thing that drops a person with cataplexy). */
   static boolean isLaughter(String text) {
      return text != null && LAUGHTER.matcher(text).find();
   }

   /** How long a hit leaves a wound you cannot feel: 10 seconds for any hit of 3 or more, stacking up to 30 seconds. */
   static int woundAfter(int current, float taken) {
      return taken >= 3.0F ? Math.min(600, current + 200) : current;
   }

   /** How much of {@code want} damage can be dealt without taking someone below {@code floor}; never negative. */
   static float nonLethal(float want, float health, float floor) {
      return Math.max(0.0F, Math.min(want, health - floor));
   }
}
