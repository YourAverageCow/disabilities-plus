package com.disabilitiesplus;

import com.studiobing.disabilities.Disability;
import com.studiobing.disabilities.RunState;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Server-side mechanics of the ten disabilities added in 1.1: the allergy, the deficiency, the four infectious
 * illnesses (which run their course and can spread between players), and the quirks that need server state.
 */
final class PlusConditions {
   private static final Random RNG = new Random();
   private static final String NS = PlusNet.MOD_ID;

   /** The illnesses that run their course. Index into {@link State#ill} and {@link State#immune}. */
   private static final PlusDisability[] ACUTE = {PlusDisability.COVID_19, PlusDisability.EBOLA, PlusDisability.E_COLI, PlusDisability.MONO};
   private static final int[] ACUTE_TICKS = {9600, 14400, 7200, 12000};
   private static final int COVID = 0;
   private static final int EBOLA = 1;
   private static final int E_COLI = 2;
   private static final int MONO = 3;
   /** After recovering you cannot catch the same thing again for ten minutes. */
   private static final int IMMUNITY_TICKS = 12000;

   private static final int COVID_QUIET_TICKS = 1200;
   private static final double COVID_RADIUS = 3.0;
   private static final double COVID_CHANCE = 0.10;
   private static final double EBOLA_RADIUS = 2.0;
   private static final double EBOLA_CHANCE = 0.15;
   private static final int SPREAD_INTERVAL_TICKS = 20;

   private static final int EBOLA_LOSS_INTERVAL_TICKS = 900;
   private static final int EBOLA_MAX_LOSS = 16;
   private static final float SPLEEN_DAMAGE = 4.0F;
   private static final float SPLEEN_FLOOR_HEALTH = 2.0F;
   private static final int SPLEEN_BLACKOUT_TICKS = 100;
   private static final float SPLEEN_HIT = 6.0F;
   private static final int SPLEEN_COOLDOWN_TICKS = 1200;

   private static final int ALLERGY_DELAY_TICKS = 800;
   private static final float ALLERGY_DAMAGE = 4.0F;
   private static final float ALLERGY_FLOOR_HEALTH = 2.0F;
   private static final int ALLERGY_FOOD_LOSS = 8;

   private static final int FEVER_TICKS = 400;
   private static final int FEVER_MIN_GAP_TICKS = 1200;
   private static final int FEVER_GAP_SPAN_TICKS = 1200;
   private static final int SNEEZE_MIN_GAP_TICKS = 1200;
   private static final int SNEEZE_GAP_SPAN_TICKS = 2400;
   private static final double SNEEZE_ALERT_RANGE = 16.0;
   private static final int FOOD_POISON_TICKS = 500;
   private static final int WATER_POISON_TICKS = 300;
   private static final int SWELLING_TICKS = 400;
   private static final int B12_START_TICKS = 6000;
   private static final int B12_MEAL_TICKS = 9000;
   private static final int B12_MAX_TICKS = 18000;

   private static final double AUDIENCE_RANGE = 8.0;
   private static final int CRAVING_GIVE_UP_TICKS = 12000;
   private static final int CRAVING_REST_TICKS = 3600;
   private static final int CRAVING_REGEN_TICKS = 200;
   private static final Item[] CRAVINGS = {
      Items.PUMPKIN_PIE, Items.COOKED_SALMON, Items.MELON_SLICE, Items.BAKED_POTATO, Items.HONEY_BOTTLE, Items.SWEET_BERRIES,
      Items.COOKED_PORKCHOP, Items.CARROT, Items.COOKIE, Items.BREAD, Items.APPLE, Items.COOKED_CHICKEN
   };
   private static final int WATER_TICKS = 80;
   private static final int WATER_COOLDOWN_TICKS = 600;
   private static final int RAW_FOOD_LOSS = 6;

   private static final int NAP_TICKS = 80;
   private static final int NAP_MIN_GAP_TICKS = 1200;
   private static final int NAP_GAP_SPAN_TICKS = 1200;

