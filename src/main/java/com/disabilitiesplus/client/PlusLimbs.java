package com.disabilitiesplus.client;

/** Extra per-render-state flags the add-on stores on {@code AvatarRenderState}. */
public interface PlusLimbs {
   void disabilitiesPlus$setBody(boolean hideAll, float girthX, float girthZ);

   boolean disabilitiesPlus$hideAll();

   void disabilitiesPlus$setBald(boolean bald);

   boolean disabilitiesPlus$bald();

   float disabilitiesPlus$girthX();

   float disabilitiesPlus$girthZ();
}
