package com.disabilitiesplus.mixin;

import com.disabilitiesplus.client.PlusHudList;
import com.studiobing.disabilities.client.DisabilityHud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DisabilityHud.class, remap = false)
public class PlusHudListMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private static void disabilitiesPlus$combinedList(GuiGraphicsExtractor ctx, int mouseX, int mouseY, CallbackInfo ci) {
        PlusHudList.render(ctx, mouseX, mouseY);
        ci.cancel();
    }
}
