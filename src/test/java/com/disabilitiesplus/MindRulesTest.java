package com.disabilitiesplus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MindRulesTest {
   @Test
   void youAreWatchedWithinFortyDegreesOfTheirGaze() {
      assertTrue(MindRules.facing(0, 0, 1, 0, 0, 5), "straight ahead");
      assertTrue(MindRules.facing(0, 0, 1, 1, 0, 5), "a little off");
      assertFalse(MindRules.facing(0, 0, 1, 5, 0, 1), "well off to the side");
      assertFalse(MindRules.facing(0, 0, 1, 0, 0, -5), "behind them");
      assertFalse(MindRules.facing(0, 0, 0, 0, 0, 5), "no gaze");
   }

   @Test
   void anxietyBuildsWithWatchersAndEasesWithoutThemOrWithAFriend() {
      assertEquals(3.0F, MindRules.anxietyNext(0.0F, 1, false), 1.0E-6F);
      assertEquals(12.0F, MindRules.anxietyNext(0.0F, 4, false), 1.0E-6F);
      assertEquals(12.0F, MindRules.anxietyNext(0.0F, 9, false), 1.0E-6F, "capped at four watchers");
      assertEquals(6.0F, MindRules.anxietyNext(10.0F, 0, false), 1.0E-6F);
      assertEquals(0.0F, MindRules.anxietyNext(2.0F, 0, true), 1.0E-6F, "never below zero");
      assertTrue(MindRules.anxietyNext(50.0F, 3, true) < 50.0F, "a close friend outweighs three watchers");
      assertEquals(100.0F, MindRules.anxietyNext(99.0F, 4, false), 1.0E-6F, "capped at 100");
   }

   @Test
   void anxietyStages() {
      assertEquals(0, MindRules.anxietyStage(29.9F));
      assertEquals(1, MindRules.anxietyStage(30.0F));
      assertEquals(2, MindRules.anxietyStage(60.0F));
      assertEquals(3, MindRules.anxietyStage(90.0F));
   }

   @Test
   void laughterIsRecognisedButOrdinaryTalkIsNot() {
      for (String funny : new String[]{"lol", "LOL that was great", "lmaooo", "hahaha", "hehe", "xd", "rofl", "kekw", "ok haha ok", "\ud83d\ude02"}) {
         assertTrue(MindRules.isLaughter(funny), funny);
      }

      for (String plain : new String[]{"hello", "follow me", "I found the village", "ha", "he said no", "", "lolly shop"}) {
         assertFalse(MindRules.isLaughter(plain), plain);
      }
   }

   @Test
   void woundsStackToThirtySecondsAndOnlyComeFromHardHits() {
      assertEquals(0, MindRules.woundAfter(0, 2.9F));
      assertEquals(200, MindRules.woundAfter(0, 3.0F));
      assertEquals(400, MindRules.woundAfter(200, 6.0F));
      assertEquals(600, MindRules.woundAfter(550, 6.0F));
   }

   @Test
   void nonLethalNeverCrossesTheFloor() {
      assertEquals(4.0F, MindRules.nonLethal(4.0F, 20.0F, 2.0F), 1.0E-6F);
      assertEquals(1.0F, MindRules.nonLethal(4.0F, 3.0F, 2.0F), 1.0E-6F);
      assertEquals(0.0F, MindRules.nonLethal(4.0F, 1.0F, 2.0F), 1.0E-6F);
   }
}
