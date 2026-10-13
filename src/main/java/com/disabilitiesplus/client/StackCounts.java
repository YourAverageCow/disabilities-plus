package com.disabilitiesplus.client;

import com.disabilitiesplus.PlusDisability;
import com.studiobing.disabilities.client.ClientState;

/**
 * Item stack counts. The original Dyslexia scrambles all text, stack counts included; in this add-on a stack count is
 * only unreadable with Dyscalculia, so counts are drawn exempt from the original's scrambling and then obfuscated
 * here when it applies.
 */
public final class StackCounts {
   private static boolean drawing = false;
   private static boolean savedExempt = false;

   private StackCounts() {
   }

   /** Called as the game starts drawing a stack count. */
   public static void enter() {
      if (!drawing) {
         drawing = true;
         savedExempt = ClientState.dyslexiaExempt();
         ClientState.setDyslexiaExempt(true);
      }
   }

   public static void leave() {
      if (drawing) {
         drawing = false;
         ClientState.setDyslexiaExempt(savedExempt);
      }
   }

   public static boolean drawing() {
      return drawing;
   }

   /** The count text as it should be drawn: obfuscated with Dyscalculia, plain otherwise. */
   public static String render(String count) {
      if (PlusClientState.has(PlusDisability.DYSCALCULIA)) {
         PlusClientState.obfuscatedCountDraws++;
         return "§k" + count;
      }

      PlusClientState.readableCountDraws++;
      return count;
   }
}
