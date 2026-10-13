package com.disabilitiesplus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.disabilitiesplus.client.Aphasia;
import com.disabilitiesplus.client.Dyscalculia;
import com.disabilitiesplus.client.Dysgraphia;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TextEffectsTest {
    @Test
    void dyscalculiaIsABijectionWithNoFixedPoints() {
        Set<Character> seen = new HashSet<>();
        for (char c = '0'; c <= '9'; c++) {
            String out = Dyscalculia.scramble(String.valueOf(c));
            assertEquals(1, out.length());
            assertNotEquals(String.valueOf(c), out, "digit " + c + " maps to itself");
            assertTrue(out.charAt(0) >= '0' && out.charAt(0) <= '9');
            assertTrue(seen.add(out.charAt(0)), "two digits map to " + out);
        }
    }

    @Test
    void dyscalculiaOnlyTouchesDigitsAndKeepsLength() {
        String in = "Stack of 64 §a(x12) at -3.5, 100%";
        String out = Dyscalculia.scramble(in);
        assertEquals(in.length(), out.length());
        for (int i = 0; i < in.length(); i++) {
            char a = in.charAt(i);
            if (a < '0' || a > '9') {
                assertEquals(a, out.charAt(i), "non-digit changed at " + i);
            } else {
                assertNotEquals(a, out.charAt(i));
            }
        }
    }

    @Test
    void dyscalculiaIsStableBetweenFramesAndReturnsSameInstanceWhenNoDigits() {
        assertEquals(Dyscalculia.scramble("64"), Dyscalculia.scramble("64"));
        String plain = "no numbers here";
        assertSame(plain, Dyscalculia.scramble(plain));
        assertEquals("", Dyscalculia.scramble(""));
    }

    @Test
    void aphasiaNeverTouchesCommandsOrEmptyInput() {
        for (int i = 0; i < 200; i++) {
            assertEquals("/give @s diamond 64", Aphasia.scramble("/give @s diamond 64"));
        }

        assertEquals("", Aphasia.scramble(""));
        assertEquals(null, Aphasia.scramble(null));
    }

    @Test
    void aphasiaNeverExceedsChatLimitAndNeverReturnsBlank() {
        String longMsg = ("encyclopedia ".repeat(30)).trim();
        for (int i = 0; i < 500; i++) {
            String out = Aphasia.scramble(longMsg);
            assertTrue(out.length() <= 256, "over the 256 char chat limit: " + out.length());
            assertTrue(!out.isBlank());
        }
    }

    @Test
    void aphasiaShuffleKeepsFirstAndLastLetterAndTheLetterMultiset() {
        String word = "international";
        Set<String> fillers = new HashSet<>(List.of("thing", "um", "that", "stuff", "the... the", "what", "..."));
        boolean sawChange = false;
        for (int i = 0; i < 2000; i++) {
            String out = Aphasia.scramble(word);
            if (word.equals(out)) {
                continue;
            }

            sawChange = true;
            if (fillers.contains(out)) {
                continue;
            }

            assertEquals(word.charAt(0), out.charAt(0));
            assertEquals(word.charAt(word.length() - 1), out.charAt(out.length() - 1));
            assertEquals(sorted(word), sorted(out), "letters were lost or invented: " + out);
        }

        assertTrue(sawChange, "aphasia never changed anything in 2000 tries");
    }

    @Test
    void aphasiaLeavesShortWordsAlone() {
        for (int i = 0; i < 500; i++) {
            assertEquals("hi me ok", Aphasia.scramble("hi me ok"));
        }
    }

    private static String sorted(String s) {
        char[] c = s.toCharArray();
        Arrays.sort(c);
        return new String(c);
    }

    @Test
    void dysgraphiaNeverTouchesCommandsOrEmptyInput() {
        assertEquals("/give @s stick 64", Dysgraphia.scramble("/give @s stick 64"));
        assertEquals("", Dysgraphia.scramble(""));
        assertEquals(null, Dysgraphia.scramble(null));
    }

    @Test
    void dysgraphiaOnlyMiswritesLettersAndNeverGrows() {
        java.util.Random rng = new java.util.Random(7);
        String message = "bad dog; please quit 12345 !? now";
        for (int i = 0; i < 300; i++) {
            String out = Dysgraphia.scramble(message, rng);
            assertTrue(out.length() <= message.length(), "never longer: " + out);
            for (char c : new char[]{' ', ';', '!', '?', '1', '2', '3', '4', '5'}) {
                long before = message.chars().filter(x -> x == c).count();
                long after = out.chars().filter(x -> x == c).count();
                assertEquals(before, after, "'" + c + "' must be untouched in: " + out);
            }
        }
    }

    @Test
    void dysgraphiaActuallyChangesLongInput() {
        java.util.Random rng = new java.util.Random(11);
        String message = "please bring the big pile of dark quartz and diamond blocks back";
        int changed = 0;
        for (int i = 0; i < 100; i++) {
            if (!message.equals(Dysgraphia.scramble(message, rng))) {
                changed++;
            }
        }

        assertTrue(changed > 90, "expected most long messages to be miswritten, got " + changed + "/100");
    }

    @Test
    void dysgraphiaCapsAtTheChatLimit() {
        String out = Dysgraphia.scramble("a".repeat(300), new java.util.Random(3));
        assertTrue(out.length() <= 256);
    }

    @Test
    void dysgraphiaReversalsAreInvolutions() {
        for (char c : "bdpqBDPQxyz".toCharArray()) {
            assertEquals(c, Dysgraphia.flip(Dysgraphia.flip(c)));
        }

        assertEquals('d', Dysgraphia.flip('b'));
        assertEquals('q', Dysgraphia.flip('p'));
    }
}
