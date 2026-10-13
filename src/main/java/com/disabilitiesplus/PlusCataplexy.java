package com.disabilitiesplus;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Cataplexy: strong emotion takes the strength out of your muscles while you stay fully awake. Laughter in chat (yours or
 * anyone's), a great find (diamonds, emeralds, ancient debris) or a kill can drop you for four seconds: your knees buckle,
 * you cannot use your hands. Bracing prevents it: be crouched (or seated) when it hits and you only wobble.
 */
final class PlusCataplexy {
   private static final int COLLAPSE_TICKS = 80;
   private static final int COOLDOWN_TICKS = 400;
   private static final int WOBBLE_COOLDOWN_TICKS = 100;
   /** One kill in this many is exciting enough; plain fields so the gametest can force it. */
   static int killOdds = 3;

   private PlusCataplexy() {
   }

   static final class State {
      int collapse = 0;
      int cooldown = 0;
   }

   private static boolean has(ServerPlayer sp) {
      return PlusState.has(sp.getUUID(), PlusDisability.CATAPLEXY);
   }

   /** True while the player is down (their hands do nothing). */
   static boolean down(ServerPlayer sp) {
      return PlusEffects.of(sp.getUUID()).cata.collapse > 0;
   }

   static void tick(ServerPlayer sp, PlusEffects.Effects fx) {
      State c = fx.cata;
      if (!has(sp)) {
         c.collapse = 0;
         c.cooldown = 0;
         return;
      }

      if (c.collapse > 0) {
         c.collapse--;
         sp.setSprinting(false);
      }

      if (c.cooldown > 0) {
         c.cooldown--;
      }
   }

   /** Something stirs strong emotion. Returns true when it drops the player. */
   static boolean trigger(ServerPlayer sp, String what) {
      State c = PlusEffects.of(sp.getUUID()).cata;
      if (!has(sp) || !PlusEffects.afflictable(sp) || c.collapse > 0 || c.cooldown > 0) {
         return false;
      }

      if (sp.isShiftKeyDown() || sp.isPassenger()) {
         c.cooldown = WOBBLE_COOLDOWN_TICKS;
         PlusEffects.hint(sp, "§eYour knees wobble at " + what + ", but bracing holds you up.");
         return false;
      }

      c.collapse = COLLAPSE_TICKS;
      c.cooldown = COOLDOWN_TICKS;
      sp.setSprinting(false);
      sp.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, COLLAPSE_TICKS, 6, false, false, false));
      sp.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, COLLAPSE_TICKS, 3, false, false, false));
      PlusEffects.sendEpisode(sp, PlusNet.EpisodePayload.COLLAPSE, COLLAPSE_TICKS);
      PlusEffects.hint(sp, "§eYour knees give way: " + what + "! (Crouch to brace next time.)");
      return true;
   }

   /** Called with every chat message: anyone with cataplexy who reads laughter goes down. */
   static void onChat(MinecraftServer server, String text) {
      if (MindRules.isLaughter(text)) {
         for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            trigger(p, "laughter");
         }
      }
   }

   /** Mining a rich ore. */
   static void onFind(ServerPlayer sp, BlockState state) {
      if (state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE) || state.is(Blocks.EMERALD_ORE)
         || state.is(Blocks.DEEPSLATE_EMERALD_ORE) || state.is(Blocks.ANCIENT_DEBRIS)) {
         trigger(sp, "the thrill of a find");
      }
   }

   /** Killing a monster, now and then. */
   static void onKill(LivingEntity victim, DamageSource source) {
      if (!PlusAlienHand.slapping && victim instanceof Enemy && source.getEntity() instanceof ServerPlayer killer && PlusEffects.RNG.nextInt(Math.max(1, killOdds)) == 0) {
         trigger(killer, "the rush of a kill");
      }
   }
}
