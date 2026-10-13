package com.disabilitiesplus.mixin;

import com.disabilitiesplus.PlusDisability;
import com.disabilitiesplus.client.PlusClientState;
import com.studiobing.disabilities.Disability;
import com.studiobing.disabilities.client.ClientState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class PlusSepiaMixin {
    @Unique
    private static final Identifier SEPIA = Identifier.fromNamespaceAndPath("disabilities_plus", "sepia");

    @Inject(method = "update", at = @At("TAIL"))
    private void disabilitiesPlus$sepia(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (PlusClientState.has(PlusDisability.GINGER) && !ClientState.has(Disability.COLOR_BLIND)) {
            ((GameRenderer)(Object)this).getRequestedPostEffects().add(SEPIA);
        }
    }
}
