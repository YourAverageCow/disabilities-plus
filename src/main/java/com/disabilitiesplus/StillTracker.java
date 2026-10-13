package com.disabilitiesplus;

import net.minecraft.server.level.ServerPlayer;

/** Counts how many ticks in a row a player has been crouching without moving sideways (the self-check and self-fix gesture). */
final class StillTracker {
   /** Less than a hundredth of a block of sideways movement in a tick counts as standing still (squared). */
   private static final double MOVED_SQ = 0.01 * 0.01;
   double lastX = 0.0;
   double lastZ = 0.0;
   int ticks = 0;

   /** Call once per tick; returns the number of consecutive crouched-and-still ticks including this one. */
   int update(ServerPlayer sp) {
      double dx = sp.getX() - this.lastX;
      double dz = sp.getZ() - this.lastZ;
      this.lastX = sp.getX();
      this.lastZ = sp.getZ();
      this.ticks = sp.isShiftKeyDown() && dx * dx + dz * dz < MOVED_SQ ? this.ticks + 1 : 0;
      return this.ticks;
   }

   void reset() {
      this.ticks = 0;
   }
}
