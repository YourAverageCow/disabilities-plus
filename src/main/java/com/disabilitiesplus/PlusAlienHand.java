package com.disabilitiesplus;

import java.util.Random;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Alien Hand Syndrome: your offhand acts on its own, every 20 to 60 seconds. It slaps whoever or whatever is nearest
 * (friends and pets included, and it is a real hit), or grabs you toward the nearest person, or swaps what is in your
 * hands. Giving it something to hold keeps it quiet: three times rarer, and its slaps are gentle.
 */
final class PlusAlienHand {
   private static final int MIN_GAP_TICKS = 400;
   private static final int GAP_SPAN_TICKS = 800;
   private static final double SLAP_REACH = 3.5;
   private static final double PULL_REACH = 8.0;
   static final float SLAP_DAMAGE = 2.0F;
   static final float BUSY_SLAP_DAMAGE = 1.0F;

   /** The three things it does. */
   enum Action {
      SLAP, PULL, SWAP
   }

   /** How often tests may force it. */
   static int gapScale = 1;
   /** True while a slap is being dealt, so the attacker-side hooks for deliberate hits (tool wear, Blood Hungry, ramming) skip it. */
   static boolean slapping = false;

   private PlusAlienHand() {
   }

   static final class State {
      int timer = 0;
   }

   private static boolean has(ServerPlayer sp) {
      return PlusState.has(sp.getUUID(), PlusDisability.ALIEN_HAND);
   }

   /** The offhand is holding something, so the other hand has less to do. */
   static boolean busy(ServerPlayer sp) {
      return !sp.getOffhandItem().isEmpty();
   }

   static Action pick(Random rng) {
      int roll = rng.nextInt(100);
      return roll < 50 ? Action.SLAP : roll < 80 ? Action.PULL : Action.SWAP;
   }

   static void tick(ServerPlayer sp, ServerLevel sw, PlusEffects.Effects fx) {
      State c = fx.alien;
      if (!has(sp) || !PlusEffects.afflictable(sp) || PlusState.has(sp.getUUID(), PlusDisability.QUADRUPLE_AMPUTEE)) {
         c.timer = 0;
         return;
      }

      if (c.timer <= 0) {
         c.timer = (MIN_GAP_TICKS + PlusEffects.RNG.nextInt(GAP_SPAN_TICKS)) * (busy(sp) ? 3 : 1) / Math.max(1, gapScale);
      } else if (--c.timer == 0) {
         act(sp, sw, pick(PlusEffects.RNG));
      }
   }

   static LivingEntity nearest(ServerPlayer sp, ServerLevel sw, double reach, boolean playersOnly) {
      LivingEntity best = null;
      double bestDist = Double.MAX_VALUE;
      for (LivingEntity e : sw.getEntitiesOfClass(LivingEntity.class, sp.getBoundingBox().inflate(reach),
         x -> x != sp && x.isAlive() && x.distanceToSqr(sp) <= reach * reach && !(x instanceof Player p && p.isSpectator())
            && (!playersOnly || x instanceof Player))) {
         double d = e.distanceToSqr(sp);
         if (d < bestDist) {
            bestDist = d;
            best = e;
         }
      }

      return best;
   }

   static void act(ServerPlayer sp, ServerLevel sw, Action action) {
      switch (action) {
         case SLAP -> slap(sp, sw);
         case PULL -> pull(sp, sw);
         case SWAP -> swap(sp);
      }
   }

   private static void slap(ServerPlayer sp, ServerLevel sw) {
      LivingEntity target = nearest(sp, sw, SLAP_REACH, false);
      if (target == null || !sp.hasLineOfSight(target)) {
         PlusEffects.hint(sp, "§5Your hand lashes out at nothing.");
         return;
      }

      slapping = true;
      try {
         target.hurtServer(sw, sw.damageSources().playerAttack(sp), busy(sp) ? BUSY_SLAP_DAMAGE : SLAP_DAMAGE);
      } finally {
         slapping = false;
      }

      sw.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.PLAYERS, 1.0F, 1.0F);
      PlusEffects.hint(sp, "§5Your hand lashes out on its own!");
   }

   private static void pull(ServerPlayer sp, ServerLevel sw) {
      LivingEntity target = nearest(sp, sw, PULL_REACH, true);
      if (target == null) {
         target = nearest(sp, sw, PULL_REACH, false);
      }

      if (target == null) {
         slap(sp, sw);
         return;
      }

      Vec3 toward = target.position().subtract(sp.position()).multiply(1.0, 0.0, 1.0);
      if (toward.lengthSqr() > 1.0E-6) {
         Vec3 push = toward.normalize().scale(0.9);
         sp.setDeltaMovement(sp.getDeltaMovement().add(push.x, 0.25, push.z));
         sp.syncVelocity = true;
      }

      PlusEffects.hint(sp, "§5Your hand grabs hold and drags you over.");
   }

   private static void swap(ServerPlayer sp) {
      ItemStack main = sp.getMainHandItem().copy();
      ItemStack off = sp.getOffhandItem().copy();
      if (main.isEmpty() && off.isEmpty()) {
         PlusEffects.hint(sp, "§5Your hand paws at nothing.");
         return;
      }

      sp.setItemInHand(InteractionHand.MAIN_HAND, off);
      sp.setItemInHand(InteractionHand.OFF_HAND, main);
      PlusEffects.hint(sp, "§5Your hand snatches your things and swaps them.");
   }
}
