package com.disabilitiesplus;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlusConditionsTest {
   @Test
   void closeUninfectedPlayerCatchesItOnAGoodRoll() {
      assertTrue(Contagion.shouldInfect(4.0, 3.0, false, false, 0.05, 0.10));
   }

   @Test
   void tooFarAwayNeverCatchesIt() {
      assertFalse(Contagion.shouldInfect(9.01, 3.0, false, false, 0.0, 0.10));
      assertTrue(Contagion.shouldInfect(9.0, 3.0, false, false, 0.0, 0.10), "exactly at the radius counts");
   }

   @Test
   void alreadySickOrImmuneNeverCatchesItAgain() {
      assertFalse(Contagion.shouldInfect(1.0, 3.0, true, false, 0.0, 0.10));
      assertFalse(Contagion.shouldInfect(1.0, 3.0, false, true, 0.0, 0.10));
   }

   @Test
   void aBadRollSpares() {
      assertFalse(Contagion.shouldInfect(1.0, 3.0, false, false, 0.10, 0.10));
      assertFalse(Contagion.shouldInfect(1.0, 3.0, false, false, 0.99, 0.10));
   }

   @Test
   void everyNewConditionIsPresentAndNamedForTheCommand() {
      for (String id : new String[]{"alpha_gal", "b12_deficiency", "covid_19", "erectile_dysfunction", "ebola", "e_coli", "mono", "baldness", "dysgraphia", "evil"}) {
         assertTrue(PlusDisability.find(id) != null, id + " must be giveable");
      }
   }
}
