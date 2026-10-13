package com.disabilitiesplus.mixin;

import com.disabilitiesplus.client.PlusLimbs;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applied after the original mod's own limb hiding (higher priority number) so hiding every limb wins. */
@Mixin(value = PlayerModel.class, priority = 2000)
public class PlusModelMixin {
   @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"), require = 0)
   private void disabilitiesPlus$shapeBody(AvatarRenderState state, CallbackInfo ci) {
      if (state instanceof PlusLimbs limbs) {
         PlayerModel self = (PlayerModel)(Object)this;
         if (limbs.disabilitiesPlus$hideAll()) {
            self.leftArm.visible = false;
            self.leftSleeve.visible = false;
            self.rightArm.visible = false;
            self.rightSleeve.visible = false;
            self.leftLeg.visible = false;
            self.leftPants.visible = false;
            self.rightLeg.visible = false;
            self.rightPants.visible = false;
         }

         if (limbs.disabilitiesPlus$bald()) {
            self.hat.visible = false;
         }

         self.body.xScale = limbs.disabilitiesPlus$girthX();
         self.body.zScale = limbs.disabilitiesPlus$girthZ();
         self.jacket.xScale = limbs.disabilitiesPlus$girthX();
         self.jacket.zScale = limbs.disabilitiesPlus$girthZ();
      }
   }
}
