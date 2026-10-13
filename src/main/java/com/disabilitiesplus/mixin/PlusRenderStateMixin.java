package com.disabilitiesplus.mixin;

import com.disabilitiesplus.client.PlusLimbs;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class PlusRenderStateMixin implements PlusLimbs {
    @Unique
    private boolean disabilitiesPlus$hideAll;
    @Unique
    private boolean disabilitiesPlus$bald;
    @Unique
    private float disabilitiesPlus$girthX = 1.0F;
    @Unique
    private float disabilitiesPlus$girthZ = 1.0F;

    @Override
    public void disabilitiesPlus$setBody(boolean hideAll, float girthX, float girthZ) {
        this.disabilitiesPlus$hideAll = hideAll;
        this.disabilitiesPlus$girthX = girthX;
        this.disabilitiesPlus$girthZ = girthZ;
    }

    @Override
    public void disabilitiesPlus$setBald(boolean bald) {
        this.disabilitiesPlus$bald = bald;
    }

    @Override
    public boolean disabilitiesPlus$bald() {
        return this.disabilitiesPlus$bald;
    }

    @Override
    public boolean disabilitiesPlus$hideAll() {
        return this.disabilitiesPlus$hideAll;
    }

    @Override
    public float disabilitiesPlus$girthX() {
        return this.disabilitiesPlus$girthX;
    }

    @Override
    public float disabilitiesPlus$girthZ() {
        return this.disabilitiesPlus$girthZ;
    }
}
