package com.disabilitiesplus.mixin;

import com.disabilitiesplus.client.PlusClientState;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public class PlusSoundDuckMixin {
    @Inject(method = "calculateVolume(FLnet/minecraft/sounds/SoundSource;)F", at = @At("RETURN"), cancellable = true)
    private void disabilitiesPlus$duck(float volume, SoundSource category, CallbackInfoReturnable<Float> cir) {
        float duck = PlusClientState.soundDuck();
        if (duck != 1.0F && category != SoundSource.VOICE) {
            cir.setReturnValue(cir.getReturnValue() * duck);
        }
    }
}
