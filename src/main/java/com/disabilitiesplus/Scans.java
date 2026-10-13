package com.disabilitiesplus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** World scans shared by the server effects and the client-side fear reactions; both sides read blocks through a Level. */
public final class Scans {
   /** How far a neighbouring column must drop below the feet to count as a dangerous edge. */
   public static final int DROP_DEPTH = 10;

   private Scans() {
   }

   public static boolean solidAt(Level level, BlockPos pos) {
      return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
   }

   /** True when any column two blocks away (orthogonally or diagonally) drops at least DROP_DEPTH blocks below the feet. */
   public static boolean exposedToDrop(Level level, BlockPos feet) {
      BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();

      for (int dx = -2; dx <= 2; dx += 2) {
         for (int dz = -2; dz <= 2; dz += 2) {
            if (dx == 0 && dz == 0) {
               continue;
            }

            int depth = 0;
            for (int y = feet.getY() - 1; depth < DROP_DEPTH && y >= level.getMinY(); y--) {
               p.set(feet.getX() + dx, y, feet.getZ() + dz);
               if (solidAt(level, p)) {
                  break;
               }

               depth++;
            }

            if (depth >= DROP_DEPTH) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean blockedWithin(Level level, BlockPos origin, Direction dir, int max) {
      BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();

      for (int i = 1; i <= max; i++) {
         if (solidAt(level, p.set(origin).move(dir, i))) {
            return true;
         }
      }

      return false;
   }

   /** Walls within 3 blocks on all four sides and a ceiling within 2 blocks above the head. */
   public static boolean enclosed(Level level, BlockPos head) {
      for (Direction d : Direction.Plane.HORIZONTAL) {
         if (!blockedWithin(level, head, d, 3)) {
            return false;
         }
      }

      return blockedWithin(level, head, Direction.UP, 2);
   }

   /** The closest fire or lava block within {@code radius} horizontally (and -1..+2 vertically), or null. */
   public static BlockPos nearestFire(Level level, BlockPos centre, int radius) {
      BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
      BlockPos best = null;
      double bestDist = Double.MAX_VALUE;

      for (int dx = -radius; dx <= radius; dx++) {
         for (int dy = -1; dy <= 2; dy++) {
            for (int dz = -radius; dz <= radius; dz++) {
               p.set(centre.getX() + dx, centre.getY() + dy, centre.getZ() + dz);
               BlockState state = level.getBlockState(p);
               if (state.is(BlockTags.FIRE) || state.getFluidState().is(FluidTags.LAVA)) {
                  double dist = dx * dx + dy * dy + dz * dz;
                  if (dist < bestDist) {
                     bestDist = dist;
                     best = p.immutable();
                  }
               }
            }
         }
      }

      return best;
   }
}
