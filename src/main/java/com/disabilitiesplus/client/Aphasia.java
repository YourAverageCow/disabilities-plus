package com.disabilitiesplus.client;

import java.util.Random;

public final class Aphasia {
    private static final Random RNG = new Random();
    private static final String[] FILLER = new String[]{"thing", "um", "that", "stuff", "the... the", "what", "..."};

    private static final int MAX_CHAT_LENGTH = 256;

    private Aphasia() {
    }

    public static String scramble(String message) {
        if (message == null || message.isEmpty() || message.startsWith("/")) {
            return message;
        }

        String[] words = message.split(" ", -1);
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            if (w.length() >= 3) {
                int roll = RNG.nextInt(100);
                if (roll < 25) {
                    words[i] = FILLER[RNG.nextInt(FILLER.length)];
                } else if (roll < 45 && w.length() > 3) {
                    words[i] = shuffleInner(w);
                }
            }
        }

        String out = String.join(" ", words);
        return out.length() > MAX_CHAT_LENGTH ? out.substring(0, MAX_CHAT_LENGTH) : out;
    }

    private static String shuffleInner(String w) {
        char[] c = w.toCharArray();

        for (int i = c.length - 2; i > 1; i--) {
            int j = 1 + RNG.nextInt(i);
            char t = c[i];
            c[i] = c[j];
            c[j] = t;
        }

        return new String(c);
    }
}
