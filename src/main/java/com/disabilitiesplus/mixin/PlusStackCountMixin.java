package com.disabilitiesplus.mixin;

import com.disabilitiesplus.client.StackCounts;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Brackets the drawing of an item's stack count so it can be told apart from other text (see {@link StackCounts}). */
@Mixin(GuiGraphicsExtractor.class)
public class PlusStackCountMixin {
   @Inject(method = "itemCount(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"))
   private void disabilitiesPlus$countStart(CallbackInfo ci) {
      StackCounts.enter();
   }

   @Inject(method = "itemCount(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At("RETURN"))
   private void disabilitiesPlus$countEnd(CallbackInfo ci) {
      StackCounts.leave();
   }
}