   private static final int SCALP_CHILL_PER_TICK = 3;
   private static final double EVIL_RANGE = 40.0;
   private static final int EVIL_INTERVAL_TICKS = 20;

   private static final Identifier EBOLA_HEALTH_ID = Identifier.fromNamespaceAndPath(NS, "ebola_health");
   private static final Identifier B12_STEP_ID = Identifier.fromNamespaceAndPath(NS, "b12_step");

   /** Mammal meat: the only thing that sets off an Alpha-gal reaction. */
   private static final Set<Item> MAMMAL_MEAT = Set.of(
      Items.BEEF, Items.COOKED_BEEF, Items.PORKCHOP, Items.COOKED_PORKCHOP, Items.MUTTON, Items.COOKED_MUTTON,
      Items.RABBIT, Items.COOKED_RABBIT, Items.RABBIT_STEW
   );
   /** Animal foods: what keeps a B12 supply topped up. */
   private static final Set<Item> B12_FOODS = Set.of(
      Items.BEEF, Items.COOKED_BEEF, Items.PORKCHOP, Items.COOKED_PORKCHOP, Items.MUTTON, Items.COOKED_MUTTON,
      Items.CHICKEN, Items.COOKED_CHICKEN, Items.RABBIT, Items.COOKED_RABBIT, Items.RABBIT_STEW,
      Items.COD, Items.COOKED_COD, Items.SALMON, Items.COOKED_SALMON, Items.MILK_BUCKET
   );
   /** Food that has not been cooked: it upsets an E. coli stomach. */
   private static final Set<Item> RAW_FOOD = Set.of(
      Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.MUTTON, Items.RABBIT, Items.COD, Items.SALMON,
      Items.ROTTEN_FLESH, Items.SPIDER_EYE
   );

   /** 1-in-N chances per qualifying tick; plain fields so the gametest can force them. */
   static int coughOdds = 400;
   static int sustainOdds = 120;
   /** When set, baldness chills as if the player were standing in a snowfield. */
   static boolean forceCold = false;

   private PlusConditions() {
   }

   /** Per-player state of the conditions; lives in {@link PlusEffects.Effects}. */
   static final class State {
      /** Ticks left of each acute illness, or 0 while it has not started. */
      final int[] ill = new int[ACUTE.length];
      final int[] immune = new int[ACUTE.length];
      int allergyDelay = 0;
      int b12Supply = -1;
      int waterTicks = 0;
      int waterCooldown = 0;
      int napTimer = 0;
      int spleenCooldown = 0;
      int feverTimer = 0;
      int feverTicks = 0;
      int sneezeTimer = 0;
      Item craving = null;
      int cravingTimer = 0;

      int illTicksSoFar(int which) {
         return ACUTE_TICKS[which] - this.ill[which];
      }
   }

   // ------------------------------------------------------------------ per tick

   static void tick(ServerPlayer sp, ServerLevel sw, PlusEffects.Effects fx) {
      State c = fx.cond;
      UUID id = sp.getUUID();
      handleAlphaGal(sp, sw, fx, c);
      handleB12(sp, fx, c);
      handleAcute(sp, c);
      handleCovid(sp, c);
      handleEbola(sp, c);
      handleEcoli(sp, fx, c);
      handleMono(sp, c);
      handleSneezing(sp, sw, c);
      handleSustain(sp, sw);
      handlePregnancy(sp, fx, c);
      handleBaldness(sp, sw);
      handleEvil(sp, sw);
      if (c.spleenCooldown > 0) {
         c.spleenCooldown--;
      }

      if (!PlusState.has(id, PlusDisability.MONO)) {
         c.spleenCooldown = 0;
      }
   }

   private static boolean has(ServerPlayer sp, PlusDisability d) {
      return PlusState.has(sp.getUUID(), d);
   }

