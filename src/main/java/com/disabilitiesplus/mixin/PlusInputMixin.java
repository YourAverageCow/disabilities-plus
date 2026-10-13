package com.disabilitiesplus.mixin;

import com.disabilitiesplus.client.PlusClientState;
import com.studiobing.disabilities.client.ClientState;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** One place for everything the add-on does to the movement keys: forced presses, swapped strafe keys, and freezing. */
@Mixin(KeyboardInput.class)
public class PlusInputMixin {
   @Inject(method = "tick", at = @At("TAIL"))
   private void disabilitiesPlus$shapeInput(CallbackInfo ci) {
      // Always drain the one-shot flags, so one raised by a source that has since ended cannot fire later.
      boolean forcedJump = PlusClientState.consumeCpJump();
      boolean forcedSneak = PlusClientState.consumeCpSneak();
      ClientInput self = (ClientInput)(Object)this;
      Input cur = self.keyPresses;

      if (PlusClientState.frozenInFear()) {
         self.keyPresses = new Input(false, false, false, false, false, false, false);
         ((PlusInputAccessor)self).disabilitiesPlus$setMoveVector(Vec2.ZERO);
         return;
      }

      boolean forward = cur.forward();
      boolean backward = cur.backward();
      boolean left = cur.left();
      boolean right = cur.right();
      boolean jump = cur.jump();
      boolean shift = cur.shift();
      boolean changed = false;

      if (PlusClientState.forcesKeys() && !ClientState.isSeizing()) {
         if (PlusClientState.cpLeft()) {
            left = true;
            changed = true;
         }

         if (PlusClientState.cpRight()) {
            right = true;
            changed = true;
         }

         if (forcedJump) {
            jump = true;
            changed = true;
         }

         if (forcedSneak) {
            shift = true;
            changed = true;
         }
      }

      if (PlusClientState.confused()) {
         boolean t = left;
         left = right;
         right = t;
         changed = true;
      }

      if (changed) {
         self.keyPresses = new Input(forward, backward, left, right, jump, shift, cur.sprint());
         float x = (left ? 1.0F : 0.0F) - (right ? 1.0F : 0.0F);
         float y = (forward ? 1.0F : 0.0F) - (backward ? 1.0F : 0.0F);
         ((PlusInputAccessor)self).disabilitiesPlus$setMoveVector(new Vec2(x, y));
      }
   }
}
