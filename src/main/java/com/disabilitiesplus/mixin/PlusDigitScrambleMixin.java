package com.disabilitiesplus.mixin;

import com.disabilitiesplus.PlusDisability;
import com.disabilitiesplus.client.Dyscalculia;
import com.disabilitiesplus.client.PlusClientState;
import com.disabilitiesplus.client.StackCounts;
import com.studiobing.disabilities.client.ClientState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GuiGraphicsExtractor.class)
public class PlusDigitScrambleMixin {
    @ModifyVariable(method = "text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V", at = @At("HEAD"), argsOnly = true)
    private String disabilitiesPlus$scrambleDigits(String text) {
        if (text == null) {
            return null;
        }

        if (StackCounts.drawing()) {
            return StackCounts.render(text);
        }

        return PlusClientState.has(PlusDisability.DYSCALCULIA) && !ClientState.dyslexiaExempt() ? Dyscalculia.scramble(text) : text;
    }
}
