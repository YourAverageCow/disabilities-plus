package com.disabilitiesplus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.studiobing.disabilities.Disability;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PlusDisabilityTest {
   @Test
   void hasSixtySevenEntries() {
      assertEquals(82, PlusDisability.values().length);
   }

   @Test
   void namesDoNotCollideWithTheOriginalMod() {
      Set<String> original = new HashSet<>();
      for (Disability d : Disability.values()) {
         original.add(d.name());
         original.add(d.displayName);
      }

      for (PlusDisability d : PlusDisability.values()) {
         assertFalse(original.contains(d.name()), d + " clashes with an original command name");
         assertFalse(original.contains(d.displayName), d.displayName + " clashes with an original display name");
      }
   }

   @Test
   void metadataIsCompleteAndUnique() {
      Set<String> names = new HashSet<>();
      Set<String> display = new HashSet<>();
      for (PlusDisability d : PlusDisability.values()) {
         assertTrue(names.add(d.name()));
         assertTrue(d.name().toLowerCase().matches("[a-z0-9_]+"), d + " is not a brigadier word() argument");
         assertNotNull(d.displayName);
         assertFalse(d.displayName.isBlank());
         assertTrue(display.add(d.displayName), "duplicate display name " + d.displayName);
         assertFalse(d.description.isBlank());
         assertTrue(d.color.matches("§[0-9a-f]"), d + " has a bad colour code: " + d.color);
         assertTrue(d.listLine.startsWith(d.color));
         assertTrue(d.weight > 0.0);
      }
   }

   @Test
   void onlyQuadrupleAmputeeIsRarer() {
      for (PlusDisability d : PlusDisability.values()) {
         assertEquals(d == PlusDisability.QUADRUPLE_AMPUTEE ? 0.5 : 1.0, d.weight, 1.0E-9, d + " weight");
      }
   }

   @Test
   void findIsCaseInsensitiveAndRejectsUnknownNames() {
      assertEquals(PlusDisability.TINNITUS, PlusDisability.find("tinnitus"));
      assertEquals(PlusDisability.TINNITUS, PlusDisability.find("TINNITUS"));
      assertNull(PlusDisability.find("blind"), "originals are not ours");
      assertNull(PlusDisability.find("nonsense"));
   }

   @Test
   void maskRoundTripsAndEveryDisabilityHasItsOwnBit() {
      for (PlusDisability d : PlusDisability.values()) {
         assertEquals(EnumSet.of(d), PlusState.fromMask(PlusState.toMask(EnumSet.of(d))));
      }

      Set<String> seen = new HashSet<>();
      for (PlusDisability d : PlusDisability.values()) {
         long[] bits = PlusState.toMask(EnumSet.of(d));
         int total = 0;
         String where = "";
         for (int w = 0; w < bits.length; w++) {
            total += Long.bitCount(bits[w]);
            if (bits[w] != 0L) {
               where = w + ":" + Long.numberOfTrailingZeros(bits[w]);
            }
         }

         assertEquals(1, total);
         assertTrue(seen.add(where), d + " collides at " + where);
      }

      Random rng = new Random(42);
      for (int n = 0; n < 1000; n++) {
         EnumSet<PlusDisability> set = EnumSet.noneOf(PlusDisability.class);
         for (PlusDisability d : PlusDisability.values()) {
            if (rng.nextBoolean()) {
               set.add(d);
            }
         }

         assertEquals(set, PlusState.fromMask(PlusState.toMask(set)));
      }

      assertEquals(EnumSet.noneOf(PlusDisability.class), PlusState.fromMask(new long[0]));
   }

   @Test
   void bitsFromANewerServerAreIgnored() {
      long[] mask = PlusState.toMask(EnumSet.of(PlusDisability.TINNITUS));
      long[] wider = new long[mask.length + 3];
      System.arraycopy(mask, 0, wider, 0, mask.length);
      wider[wider.length - 1] = -1L;
      assertEquals(EnumSet.of(PlusDisability.TINNITUS), PlusState.fromMask(wider));
   }

   @Test
   void quadrupleAmputeeIsPickedHalfAsOftenAsAnyOtherDisability() {
      List<PlusDisability> pool = new ArrayList<>(List.of(PlusDisability.values()));
      Random rng = new Random(7);
      int n = 400_000;
      int[] hits = new int[PlusDisability.values().length];
      for (int i = 0; i < n; i++) {
         hits[PlusState.pick(pool, rng.nextDouble()).ordinal()]++;
      }

      double normal = (double)hits[PlusDisability.TINNITUS.ordinal()] / n;
      double quad = (double)hits[PlusDisability.QUADRUPLE_AMPUTEE.ordinal()] / n;
      assertEquals(1.0 / (pool.size() - 0.5), normal, normal * 0.1);
      assertEquals(0.5, quad / normal, 0.08);
   }

   @Test
   void pickIsSafeAtTheEdges() {
      List<PlusDisability> pool = List.of(PlusDisability.values());
      assertEquals(pool.get(0), PlusState.pick(pool, 0.0));
      assertEquals(pool.get(pool.size() - 1), PlusState.pick(pool, 0.9999999999));
   }
}
