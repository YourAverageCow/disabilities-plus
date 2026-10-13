package com.disabilitiesplus.mixin;

import com.disabilitiesplus.PlusDisability;
import com.disabilitiesplus.client.PlusClientState;
import com.disabilitiesplus.client.PlusLimbs;
import java.util.UUID;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Flags, per rendered player, whether to hide all limbs (Quadruple Amputee), how wide the body is, and whether they are bald. */
@Mixin(AvatarRenderer.class)
public class PlusRendererMixin {
   @Inject(method = "extractRenderState", at = @At("TAIL"), require = 0)
   private void disabilitiesPlus$flagBody(Avatar entity, AvatarRenderState state, float tickDelta, CallbackInfo ci) {
      if (state instanceof PlusLimbs limbs) {
         UUID id = entity.getUUID();
         boolean obese = PlusClientState.has(id, PlusDisability.OBESITY);
         float girthX = obese ? 1.3F : 1.0F;
         float girthZ = obese ? 1.35F : (PlusClientState.has(id, PlusDisability.PREGNANCY) ? 1.45F : 1.0F);
         limbs.disabilitiesPlus$setBody(PlusClientState.has(id, PlusDisability.QUADRUPLE_AMPUTEE), girthX, girthZ);
         limbs.disabilitiesPlus$setBald(PlusClientState.has(id, PlusDisability.BALDNESS));
      }
   }
}
