package com.disabilitiesplus.client;

import com.disabilitiesplus.PlusDisability;
import com.studiobing.disabilities.Disability;
import com.disabilitiesplus.Scans;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Fears and episodes that need their own reaction instead of a potion effect. Everything here runs on the client and
 * reuses the existing channels: forced keys (cpJump/cpSneak), the freeze mixin, the dread vignette and the overload dim.
 */
public final class FearEffects {
   private static final Random RNG = new Random();
   private static final int SCAN_TICKS = 5;
   private static final double CLOSE = 8.0;
   private static final double ENDER_RANGE = 12.0;
   private static final int FIRE_RANGE = 5;

   private static Vec3 creeperAt;
   private static Vec3 spiderAt;
   private static Vec3 enderAt;
   private static Vec3 beeAt;
   private static BlockPos fireAt;
   private static boolean exposed;
   private static boolean squeezed;
   private static boolean wasSneaking;
   private static int hurtBefore;
   private static int heartbeatTimer;

   private FearEffects() {
   }

   /** Forgets everything scanned in the previous world; called when the connection ends. */
   static void reset() {
      creeperAt = null;
      spiderAt = null;
      enderAt = null;
      beeAt = null;
      fireAt = null;
      exposed = false;
      squeezed = false;
      wasSneaking = false;
      hurtBefore = 0;
      heartbeatTimer = 0;
   }

   private static <T extends Entity> Vec3 nearest(Minecraft mc, Class<T> type, double range) {
      AABB box = mc.player.getBoundingBox().inflate(range);
      List<T> found = mc.level.getEntitiesOfClass(type, box, Entity::isAlive);
      Vec3 best = null;
      double bestDist = Double.MAX_VALUE;

      for (T e : found) {
         double d = mc.player.distanceToSqr(e);
         if (d < bestDist) {
            bestDist = d;
            best = e.position();
         }
      }

      return best;
   }

   private static void scan(Minecraft mc) {
      creeperAt = PlusClientState.has(PlusDisability.CREEPERPHOBIA) ? nearest(mc, Creeper.class, CLOSE) : null;
      spiderAt = PlusClientState.has(PlusDisability.ARACHNOPHOBIA) ? nearest(mc, Spider.class, CLOSE) : null;
      enderAt = PlusClientState.has(PlusDisability.ENDERPHOBIA) ? nearest(mc, Enderman.class, ENDER_RANGE) : null;
      beeAt = PlusClientState.has(PlusDisability.APIPHOBIA) ? nearest(mc, Bee.class, CLOSE) : null;
      fireAt = PlusClientState.has(PlusDisability.PYROPHOBIA) ? Scans.nearestFire(mc.level, mc.player.blockPosition(), FIRE_RANGE) : null;
      exposed = PlusClientState.has(PlusDisability.ACROPHOBIA) && mc.player.onGround() && Scans.exposedToDrop(mc.level, mc.player.blockPosition());
      squeezed = PlusClientState.has(PlusDisability.CLAUSTROPHOBIA) && Scans.enclosed(mc.level, mc.player.blockPosition().above());
   }

   /** Yaw (degrees) that would face the given point from the player. */
   private static float bearing(LocalPlayer p, double x, double z) {
      return (float)(Mth.atan2(z - p.getZ(), x - p.getX()) * 57.29577951308232) - 90.0F;
   }

   private static void play(Minecraft mc, SoundEvent sound, SoundSource source, float volume, float pitch) {
      mc.level.playSound(mc.player, mc.player.getX(), mc.player.getY(), mc.player.getZ(), sound, source, volume, pitch);
   }

   public static void startFlashback(Minecraft mc) {
      if (PlusClientState.flashbackTicks <= 0) {
         PlusClientState.flashbackTicks = 100;
         PlusClientState.freezeTicks = Math.max(PlusClientState.freezeTicks, 20);
      }
   }

   /** Sets off a cramp (IBS, or a retch after eating something you cannot keep down). */
   public static void cramp(int ticks) {
      PlusClientState.painTicks = Math.max(PlusClientState.painTicks, ticks);
   }

