package com.disabilitiesplus.mixin;

import com.disabilitiesplus.client.PlusClientState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public class PlusInvertLookMixin {
    @ModifyVariable(method = "turn(DD)V", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private double disabilitiesPlus$invertPitch(double xRot) {
        return PlusClientState.reversed() && (Object)this instanceof LocalPlayer ? -xRot : xRot;
    }
}
