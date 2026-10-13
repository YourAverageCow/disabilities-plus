package com.disabilitiesplus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OddsTest {
    @Test
    void aloneItIsTheBaseOdds() {
        assertEquals(120, Odds.audience(120, 0));
    }

    @Test
    void moreWatchersMeansShorterOdds() {
        int previous = Odds.audience(120, 0);
        for (int n = 1; n <= 12; n++) {
            int now = Odds.audience(120, n);
            assertTrue(now <= previous, "never less likely with " + n + " watching");
            previous = now;
        }

        assertEquals(60, Odds.audience(120, 1));
        assertEquals(30, Odds.audience(120, 3));
    }

    @Test
    void neverBelowOneAndNegativeAudienceIsIgnored() {
        assertEquals(1, Odds.audience(1, 50));
        assertEquals(1, Odds.audience(3, 50));
        assertEquals(120, Odds.audience(120, -5));
    }
}