   /** Tells the player something unless Anosognosia hides their own conditions from them. */
   private static void notice(ServerPlayer sp, String message) {
      if (!RunState.has(sp.getUUID(), Disability.ANOSOGNOSIA)) {
         sp.sendSystemMessage(Component.literal(message));
      }
   }

   // ------------------------------------------------------------------ Alpha-gal

   /** Mammal meat sets off a reaction about 40 seconds later: swollen hands, an emptied stomach, and a painful hit. */
   private static void handleAlphaGal(ServerPlayer sp, ServerLevel sw, PlusEffects.Effects fx, State c) {
      if (!has(sp, PlusDisability.ALPHA_GAL)) {
         c.allergyDelay = 0;
         return;
      }

      if (c.allergyDelay == 0 && fx.finished != null && MAMMAL_MEAT.contains(fx.finished)) {
         c.allergyDelay = ALLERGY_DELAY_TICKS;
      } else if (c.allergyDelay > 0 && --c.allergyDelay == 0 && PlusEffects.afflictable(sp)) {
         FoodData food = sp.getFoodData();
         food.setFoodLevel(Math.max(0, food.getFoodLevel() - ALLERGY_FOOD_LOSS));
         sp.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, SWELLING_TICKS, 2));
         sp.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, SWELLING_TICKS, 1));
         float dmg = Math.min(ALLERGY_DAMAGE, sp.getHealth() - ALLERGY_FLOOR_HEALTH);
         if (dmg > 0.0F) {
            sp.hurtServer(sw, PlusEffects.damageSource(sw, PlusEffects.ALLERGIC_DAMAGE), dmg);
         }

         notice(sp, "§cYour hands swell and your throat tightens: that was meat.");
      }
   }

   // ------------------------------------------------------------------ B12

   /** Animal foods top up a supply that drains all the time; with it empty your legs are numb and cannot step up a block. */
   private static void handleB12(ServerPlayer sp, PlusEffects.Effects fx, State c) {
      if (!has(sp, PlusDisability.B12_DEFICIENCY)) {
         c.b12Supply = -1;
         PlusEffects.toggleModifier(sp.getAttribute(Attributes.STEP_HEIGHT), B12_STEP_ID, false, 0.0, Operation.ADD_VALUE);
         return;
      }

      if (c.b12Supply < 0) {
         c.b12Supply = B12_START_TICKS;
      }

      boolean ateB12 = fx.ateCakeSlice || fx.finished != null && B12_FOODS.contains(fx.finished);
      if (ateB12) {
         c.b12Supply = Math.min(B12_MAX_TICKS, c.b12Supply + B12_MEAL_TICKS);
      } else if (c.b12Supply > 0) {
         c.b12Supply--;
      }

      // The base step height is 0.6, so -0.6 leaves none: stairs and slabs have to be jumped.
      PlusEffects.toggleModifier(sp.getAttribute(Attributes.STEP_HEIGHT), B12_STEP_ID, c.b12Supply == 0, -0.6, Operation.ADD_VALUE);
   }

   // ------------------------------------------------------------------ acute illnesses

   /** Starts each illness's clock when it is first seen, and recovers the player when it runs out. */
   private static void handleAcute(ServerPlayer sp, State c) {
      for (int i = 0; i < ACUTE.length; i++) {
         if (c.immune[i] > 0) {
            c.immune[i]--;
         }

         if (!has(sp, ACUTE[i])) {
            c.ill[i] = 0;
         } else if (c.ill[i] == 0) {
            c.ill[i] = ACUTE_TICKS[i];
         } else if (--c.ill[i] == 0) {
            PlusState.remove(sp.getUUID(), ACUTE[i]);
            c.immune[i] = IMMUNITY_TICKS;
            notice(sp, "§aYou recovered from " + ACUTE[i].displayName + ".");
         }
      }
   }

   /** After the first minute: fits of coughing, and now and then a fever spike that leaves you weak and flushed. */
   private static void handleCovid(ServerPlayer sp, State c) {
      boolean symptomatic = has(sp, PlusDisability.COVID_19) && c.illTicksSoFar(COVID) >= COVID_QUIET_TICKS && PlusEffects.afflictable(sp);
      if (!symptomatic) {
         c.feverTimer = 0;
         c.feverTicks = 0;
         return;
      }

      if (RNG.nextInt(coughOdds) == 0) {
         PlusEffects.sendEpisode(sp, PlusNet.EpisodePayload.COUGH, 60);
      }

      if (c.feverTicks > 0) {
         c.feverTicks--;
         if (c.feverTicks % 20 == 0) {
            PlusEffects.applyQuiet(sp, MobEffects.WEAKNESS, 60, 0);
         }
      } else if (c.feverTimer <= 0) {
         c.feverTimer = FEVER_MIN_GAP_TICKS + RNG.nextInt(FEVER_GAP_SPAN_TICKS);
      } else if (--c.feverTimer == 0) {
         c.feverTicks = FEVER_TICKS;
         PlusEffects.sendEpisode(sp, PlusNet.EpisodePayload.FEVER, FEVER_TICKS);
      }
   }

   /** The disease wastes your maximum health away a heart at a time and leaves you weak. */
   private static void handleEbola(ServerPlayer sp, State c) {
      boolean sick = has(sp, PlusDisability.EBOLA) && c.ill[EBOLA] > 0;
      int loss = sick ? Math.min(EBOLA_MAX_LOSS, 2 * (c.illTicksSoFar(EBOLA) / EBOLA_LOSS_INTERVAL_TICKS)) : 0;
      PlusEffects.setModifier(sp.getAttribute(Attributes.MAX_HEALTH), EBOLA_HEALTH_ID, -loss, Operation.ADD_VALUE);
      if (sick && sp.tickCount % 20 == 0 && PlusEffects.afflictable(sp)) {
         PlusEffects.applyQuiet(sp, MobEffects.WEAKNESS, 60, 0);
      }
   }

   /** Raw food and dirty water run straight through you: a good chunk of your hunger goes, and the rest drains away. */
   private static void handleEcoli(ServerPlayer sp, PlusEffects.Effects fx, State c) {
      if (!has(sp, PlusDisability.E_COLI)) {
         c.waterTicks = 0;
         c.waterCooldown = 0;
         return;
      }

      if (fx.finished != null && RAW_FOOD.contains(fx.finished)) {
         FoodData food = sp.getFoodData();
         food.setFoodLevel(Math.max(0, food.getFoodLevel() - RAW_FOOD_LOSS));
         sp.addEffect(new MobEffectInstance(MobEffects.HUNGER, FOOD_POISON_TICKS, 1));
      }

      if (c.waterCooldown > 0) {
         c.waterCooldown--;
      }

      c.waterTicks = sp.isInWater() ? c.waterTicks + 1 : 0;
      if (c.waterTicks >= WATER_TICKS && c.waterCooldown == 0 && PlusEffects.afflictable(sp)) {
         c.waterTicks = 0;
         c.waterCooldown = WATER_COOLDOWN_TICKS;
         sp.addEffect(new MobEffectInstance(MobEffects.HUNGER, WATER_POISON_TICKS, 1));
      }
   }

   /** Exhaustion: you drop off for a few seconds every minute or two. */
   private static void handleMono(ServerPlayer sp, State c) {
      if (!has(sp, PlusDisability.MONO)) {
         c.napTimer = 0;
      } else if (c.napTimer <= 0) {
         c.napTimer = NAP_MIN_GAP_TICKS + RNG.nextInt(NAP_GAP_SPAN_TICKS);
      } else if (--c.napTimer == 0 && PlusEffects.afflictable(sp)) {
         PlusEffects.sendEpisode(sp, PlusNet.EpisodePayload.NAP, NAP_TICKS);
      }
   }

   /** A hard hit can rupture the spleen of someone with mono: a heavy internal blow and a blackout, then a cooldown. */
   static void afterHurt(ServerPlayer sp, ServerLevel sw, PlusEffects.Effects fx, float taken) {
      State c = fx.cond;
      if (taken >= SPLEEN_HIT && c.spleenCooldown == 0 && has(sp, PlusDisability.MONO)) {
         c.spleenCooldown = SPLEEN_COOLDOWN_TICKS;
         float dmg = Math.min(SPLEEN_DAMAGE, sp.getHealth() - SPLEEN_FLOOR_HEALTH);
         if (dmg > 0.0F) {
            // The blow that caused this has just started invulnerability frames; the bleeding damage type is tagged
            // bypasses_cooldown so the rupture is not absorbed by them.
            sp.hurtServer(sw, PlusEffects.damageSource(sw, PlusEffects.BLEEDING_DAMAGE), dmg);
         }

         sp.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, SPLEEN_BLACKOUT_TICKS, 0, false, false, false));
         notice(sp, "§cSomething tears inside you: your spleen has ruptured.");
      }
   }

   /**
    * Pregnancy: the baby craves a particular food. Eating it is rewarded with regeneration; ignoring it costs nothing,
    * the craving just changes after ten minutes.
    */
   private static void handlePregnancy(ServerPlayer sp, PlusEffects.Effects fx, State c) {
      if (!has(sp, PlusDisability.PREGNANCY)) {
         c.craving = null;
         c.cravingTimer = 0;
      } else if (c.craving == null) {
         if (c.cravingTimer > 0) {
            c.cravingTimer--;
         } else {
            c.craving = CRAVINGS[RNG.nextInt(CRAVINGS.length)];
            c.cravingTimer = CRAVING_GIVE_UP_TICKS;
            PlusEffects.hint(sp, "\u00a7dThe baby is craving " + new net.minecraft.world.item.ItemStack(c.craving).getHoverName().getString() + ".");
         }
      } else if (fx.finished == c.craving) {
         sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, CRAVING_REGEN_TICKS, 1));
         PlusEffects.hint(sp, "\u00a7dThe baby is content.");
         c.craving = null;
         c.cravingTimer = CRAVING_REST_TICKS;
      } else if (--c.cravingTimer <= 0) {
         c.craving = null;
         c.cravingTimer = 0;
      }
   }

   /** Sneezing Fits: every minute or two a sneeze jerks your head and wakes every monster within 16 blocks. */
   private static void handleSneezing(ServerPlayer sp, ServerLevel sw, State c) {
      if (!has(sp, PlusDisability.SNEEZING)) {
         c.sneezeTimer = 0;
      } else if (c.sneezeTimer <= 0) {
         c.sneezeTimer = SNEEZE_MIN_GAP_TICKS + RNG.nextInt(SNEEZE_GAP_SPAN_TICKS);
      } else if (--c.sneezeTimer == 0 && PlusEffects.afflictable(sp)) {
         PlusEffects.sendEpisode(sp, PlusNet.EpisodePayload.SNEEZE, 0);
         sw.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PANDA_SNEEZE, SoundSource.PLAYERS, 1.0F, 1.0F);
         for (Mob mob : sw.getEntitiesOfClass(Mob.class, sp.getBoundingBox().inflate(SNEEZE_ALERT_RANGE), m -> m.isAlive() && m instanceof Enemy)) {
            mob.setTarget(sp);
         }
      }
   }

   // ------------------------------------------------------------------ contagion

   /** Once a second, COVID-19 and Ebola carriers may pass the illness to a player standing close by. */
   static void spread(MinecraftServer server) {
      if (server.getTickCount() % SPREAD_INTERVAL_TICKS != 0) {
         return;
      }

      List<ServerPlayer> players = new ArrayList<>(server.getPlayerList().getPlayers());
      if (players.size() < 2) {
         return;
      }

      for (ServerPlayer carrier : players) {
         if (!carrier.isDeadOrDying()) {
            spreadFrom(carrier, players, PlusDisability.COVID_19, COVID, COVID_RADIUS, COVID_CHANCE);
            spreadFrom(carrier, players, PlusDisability.EBOLA, EBOLA, EBOLA_RADIUS, EBOLA_CHANCE);
         }
      }
   }

   private static void spreadFrom(ServerPlayer carrier, List<ServerPlayer> players, PlusDisability d, int which, double radius, double chance) {
      if (!has(carrier, d)) {
         return;
      }

      for (ServerPlayer other : players) {
         if (other != carrier && other.level() == carrier.level() && PlusEffects.afflictable(other)) {
            boolean immune = PlusEffects.of(other.getUUID()).cond.immune[which] > 0;
            if (Contagion.shouldInfect(carrier.distanceToSqr(other), radius, has(other, d), immune, RNG.nextDouble(), chance)) {
               PlusState.give(other.getUUID(), d);
               notice(other, "§cYou caught " + d.displayName + " from " + carrier.getName().getString() + ".");
            }
         }
      }
   }

   /** Death clears every infectious illness (and a fresh body is not immune). */
   static void cureOnDeath(ServerPlayer sp) {
      State c = PlusEffects.of(sp.getUUID()).cond;
      for (int i = 0; i < ACUTE.length; i++) {
         c.ill[i] = 0;
         c.immune[i] = 0;
         if (PlusState.remove(sp.getUUID(), ACUTE[i])) {
            notice(sp, "§aDying cured your " + ACUTE[i].displayName + ".");
         }
      }
   }

   // ------------------------------------------------------------------ the rest

   /**
    * Cannot sustain: using an item (drawing, eating, blocking) cuts out. Alone it is rare; the more people and animals
    * are watching within eight blocks, the more likely it is.
    */
   private static void handleSustain(ServerPlayer sp, ServerLevel sw) {
      if (has(sp, PlusDisability.ERECTILE_DYSFUNCTION) && sp.isUsingItem()) {
         int audience = 0;
         for (net.minecraft.world.entity.LivingEntity watcher : sw.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
            sp.getBoundingBox().inflate(AUDIENCE_RANGE), e -> e != sp && e.isAlive())) {
            // A person watching is twice as bad as an animal.
            audience += watcher instanceof net.minecraft.world.entity.player.Player ? 2 : 1;
         }

         if (RNG.nextInt(Odds.audience(sustainOdds, audience)) == 0) {
            sp.stopUsingItem();
         }
      }
   }

   /** Nothing on your head: a cold biome or rain bites at the scalp and freezes you fast. */
   private static void handleBaldness(ServerPlayer sp, ServerLevel sw) {
      if (has(sp, PlusDisability.BALDNESS) && PlusEffects.afflictable(sp) && sp.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
         BlockPos pos = sp.blockPosition();
         boolean cold = forceCold || sw.getBiome(pos).value().coldEnoughToSnow(pos, sw.getSeaLevel()) || sw.isRainingAt(pos);
         if (cold) {
            sp.setTicksFrozen(Math.min(sp.getTicksRequiredToFreeze(), sp.getTicksFrozen() + SCALP_CHILL_PER_TICK));
         }
      }
   }

   /** Everything hostile or neutral within 40 blocks is set on you, and neutrals stay angry. */
   private static void handleEvil(ServerPlayer sp, ServerLevel sw) {
      if (!has(sp, PlusDisability.EVIL) || !PlusEffects.afflictable(sp) || sp.tickCount % EVIL_INTERVAL_TICKS != 0) {
         return;
      }

      for (Mob mob : sw.getEntitiesOfClass(Mob.class, sp.getBoundingBox().inflate(EVIL_RANGE), m -> m.isAlive() && (m instanceof Enemy || m instanceof NeutralMob))) {
         if (mob.getTarget() != sp) {
            mob.setTarget(sp);
         }

         if (mob instanceof NeutralMob neutral) {
            neutral.startPersistentAngerTimer();
         }
      }
   }
}
