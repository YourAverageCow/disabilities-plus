package com.disabilitiesplus.mixin;

import com.disabilitiesplus.PlusDisability;
import com.disabilitiesplus.PlusView;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class LimblessSlotMixin {
    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void disabilitiesPlus$noArms(int slotIndex, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
        if (PlusView.has(player, PlusDisability.QUADRUPLE_AMPUTEE)) {
            ci.cancel();
        }
    }
}
