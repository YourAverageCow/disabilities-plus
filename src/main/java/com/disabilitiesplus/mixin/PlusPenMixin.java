package com.disabilitiesplus.mixin;

import com.disabilitiesplus.client.Dysgraphia;
import net.minecraft.client.gui.font.TextFieldHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Dysgraphia on signs and books, which edit through TextFieldHelper instead of a text box. */
@Mixin(TextFieldHelper.class)
public class PlusPenMixin {
   @ModifyVariable(method = "insertText(Ljava/lang/String;Ljava/lang/String;)V", at = @At("HEAD"), ordinal = 1, argsOnly = true)
   private String disabilitiesPlus$miswrite(String text) {
      return Dysgraphia.onTyped(text);
   }
}
