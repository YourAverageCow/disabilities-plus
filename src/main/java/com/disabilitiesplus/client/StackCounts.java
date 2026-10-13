package com.disabilitiesplus.client;

import com.disabilitiesplus.PlusDisability;
import com.studiobing.disabilities.client.ClientState;

public final class StackCounts {
    private static boolean drawing = false;
    private static boolean savedExempt = false;

    private StackCounts() {
    }

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

    public static String render(String count) {
        if (PlusClientState.has(PlusDisability.DYSCALCULIA)) {
            PlusClientState.obfuscatedCountDraws++;
            return "§k" + count;
        }

        PlusClientState.readableCountDraws++;
        return count;
    }
}