   static void tick(Minecraft mc) {
      LocalPlayer p = mc.player;
      if (mc.level == null || p == null) {
         return;
      }

      if (p.tickCount % SCAN_TICKS == 0) {
         scan(mc);
      }

      boolean free = mc.gui.screen() == null;
      float dreadTarget = 0.0F;
      int beatGap = 0;
      if (PlusClientState.freezeTicks > 0) {
         PlusClientState.freezeTicks--;
      }

      if (PlusClientState.freezeCooldown > 0) {
         PlusClientState.freezeCooldown--;
      }

      // Creeperphobia: you freeze up in bursts while your heartbeat quickens as it closes in.
      if (creeperAt != null && PlusClientState.has(PlusDisability.CREEPERPHOBIA)) {
         double d = Math.sqrt(p.distanceToSqr(creeperAt));
         beatGap = Mth.clamp((int)(6.0 + d * 3.5), 8, 36);
         if (PlusClientState.freezeCooldown == 0) {
            PlusClientState.freezeTicks = 12;
            PlusClientState.freezeCooldown = 45;
            PlusClientState.freezeCount++;
         }
      }

      // Arachnophobia: your head snaps away from the spider.
      if (spiderAt != null && PlusClientState.has(PlusDisability.ARACHNOPHOBIA) && free) {
         p.setYRot(Mth.approachDegrees(p.getYRot(), bearing(p, spiderAt.x, spiderAt.z) + 180.0F, 3.0F));
      }

      // Enderphobia: dread closes your vision in as the Enderman gets nearer, and it whispers.
      if (enderAt != null && PlusClientState.has(PlusDisability.ENDERPHOBIA)) {
         double d = Math.sqrt(p.distanceToSqr(enderAt));
         dreadTarget = Math.max(dreadTarget, Mth.clamp((float)((ENDER_RANGE - d) / (ENDER_RANGE - 3.0)), 0.0F, 1.0F));
         if (RNG.nextInt(90) == 0) {
            play(mc, SoundEvents.ENDERMAN_STARE, SoundSource.AMBIENT, 0.5F, 0.5F);
         }
      }

      // Apiphobia: a buzz at your ear and twitching hands.
      if (beeAt != null && PlusClientState.has(PlusDisability.APIPHOBIA)) {
         if (free && (p.tickCount & 1) == 0) {
            p.setYRot(p.getYRot() + (RNG.nextFloat() - 0.5F) * 3.0F);
            p.setXRot(Mth.clamp(p.getXRot() + (RNG.nextFloat() - 0.5F) * 3.0F, -90.0F, 90.0F));
         }

         if (p.tickCount % 20 == 0) {
            PlusClientState.buzzCount++;
            play(mc, SoundEvents.BEE_LOOP_AGGRESSIVE, SoundSource.AMBIENT, 0.7F, 1.2F);
         }
      }

      // Aquaphobia: in water you thrash upwards and your view rolls.
      if (PlusClientState.has(PlusDisability.AQUAPHOBIA) && p.isInWater()) {
         if (p.tickCount % 3 == 0) {
            PlusClientState.cpJump = true;
            PlusClientState.thrashCount++;
         }

         PlusClientState.thrashTicks = 4;
      } else if (PlusClientState.thrashTicks > 0) {
         PlusClientState.thrashTicks--;
      }

      // Pyrophobia: your gaze is drawn to the flames.
      if (fireAt != null && PlusClientState.has(PlusDisability.PYROPHOBIA) && free) {
         p.setYRot(Mth.approachDegrees(p.getYRot(), bearing(p, fireAt.getX() + 0.5, fireAt.getZ() + 0.5), 4.0F));
      }

      // Acrophobia: you cling to the ground in a crouch (which also keeps you from stepping off).
      PlusClientState.clinging = exposed && PlusClientState.has(PlusDisability.ACROPHOBIA);
      if (PlusClientState.clinging) {
         PlusClientState.cpSneak = true;
         dreadTarget = Math.max(dreadTarget, 0.35F);
         beatGap = beatGap == 0 ? 20 : Math.min(beatGap, 20);
      }

      // Claustrophobia: walls closing in (the air itself is drained server side).
      if (squeezed && PlusClientState.has(PlusDisability.CLAUSTROPHOBIA)) {
         dreadTarget = Math.max(dreadTarget, 0.8F);
         beatGap = beatGap == 0 ? 14 : Math.min(beatGap, 14);
      }

      // Head Rush: standing up from a crouch blacks you out.
      boolean sneaking = p.isShiftKeyDown();
      if (PlusClientState.has(PlusDisability.HEAD_RUSH) && wasSneaking && !sneaking) {
         PlusClientState.blinkTicks = Math.max(PlusClientState.blinkTicks, 20);
      }

      wasSneaking = sneaking;

      // PTSD: a hit or a loud noise throws you into a flashback.
      if (PlusClientState.has(PlusDisability.PTSD)) {
         if (p.hurtTime > 0 && hurtBefore == 0) {
            startFlashback(mc);
         }

         hurtBefore = p.hurtTime;
         if (PlusClientState.flashbackTicks > 0) {
            PlusClientState.flashbackTicks--;
            dreadTarget = Math.max(dreadTarget, 0.9F * Math.min(1.0F, PlusClientState.flashbackTicks / 30.0F));
            beatGap = 8;
         }
      } else {
         PlusClientState.flashbackTicks = 0;
      }

      // Social Anxiety: the vignette closes in and the heart pounds with the meter; hands shake from nervousness on.
      int anxiety = PlusClientState.has(PlusDisability.SOCIAL_ANXIETY) ? PlusClientState.anxiety : 0;
      if (anxiety > 0) {
         dreadTarget = Math.max(dreadTarget, 0.8F * anxiety / 100.0F);
         if (anxiety >= com.disabilitiesplus.MindRules.ANXIETY_PANIC) {
            beatGap = beatGap == 0 ? 12 : Math.min(beatGap, 12);
         }

         if (anxiety >= com.disabilitiesplus.MindRules.ANXIETY_NERVOUS && free) {
            float shake = 0.6F * anxiety / 100.0F;
            p.setYRot(p.getYRot() + (RNG.nextFloat() - 0.5F) * shake);
            p.setXRot(Mth.clamp(p.getXRot() + (RNG.nextFloat() - 0.5F) * shake, -90.0F, 90.0F));
         }
      }

      // Cramps, kidney stones and retching: doubled over with the view pulled down.
      if (PlusClientState.painTicks > 0) {
         PlusClientState.painTicks--;
         PlusClientState.cpSneak = true;
         p.setXRot(Mth.approachDegrees(p.getXRot(), 40.0F, 4.0F));
      }

      // Black lung cough: locked up with the head jerking.
      if (PlusClientState.coughTicks > 0) {
         PlusClientState.coughTicks--;
         PlusClientState.freezeTicks = Math.max(PlusClientState.freezeTicks, 2);
         if (PlusClientState.coughTicks % 10 == 0) {
            p.setXRot(Mth.clamp(p.getXRot() + 14.0F, -90.0F, 90.0F));
            play(mc, SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 0.5F, 1.5F);
         } else if (PlusClientState.coughTicks % 10 == 5) {
            p.setXRot(Mth.clamp(p.getXRot() - 14.0F, -90.0F, 90.0F));
         }
      }

      PlusClientState.dread += (dreadTarget - PlusClientState.dread) * 0.15F;
      if (PlusClientState.dread < 0.01F) {
         PlusClientState.dread = 0.0F;
      }

      if (beatGap > 0) {
         if (++heartbeatTimer >= beatGap) {
            heartbeatTimer = 0;
            PlusClientState.heartbeatCount++;
            play(mc, SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.9F, 1.0F);
         }
      } else {
         heartbeatTimer = 0;
      }
   }
}
