package com.disabilitiesplus.client;

import com.disabilitiesplus.PlusDisability;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.network.chat.Component;

public final class ClientProbe {
    private ClientProbe() {
    }

    public static boolean has(PlusDisability d) {
        return PlusClientState.has(d);
    }

    public static float nightDim() {
        return PlusClientState.nightDim;
    }

    public static float migrainePulse() {
        return PlusClientState.migrainePulse();
    }

    public static int migraineTicks() {
        return PlusClientState.migraineTicks;
    }

    public static void startMigraine(int ticks) {
        PlusClientState.migraineTicks = ticks;
    }

    public static void startReverse(int ticks) {
        PlusClientState.swapTicks = ticks;
    }

    public static void setBlabOdds(int odds) {
        PlusClientEffects.blabOdds = odds;
    }

    public static int overloadNow() {
        return PlusClientState.overloadTicks;
    }

    public static int freezeCount() {
        return PlusClientState.freezeCount;
    }

    public static int heartbeatCount() {
        return PlusClientState.heartbeatCount;
    }

    public static float dread() {
        return PlusClientState.dread;
    }

    public static boolean clinging() {
        return PlusClientState.clinging;
    }

    public static int thrashCount() {
        return PlusClientState.thrashCount;
    }

    public static int buzzCount() {
        return PlusClientState.buzzCount;
    }

    public static int blinkTicks() {
        return PlusClientState.blinkTicks;
    }

    public static int painTicks() {
        return PlusClientState.painTicks;
    }

    public static int coughTicks() {
        return PlusClientState.coughTicks;
    }

    public static int necrosisStage() {
        return PlusClientState.necrosisStage;
    }

    public static int flashbackTicks() {
        return PlusClientState.flashbackTicks;
    }

    public static void kick() {
        PlusClientEffects.babyKick(Minecraft.getInstance());
    }

    public static int kickCount() {
        return PlusClientState.kickCount;
    }

    public static void setParanoiaOdds(int odds) {
        PlusClientEffects.paranoiaOdds = odds;
    }

    public static int paranoiaCount() {
        return PlusClientState.paranoiaCount;
    }

    public static int squeakCount() {
        return PlusClientState.squeakCount;
    }

    public static int overloadTicks() {
        return PlusClientState.overloadTicks;
    }

    public static void startConfusion(int ticks) {
        PlusClientState.confuseTicks = ticks;
    }

    public static void hiccup() {
        PlusClientEffects.hiccup(Minecraft.getInstance());
    }

    public static void sneeze() {
        PlusClientEffects.sneeze(Minecraft.getInstance());
    }

    public static int tinnitusTimer() {
        return PlusClientState.tinnitusTimer;
    }

    public static int napTicks() {
        return PlusClientState.napTicks;
    }

    public static float napFade() {
        return PlusClientState.napFade();
    }

    public static int obfuscatedCountDraws() {
        return PlusClientState.obfuscatedCountDraws;
    }

    public static int readableCountDraws() {
        return PlusClientState.readableCountDraws;
    }

    public static String garble(String message) {
        return DisabilitiesPlusClient.garbleChat(message);
    }

    public static int dustLevel() {
        return PlusClientState.dustLevel;
    }

    public static float soot() {
        return PlusClientState.soot();
    }

    public static int feverTicks() {
        return PlusClientState.feverTicks;
    }

    public static int agonyTicks() {
        return PlusClientState.agonyTicks;
    }

    public static float lidLevel() {
        return PlusClientState.lidLevel;
    }

    public static void setLidLevel(float level) {
        PlusClientState.lidLevel = level;
    }

    public static int echoCount() {
        return PlusClientState.echoCount;
    }

    public static int sneezeCount() {
        return PlusClientState.sneezeCount;
    }

    public static int freezeTicks() {
        return PlusClientState.freezeTicks;
    }

    public static void hearSound(String path) {
        Minecraft mc = Minecraft.getInstance();
        PlusClientEffects.onSoundPlayed(path, mc.player.getX(), mc.player.getY(), mc.player.getZ());
    }

    public static void startFlashback() {
        FearEffects.startFlashback(Minecraft.getInstance());
    }

    public static String typed(String text) {
        return Dysgraphia.onTyped(text);
    }

    public static float soundDuck() {
        return PlusClientState.soundDuck;
    }

    public static float cameraRightHeight() {
        Camera cam = Minecraft.getInstance().gameRenderer.mainCamera();
        return cam.rotation().transform(new org.joml.Vector3f(1.0F, 0.0F, 0.0F)).y;
    }

    public static String typedIntoBox(String text) {
        Minecraft mc = Minecraft.getInstance();
        EditBox box =
            new EditBox(mc.font, 0, 0, 200, 20, Component.empty());
        box.setMaxLength(1000);
        box.insertText(text);
        return box.getValue();
    }

    public static String typedIntoPen(String text) {
        StringBuilder page = new StringBuilder();
        TextFieldHelper helper = new TextFieldHelper(
            page::toString, v -> {
                page.setLength(0);
                page.append(v);
            }, () -> "", v -> {
            }, v -> true
        );
        helper.insertText(text);
        return page.toString();
    }

    public static int anxiety() {
        return PlusClientState.anxiety;
    }

    public static void setAnxiety(int value) {
        PlusClientState.anxiety = value;
    }

    public static int collapseTicks() {
        return PlusClientState.collapseTicks;
    }

    public static boolean chatAllowed(String message) {
        return ClientSendMessageEvents.ALLOW_CHAT.invoker().allowSendChatMessage(message);
    }

    public static void setAmnesiaTimer(int ticks) {
        PlusClientState.amnesiaTimer = ticks;
    }
}
