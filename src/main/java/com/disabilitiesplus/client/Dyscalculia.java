package com.disabilitiesplus.client;

public final class Dyscalculia {
   // Fixed permutation with no fixed points, so numbers are always wrong but stay stable frame to frame.
   private static final String MAP = "3814067925";

   private Dyscalculia() {
   }

   public static String scramble(String text) {
      StringBuilder sb = null;

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (c >= '0' && c <= '9') {
            if (sb == null) {
               sb = new StringBuilder(text);
            }

            sb.setCharAt(i, MAP.charAt(c - '0'));
         }
      }

      return sb == null ? text : sb.toString();
   }
}
