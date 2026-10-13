package com.disabilitiesplus.mixin;

import com.disabilitiesplus.client.Dysgraphia;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EditBox.class)
public class PlusTypingMixin {
    @ModifyVariable(method = "insertText(Ljava/lang/String;)V", at = @At("HEAD"), argsOnly = true)
    private String disabilitiesPlus$miswrite(String text) {
        return Dysgraphia.onTyped(text);
    }
}
