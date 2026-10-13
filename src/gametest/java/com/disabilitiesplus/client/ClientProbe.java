package com.disabilitiesplus.client;

import com.disabilitiesplus.PlusDisability;

/** Test-only window into package-private client state. Lives in the gametest source set, never shipped. */
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
      PlusClientEffects.babyKick(net.minecraft.client.Minecraft.getInstance());
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
      PlusClientEffects.hiccup(net.minecraft.client.Minecraft.getInstance());
   }

   public static void sneeze() {
      PlusClientEffects.sneeze(net.minecraft.client.Minecraft.getInstance());
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

   /** What a chat message becomes on its way out, through both Dysgraphia and Aphasia. */
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

   /** Plays a sound name through the same hook the sound engine calls, as if it came from the player's own position. */
   public static void hearSound(String path) {
      net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
      PlusClientEffects.onSoundPlayed(path, mc.player.getX(), mc.player.getY(), mc.player.getZ());
   }

   public static void startFlashback() {
      FearEffects.startFlashback(net.minecraft.client.Minecraft.getInstance());
   }

   /** The text that lands in a text box with the current disabilities. */
   public static String typed(String text) {
      return Dysgraphia.onTyped(text);
   }

   public static float soundDuck() {
      return PlusClientState.soundDuck;
   }

   /** Height of the camera's right-hand axis; 0 when the horizon is level, so it reads the camera roll (at pitch 0). */
   public static float cameraRightHeight() {
      net.minecraft.client.Camera cam = net.minecraft.client.Minecraft.getInstance().gameRenderer.mainCamera();
      return cam.rotation().transform(new org.joml.Vector3f(1.0F, 0.0F, 0.0F)).y;
   }

   /** What ends up in a text box when {@code text} is typed into it. */
   public static String typedIntoBox(String text) {
      net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
      net.minecraft.client.gui.components.EditBox box =
         new net.minecraft.client.gui.components.EditBox(mc.font, 0, 0, 200, 20, net.minecraft.network.chat.Component.empty());
      box.setMaxLength(1000);
      box.insertText(text);
      return box.getValue();
   }

   /** What ends up on a sign or book page (edited through TextFieldHelper) when {@code text} is typed. */
   public static String typedIntoPen(String text) {
      StringBuilder page = new StringBuilder();
      net.minecraft.client.gui.font.TextFieldHelper helper = new net.minecraft.client.gui.font.TextFieldHelper(
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

   /** Whether the client would let this chat message out right now. */
   public static boolean chatAllowed(String message) {
      return net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents.ALLOW_CHAT.invoker().allowSendChatMessage(message);
   }

   public static void setAmnesiaTimer(int ticks) {
      PlusClientState.amnesiaTimer = ticks;
   }
}
