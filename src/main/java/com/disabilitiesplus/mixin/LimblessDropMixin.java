package com.disabilitiesplus.mixin;

import com.disabilitiesplus.PlusDisability;
import com.disabilitiesplus.PlusView;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A player with no arms has nothing to throw with. */
@Mixin(Inventory.class)
public abstract class LimblessDropMixin {
   @Inject(method = "removeFromSelected", at = @At("HEAD"), cancellable = true)
   private void disabilitiesPlus$noArms(boolean entireStack, CallbackInfoReturnable<ItemStack> cir) {
      Inventory self = (Inventory)(Object)this;
      if (PlusView.has(self.player, PlusDisability.QUADRUPLE_AMPUTEE)) {
         cir.setReturnValue(ItemStack.EMPTY);
      }
   }
}
