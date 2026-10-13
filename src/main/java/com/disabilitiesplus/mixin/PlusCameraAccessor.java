package com.disabilitiesplus.mixin;

import net.minecraft.client.Camera;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Camera.class)
public interface PlusCameraAccessor {
    @Accessor("rotation")
    Quaternionf disabilitiesPlus$getRotation();
}
