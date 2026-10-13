package com.disabilitiesplus.mixin;

import com.disabilitiesplus.PlusState;
import com.studiobing.disabilities.Disability;
import com.studiobing.disabilities.RunState;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RunState.class, remap = false)
public class GrantRandomMixin {
    @Inject(method = "grantRandom", at = @At("HEAD"), cancellable = true)
    private static void disabilitiesPlus$pickAcrossBoth(UUID id, CallbackInfoReturnable<Disability> cir) {
        PlusState.Grant grant = PlusState.grantRandom(id);
        if (grant != null && grant.extra() != null) {
            MinecraftServer server = PlusState.server;
            ServerPlayer player = server == null ? null : server.getPlayerList().getPlayer(id);
            if (player != null) {
                PlusState.announce(player, grant.extra(), "developed");
            }
        }

        cir.setReturnValue(grant == null ? null : grant.original());
    }

    @Inject(method = "resetAll", at = @At("TAIL"))
    private static void disabilitiesPlus$resetToo(CallbackInfo ci) {
        PlusState.clearEveryone();
    }
}
