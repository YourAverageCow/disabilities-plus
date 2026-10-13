package com.disabilitiesplus.client;

import com.disabilitiesplus.PlusDisability;
import com.disabilitiesplus.PlusView;
import java.util.Set;
import net.minecraft.client.Minecraft;

public final class PlusClientState {
    static boolean cpJump = false;
    static boolean cpSneak = false;
    static int cpMoveDir = -1;
    static int cpMoveTicks = 0;

    static int migraineTicks = 0;
    static float nightDim = 0.0F;
    static int tinnitusTimer = 0;
    static int amnesiaTimer = -1;
    static float soundDuck = 1.0F;
    static int overloadTicks = 0;

    static boolean soundReactive = false;
    static int confuseTicks = 0;
    static int swapTicks = 0;
    static int blinkTicks = 0;
    static float dread = 0.0F;
    static int freezeTicks = 0;
    static int freezeCooldown = 0;
    static int thrashTicks = 0;
    static boolean clinging = false;
    static int painTicks = 0;
    static int coughTicks = 0;
    static int necrosisStage = 0;
    static int flashbackTicks = 0;
    static int napTicks = 0;

    static int anxiety = 0;
    static int collapseTicks = 0;
    static int stillTicks = 0;

    static boolean companyNearby = false;
    static double stillX = 0.0;
    static double stillZ = 0.0;
    static int dustLevel = 0;
    static int feverTicks = 0;
    static int agonyTicks = 0;

    static float lidLevel = 0.0F;
    static int echoesLeft = 0;
    static int echoDelay = 0;
    static boolean echoing = false;
    static int pogoTimer = 0;
    static double squeakDistance = 0.0;
    static double lastStepX = 0.0;
    static double lastStepZ = 0.0;

    static int squeakCount = 0;
    static int kickCount = 0;
    static int paranoiaCount = 0;
    static int freezeCount = 0;
    static int heartbeatCount = 0;
    static int thrashCount = 0;
    static int buzzCount = 0;
    static int echoCount = 0;
    static int sneezeCount = 0;

    static int obfuscatedCountDraws = 0;
    static int readableCountDraws = 0;

    private static final int NAP_FADE_OUT_TICKS = 15;
    private static final int SOOT_FULL_LEVEL = 16;
    private static final int FEVER_TOTAL_TICKS = 400;

    private PlusClientState() {
    }

    public static boolean has(PlusDisability d) {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && PlusView.clientHas(mc.player.getUUID(), d);
    }

    public static boolean has(java.util.UUID player, PlusDisability d) {
        return PlusView.clientHas(player, d);
    }

    public static Set<PlusDisability> mine() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player == null ? Set.of() : PlusView.clientSet(mc.player.getUUID());
    }

    public static boolean forcesKeys() {
        return has(PlusDisability.ATAXIA)
            || has(PlusDisability.POGO)
            || has(PlusDisability.JUMPY)
            || has(PlusDisability.ACROPHOBIA)
            || has(PlusDisability.AQUAPHOBIA)
            || painTicks > 0
            || collapseTicks > 0;
    }

    public static boolean cpLeft() {
        return cpMoveTicks > 0 && cpMoveDir == 2;
    }

    public static boolean cpRight() {
        return cpMoveTicks > 0 && cpMoveDir == 3;
    }

    public static boolean consumeCpJump() {
        boolean v = cpJump;
        cpJump = false;
        return v;
    }

    public static boolean consumeCpSneak() {
        boolean v = cpSneak;
        cpSneak = false;
        return v;
    }

    public static boolean confused() {
        return confuseTicks > 0;
    }

    public static boolean reversed() {
        return swapTicks > 0;
    }

    public static boolean frozenInFear() {
        return freezeTicks > 0;
    }

    public static boolean clinging() {
        return clinging;
    }

    public static boolean thrashing() {
        return thrashTicks > 0;
    }

    public static boolean thrashSide() {
        return (thrashCount & 1) == 0;
    }

    public static int painTicks() {
        return painTicks;
    }

    public static int necrosisStage() {
        return necrosisStage;
    }

    public static float napFade() {
        return napTicks <= 0 ? 0.0F : Math.min(1.0F, napTicks / (float)NAP_FADE_OUT_TICKS);
    }

    public static int anxiety() {
        return anxiety;
    }

    public static int collapseTicks() {
        return collapseTicks;
    }

    public static int napTicks() {
        return napTicks;
    }

    public static float soot() {
        return Math.min(1.0F, dustLevel / (float)SOOT_FULL_LEVEL);
    }

    public static float feverFade() {
        return feverTicks <= 0 ? 0.0F : Math.min(1.0F, Math.min(feverTicks, FEVER_TOTAL_TICKS - feverTicks) / 30.0F + 0.0F);
    }

    public static int feverTicks() {
        return feverTicks;
    }

    public static int agonyTicks() {
        return agonyTicks;
    }

    public static float lidLevel() {
        return lidLevel;
    }

    public static int obfuscatedCountDraws() {
        return obfuscatedCountDraws;
    }

    public static int readableCountDraws() {
        return readableCountDraws;
    }

    public static float dread() {
        return dread;
    }

    public static int migraineTicks() {
        return migraineTicks;
    }

    public static float migraineFade() {
        return Math.min(1.0F, migraineTicks / 30.0F);
    }

    public static float migrainePulse() {
        return migraineTicks <= 0 ? 0.0F : migraineFade() * (0.5F + 0.5F * (float)Math.sin(migraineTicks * 0.2));
    }

    public static float nightDim() {
        return nightDim;
    }

    public static float overloadFade() {
        return Math.min(1.0F, overloadTicks / 30.0F);
    }

    public static int overloadTicks() {
        return overloadTicks;
    }

    public static float blinkFade() {
        return blinkTicks <= 0 ? 0.0F : Math.max(0.0F, 1.0F - Math.abs(blinkTicks - 10) / 10.0F);
    }

    public static float soundDuck() {
        return soundDuck;
    }

    public static void clear() {
        cpJump = false;
        cpSneak = false;
        cpMoveDir = -1;
        cpMoveTicks = 0;
        migraineTicks = 0;
        nightDim = 0.0F;
        tinnitusTimer = 0;
        amnesiaTimer = -1;
        soundDuck = 1.0F;
        overloadTicks = 0;
        soundReactive = false;
        confuseTicks = 0;
        swapTicks = 0;
        blinkTicks = 0;
        dread = 0.0F;
        freezeTicks = 0;
        freezeCooldown = 0;
        thrashTicks = 0;
        clinging = false;
        painTicks = 0;
        coughTicks = 0;
        necrosisStage = 0;
        flashbackTicks = 0;
        napTicks = 0;
        anxiety = 0;
        collapseTicks = 0;
        stillTicks = 0;
        companyNearby = false;
        dustLevel = 0;
        feverTicks = 0;
        agonyTicks = 0;
        lidLevel = 0.0F;
        echoesLeft = 0;
        echoDelay = 0;
        echoing = false;
        pogoTimer = 0;
        squeakDistance = 0.0;
        squeakCount = 0;
        kickCount = 0;
        paranoiaCount = 0;
        freezeCount = 0;
        heartbeatCount = 0;
        thrashCount = 0;
        buzzCount = 0;
        echoCount = 0;
        sneezeCount = 0;
        obfuscatedCountDraws = 0;
        readableCountDraws = 0;
        PlusView.clearClient();
        FearEffects.reset();
    }
}
