package com.disabilitiesplus.mixin;

import com.disabilitiesplus.client.PlusClientState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class PlusCameraMixin {
    private static final double TIPSY_ROLL_DEGREES = 5.0;

    @Inject(method = "setRotation", at = @At("TAIL"), require = 0)
    private void disabilitiesPlus$roll(float yaw, float pitch, CallbackInfo ci) {
        float roll = 0.0F;
        if (PlusClientState.thrashing()) {
            roll += PlusClientState.thrashSide() ? 9.0F : -9.0F;
        }

        int migraine = PlusClientState.migraineTicks();
        if (migraine > 0) {
            roll += (float)(Math.sin(migraine * 0.15) * 6.0 * PlusClientState.migraineFade());
        }

        if (PlusClientState.has(com.disabilitiesplus.PlusDisability.TIPSY)) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                roll += (float)(Math.sin(player.tickCount * 0.045) * TIPSY_ROLL_DEGREES);
            }
        }

        if (roll != 0.0F) {
            ((PlusCameraAccessor)(Object)this).disabilitiesPlus$getRotation().rotateZ((float)Math.toRadians(roll));
        }
    }

    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true, require = 0)
    private void disabilitiesPlus$narrow(float partialTicks, CallbackInfoReturnable<Float> cir) {
        float factor = 1.0F;
        if (PlusClientState.has(com.disabilitiesplus.PlusDisability.TUNNEL_VISION)) {
            factor *= 0.62F;
        }

        factor *= 1.0F - 0.35F * PlusClientState.dread();
        if (PlusClientState.has(com.disabilitiesplus.PlusDisability.STONED)) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                factor *= 1.0F + 0.05F * (float)Math.sin(player.tickCount * 0.04);
            }
        }

        if (factor != 1.0F) {
            cir.setReturnValue(cir.getReturnValue() * factor);
        }
    }
}
