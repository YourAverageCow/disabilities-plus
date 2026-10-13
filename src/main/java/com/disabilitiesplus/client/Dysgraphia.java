package com.disabilitiesplus.client;

import com.disabilitiesplus.PlusDisability;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

public final class Dysgraphia {
    private static final Random RNG = new Random();
    private static final int MAX_CHAT_LENGTH = 256;

    private static final int SLIP_PERCENT = 12;

    private Dysgraphia() {
    }

    public static String scramble(String message) {
        return scramble(message, RNG);
    }

    public static String onTyped(String typed) {
        if (typed == null || typed.isEmpty() || !PlusClientState.has(PlusDisability.DYSGRAPHIA)) {
            return typed;
        }

        return Minecraft.getInstance().gui.screen() instanceof ChatScreen ? typed : scramble(typed);
    }

    public static String scramble(String message, Random rng) {
        if (message == null || message.isEmpty() || message.startsWith("/")) {
            return message;
        }

        StringBuilder out = new StringBuilder(message.length());
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (!Character.isLetter(c) || rng.nextInt(100) >= SLIP_PERCENT) {
                out.append(c);
                continue;
            }

            int kind = rng.nextInt(3);
            char flipped = flip(c);
            if (kind == 0 && flipped != c) {
                out.append(flipped);
            } else if (kind == 1 && i + 1 < message.length() && Character.isLetter(message.charAt(i + 1))) {
                out.append(message.charAt(i + 1)).append(c);
                i++;
            } else if (kind != 2) {
                out.append(c);
            }
        }

        return out.length() > MAX_CHAT_LENGTH ? out.substring(0, MAX_CHAT_LENGTH) : out.toString();
    }

    public static char flip(char c) {
        return switch (c) {
            case 'b' -> 'd';
            case 'd' -> 'b';
            case 'p' -> 'q';
            case 'q' -> 'p';
            case 'B' -> 'D';
            case 'D' -> 'B';
            case 'P' -> 'Q';
            case 'Q' -> 'P';
            default -> c;
        };
    }
}
