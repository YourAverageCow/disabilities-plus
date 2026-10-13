package com.disabilitiesplus.mixin;

import com.disabilitiesplus.client.PlusClientEffects;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public class PlusOverloadSoundMixin {
    @Inject(
        method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;",
        at = @At("HEAD")
    )
    private void disabilitiesPlus$noteSound(SoundInstance sound, CallbackInfoReturnable<?> cir) {
        PlusClientEffects.onSoundPlayed(sound.getIdentifier().getPath(), sound.getX(), sound.getY(), sound.getZ());
    }
}
