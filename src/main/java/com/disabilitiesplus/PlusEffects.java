package com.disabilitiesplus;

import com.studiobing.disabilities.Disability;
import com.studiobing.disabilities.RunState;
import com.studiobing.disabilities.Sugar;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Server-side mechanics of the add-on's disabilities. Original disabilities are only ever read through RunState. */
public final class PlusEffects {
   static final Random RNG = new Random();
   private static final Map<UUID, Effects> STATE = new ConcurrentHashMap<>();
   private static final String NS = PlusNet.MOD_ID;
   /** Borrowed from the original mod (its death message reads "couldn't breathe anymore"). */
   private static final ResourceKey<DamageType> ASTHMA_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("disabilities", "asthma"));
   static final ResourceKey<DamageType> BLEEDING_DAMAGE = damageKey("bleeding");
   static final ResourceKey<DamageType> ALLERGIC_DAMAGE = damageKey("allergic_reaction");
   private static final int BLEED_INTERVAL_TICKS = 40;
   private static final int BUZZ_TICKS = 1800;
   private static final Identifier CANNON_ATTACK_ID = Identifier.fromNamespaceAndPath(NS, "cannon_attack");
   private static final Identifier CANNON_HEALTH_ID = Identifier.fromNamespaceAndPath(NS, "cannon_health");
   private static final int DUST_LIMIT = 12;
   private static final int FEAR_SCAN_INTERVAL_TICKS = 10;
   private static final Identifier FRAIL_ATTACK_ID = Identifier.fromNamespaceAndPath(NS, "frail_attack");
   private static final Identifier FRAIL_HEALTH_ID = Identifier.fromNamespaceAndPath(NS, "frail_health");
   private static final Identifier GIANT_BLOCK_REACH_ID = Identifier.fromNamespaceAndPath(NS, "giant_block_reach");
   private static final Identifier GIANT_ENTITY_REACH_ID = Identifier.fromNamespaceAndPath(NS, "giant_entity_reach");
   private static final Identifier GIANT_SCALE_ID = Identifier.fromNamespaceAndPath(NS, "giant_scale");
   private static final Identifier HANGRY_MINE_ID = Identifier.fromNamespaceAndPath(NS, "hangry_mine");
   private static final ResourceKey<DamageType> HEART_DAMAGE = damageKey("heart_attack");
   private static final double HEART_STRAIN_LIMIT = 300.0;
   private static final Identifier HOARD_HEAVY_ID = Identifier.fromNamespaceAndPath(NS, "hoarder_heavy");
   private static final int HOARD_HEAVY_SLOTS = 28;
   private static final Identifier HOARD_LIGHT_ID = Identifier.fromNamespaceAndPath(NS, "hoarder_light");
   private static final int HOARD_LIGHT_SLOTS = 18;
   private static final ResourceKey<DamageType> KIDNEY_DAMAGE = damageKey("kidney_stone");
   private static final Identifier LEAD_HEALTH_ID = Identifier.fromNamespaceAndPath(NS, "lead_health");
   private static final Identifier LEAD_LOAD_ID = Identifier.fromNamespaceAndPath(NS, "lead_load");
   private static final Identifier LOW_MINE_ID = Identifier.fromNamespaceAndPath(NS, "low_mine");
   private static final Identifier LOW_SPEED_ID = Identifier.fromNamespaceAndPath(NS, "low_speed");
   private static final Identifier MANIC_MINE_ID = Identifier.fromNamespaceAndPath(NS, "manic_mine");
   private static final Identifier MANIC_SPEED_ID = Identifier.fromNamespaceAndPath(NS, "manic_speed");
   private static final int MOOD_MIN_TICKS = 3600;
   private static final int MOOD_SPAN_TICKS = 3600;
   private static final Identifier MOON_GRAVITY_ID = Identifier.fromNamespaceAndPath(NS, "moon_gravity");
   private static final int NECROSIS_STAGE_TICKS = 6000;
   private static final Identifier NECRO_ATTACK_ID = Identifier.fromNamespaceAndPath(NS, "necro_attack");
   private static final Identifier NECRO_MINE_ID = Identifier.fromNamespaceAndPath(NS, "necro_mine");
   private static final Identifier NECRO_SPEED_ID = Identifier.fromNamespaceAndPath(NS, "necro_speed");
   private static final Identifier OBESE_JUMP_ID = Identifier.fromNamespaceAndPath(NS, "obese_jump");
   private static final int PICKY_COOLDOWN_TICKS = 600;
   private static final int LANDING_AIR_TICKS = 6;
   private static final Identifier STONED_REFLEX_ID = Identifier.fromNamespaceAndPath(NS, "stoned_reflex");
   private static final int DUST_MAX = 16;
   private static final double SCARED_RANGE = 12.0;
   private static final int IRON_TICKS = 6000;
   private static final int CRACK_TICKS = 3600;
   private static final float CRACK_HIT = 4.0F;
   private static final int MAX_CRACKS = 3;
   private static final Identifier CRACK_ID = Identifier.fromNamespaceAndPath(NS, "cannon_crack");
   private static final float STOMP_MIN_FALL = 3.0F;
   private static final double STOMP_RADIUS = 4.5;
   private static final double STOMP_ALERT_RANGE = 12.0;
   private static final float BOUNCE_MIN_FALL = 2.0F;
   private static final double LOW_GRAVITY = 0.032;
   private static final double BOUNCE_SHARE_RANGE = 3.0;
   private static final double STEADY_RANGE = 2.5;
   private static final double BOUNCE_KEEP = 0.6;
   private static final Identifier HOARD_LIGHT_ARMOR_ID = Identifier.fromNamespaceAndPath(NS, "hoarder_light_armor");
   private static final Identifier HOARD_HEAVY_ARMOR_ID = Identifier.fromNamespaceAndPath(NS, "hoarder_heavy_armor");
   private static final Identifier OBESE_RAM_ID = Identifier.fromNamespaceAndPath(NS, "obese_ram");
   private static final double SQUEAK_STRIDE = 1.5;
   private static final double SQUEAK_LURE_RANGE = 12.0;
   private static final float HORIZON_PITCH = 15.0F;
   private static final int HYDRATED_TICKS = 3600;
   private static final int PAINLESS_STONE_TICKS = 40;
   private static final int CHELATED_TICKS = 6000;
   /** Iron-rich foods: eating one thickens an anemic person's blood for five minutes. */
   private static final Set<Item> IRON_FOODS = Set.of(
      Items.BEEF, Items.COOKED_BEEF, Items.MUTTON, Items.COOKED_MUTTON, Items.PORKCHOP, Items.COOKED_PORKCHOP,
      Items.RABBIT, Items.COOKED_RABBIT, Items.RABBIT_STEW, Items.BEETROOT, Items.BEETROOT_SOUP
   );
   private static final Identifier PREGNANT_SPEED_ID = Identifier.fromNamespaceAndPath(NS, "pregnant_speed");
   private static final Identifier QUAD_JUMP_ID = Identifier.fromNamespaceAndPath(NS, "quad_jump");
   private static final Identifier QUAD_SPEED_ID = Identifier.fromNamespaceAndPath(NS, "quad_speed");
   private static final int RUSH_TICKS = 1200;
   /** The only foods someone with ARFID can keep down. */
   private static final Set<Item> SAFE_FOODS = Set.of(Items.BREAD, Items.BAKED_POTATO, Items.COOKED_CHICKEN, Items.APPLE, Items.COOKIE);
   private static final int STONE_EPISODE_TICKS = 100;
   private static final int SUGAR_CRASH_TICKS = 200;
   private static final int SUGAR_RUSH_TICKS = 600;
   private static final double TIGHT_AIR_DRAIN_PER_TICK = 1.5;

   /** 1-in-N chances per qualifying tick, and craving limits; plain fields so the gametest can force them. */
   static int butterfingersOdds = 2400;
   static int eggOdds = 1200;
   /** A stumble happens on 1 in this many landings (Two Left Feet). */
   static int tripOdds = 3;
   /** Squeaky Shoes lures, counted so the gametest can see them. */
   static volatile int squeakLures = 0;
   static int alcoholDryLimit = 4800;
   static int drugDryLimit = 6000;

   private static final int BLEED_BASE_TICKS = 100;
   private static final int BLEED_TICKS_PER_DAMAGE = 20;
   private static final int BLEED_MAX_TICKS = 600;

   private PlusEffects() {
   }

   public static Effects of(UUID id) {
      return STATE.computeIfAbsent(id, k -> new Effects());
   }

   public static void forget(UUID id) {
      STATE.remove(id);
   }

   public static void tick(MinecraftServer server) {
      for (ServerPlayer sp : new ArrayList<>(server.getPlayerList().getPlayers())) {
         ServerLevel st = sp.level();
         Effects stx = of(sp.getUUID());
         trackConsumption(sp, stx);
         trackLanding(sp, stx);
         PlusSocial.updateCarried(sp, st);
         handleAnemia(sp, stx);
         handleHemophilia(sp, st, stx);
         handlePhotosensitivity(sp, st);
         handleHeartCondition(sp, st, stx);
         handleMotionSickness(sp, stx);
         handleClaustrophobia(sp, st, stx);
         handleButterfingers(sp);
         handleHangry(sp);
         handleTwoLeftFeet(sp, stx);
         handleBipolar(sp, stx);
         handlePickyEater(sp, stx);
         handleSweetTooth(sp, stx);
         handleEggLayer(sp, st);
         handleMoonBoots(sp, st, stx);
         handleGiant(sp, st, stx);
         handleGlassCannon(sp, stx);
         handleQuadrupleAmputee(sp);
         handlePregnancy(sp);
         handleObesity(sp);
         handleSqueakyShoes(sp, st, stx);
         handleOrphan(sp);
         handleAlcoholism(sp, stx);
         handleDrugAddiction(sp, stx);
         handleBloodHungry(sp);
         handleAquaphobia(sp);
         handleThrillSeeker(sp, st);
         handleNightOwl(sp, st);
         handleHoarder(sp, stx);
         handleKidneyStones(sp, st, stx);
         handleLeadPoisoning(sp, stx);
         handleBlackLung(sp, st, stx);
         handleNecrosis(sp, st, stx);
         handleStoned(sp);
         handleScaredyCat(sp, st);
         PlusConditions.tick(sp, st, stx);
         PlusAnxiety.tick(sp, st, stx);
         PlusAlienHand.tick(sp, st, stx);
         PlusCataplexy.tick(sp, stx);
         PlusNumbness.tick(sp, st, stx);
         PlusJoints.tick(sp, st, stx);
         handleEatingDisorders(sp, stx);
      }

      PlusConditions.spread(server);
      PlusSocial.applyHoarderCover(new ArrayList<>(server.getPlayerList().getPlayers()));
   }

   /** Notes the item a player has just finished eating or drinking, and cake slices (the original tracks these privately). */
   private static void trackConsumption(ServerPlayer sp, Effects st) {
      st.finished = null;
      if (st.fedItem != null) {
         // A friend fed you: for every condition that cares what you ate, you ate it.
         st.finished = st.fedItem;
         st.fedItem = null;
      }

      boolean using = sp.isUsingItem();
      int cakeSlices = sp.getStats().getValue(Stats.CUSTOM, Stats.EAT_CAKE_SLICE);
      st.ateCakeSlice = st.lastCakeSlices >= 0 && cakeSlices > st.lastCakeSlices;
      st.lastCakeSlices = cakeSlices;
      if (st.finished == null && st.prevUsingItem && !using && st.prevUseTimeLeft <= 1 && st.prevUseItem != null) {
         st.finished = st.prevUseItem;
      }

      if (using) {
         st.prevUseTimeLeft = sp.getUseItemRemainingTicks();
         st.prevUseItem = sp.getUseItem().getItem();
      }

      st.prevUsingItem = using;
   }

   /** Tracks how far a fall was when it ends, for the disabilities that react to landing (Giant, Moon Boots, Two Left Feet). */
   private static void trackLanding(ServerPlayer sp, Effects st) {
      st.landedFall = 0.0F;
      if (sp.onGround()) {
         st.landedFall = st.peakFall;
         st.peakFall = 0.0F;
      } else if (sp.isInWater() || sp.onClimbable()) {
         st.peakFall = 0.0F;
      } else {
         st.peakFall = Math.max(st.peakFall, (float)sp.fallDistance);
      }
   }

   /** A short hint above the hotbar, unless Anosognosia hides your own conditions from you. */
   static void hint(ServerPlayer sp, String message) {
      if (!RunState.has(sp.getUUID(), Disability.ANOSOGNOSIA)) {
         sp.sendOverlayMessage(Component.literal(message));
      }
   }

   /** Squeaky Shoes: every stride squeaks, and every squeak draws the hostile mobs around you toward you. Sneak to be silent. */
   private static void handleSqueakyShoes(ServerPlayer sp, ServerLevel sw, Effects st) {
      double moved = Math.hypot(sp.getX() - st.squeakX, sp.getZ() - st.squeakZ);
      st.squeakX = sp.getX();
      st.squeakZ = sp.getZ();
      if (!PlusState.has(sp.getUUID(), PlusDisability.SQUEAKY_SHOES) || !afflictable(sp)) {
         st.squeakDistance = 0.0;
      } else if (moved < 3.0 && sp.onGround() && !sp.isShiftKeyDown()) {
         st.squeakDistance += moved;
         if (st.squeakDistance >= SQUEAK_STRIDE) {
            st.squeakDistance = 0.0;
            // Everyone else within earshot hears it too (you hear your own on the client).
            sw.playSound(sp, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.PLAYERS, 0.6F, 1.9F + RNG.nextFloat() * 0.2F);
            for (net.minecraft.world.entity.Mob mob : sw.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, sp.getBoundingBox().inflate(SQUEAK_LURE_RANGE),
               m -> m.isAlive() && m instanceof net.minecraft.world.entity.monster.Enemy && m.getTarget() == null)) {
               mob.getNavigation().moveTo(sp.getX(), sp.getY(), sp.getZ(), 1.0);
               squeakLures++;
            }
         }
      }
   }

   /** Hemophilia: a sheet of paper is a bandage. Using one while you bleed stops it. Returns true when one was used. */
   static boolean bandage(ServerPlayer sp, net.minecraft.world.InteractionHand hand) {
      Effects st = of(sp.getUUID());
      ItemStack held = sp.getItemInHand(hand);
      if (PlusState.has(sp.getUUID(), PlusDisability.HEMOPHILIA) && st.bleedTicks > 0 && held.is(Items.PAPER)) {
         held.shrink(1);
         st.bleedTicks = 0;
         hint(sp, "\u00a7aYou bind the wound.");
         return true;
      }

      return false;
   }

   /** Attacker-side effects (Blood Hungry, Slippery Grip) and victim-side bleeding (Hemophilia). */
   public static void afterDamage(LivingEntity entity, DamageSource source, float base, float taken, boolean blocked) {
      if (taken > 0.0F && !PlusAlienHand.slapping && source.getEntity() instanceof ServerPlayer attacker && attacker != entity) {
         if (PlusState.has(attacker.getUUID(), PlusDisability.BLOOD_HUNGRY)) {
            attacker.heal(2.0F);
            attacker.getFoodData().eat(2, 0.5F);
         }

         wearTool(attacker);
         if (PlusState.has(attacker.getUUID(), PlusDisability.OBESITY) && attacker.isSprinting()) {
            ram(attacker, entity);
         }
      }

      if (taken > 0.0F && entity instanceof ServerPlayer sp && !source.is(BLEEDING_DAMAGE)) {
         Effects victim = of(sp.getUUID());
         if (PlusState.has(sp.getUUID(), PlusDisability.HEMOPHILIA)) {
            victim.bleedTicks = Math.min(BLEED_MAX_TICKS, victim.bleedTicks + BLEED_BASE_TICKS + (int)(taken * BLEED_TICKS_PER_DAMAGE));
         }

         if (PlusState.has(sp.getUUID(), PlusDisability.GLASS_CANNON) && taken >= CRACK_HIT) {
            victim.cracks = Math.min(MAX_CRACKS, victim.cracks + 1);
            victim.crackTicks = CRACK_TICKS;
            hint(sp, "\u00a7bCrack! You will not last long if that happens again.");
         }

         PlusConditions.afterHurt(sp, sp.level(), victim, taken);
         PlusNumbness.afterDamage(sp, taken);
         PlusJoints.afterDamage(sp, sp.level(), taken);
      }
   }

   /** Obesity: a sprinting hit from a heavy body sends the target flying. */
   private static void ram(ServerPlayer attacker, LivingEntity target) {
      double dx = target.getX() - attacker.getX();
      double dz = target.getZ() - attacker.getZ();
      double len = Math.sqrt(dx * dx + dz * dz);
      if (len > 1.0E-4) {
         target.setDeltaMovement(target.getDeltaMovement().add(dx / len * 0.9, 0.3, dz / len * 0.9));
         target.syncVelocity = true;
      }
   }

   /** Slippery Grip: you fumble your tools, so every swing and every block broken wears the held tool one extra use. */
   static void wearTool(ServerPlayer sp) {
      ItemStack held = sp.getMainHandItem();
      if (PlusState.has(sp.getUUID(), PlusDisability.SLIPPERY_GRIP) && held.isDamageableItem()) {
         held.hurtAndBreak(1, sp, net.minecraft.world.InteractionHand.MAIN_HAND);
      }
   }

   /** Sets a modifier on or off with a fixed amount. */
   static void toggleModifier(AttributeInstance inst, Identifier id, boolean want, double value, Operation op) {
      if (inst != null) {
         boolean has = inst.hasModifier(id);
         if (want && !has) {
            inst.addTransientModifier(new AttributeModifier(id, value, op));
         } else if (!want && has) {
            inst.removeModifier(id);
         }
      }
   }

   static void sendEpisode(ServerPlayer sp, int kind, int ticks) {
      if (ServerPlayNetworking.canSend(sp, PlusNet.EpisodePayload.ID)) {
         ServerPlayNetworking.send(sp, new PlusNet.EpisodePayload(kind, ticks));
      }
   }

   /**
    * Thin blood: whatever you heal, only half of it sticks, unless you have eaten something iron-rich (red meat, beetroot)
    * in the last five minutes.
    */
   private static void handleAnemia(ServerPlayer sp, Effects st) {
      if (!PlusState.has(sp.getUUID(), PlusDisability.ANEMIA) || !afflictable(sp)) {
         st.lastHealth = -1.0F;
         st.ironTicks = 0;
         return;
      }

      if (st.finished != null && IRON_FOODS.contains(st.finished)) {
         st.ironTicks = IRON_TICKS;
         hint(sp, "\u00a7cIron-rich: your blood thickens for a while.");
      }

      float now = sp.getHealth();
      if (st.ironTicks > 0) {
         st.ironTicks--;
      } else if (st.lastHealth >= 0.0F && now > st.lastHealth) {
         sp.setHealth(st.lastHealth + (now - st.lastHealth) / 2.0F);
      }

      st.lastHealth = sp.getHealth();
   }

   private static void handleHemophilia(ServerPlayer sp, ServerLevel sw, Effects st) {
      if (!PlusState.has(sp.getUUID(), PlusDisability.HEMOPHILIA) || sp.isDeadOrDying()) {
         st.bleedTicks = 0;
      } else if (st.bleedTicks > 0) {
         st.bleedTicks--;
         if (st.bleedTicks % BLEED_INTERVAL_TICKS == 0) {
            sp.hurtServer(sw, damageSource(sw, BLEEDING_DAMAGE), 1.0F);
         }
      }
   }

   private static void handlePhotosensitivity(ServerPlayer sp, ServerLevel sw) {
      if (sp.tickCount % 20 == 0
         && PlusState.has(sp.getUUID(), PlusDisability.PHOTOSENSITIVITY)
         && afflictable(sp)
         && sw.isBrightOutside()
         && !sp.isInWaterOrRain()
         && sp.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
         && sw.canSeeSky(sp.blockPosition().above())) {
         sp.igniteForSeconds(4.0F);
      }
   }

   private static void handleHeartCondition(ServerPlayer sp, ServerLevel sw, Effects st) {
      if (!PlusState.has(sp.getUUID(), PlusDisability.HEART_CONDITION) || !afflictable(sp)) {
         st.heartStrain = 0.0;
         return;
      }

      st.heartStrain = sp.isSprinting() ? st.heartStrain + 1.0 : Math.max(0.0, st.heartStrain - 0.5);
      if (st.heartStrain >= HEART_STRAIN_LIMIT) {
         st.heartStrain = 0.0;
         sp.hurtServer(sw, damageSource(sw, HEART_DAMAGE), 2.0F);
         sp.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
         sp.setSprinting(false);
      }
   }

   /** Riding or moving fast turns your stomach, unless you keep your eyes level on the horizon. */
   private static void handleMotionSickness(ServerPlayer sp, Effects st) {
      double dx = sp.getX() - st.lastX;
      double dz = sp.getZ() - st.lastZ;
      st.lastX = sp.getX();
      st.lastZ = sp.getZ();
      boolean moving = sp.isPassenger() || dx * dx + dz * dz > 0.36;
      boolean sick = PlusState.has(sp.getUUID(), PlusDisability.MOTION_SICKNESS) && moving && Math.abs(sp.getXRot()) > HORIZON_PITCH;
      st.motionTimer = sick ? st.motionTimer + 1 : 0;
      if (st.motionTimer >= 40) {
         st.motionTimer = 0;
         applyQuiet(sp, MobEffects.NAUSEA, 160, 0);
         if (!st.motionHinted) {
            st.motionHinted = true;
            hint(sp, "\u00a72Keep your eyes on the horizon.");
         }
      }
   }

   /** Tight spaces: the air runs out (like Asthma's lung meter) and you start to suffocate. */
   private static void handleClaustrophobia(ServerPlayer sp, ServerLevel sw, Effects st) {
      if (!PlusState.has(sp.getUUID(), PlusDisability.CLAUSTROPHOBIA) || !afflictable(sp)) {
         st.tightAir = -1.0;
         st.tight = false;
         return;
      }

      if (sp.tickCount % FEAR_SCAN_INTERVAL_TICKS == 0) {
         st.tight = Scans.enclosed(sw, sp.blockPosition().above());
      }

      int max = sp.getMaxAirSupply();
      if (st.tightAir < 0.0) {
         st.tightAir = max;
      }

      if (st.tight && !sp.isUnderWater()) {
         st.tightAir = Math.max(0.0, st.tightAir - TIGHT_AIR_DRAIN_PER_TICK);
         sp.setAirSupply((int)Math.round(st.tightAir));
         if (st.tightAir <= 0.0 && ++st.tightTimer >= 20) {
            st.tightTimer = 0;
            sp.hurtServer(sw, damageSource(sw, ASTHMA_DAMAGE), 1.0F);
         }
      } else {
         st.tightAir = max;
         st.tightTimer = 0;
      }
   }

   private static void handleButterfingers(ServerPlayer sp) {
      if (PlusState.has(sp.getUUID(), PlusDisability.BUTTERFINGERS)
         && !RunState.has(sp.getUUID(), Disability.KLEPTOMANIA)
         && afflictable(sp)
         && !sp.getMainHandItem().isEmpty()
         && RNG.nextInt(butterfingersOdds) == 0) {
         sp.drop(false);
      }
   }

   /** Below three shanks you are irritable and weak: mining is slower and you hit softer. Nothing drains faster. */
   private static void handleHangry(ServerPlayer sp) {
      boolean starving = PlusState.has(sp.getUUID(), PlusDisability.HANGRY) && sp.getFoodData().getFoodLevel() < 6;
      toggleModifier(sp.getAttribute(Attributes.BLOCK_BREAK_SPEED), HANGRY_MINE_ID, starving, -0.25, Operation.ADD_MULTIPLIED_TOTAL);
      if (starving && sp.tickCount % 20 == 0 && afflictable(sp)) {
         applyQuiet(sp, MobEffects.WEAKNESS, 60, 0);
      }
   }

   /** Your feet tangle on landing: after a real jump or drop you sometimes stagger and stop sprinting. */
   private static void handleTwoLeftFeet(ServerPlayer sp, Effects st) {
      if (!PlusState.has(sp.getUUID(), PlusDisability.TWO_LEFT_FEET) || !afflictable(sp)) {
         st.airTicks = 0;
      } else if (sp.onGround()) {
         // Landing in a crouch is a roll: no stumble.
         if (st.airTicks >= LANDING_AIR_TICKS && !sp.isShiftKeyDown() && RNG.nextInt(tripOdds) == 0
            && PlusSocial.nearbyPlayers(sp.level(), sp, STEADY_RANGE).isEmpty()) {
            sp.setSprinting(false);
            sp.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 3, false, false, false));
            if (!st.rollHinted) {
               st.rollHinted = true;
               hint(sp, "\u00a7dTip: land in a crouch to roll through it, or stay close to a friend who can steady you.");
            }
         }

         st.airTicks = 0;
      } else if (!sp.isInWater() && !sp.onClimbable()) {
         st.airTicks++;
      }
   }

   private static void handleBipolar(ServerPlayer sp, Effects st) {
      if (!PlusState.has(sp.getUUID(), PlusDisability.BIPOLAR)) {
         st.moodPhase = 0;
      } else if (st.moodPhase == 0 || --st.moodTicks <= 0) {
         st.moodPhase = st.moodPhase == 0 ? (RNG.nextBoolean() ? 1 : -1) : -st.moodPhase;
         st.moodTicks = MOOD_MIN_TICKS + RNG.nextInt(MOOD_SPAN_TICKS);
         if (!RunState.has(sp.getUUID(), Disability.ANOSOGNOSIA)) {
            sp.sendOverlayMessage(Component.literal(st.moodPhase > 0 ? "\u00a76You feel driven and unstoppable." : "\u00a79Everything feels heavy."));
         }
      }

      boolean high = st.moodPhase > 0;
      boolean low = st.moodPhase < 0;
      toggleModifier(sp.getAttribute(Attributes.MOVEMENT_SPEED), MANIC_SPEED_ID, high, 0.15, Operation.ADD_MULTIPLIED_TOTAL);
      toggleModifier(sp.getAttribute(Attributes.BLOCK_BREAK_SPEED), MANIC_MINE_ID, high, 0.3, Operation.ADD_MULTIPLIED_TOTAL);
      toggleModifier(sp.getAttribute(Attributes.MOVEMENT_SPEED), LOW_SPEED_ID, low, -0.1, Operation.ADD_MULTIPLIED_TOTAL);
      toggleModifier(sp.getAttribute(Attributes.BLOCK_BREAK_SPEED), LOW_MINE_ID, low, -0.3, Operation.ADD_MULTIPLIED_TOTAL);
   }

   /** Whatever you just ate, you cannot face again for half a minute: the item goes on cooldown. */
   private static void handlePickyEater(ServerPlayer sp, Effects st) {
      if (PlusState.has(sp.getUUID(), PlusDisability.PICKY_EATER) && st.finished != null) {
         ItemStack meal = new ItemStack(st.finished);
         if (meal.getUseAnimation() == net.minecraft.world.item.ItemUseAnimation.EAT) {
            sp.getCooldowns().addCooldown(meal, PICKY_COOLDOWN_TICKS);
         }
      }
   }

   /** Sweets give a speed rush; when it wears off the crash arrives as a short slowness. */
   private static void handleSweetTooth(ServerPlayer sp, Effects st) {
      if (!PlusState.has(sp.getUUID(), PlusDisability.SWEET_TOOTH)) {
         st.rushTicks = 0;
         return;
      }

      boolean sweet = st.ateCakeSlice || st.finished != null && Sugar.damageFor(st.finished) >= Sugar.MODERATE;
      if (sweet) {
         sp.addEffect(new MobEffectInstance(MobEffects.SPEED, SUGAR_RUSH_TICKS, 1));
         st.rushTicks = SUGAR_RUSH_TICKS;
      } else if (st.rushTicks > 0 && --st.rushTicks == 0) {
         sp.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SUGAR_CRASH_TICKS, 0));
      }
   }

   private static void handleEggLayer(ServerPlayer sp, ServerLevel sw) {
      if (PlusState.has(sp.getUUID(), PlusDisability.EGG_LAYER) && afflictable(sp) && RNG.nextInt(eggOdds) == 0) {
         ItemEntity egg = new ItemEntity(sw, sp.getX(), sp.getY() + 0.5, sp.getZ(), new ItemStack(Items.EGG));
         egg.setDefaultPickUpDelay();
         sw.addFreshEntity(egg);
         sw.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 1.0F, 1.0F);
      }
   }

   /** Low gravity, and a hard landing bounces you back up like a slime block unless you land in a crouch. */
   private static void handleMoonBoots(ServerPlayer sp, ServerLevel sw, Effects st) {
      boolean want = PlusState.has(sp.getUUID(), PlusDisability.MOON_BOOTS);
      toggleModifier(sp.getAttribute(Attributes.GRAVITY), MOON_GRAVITY_ID, want, -0.6, Operation.ADD_MULTIPLIED_TOTAL);
      if (want && afflictable(sp) && st.landedFall >= BOUNCE_MIN_FALL && !sp.isShiftKeyDown()) {
         double up = BOUNCE_KEEP * Math.sqrt(2.0 * LOW_GRAVITY * st.landedFall);
         net.minecraft.world.phys.Vec3 v = sp.getDeltaMovement();
         sp.setDeltaMovement(v.x, up, v.z);
         sp.syncVelocity = true;
         for (ServerPlayer friend : PlusSocial.nearbyPlayers(sw, sp, BOUNCE_SHARE_RANGE)) {
            net.minecraft.world.phys.Vec3 fv = friend.getDeltaMovement();
            friend.setDeltaMovement(fv.x, Math.max(fv.y, up), fv.z);
            friend.syncVelocity = true;
         }

         sw.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.SLIME_BLOCK_FALL, SoundSource.PLAYERS, 1.0F, 1.2F);
      }
   }

   /**
    * Big: long reach and no fitting through two-block gaps. A hard landing is a stomp that hurts and shoves hostile mobs
    * next to you, and the noise wakes every monster within twelve blocks.
    */
   private static void handleGiant(ServerPlayer sp, ServerLevel sw, Effects st) {
      boolean want = PlusState.has(sp.getUUID(), PlusDisability.GIANT);
      toggleModifier(sp.getAttribute(Attributes.SCALE), GIANT_SCALE_ID, want, 0.5, Operation.ADD_VALUE);
      toggleModifier(sp.getAttribute(Attributes.BLOCK_INTERACTION_RANGE), GIANT_BLOCK_REACH_ID, want, 1.0, Operation.ADD_VALUE);
      toggleModifier(sp.getAttribute(Attributes.ENTITY_INTERACTION_RANGE), GIANT_ENTITY_REACH_ID, want, 1.0, Operation.ADD_VALUE);
      if (want && afflictable(sp) && st.landedFall >= STOMP_MIN_FALL) {
         stomp(sp, sw, st.landedFall);
      }
   }

   static void stomp(ServerPlayer sp, ServerLevel sw, float fall) {
      float damage = Math.min(10.0F, fall);
      sw.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0F, 0.5F);
      for (ServerPlayer friend : PlusSocial.nearbyPlayers(sw, sp, STOMP_RADIUS)) {
         // Friends are tossed into the air by the shock, unhurt: a giant makes a launch pad.
         double dx = friend.getX() - sp.getX();
         double dz = friend.getZ() - sp.getZ();
         double len = Math.max(1.0E-4, Math.sqrt(dx * dx + dz * dz));
         friend.setDeltaMovement(friend.getDeltaMovement().add(dx / len * 0.5, 0.9, dz / len * 0.5));
         friend.syncVelocity = true;
      }

      for (net.minecraft.world.entity.Mob mob : sw.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, sp.getBoundingBox().inflate(STOMP_ALERT_RANGE), m -> m.isAlive())) {
         double dx = mob.getX() - sp.getX();
         double dz = mob.getZ() - sp.getZ();
         double len = Math.sqrt(dx * dx + dz * dz);
         boolean hostile = mob instanceof net.minecraft.world.entity.monster.Enemy || mob instanceof net.minecraft.world.entity.NeutralMob;
         if (len <= STOMP_RADIUS) {
            if (hostile) {
               mob.hurtServer(sw, sw.damageSources().playerAttack(sp), damage);
            }

            if (len > 1.0E-4) {
               mob.setDeltaMovement(mob.getDeltaMovement().add(dx / len * 0.7, 0.4, dz / len * 0.7));
               mob.syncVelocity = true;
            }
         }

         if (mob instanceof net.minecraft.world.entity.monster.Enemy) {
            mob.setTarget(sp);
         }
      }
   }

   /**
    * Multiplicative so it composes with Cancer, which lowers the base value. A hit of two hearts or more also cracks you:
    * three minutes of one heart less each, up to three cracks.
    */
   private static void handleGlassCannon(ServerPlayer sp, Effects st) {
      boolean want = PlusState.has(sp.getUUID(), PlusDisability.GLASS_CANNON);
      toggleModifier(sp.getAttribute(Attributes.ATTACK_DAMAGE), CANNON_ATTACK_ID, want, 1.0, Operation.ADD_MULTIPLIED_TOTAL);
      toggleModifier(sp.getAttribute(Attributes.MAX_HEALTH), CANNON_HEALTH_ID, want, -0.5, Operation.ADD_MULTIPLIED_TOTAL);
      if (!want) {
         st.cracks = 0;
         st.crackTicks = 0;
      } else if (st.cracks > 0 && --st.crackTicks <= 0) {
         st.cracks--;
         st.crackTicks = st.cracks > 0 ? CRACK_TICKS / 3 : 0;
      }

      // Each crack is -4 before the halving, so a heart gone from the 10 you have.
      setModifier(sp.getAttribute(Attributes.MAX_HEALTH), CRACK_ID, -4.0 * st.cracks, Operation.ADD_VALUE);
   }

   /** No arms or legs: no walking, no jumping (interaction is blocked by event callbacks in DisabilitiesMod). */
   private static void handleQuadrupleAmputee(ServerPlayer sp) {
      boolean want = PlusState.has(sp.getUUID(), PlusDisability.QUADRUPLE_AMPUTEE);
      toggleModifier(sp.getAttribute(Attributes.MOVEMENT_SPEED), QUAD_SPEED_ID, want, -1.0, Operation.ADD_MULTIPLIED_TOTAL);
      toggleModifier(sp.getAttribute(Attributes.JUMP_STRENGTH), QUAD_JUMP_ID, want, -1.0, Operation.ADD_MULTIPLIED_TOTAL);
   }

   private static void handlePregnancy(ServerPlayer sp) {
      boolean want = PlusState.has(sp.getUUID(), PlusDisability.PREGNANCY);
      toggleModifier(sp.getAttribute(Attributes.MOVEMENT_SPEED), PREGNANT_SPEED_ID, want, -0.1, Operation.ADD_MULTIPLIED_TOTAL);
   }

   private static void handleObesity(ServerPlayer sp) {
      boolean want = PlusState.has(sp.getUUID(), PlusDisability.OBESITY);
      toggleModifier(sp.getAttribute(Attributes.JUMP_STRENGTH), OBESE_JUMP_ID, want, -0.2, Operation.ADD_MULTIPLIED_TOTAL);
      // Heavy and hard to move once you get going: nothing shoves you around while you sprint.
      toggleModifier(sp.getAttribute(Attributes.KNOCKBACK_RESISTANCE), OBESE_RAM_ID, want && sp.isSprinting(), 0.8, Operation.ADD_VALUE);
      if (want && sp.isSprinting() && afflictable(sp)) {
         sp.causeFoodExhaustion(0.015F);
      }
   }

   /** Nothing sets a respawn point for an orphan, so every death returns to world spawn. */
   private static void handleOrphan(ServerPlayer sp) {
      if (PlusState.has(sp.getUUID(), PlusDisability.ORPHAN) && sp.getRespawnConfig() != null) {
         sp.setRespawnPosition(null, false);
      }
   }

   /** A potion or honey bottle gives a 90 s buzz (resistance, a little nausea); 4 minutes dry brings withdrawal. */
   private static void handleAlcoholism(ServerPlayer sp, Effects st) {
      Craving c = st.alcohol;
      if (!PlusState.has(sp.getUUID(), PlusDisability.ALCOHOLISM)) {
         c.reset();
      } else if (st.finished == Items.POTION || st.finished == Items.HONEY_BOTTLE) {
         c.dryTicks = 0;
         c.warned = false;
         sp.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, BUZZ_TICKS, 0));
         sp.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 160, 0, false, false, false));
      } else if (afflictable(sp) && ++c.dryTicks >= alcoholDryLimit) {
         warnOnce(sp, c, "\u00a77You need a drink...");
         if (sp.tickCount % 20 == 0) {
            applyQuiet(sp, MobEffects.DARKNESS, 160, 0);
         }
      }
   }

   /** Golden food is the fix: strength and haste for a minute, then a crash of weakness and fatigue. Going 5 minutes without brings withdrawal. */
   private static void handleDrugAddiction(ServerPlayer sp, Effects st) {
      Craving c = st.drug;
      if (!PlusState.has(sp.getUUID(), PlusDisability.DRUG_ADDICTION)) {
         c.reset();
         return;
      }

      Item f = st.finished;
      if (f == Items.GOLDEN_APPLE || f == Items.GOLDEN_CARROT || f == Items.GLISTERING_MELON_SLICE) {
         c.dryTicks = 0;
         c.warned = false;
         c.crashTicks = RUSH_TICKS;
         sp.addEffect(new MobEffectInstance(MobEffects.STRENGTH, RUSH_TICKS, 0));
         sp.addEffect(new MobEffectInstance(MobEffects.HASTE, RUSH_TICKS, 1));
      } else {
         if (c.crashTicks > 0 && --c.crashTicks == 0) {
            sp.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 1));
            sp.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 300, 0));
         }

         if (afflictable(sp) && ++c.dryTicks >= drugDryLimit) {
            warnOnce(sp, c, "\u00a75You need a fix...");
            if (sp.tickCount % 20 == 0) {
               applyQuiet(sp, MobEffects.WEAKNESS, 60, 0);
               applyQuiet(sp, MobEffects.HUNGER, 60, 0);
               applyQuiet(sp, MobEffects.NAUSEA, 100, 0);
            }
         }
      }
   }

   private static void handleBloodHungry(ServerPlayer sp) {
      if (PlusState.has(sp.getUUID(), PlusDisability.BLOOD_HUNGRY) && afflictable(sp)) {
         sp.causeFoodExhaustion(0.006F);
      }
   }

   /** In water you thrash (client side); in the rain you bolt for cover, so the fear makes you faster. */
   private static void handleAquaphobia(ServerPlayer sp) {
      if (PlusState.has(sp.getUUID(), PlusDisability.AQUAPHOBIA)
         && sp.tickCount % FEAR_SCAN_INTERVAL_TICKS == 0
         && afflictable(sp)
         && !sp.isInWater()
         && sp.isInWaterOrRain()) {
         applyQuiet(sp, MobEffects.SPEED, 40, 1);
      }
   }

   /** The opposite of Acrophobia and Claustrophobia, reusing both of their scans. */
   private static void handleThrillSeeker(ServerPlayer sp, ServerLevel sw) {
      if (PlusState.has(sp.getUUID(), PlusDisability.THRILL_SEEKER) && sp.tickCount % FEAR_SCAN_INTERVAL_TICKS == 0 && afflictable(sp)) {
         if (sp.onGround() && Scans.exposedToDrop(sw, sp.blockPosition())) {
            applyQuiet(sp, MobEffects.SPEED, 60, 0);
            applyQuiet(sp, MobEffects.HASTE, 60, 0);
         } else if (Scans.enclosed(sw, sp.blockPosition().above())) {
            applyQuiet(sp, MobEffects.MINING_FATIGUE, 60, 0);
         }
      }
   }

   /** Light level decides it, like Scaredy Cat and Night Blindness: bright is groggy, dark is sharp. */
   private static void handleNightOwl(ServerPlayer sp, ServerLevel sw) {
      if (PlusState.has(sp.getUUID(), PlusDisability.NIGHT_OWL) && sp.tickCount % 20 == 0 && afflictable(sp)) {
         int light = sw.getMaxLocalRawBrightness(sp.blockPosition());
         if (light >= 12) {
            applyQuiet(sp, MobEffects.SLOWNESS, 60, 0);
            applyQuiet(sp, MobEffects.WEAKNESS, 60, 0);
         } else if (light <= 4) {
            applyQuiet(sp, MobEffects.SPEED, 60, 0);
            applyQuiet(sp, MobEffects.NIGHT_VISION, 260, 0);
         }
      }
   }

   /** The fuller your pack, the slower you are, and the better padded: a trade between speed and armor. */
   private static void handleHoarder(ServerPlayer sp, Effects st) {
      boolean want = PlusState.has(sp.getUUID(), PlusDisability.HOARDER);
      int used = 0;
      if (want) {
         for (ItemStack stack : sp.getInventory().getNonEquipmentItems()) {
            if (!stack.isEmpty()) {
               used++;
            }
         }
      }

      boolean light = want && used >= HOARD_LIGHT_SLOTS && used < HOARD_HEAVY_SLOTS;
      boolean heavy = want && used >= HOARD_HEAVY_SLOTS;
      AttributeInstance speed = sp.getAttribute(Attributes.MOVEMENT_SPEED);
      toggleModifier(speed, HOARD_LIGHT_ID, light, -0.1, Operation.ADD_MULTIPLIED_TOTAL);
      toggleModifier(speed, HOARD_HEAVY_ID, heavy, -0.25, Operation.ADD_MULTIPLIED_TOTAL);
      AttributeInstance armor = sp.getAttribute(Attributes.ARMOR);
      toggleModifier(armor, HOARD_LIGHT_ARMOR_ID, light, 2.0, Operation.ADD_VALUE);
      toggleModifier(armor, HOARD_HEAVY_ARMOR_ID, heavy, 6.0, Operation.ADD_VALUE);
      st.hoardArmor = heavy ? 6.0 : light ? 2.0 : 0.0;
   }

   /**
    * A stone passes every 3-6 minutes: you lock up in agony for a few seconds. It hurts but will not finish you off.
    * Drinking (water, a potion, milk) keeps you hydrated for three minutes: the next stone is delayed, and one that comes
    * while you are hydrated passes quickly and painlessly.
    */
   private static void handleKidneyStones(ServerPlayer sp, ServerLevel sw, Effects st) {
      if (!PlusState.has(sp.getUUID(), PlusDisability.KIDNEY_STONES) || !afflictable(sp)) {
         st.stoneTicks = 0;
         st.hydrated = 0;
         return;
      }

      if (st.finished == Items.POTION || st.finished == Items.MILK_BUCKET) {
         st.hydrated = HYDRATED_TICKS;
         st.stoneCooldown += 1200;
         hint(sp, "\u00a7bHydrated: the next stone will pass easily.");
      } else if (st.hydrated > 0) {
         st.hydrated--;
      }

      if (st.stoneTicks > 0) {
         st.stoneTicks--;
         if (!st.stonePainless && st.stoneTicks % 20 == 0 && sp.getHealth() > 2.0F) {
            sp.hurtServer(sw, damageSource(sw, KIDNEY_DAMAGE), 1.0F);
         }
      } else if (--st.stoneCooldown <= 0) {
         st.stonePainless = st.hydrated > 0;
         st.stoneTicks = st.stonePainless ? PAINLESS_STONE_TICKS : STONE_EPISODE_TICKS;
         st.stoneCooldown = 3600 + RNG.nextInt(3600);
         sendEpisode(sp, PlusNet.EpisodePayload.AGONY, st.stoneTicks);
      }
   }

   /**
    * Lower health, and each Lead (the leash item) in the pack takes another heart and poisons you. A bucket of milk flushes
    * the lead for five minutes: full health, and Leads leave you alone.
    */
   private static void handleLeadPoisoning(ServerPlayer sp, Effects st) {
      boolean want = PlusState.has(sp.getUUID(), PlusDisability.LEAD_POISONING);
      if (want && st.finished == Items.MILK_BUCKET) {
         st.chelated = CHELATED_TICKS;
         hint(sp, "\u00a7aThe milk flushes the lead out for a while.");
      } else if (st.chelated > 0) {
         st.chelated--;
      }

      boolean flushed = st.chelated > 0;
      int leads = want && !flushed ? sp.getInventory().countItem(Items.LEAD) : 0;
      AttributeInstance health = sp.getAttribute(Attributes.MAX_HEALTH);
      toggleModifier(health, LEAD_HEALTH_ID, want && !flushed, -2.0, Operation.ADD_VALUE);
      setModifier(health, LEAD_LOAD_ID, -2.0 * Math.min(leads, 4), Operation.ADD_VALUE);
      if (leads > 0 && sp.tickCount % 40 == 0 && afflictable(sp)) {
         applyQuiet(sp, MobEffects.POISON, 100, 0);
      }
   }

   /** Dust builds up underground and sooty vision closes in; past the limit you choke until you get under open sky. */
   private static void handleBlackLung(ServerPlayer sp, ServerLevel sw, Effects st) {
      if (!PlusState.has(sp.getUUID(), PlusDisability.BLACK_LUNG)) {
         st.dust = 0;
         if (st.dustSent != 0) {
            st.dustSent = 0;
            sendEpisode(sp, PlusNet.EpisodePayload.DUST, 0);
         }

         return;
      }

      if (st.dust > 0 && sp.tickCount % 40 == 0 && sw.canSeeSky(sp.blockPosition().above())) {
         st.dust--;
      }

      if (st.dust >= DUST_LIMIT && sp.tickCount % 40 == 0 && afflictable(sp) && sp.getHealth() > 2.0F) {
         sp.hurtServer(sw, damageSource(sw, ASTHMA_DAMAGE), 1.0F);
      }

      if (st.dust != st.dustSent || sp.tickCount % 100 == 0) {
         st.dustSent = st.dust;
         sendEpisode(sp, PlusNet.EpisodePayload.DUST, st.dust);
      }
   }

   /** The dead tissue spreads every 5 minutes: first the hands (mining), then the legs (speed), then your strength. */
   private static void handleNecrosis(ServerPlayer sp, ServerLevel sw, Effects st) {
      boolean want = PlusState.has(sp.getUUID(), PlusDisability.NECROSIS);
      if (!want) {
         st.necroTicks = 0;
         if (st.necroStage != 0) {
            st.necroStage = 0;
            sendEpisode(sp, PlusNet.EpisodePayload.NECROSIS, 0);
         }
      } else if (st.finished == Items.ENCHANTED_GOLDEN_APPLE && st.necroStage > 0) {
         st.necroStage = 0;
         st.necroTicks = 0;
         sendEpisode(sp, PlusNet.EpisodePayload.NECROSIS, 0);
         hint(sp, "\u00a7aThe enchanted apple burns the rot out of you.");
      } else if (st.finished == Items.GOLDEN_APPLE && st.necroStage > 0) {
         st.necroStage--;
         st.necroTicks = 0;
         sendEpisode(sp, PlusNet.EpisodePayload.NECROSIS, st.necroStage);
         hint(sp, "\u00a7aThe golden apple pushes the rot back a stage.");
      } else if (afflictable(sp) && st.necroStage < 3 && ++st.necroTicks >= NECROSIS_STAGE_TICKS) {
         st.necroTicks = 0;
         st.necroStage++;
         sendEpisode(sp, PlusNet.EpisodePayload.NECROSIS, st.necroStage);
         if (!RunState.has(sp.getUUID(), Disability.ANOSOGNOSIA)) {
            sp.sendOverlayMessage(Component.literal("\u00a75The dead tissue spreads..."));
         }
      }

      double mining = st.necroStage >= 3 ? -0.75 : st.necroStage >= 1 ? -0.5 : 0.0;
      setModifier(sp.getAttribute(Attributes.BLOCK_BREAK_SPEED), NECRO_MINE_ID, mining, Operation.ADD_MULTIPLIED_TOTAL);
      toggleModifier(sp.getAttribute(Attributes.MOVEMENT_SPEED), NECRO_SPEED_ID, st.necroStage >= 2, -0.5, Operation.ADD_MULTIPLIED_TOTAL);
      toggleModifier(sp.getAttribute(Attributes.ATTACK_DAMAGE), NECRO_ATTACK_ID, st.necroStage >= 3, -0.75, Operation.ADD_MULTIPLIED_TOTAL);
   }

   /** Slow reflexes: your swings come around a third slower. (The sway, haze and droopy eyes are client side.) */
   private static void handleStoned(ServerPlayer sp) {
      toggleModifier(sp.getAttribute(Attributes.ATTACK_SPEED), STONED_REFLEX_ID, PlusState.has(sp.getUUID(), PlusDisability.STONED), -0.3, Operation.ADD_MULTIPLIED_TOTAL);
   }

   /** Scaredy Cat: a hostile mob within 12 blocks sends you bolting (the callback also stops you fighting back). */
   private static void handleScaredyCat(ServerPlayer sp, ServerLevel sw) {
      if (PlusState.has(sp.getUUID(), PlusDisability.SCAREDY_CAT) && sp.tickCount % FEAR_SCAN_INTERVAL_TICKS == 0 && afflictable(sp)) {
         boolean monster = !sw.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, sp.getBoundingBox().inflate(SCARED_RANGE),
            m -> m.isAlive() && m instanceof net.minecraft.world.entity.monster.Enemy).isEmpty();
         if (monster) {
            applyQuiet(sp, MobEffects.SPEED, 40, 1);
         }
      }
   }

   /**
    * ARFID: anything outside a handful of safe foods comes straight back up. Anorexia cuts what a meal gives to a third
    * and leaves you frail while hungry.
    */
   private static void handleEatingDisorders(ServerPlayer sp, Effects st) {
      net.minecraft.world.food.FoodData food = sp.getFoodData();
      boolean arfid = PlusState.has(sp.getUUID(), PlusDisability.ARFID);
      boolean anorexia = PlusState.has(sp.getUUID(), PlusDisability.ANOREXIA);
      int now = food.getFoodLevel();
      if ((arfid || anorexia) && st.finished != null && st.foodBefore >= 0 && now > st.foodBefore) {
         int gain = now - st.foodBefore;
         if (arfid && !SAFE_FOODS.contains(st.finished)) {
            food.setFoodLevel(st.foodBefore);
            food.setSaturation(st.satBefore);
            sendEpisode(sp, PlusNet.EpisodePayload.PAIN, 30);
         } else if (anorexia) {
            food.setFoodLevel(st.foodBefore + Math.max(1, gain / 3));
            food.setSaturation(Math.min(food.getSaturationLevel(), st.satBefore));
         }
      }

      st.foodBefore = food.getFoodLevel();
      st.satBefore = food.getSaturationLevel();
      toggleModifier(sp.getAttribute(Attributes.MAX_HEALTH), FRAIL_HEALTH_ID, anorexia && food.getFoodLevel() < 10, -4.0, Operation.ADD_VALUE);
      toggleModifier(sp.getAttribute(Attributes.ATTACK_DAMAGE), FRAIL_ATTACK_ID, anorexia, -0.25, Operation.ADD_MULTIPLIED_TOTAL);
   }

   /** Internal damage (the bleeding type, so it ignores armor and i-frames) that never takes anyone below one heart. */
   static void bleed(ServerPlayer sp, ServerLevel sw, float amount) {
      float hurt = MindRules.nonLethal(amount, sp.getHealth(), 2.0F);
      if (hurt > 0.0F) {
         sp.hurtServer(sw, damageSource(sw, BLEEDING_DAMAGE), hurt);
      }
   }

   static DamageSource damageSource(ServerLevel sw, ResourceKey<DamageType> key) {
      return new DamageSource(sw.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key));
   }

   static ResourceKey<DamageType> damageKey(String path) {
      return ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(NS, path));
   }

   /** Alive, and not in creative/spectator: the players the new environmental effects apply to. */
   static boolean afflictable(ServerPlayer sp) {
      return !sp.isDeadOrDying() && !sp.isCreative() && !sp.isSpectator();
   }

   /** Adds a quiet effect, but only when it is missing, weaker, or half-expired, so a periodic scan does not spam packets. */
   static void applyQuiet(ServerPlayer sp, Holder<MobEffect> effect, int duration, int amplifier) {
      MobEffectInstance cur = sp.getEffect(effect);
      if (cur == null || cur.getAmplifier() < amplifier || cur.getDuration() <= duration / 2) {
         sp.addEffect(new MobEffectInstance(effect, duration, amplifier, false, false, false));
      }
   }

   private static void warnOnce(ServerPlayer sp, Craving c, String message) {
      if (!c.warned) {
         c.warned = true;
         if (!RunState.has(sp.getUUID(), Disability.ANOSOGNOSIA)) {
            sp.sendOverlayMessage(Component.literal(message));
         }
      }
   }

   /** Sets a modifier to an exact amount, replacing it when the amount changes and removing it at zero. */
   static void setModifier(AttributeInstance inst, Identifier id, double amount, Operation op) {
      if (inst != null) {
         AttributeModifier cur = inst.getModifier(id);
         if (amount == 0.0) {
            if (cur != null) {
               inst.removeModifier(id);
            }
         } else if (cur == null || cur.amount() != amount) {
            inst.removeModifier(id);
            inst.addTransientModifier(new AttributeModifier(id, amount, op));
         }
      }
   }

   /** Mining underground fills the lungs with dust; fresh air under open sky clears it. */
   static void breatheDust(ServerPlayer sp, BlockState state, BlockPos pos) {
      if (PlusState.has(sp.getUUID(), PlusDisability.BLACK_LUNG) && pos.getY() < 60) {
         boolean coal = state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE);
         Effects st = of(sp.getUUID());
         st.dust = Math.min(DUST_MAX, st.dust + (coal ? 3 : 1));
      }
   }

   /** Tracks time since the last drink or fix, and the pending crash after a rush. */
   static final class Craving {
      int dryTicks = 0;
      int crashTicks = 0;
      boolean warned = false;

      void reset() {
         this.dryTicks = 0;
         this.crashTicks = 0;
         this.warned = false;
      }
   }

   public static final class Effects {
      int bleedTicks = 0;
      int dust = 0;
      int dustSent = 0;
      int ironTicks = 0;
      int cracks = 0;
      int crackTicks = 0;
      float peakFall = 0.0F;
      float landedFall = 0.0F;
      int hydrated = 0;
      boolean stonePainless = false;
      int chelated = 0;
      double squeakX = 0.0;
      double squeakZ = 0.0;
      double squeakDistance = 0.0;
      Item fedItem = null;
      java.util.UUID carriedBy = null;
      double hoardArmor = 0.0;
      boolean motionHinted = false;
      boolean rollHinted = false;
      int airTicks = 0;
      float lastHealth = -1.0F;
      int foodBefore = -1;
      double heartStrain = 0.0;
      double lastX = 0.0;
      double lastZ = 0.0;
      int moodPhase = 0;
      int moodTicks = 0;
      int motionTimer = 0;
      int necroStage = 0;
      int necroTicks = 0;
      int rushTicks = 0;
      float satBefore = 0.0F;
      int stoneCooldown = 1800;
      int stoneTicks = 0;
      boolean tight = false;
      double tightAir = -1.0;
      int tightTimer = 0;
      Item finished = null;
      boolean ateCakeSlice = false;
      final Craving alcohol = new Craving();
      final Craving drug = new Craving();
      final PlusConditions.State cond = new PlusConditions.State();
      final PlusAnxiety.State anxiety = new PlusAnxiety.State();
      final PlusAlienHand.State alien = new PlusAlienHand.State();
      final PlusCataplexy.State cata = new PlusCataplexy.State();
      final PlusNumbness.State numb = new PlusNumbness.State();
      final PlusJoints.State joints = new PlusJoints.State();
      boolean prevUsingItem = false;
      int prevUseTimeLeft = 0;
      Item prevUseItem = null;
      int lastCakeSlices = -1;
   }
}
