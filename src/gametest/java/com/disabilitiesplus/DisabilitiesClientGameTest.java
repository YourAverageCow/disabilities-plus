package com.disabilitiesplus;

import com.disabilitiesplus.client.ClientProbe;
import com.studiobing.disabilities.Disabilities;
import com.studiobing.disabilities.Disability;
import com.studiobing.disabilities.RunState;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.storage.LevelResource;

/**
 * Headless end-to-end check of the add-on's disabilities in a real 26.3 client + integrated server.
 * Prints [GAMETEST] PASS/FAIL lines and throws at the end if anything failed.
 */
public class DisabilitiesClientGameTest implements FabricClientGameTest {
   private final List<String> failures = new ArrayList<>();
   private int passes = 0;
   private ClientGameTestContext ctx;
   private TestServerContext server;
   private int px;
   private int pz;

   @Override
   public void runTest(ClientGameTestContext context) {
      this.ctx = context;
      try (TestSingleplayerContext world = context.worldBuilder().create()) {
         this.server = world.getServer();
         world.getConnection().waitForChunksRender();
         try {
            this.everything();
         } catch (Throwable t) {
            t.printStackTrace();
            this.failures.add("harness threw " + t);
         }
      }

      System.out.println("[GAMETEST] passes=" + this.passes + " failures=" + this.failures.size());
      if (!this.failures.isEmpty()) {
         throw new AssertionError("Gametest failures:\n - " + String.join("\n - ", this.failures));
      }
   }

   // ---------------------------------------------------------------- helpers

   private void check(String name, boolean ok, String detail) {
      if (ok) {
         this.passes++;
         System.out.println("[GAMETEST] PASS " + name + (detail.isEmpty() ? "" : " :: " + detail));
      } else {
         this.failures.add(name + " :: " + detail);
         System.out.println("[GAMETEST] FAIL " + name + " :: " + detail);
      }
   }

   private void cmd(String command) {
      this.server.runCommand(command);
   }

   private void ticks(int n) {
      this.ctx.waitTicks(n);
   }

   private UUID id() {
      return this.server.computeOnServer(s -> s.getPlayerList().getPlayers().get(0).getUUID());
   }

   private <T> T onPlayer(java.util.function.Function<ServerPlayer, T> f) {
      return this.server.computeOnServer(s -> f.apply(s.getPlayerList().getPlayers().get(0)));
   }

   private boolean serverHas(PlusDisability d) {
      UUID id = this.id();
      return PlusState.has(id, d);
   }

   private boolean clientHas(PlusDisability d) {
      return this.ctx.computeOnClient(mc -> PlusView.clientHas(mc.player.getUUID(), d));
   }

   private boolean waitServer(Predicate<MinecraftServer> p, int max) {
      try {
         this.server.waitFor(p, max);
         return true;
      } catch (Throwable t) {
         return false;
      }
   }

   private boolean hasEffect(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> e, int amp) {
      return this.onPlayer(p -> {
         MobEffectInstance i = p.getEffect(e);
         return i != null && i.getAmplifier() == amp;
      });
   }

   private void giveOnly(String name) {
      this.cmd("disability remove @a all");
      this.cmd("disability give @a " + name);
      this.ticks(4);
   }

   private void clean() {
      this.cmd("disability remove @a all");
      this.cmd("effect clear @a");
      this.onPlayer(p -> {
         p.setHealth(p.getMaxHealth());
         p.getFoodData().setFoodLevel(10);
         p.clearFire();
         return null;
      });
      this.ticks(3);
   }

   private void platform() {
      // Big stone slab high above the terrain, daylight, clear sky.
      this.cmd("fill " + (this.px - 14) + " 249 " + (this.pz - 14) + " " + (this.px + 14) + " 249 " + (this.pz + 14) + " minecraft:stone");
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.ticks(10);
   }

   private List<String> chatLines() {
      try {
         return this.chatLinesChecked();
      } catch (ReflectiveOperationException e) {
         throw new IllegalStateException(e);
      }
   }

   private List<String> chatLinesChecked() throws ReflectiveOperationException {
      return this.ctx.computeOnClient(mc -> {
         ChatComponent chat = mc.gui.hud.getChat();
         Field f = ChatComponent.class.getDeclaredField("allMessages");
         f.setAccessible(true);
         List<?> all = (List<?>)f.get(chat);
         List<String> out = new ArrayList<>();
         for (Object gm : all) {
            Method m = gm.getClass().getMethod("content");
            out.add(((Component)m.invoke(gm)).getString());
         }

         return out;
      });
   }

   // ---------------------------------------------------------------- the run

   private void everything() throws Exception {
      this.cmd("difficulty easy");
      this.cmd("disability timer off");
      this.cmd("time set noon");
      this.cmd("weather clear");
      double[] pos = this.onPlayer(p -> new double[]{p.getX(), p.getZ()});
      this.px = (int)Math.floor(pos[0]);
      this.pz = (int)Math.floor(pos[1]);
      this.ticks(5);

      this.commandsAndSync();
      this.persistence();
      this.cmd("gamemode survival @a");
      this.platform();
      this.clientVisuals();
      this.serverMechanics();
      this.moreDisabilities();
      this.jokesAndDeath();
      this.traitsAndCravings();
      this.fearsAndEpisodes();
      this.coverageGaps();
      this.ataxia();
      this.illnessesAndQuirks();
      this.gimmicks();
      this.social();
      this.bodies();
      this.clean();
   }

   private void commandsAndSync() {
      this.cmd("disability give @a all");
      this.ticks(5);
      UUID id = this.id();
      int serverCount = PlusState.totalCount(id);
      int clientCount = this.ctx.computeOnClient(mc -> (Disabilities.clientSet(mc.player.getUUID()).size() + PlusView.clientSet(mc.player.getUUID()).size()));
      this.check("give all -> server has 112", serverCount == 112, "server=" + serverCount);
      this.check("give all -> client sees 112 (multi-word mask, no wrap-around)", clientCount == 112, "client=" + clientCount);
      for (String hide : new String[]{"blind", "anosognosia", "narcolepsy", "tunnel_vision", "night_blindness", "seizures", "vertigo", "schizophrenia", "claustrophobia", "acrophobia"}) {
         this.cmd("disability remove @a " + hide);
      }

      this.ticks(8);
      int shown = this.ctx.computeOnClient(mc -> (Disabilities.clientSet(mc.player.getUUID()).size() + PlusView.clientSet(mc.player.getUUID()).size()));
      this.check("hud list has 102 entries for the layout shot", shown == 102, "shown=" + shown);
      this.ctx.takeScreenshot("hud_list_102_multicolumn");
      this.cmd("disability give @a all");
      this.ticks(4);
      this.cmd("disability remove @a all");
      this.ticks(5);
      this.check("remove all -> 0", PlusState.totalCount(id) == 0 && !this.clientHas(PlusDisability.TINNITUS), "");

      for (int i = 0; i < 3; i++) {
         this.cmd("disability give @a random");
         this.ticks(2);
      }

      this.check("give random x3 -> 3", PlusState.totalCount(id) == 3, "size=" + PlusState.totalCount(id));
      this.cmd("disability next @a");
      this.ticks(2);
      this.check("next -> 4", PlusState.totalCount(id) == 4, "size=" + PlusState.totalCount(id));
      this.cmd("disability remove @a random");
      this.ticks(2);
      this.check("remove random -> 3", PlusState.totalCount(id) == 3, "size=" + PlusState.totalCount(id));
      this.cmd("disability remove @a all");

      for (PlusDisability d : PlusDisability.values()) {
         String n = d.name().toLowerCase();
         this.cmd("disability give @a " + n);
         this.ticks(3);
         boolean s = this.serverHas(d);
         boolean c = this.clientHas(d);
         int clientSize = this.ctx.computeOnClient(mc -> (Disabilities.clientSet(mc.player.getUUID()).size() + PlusView.clientSet(mc.player.getUUID()).size()));
         boolean only = PlusState.totalCount(id) == 1 && clientSize == 1;
         this.check("command+sync " + n, s && c && only, "server=" + s + " client=" + c + " onlyOne=" + only + " clientSize=" + clientSize);
         this.cmd("disability remove @a " + n);
         this.ticks(3);
         this.check("remove " + n, !this.serverHas(d) && !this.clientHas(d), "");
      }
   }

   private void persistence() throws Exception {
      this.cmd("disability remove @a all");
      this.cmd("disability give @a tinnitus");
      this.cmd("disability give @a heart_condition");
      this.cmd("disability give @a night_blindness");
      this.ticks(3);
      UUID id = this.id();
      Path file = this.server.computeOnServer(s -> {
         PlusState.save();
         return s.getWorldPath(LevelResource.ROOT).resolve("disabilities-plus.txt");
      });
      String text = Files.readString(file);
      this.check("save file lists new names", text.contains("TINNITUS") && text.contains("HEART_CONDITION") && text.contains("NIGHT_BLINDNESS"), text.replace('\n', ' '));
      this.server.runOnServer(s -> {
         PlusState.clearEveryone();
         PlusState.save();
      });
      this.server.runOnServer(s -> {
         try {
            Files.writeString(file, text);
         } catch (Exception e) {
            throw new RuntimeException(e);
         }

         PlusState.load(s);
      });
      this.check(
         "load restores new disabilities from file",
         PlusState.has(id, PlusDisability.TINNITUS) && PlusState.has(id, PlusDisability.HEART_CONDITION) && PlusState.has(id, PlusDisability.NIGHT_BLINDNESS),
         String.valueOf(PlusState.active(id))
      );
      this.cmd("disability remove @a all");
   }

   private void clientVisuals() {
      this.clean();
      this.ctx.takeScreenshot("00_baseline");

      // Tunnel vision: FOV modifier shrinks to 0.62x and the vignette is drawn.
      float base = this.ctx.computeOnClient(mc -> mc.gameRenderer.mainCamera().getFov());
      this.giveOnly("tunnel_vision");
      float narrow = this.ctx.computeOnClient(mc -> mc.gameRenderer.mainCamera().getFov());
      this.check("tunnel_vision narrows FOV x0.62", Math.abs(narrow - base * 0.62F) < 0.05F, "base=" + base + " narrow=" + narrow);
      this.ctx.takeScreenshot("tunnel_vision");
      // Regression: Amputated Leg forces the FOV modifier to 1.0 and must still compose with Tunnel Vision.
      this.cmd("disability give @a amputated_leg");
      this.ticks(4);
      float both = this.ctx.computeOnClient(mc -> mc.gameRenderer.mainCamera().getFov());
      this.check("tunnel_vision composes with amputated_leg (still x0.62)", Math.abs(both - base * 0.62F) < 0.05F, "base=" + base + " both=" + both);
      this.cmd("disability remove @a amputated_leg");

      // Migraine: force an episode, pulse is drawn, sound is ducked, roll mixin runs.
      this.giveOnly("migraine");
      this.ctx.runOnClient(mc -> ClientProbe.startMigraine(200));
      this.ticks(20);
      float pulse = this.ctx.computeOnClient(mc -> ClientProbe.migrainePulse());
      int left = this.ctx.computeOnClient(mc -> ClientProbe.migraineTicks());
      float peak = 0.0F;
      for (int i = 0; i < 40; i++) {
         this.ticks(1);
         peak = Math.max(peak, this.ctx.computeOnClient(mc -> ClientProbe.migrainePulse()));
      }

      this.check("migraine episode ticking down", left > 0 && left < 200, "left=" + left + " pulse=" + pulse);
      this.check("migraine pulse reaches a strong value", peak > 0.7F, "peak=" + peak);
      this.ticks(15);
      this.ctx.takeScreenshot("migraine");
      this.ctx.runOnClient(mc -> ClientProbe.startMigraine(0));

      // Tinnitus: ring timer wraps (proves the sound path ran without throwing).
      this.giveOnly("tinnitus");
      int max = 0;
      boolean wrapped = false;
      int prev = this.ctx.computeOnClient(mc -> ClientProbe.tinnitusTimer());
      this.ctx.getInput().holdKey(o -> o.keyUp);
      for (int i = 0; i < 70; i++) {
         if (i == 35) {
            this.ctx.getInput().releaseKey(o -> o.keyUp);
            this.ctx.getInput().holdKey(o -> o.keyDown);
         }

         this.ticks(1);
         int now = this.ctx.computeOnClient(mc -> ClientProbe.tinnitusTimer());
         max = Math.max(max, now);
         if (now < prev) {
            wrapped = true;
         }

         prev = now;
      }

      this.ctx.getInput().releaseKey(o -> o.keyUp);
      this.ctx.getInput().releaseKey(o -> o.keyDown);
      this.check("tinnitus ring fires every ~30 ticks while you move", wrapped && max >= 25, "max=" + max + " wrapped=" + wrapped);
      this.cmd("disability remove @a tinnitus");

      // Night blindness: bright outdoors -> ~0 dim, sealed dark room -> heavy dim.
      this.giveOnly("night_blindness");
      this.ticks(40);
      float outdoors = this.ctx.computeOnClient(mc -> ClientProbe.nightDim());
      this.check("night_blindness: no dimming in daylight", outdoors < 0.1F, "dim=" + outdoors);
      this.ctx.takeScreenshot("night_blindness_day");
      this.cmd("fill " + (this.px - 2) + " 249 " + (this.pz - 2) + " " + (this.px + 2) + " 253 " + (this.pz + 2) + " minecraft:stone hollow");
      this.ticks(70);
      float room = this.ctx.computeOnClient(mc -> ClientProbe.nightDim());
      this.check("night_blindness: heavy dimming in dark room", room > 0.6F, "dim=" + room);
      this.ctx.takeScreenshot("night_blindness_dark_room");
      this.cmd("fill " + (this.px - 2) + " 250 " + (this.pz - 2) + " " + (this.px + 2) + " 253 " + (this.pz + 2) + " minecraft:air");
      this.cmd("fill " + (this.px - 14) + " 249 " + (this.pz - 14) + " " + (this.px + 14) + " 249 " + (this.pz + 14) + " minecraft:stone");
      this.ticks(5);

      // Aphasia: real MODIFY_CHAT path through the server and back into the chat HUD.
      String phrase = "the quick brown fox jumps over the lazy dog";
      this.cmd("disability remove @a all");
      this.ticks(3);
      this.ctx.runOnClient(mc -> mc.player.connection.sendChat(phrase));
      this.ticks(10);
      long normal = this.chatLines().stream().filter(l -> l.contains(phrase)).count();
      this.check("chat unmodified without aphasia", normal >= 1, "matches=" + normal);
      this.giveOnly("aphasia");
      for (int i = 0; i < 12; i++) {
         this.ctx.runOnClient(mc -> mc.player.connection.sendChat(phrase));
         this.ticks(6);
      }

      List<String> lines = this.chatLines();
      long exact = lines.stream().filter(l -> l.contains(phrase)).count();
      long mentions = lines.stream().filter(l -> l.contains("<") || l.contains(">")).count();
      this.check("aphasia garbles outgoing chat", exact < 1 + 12 && exact <= 3, "exact=" + exact + " total chat lines=" + lines.size() + " (" + mentions + " player lines)");
      this.ctx.takeScreenshot("aphasia_chat");

      // Amnesia: chat history is wiped when the timer fires.
      this.cmd("disability give @a amnesia");
      this.ticks(4);
      int before = this.chatLines().size();
      this.ctx.runOnClient(mc -> ClientProbe.setAmnesiaTimer(3));
      this.ticks(10);
      int after = this.chatLines().size();
      this.check("amnesia wipes chat history", before > 0 && after == 0, "before=" + before + " after=" + after);
      this.cmd("disability remove @a all");

      // Dyscalculia: item counts are drawn scrambled (64 -> 70 with the fixed map). Screenshot for visual inspection.
      this.giveOnly("dyscalculia");
      this.cmd("give @a minecraft:cobblestone 64");
      this.ticks(10);
      this.ctx.takeScreenshot("dyscalculia_64_should_read_70");
      this.cmd("disability remove @a all");
   }

   private void serverMechanics() {
      // Anemia: healing is halved (8 + 4 heals to 12 normally and to 10 with it).
      this.clean();
      this.server.runOnServer(s -> s.getPlayerList().getPlayers().get(0).setHealth(8.0F));
      this.ticks(3);
      this.server.runOnServer(s -> s.getPlayerList().getPlayers().get(0).heal(4.0F));
      this.ticks(3);
      float fullHeal = this.onPlayer(ServerPlayer::getHealth);
      this.giveOnly("anemia");
      this.server.runOnServer(s -> s.getPlayerList().getPlayers().get(0).setHealth(8.0F));
      this.ticks(3);
      this.server.runOnServer(s -> s.getPlayerList().getPlayers().get(0).heal(4.0F));
      this.ticks(3);
      float halfHeal = this.onPlayer(ServerPlayer::getHealth);
      this.check("anemia: healing is halved (12 normally, 10 with it)", Math.abs(fullHeal - 12.0F) < 0.6F && Math.abs(halfHeal - 10.0F) < 0.6F,
         "control=" + fullHeal + " anemic=" + halfHeal);
      this.cmd("disability remove @a anemia");
      this.ticks(3);
      boolean noOldModifiers = !this.hasModifier(Attributes.MOVEMENT_SPEED, "anemia_speed") && !this.hasModifier(Attributes.ATTACK_DAMAGE, "anemia_attack");
      this.check("anemia no longer slows you or weakens your attack", noOldModifiers, "");

      // Hemophilia: a 2 damage hit keeps bleeding afterwards; control without it loses only 2.
      this.clean();
      this.giveOnly("hemophilia");
      this.cmd("damage @a[limit=1] 2 minecraft:generic");
      this.ticks(135);
      float hemo = this.onPlayer(ServerPlayer::getHealth);
      this.check("hemophilia: bleeds after the hit (20 -> <=16)", hemo <= 16.0F, "health=" + hemo);
      this.clean();
      this.cmd("damage @a[limit=1] 2 minecraft:generic");
      this.ticks(135);
      float ctl = this.onPlayer(ServerPlayer::getHealth);
      this.check("control without hemophilia only loses the hit (20 -> 18)", ctl >= 17.9F, "health=" + ctl);

      // Heart condition: strain pre-loaded, then real sprinting through the input layer triggers damage + slowness III.
      this.clean();
      this.giveOnly("heart_condition");
      UUID id = this.id();
      this.platform();
      this.server.runOnServer(s -> PlusEffects.of(id).heartStrain = 298.0);
      float h0 = this.onPlayer(ServerPlayer::getHealth);
      this.ctx.getInput().holdKey(o -> o.keyUp);
      this.ctx.getInput().holdKey(o -> o.keySprint);
      boolean slowed = this.waitServer(s -> {
         MobEffectInstance e = s.getPlayerList().getPlayers().get(0).getEffect(MobEffects.SLOWNESS);
         return e != null && e.getAmplifier() == 2;
      }, 200);
      this.ctx.getInput().releaseKey(o -> o.keyUp);
      this.ctx.getInput().releaseKey(o -> o.keySprint);
      float h1 = this.onPlayer(ServerPlayer::getHealth);
      this.check("heart_condition: sprint strain -> slowness III", slowed, "");
      this.check("heart_condition: strain dealt damage", h1 < h0, "before=" + h0 + " after=" + h1);

      // Motion sickness: a boat ride is fine with your eyes on the horizon and makes you nauseous when you look down.
      this.clean();
      this.platform();
      this.giveOnly("motion_sickness");
      this.cmd("summon minecraft:oak_boat " + (this.px + 0.5) + " 250 " + (this.pz + 0.5));
      this.ticks(3);
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.cmd("ride @a[limit=1] mount @e[type=minecraft:oak_boat,limit=1]");
      boolean wellOnHorizon = !this.waitServer(s -> s.getPlayerList().getPlayers().get(0).hasEffect(MobEffects.NAUSEA), 100);
      this.cmd("rotate @a[limit=1] 0 60");
      boolean sick = this.waitServer(s -> s.getPlayerList().getPlayers().get(0).hasEffect(MobEffects.NAUSEA), 120);
      this.check("motion_sickness: fine on the horizon, nauseous looking down in a boat", wellOnHorizon && sick, "horizon=" + wellOnHorizon + " lookingDown=" + sick);
      this.cmd("ride @a[limit=1] dismount");
      this.cmd("kill @e[type=minecraft:oak_boat]");
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");

      // Claustrophobia: open sky leaves the air alone; a sealed room drains it, closes the view in and pounds the heart.
      this.clean();
      this.platform();
      this.giveOnly("claustrophobia");
      this.ticks(40);
      boolean openAir = this.onPlayer(p -> p.getAirSupply() >= p.getMaxAirSupply());
      this.check("claustrophobia: open sky leaves the air alone", openAir, "");
      int beats0 = this.ctx.computeOnClient(mc -> ClientProbe.heartbeatCount());
      this.cmd("fill " + (this.px - 2) + " 249 " + (this.pz - 2) + " " + (this.px + 2) + " 253 " + (this.pz + 2) + " minecraft:stone hollow");
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5));
      this.ticks(90);
      boolean airGone = this.onPlayer(p -> p.getAirSupply() < p.getMaxAirSupply() - 60);
      float squeezedDread = this.ctx.computeOnClient(mc -> ClientProbe.dread());
      int beats1 = this.ctx.computeOnClient(mc -> ClientProbe.heartbeatCount());
      this.check("claustrophobia: a sealed room drains the air, closes the view in and pounds the heart",
         airGone && squeezedDread > 0.5F && beats1 > beats0, "airGone=" + airGone + " dread=" + squeezedDread + " beats=" + (beats1 - beats0));
      this.cmd("fill " + (this.px - 2) + " 250 " + (this.pz - 2) + " " + (this.px + 2) + " 253 " + (this.pz + 2) + " minecraft:air");

      // Acrophobia: a wide platform is fine; a lone pillar makes you cling in a crouch.
      this.clean();
      this.platform();
      this.giveOnly("acrophobia");
      this.ticks(40);
      boolean relaxed = !this.onPlayer(p -> p.isShiftKeyDown()) && !this.ctx.computeOnClient(mc -> ClientProbe.clinging());
      int tx = this.px + 30;
      int tz = this.pz + 30;
      this.cmd("setblock " + tx + " 150 " + tz + " minecraft:stone");
      this.cmd("tp @a " + (tx + 0.5) + " 151 " + (tz + 0.5));
      this.ticks(40);
      boolean clung = this.onPlayer(p -> p.isShiftKeyDown()) && this.ctx.computeOnClient(mc -> ClientProbe.clinging());
      this.check("acrophobia: a wide platform is fine, a lone pillar makes you cling in a crouch", relaxed && clung, "relaxed=" + relaxed + " clung=" + clung);
      this.ctx.takeScreenshot("acrophobia_pillar");
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5));
      this.ticks(3);
      this.cmd("setblock " + tx + " 150 " + tz + " minecraft:air");

      // Photosensitivity: helmet protects, bare head ignites.
      this.clean();
      this.platform();
      this.cmd("time set noon");
      this.cmd("item replace entity @a armor.head with minecraft:iron_helmet");
      this.giveOnly("photosensitivity");
      this.ticks(70);
      boolean protectedHead = !this.onPlayer(ServerPlayer::isOnFire);
      this.check("photosensitivity: helmet prevents burning", protectedHead, "");
      this.cmd("item replace entity @a armor.head with minecraft:air");
      boolean burning = this.waitServer(s -> s.getPlayerList().getPlayers().get(0).isOnFire(), 80);
      this.check("photosensitivity: bare head burns in sunlight", burning, "");
      this.ctx.takeScreenshot("photosensitivity_burning");
      this.cmd("time set midnight");
      this.cmd("disability remove @a all");
      this.cmd("time set noon");
   }

   private float exhaustion() {
      return this.onPlayer(p -> {
         try {
            Field f = net.minecraft.world.food.FoodData.class.getDeclaredField("exhaustionLevel");
            f.setAccessible(true);
            return f.getFloat(p.getFoodData());
         } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
         }
      });
   }

   private boolean hasModifier(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, String path) {
      Identifier id = Identifier.fromNamespaceAndPath("disabilities_plus", path);
      return this.onPlayer(p -> p.getAttribute(attr).hasModifier(id));
   }

   /** The ten disabilities added in 0.7.0: autism, bipolar and eight light-hearted ones. */
   private void moreDisabilities() {
      UUID id = this.id();

      // Butterfingers: forced odds -> the held item is dropped; Kleptomania still blocks it.
      this.clean();
      this.platform();
      this.server.runOnServer(s -> PlusEffects.butterfingersOdds = 1);
      this.cmd("clear @a");
      this.cmd("item replace entity @a[limit=1] weapon.mainhand with minecraft:stick");
      this.ticks(3);
      this.giveOnly("butterfingers");
      this.ticks(5);
      boolean dropped = this.onPlayer(p -> p.getMainHandItem().isEmpty());
      this.check("butterfingers: held item is dropped", dropped, "");
      this.cmd("disability give @a kleptomania");
      this.ticks(2);
      this.cmd("item replace entity @a[limit=1] weapon.mainhand with minecraft:stick");
      this.ticks(30);
      boolean kept = this.onPlayer(p -> !p.getMainHandItem().isEmpty());
      this.check("butterfingers: kleptomania keeps the item", kept, "");
      this.server.runOnServer(s -> PlusEffects.butterfingersOdds = 2400);
      this.cmd("kill @e[type=minecraft:item]");
      this.cmd("clear @a");

      // Hangry: nothing extra drains, but below three shanks mining is slow and you are weak.
      this.clean();
      this.giveOnly("hangry");
      this.onPlayer(p -> {
         p.getFoodData().setFoodLevel(15);
         return null;
      });
      this.ticks(4);
      boolean fed = !this.hasModifier(Attributes.BLOCK_BREAK_SPEED, "hangry_mine") && !this.onPlayer(p -> p.hasEffect(MobEffects.WEAKNESS));
      this.onPlayer(p -> {
         p.getFoodData().setFoodLevel(4);
         return null;
      });
      boolean hungry = this.waitServer(s -> s.getPlayerList().getPlayers().get(0).hasEffect(MobEffects.WEAKNESS), 40)
         && this.hasModifier(Attributes.BLOCK_BREAK_SPEED, "hangry_mine");
      this.check("hangry: fine when fed, slow mining and weakness when hungry", fed && hungry, "fed=" + fed + " hungry=" + hungry);
      this.onPlayer(p -> {
         p.getFoodData().setFoodLevel(15);
         return null;
      });
      float e0 = this.exhaustion();
      this.ticks(100);
      float e1 = this.exhaustion();
      float gained = e1 >= e0 ? e1 - e0 : e1 + 4.0F - e0;
      this.check("hangry: no extra hunger drain any more", gained < 0.1F, "gained=" + gained);

      // Two Left Feet: standing still never staggers; a real jump and landing does (odds forced to 1).
      this.clean();
      this.platform();
      this.server.runOnServer(s -> PlusEffects.tripOdds = 1);
      this.giveOnly("two_left_feet");
      this.ticks(15);
      boolean steady = !this.onPlayer(p -> p.hasEffect(MobEffects.SLOWNESS));
      this.ctx.getInput().holdKeyFor(o -> o.keyJump, 3);
      boolean tripped = this.waitServer(s -> {
         MobEffectInstance e = s.getPlayerList().getPlayers().get(0).getEffect(MobEffects.SLOWNESS);
         return e != null && e.getAmplifier() == 3;
      }, 80);
      this.server.runOnServer(s -> PlusEffects.tripOdds = 3);
      this.cmd("effect clear @a");
      this.ticks(40);
      this.server.runOnServer(s -> PlusEffects.tripOdds = 1);
      this.ctx.getInput().holdKey(o -> o.keyShift);
      this.ctx.getInput().holdKeyFor(o -> o.keyJump, 3);
      this.ticks(60);
      boolean rolled = !this.onPlayer(p -> p.hasEffect(MobEffects.SLOWNESS));
      this.ctx.getInput().releaseKey(o -> o.keyShift);
      this.server.runOnServer(s -> PlusEffects.tripOdds = 3);
      this.check("two_left_feet: no stagger standing still, a jump's landing staggers you, a crouched landing rolls through it",
         steady && tripped && rolled, "steady=" + steady + " tripped=" + tripped + " rolled=" + rolled);

      // Creeperphobia: a creeper freezes you in bursts with a quickening heart; a spider does not.
      this.clean();
      this.platform();
      String inert = "{NoAI:1b,Silent:1b,Invulnerable:1b,PersistenceRequired:1b}";
      this.cmd("summon minecraft:creeper " + (this.px + 4.5) + " 250 " + (this.pz + 0.5) + " " + inert);
      int f0 = this.ctx.computeOnClient(mc -> ClientProbe.freezeCount());
      int h0 = this.ctx.computeOnClient(mc -> ClientProbe.heartbeatCount());
      this.giveOnly("creeperphobia");
      this.ticks(60);
      int f1 = this.ctx.computeOnClient(mc -> ClientProbe.freezeCount());
      int h1 = this.ctx.computeOnClient(mc -> ClientProbe.heartbeatCount());
      this.check("creeperphobia: a creeper freezes you and quickens the heart", f1 > f0 && h1 > h0, "freezes=" + (f1 - f0) + " beats=" + (h1 - h0));
      this.cmd("kill @e[type=minecraft:creeper]");
      this.clean();
      this.cmd("summon minecraft:spider " + (this.px + 4.5) + " 250 " + (this.pz + 0.5) + " " + inert);
      int f2 = this.ctx.computeOnClient(mc -> ClientProbe.freezeCount());
      this.giveOnly("creeperphobia");
      this.ticks(60);
      int f3 = this.ctx.computeOnClient(mc -> ClientProbe.freezeCount());
      this.check("creeperphobia: a spider does NOT freeze you", f3 == f2, "freezes=" + (f3 - f2));

      // Arachnophobia: your head turns away. The spider is east (+X), so away is yaw +90.
      this.giveOnly("arachnophobia");
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.ticks(60);
      float away = this.ctx.computeOnClient(mc -> mc.player.getYRot());
      this.check("arachnophobia: your head snaps away from the spider", Math.abs(net.minecraft.util.Mth.wrapDegrees(away - 90.0F)) < 25.0F, "yaw=" + away);
      this.cmd("kill @e[type=minecraft:spider]");

      // Bipolar: phases flip the speed/mining modifiers; removing the disability clears them.
      this.clean();
      this.giveOnly("bipolar");
      this.server.runOnServer(s -> {
         PlusEffects.Effects st = PlusEffects.of(id);
         st.moodPhase = 1;
         st.moodTicks = 100000;
      });
      this.ticks(4);
      boolean high = this.hasModifier(Attributes.MOVEMENT_SPEED, "manic_speed")
         && this.hasModifier(Attributes.BLOCK_BREAK_SPEED, "manic_mine")
         && !this.hasModifier(Attributes.MOVEMENT_SPEED, "low_speed");
      this.server.runOnServer(s -> PlusEffects.of(id).moodTicks = 1);
      this.ticks(4);
      boolean low = this.hasModifier(Attributes.MOVEMENT_SPEED, "low_speed")
         && this.hasModifier(Attributes.BLOCK_BREAK_SPEED, "low_mine")
         && !this.hasModifier(Attributes.MOVEMENT_SPEED, "manic_speed");
      this.cmd("disability remove @a bipolar");
      this.ticks(4);
      boolean gone = !this.hasModifier(Attributes.MOVEMENT_SPEED, "low_speed") && !this.hasModifier(Attributes.BLOCK_BREAK_SPEED, "low_mine");
      this.check("bipolar: high phase -> speed/mining up", high, "");
      this.check("bipolar: phase flips to the low", low, "");
      this.check("bipolar: modifiers removed with the disability", gone, "");

      // Autism: a real explosion sound through the sound engine overwhelms, a quiet one does not.
      this.clean();
      this.platform();
      this.giveOnly("autism");
      this.cmd("playsound minecraft:block.note_block.harp master @a " + this.px + " 250 " + this.pz + " 1");
      this.ticks(5);
      int quiet = this.ctx.computeOnClient(mc -> ClientProbe.overloadTicks());
      this.cmd("playsound minecraft:entity.generic.explode master @a " + this.px + " 250 " + this.pz + " 4");
      this.ticks(5);
      int loud = this.ctx.computeOnClient(mc -> ClientProbe.overloadTicks());
      this.check("autism: a quiet sound does not overload", quiet == 0, "ticks=" + quiet);
      this.check("autism: an explosion overloads (real sound-engine hook)", loud > 60, "ticks=" + loud);
      this.ctx.takeScreenshot("autism_overload");
      this.cmd("disability remove @a autism");
      this.ticks(5);
      int cleared = this.ctx.computeOnClient(mc -> ClientProbe.overloadTicks());
      this.check("autism: overload clears when removed", cleared == 0, "ticks=" + cleared);

      // Left-Right Confusion: A/D strafing is inverted while an episode runs.
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.ticks(5);
      double sx = this.onPlayer(p -> p.getX());
      this.ctx.getInput().holdKeyFor(o -> o.keyLeft, 20);
      this.ticks(5);
      double normalDx = this.onPlayer(p -> p.getX()) - sx;
      this.giveOnly("left_right_confusion");
      this.ctx.runOnClient(mc -> ClientProbe.startConfusion(200));
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.ticks(5);
      double cx = this.onPlayer(p -> p.getX());
      this.ctx.getInput().holdKeyFor(o -> o.keyLeft, 20);
      this.ticks(5);
      double confusedDx = this.onPlayer(p -> p.getX()) - cx;
      this.check("left_right_confusion: control strafes left toward +X", normalDx > 0.3, "dx=" + normalDx);
      this.check("left_right_confusion: confused 'left' goes the other way", confusedDx < -0.3, "dx=" + confusedDx);
      this.ctx.runOnClient(mc -> ClientProbe.startConfusion(0));

      // Hiccups / sneezing: forced through the same code the random trigger calls.
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.giveOnly("hiccups");
      this.ticks(3);
      float p0 = this.ctx.computeOnClient(mc -> mc.player.getXRot());
      this.ctx.runOnClient(mc -> ClientProbe.hiccup());
      float p1 = this.ctx.computeOnClient(mc -> mc.player.getXRot());
      this.check("hiccups: camera jolts upward 5 degrees", Math.abs((p0 - p1) - 5.0F) < 0.01F, "before=" + p0 + " after=" + p1);
      this.giveOnly("sneezing");
      float y0 = this.ctx.computeOnClient(mc -> mc.player.getYRot());
      float x0 = this.ctx.computeOnClient(mc -> mc.player.getXRot());
      this.ctx.runOnClient(mc -> ClientProbe.sneeze());
      float y1 = this.ctx.computeOnClient(mc -> mc.player.getYRot());
      float x1 = this.ctx.computeOnClient(mc -> mc.player.getXRot());
      this.check("sneezing: yaw jerks 14 degrees and the head dips 9", Math.abs(Math.abs(y1 - y0) - 14.0F) < 0.01F && Math.abs((x1 - x0) - 9.0F) < 0.01F,
         "dyaw=" + (y1 - y0) + " dpitch=" + (x1 - x0));
      this.cmd("disability remove @a all");
   }

   private int respawnAfterKill() {
      this.cmd("kill @a[limit=1]");
      this.ctx.waitForScreen(net.minecraft.client.gui.screens.DeathScreen.class);
      this.ctx.clickScreenButton("deathScreen.respawn");
      this.ctx.waitFor(mc -> mc.gui.screen() == null && mc.player != null && mc.player.isAlive());
      this.ticks(10);
      return this.onPlayer(p -> PlusState.totalCount(p.getUUID()));
   }

   /** Eats the main-hand item the way a player does: hold the use key (the client cancels a meal if it is not held). */
   private void eatFor(int ticks) {
      this.ctx.getInput().holdKey(o -> o.keyUse);
      this.ticks(ticks);
      this.ctx.getInput().releaseKey(o -> o.keyUse);
      this.ticks(3);
   }

   /** The ten disabilities added in 0.8.0, and the /disability deathremoval setting. */
   private void jokesAndDeath() {
      UUID id = this.id();

      // Moon Boots, Giant, Glass Cannon: attribute modifiers on / off.
      this.clean();
      this.platform();
      this.giveOnly("moon_boots");
      boolean moon = this.hasModifier(Attributes.GRAVITY, "moon_gravity");
      double g = this.onPlayer(p -> p.getAttributeValue(Attributes.GRAVITY));
      this.cmd("disability remove @a moon_boots");
      this.ticks(4);
      double g0 = this.onPlayer(p -> p.getAttributeValue(Attributes.GRAVITY));
      this.check("moon_boots: gravity drops to 40% and restores", moon && g < g0 * 0.5 && Math.abs(g0 - 0.08) < 0.001, "on=" + g + " off=" + g0);

      this.giveOnly("giant");
      double scale = this.onPlayer(p -> p.getAttributeValue(Attributes.SCALE));
      float height = this.onPlayer(p -> p.getBbHeight());
      this.cmd("disability remove @a giant");
      this.ticks(4);
      float height0 = this.onPlayer(p -> p.getBbHeight());
      this.check("giant: scale 1.5 and a taller hitbox, back to normal after", Math.abs(scale - 1.5) < 0.01 && height > 2.5F && height0 < 1.9F,
         "scale=" + scale + " height=" + height + " after=" + height0);

      this.giveOnly("glass_cannon");
      float maxHp = this.onPlayer(ServerPlayer::getMaxHealth);
      double atk = this.onPlayer(p -> p.getAttributeValue(Attributes.ATTACK_DAMAGE));
      this.cmd("disability remove @a glass_cannon");
      this.ticks(4);
      float maxHp0 = this.onPlayer(ServerPlayer::getMaxHealth);
      this.check("glass_cannon: half health, double attack, restored after", Math.abs(maxHp - 10.0F) < 0.01F && atk >= 1.99 && Math.abs(maxHp0 - 20.0F) < 0.01F,
         "maxHp=" + maxHp + " atk=" + atk + " after=" + maxHp0);

      // Egg Layer: forced odds lay eggs.
      this.clean();
      this.platform();
      this.server.runOnServer(s -> PlusEffects.eggOdds = 1);
      this.giveOnly("egg_layer");
      this.ticks(5);
      this.server.runOnServer(s -> PlusEffects.eggOdds = 1200);
      int eggs = this.onPlayer(p -> p.level().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, p.getBoundingBox().inflate(8.0),
         e -> e.getItem().getItem() == net.minecraft.world.item.Items.EGG).size());
      int held = this.onPlayer(p -> p.getInventory().countItem(net.minecraft.world.item.Items.EGG));
      this.check("egg_layer: eggs appear in the world or inventory", eggs + held > 0, "ground=" + eggs + " picked up=" + held);
      this.cmd("kill @e[type=minecraft:item]");
      this.cmd("clear @a");

      // Picky Eater: what you just ate goes on cooldown (you cannot eat it again for a while); other foods are fine.
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.giveOnly("picky_eater");
      this.cmd("item replace entity @a[limit=1] weapon.mainhand with minecraft:cooked_beef 2");
      this.eatFor(40);
      boolean beefCooling = this.onPlayer(p -> p.getCooldowns().isOnCooldown(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COOKED_BEEF)));
      boolean appleFree = this.onPlayer(p -> !p.getCooldowns().isOnCooldown(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.APPLE)));
      this.eatFor(40);
      int beefLeft = this.onPlayer(p -> p.getMainHandItem().getCount());
      this.check("picky_eater: the food you just ate is on cooldown, the second helping stays uneaten, another food is free",
         beefCooling && appleFree && beefLeft == 1, "cooling=" + beefCooling + " appleFree=" + appleFree + " left=" + beefLeft);
      this.cmd("effect clear @a");
      this.cmd("disability remove @a picky_eater");
      this.ticks(3);

      // Sweet Tooth: a cookie gives speed II, bread does not, and the crash follows the rush.
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.giveOnly("sweet_tooth");
      this.cmd("item replace entity @a[limit=1] weapon.mainhand with minecraft:bread 1");
      this.eatFor(40);
      boolean noRush = !this.onPlayer(p -> p.hasEffect(MobEffects.SPEED));
      this.onPlayer(p -> {
         p.getFoodData().setFoodLevel(10);
         return null;
      });
      this.cmd("item replace entity @a[limit=1] weapon.mainhand with minecraft:cookie 1");
      this.eatFor(40);
      boolean rush = this.hasEffect(MobEffects.SPEED, 1);
      this.server.runOnServer(s -> PlusEffects.of(id).rushTicks = 2);
      this.ticks(6);
      boolean crash = this.onPlayer(p -> p.hasEffect(MobEffects.SLOWNESS));
      this.check("sweet_tooth: bread no rush, cookie = speed II, then the crash", noRush && rush && crash, "noRush=" + noRush + " rush=" + rush + " crash=" + crash);

      // Pogo: standing still, you still leave the ground; control stays put.
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.ticks(10);
      double still = 0.0;
      for (int i = 0; i < 40; i++) {
         this.ticks(1);
         still = Math.max(still, this.onPlayer(p -> p.getY()) - 250.0);
      }

      this.giveOnly("pogo");
      double hop = 0.0;
      for (int i = 0; i < 100; i++) {
         this.ticks(1);
         hop = Math.max(hop, this.onPlayer(p -> p.getY()) - 250.0);
      }

      this.check("pogo: control stays on the ground, pogo bounces over 0.5 blocks", still < 0.05 && hop > 0.5, "control=" + still + " pogo=" + hop);

      // Tipsy: the horizon rolls (camera roll), level without it.
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.ticks(10);
      float calmLo = 0.0F;
      float calmHi = 0.0F;
      for (int i = 0; i < 30; i++) {
         this.ticks(2);
         float r = this.ctx.computeOnClient(mc -> ClientProbe.cameraRightHeight());
         calmLo = Math.min(calmLo, r);
         calmHi = Math.max(calmHi, r);
      }

      this.giveOnly("tipsy");
      float swayLo = 0.0F;
      float swayHi = 0.0F;
      for (int i = 0; i < 60; i++) {
         this.ticks(2);
         float r = this.ctx.computeOnClient(mc -> ClientProbe.cameraRightHeight());
         swayLo = Math.min(swayLo, r);
         swayHi = Math.max(swayHi, r);
      }

      this.check("tipsy: the horizon rolls about 5 degrees each way, level without it", calmHi - calmLo < 0.02F && swayHi - swayLo > 0.1F,
         "level=" + (calmHi - calmLo) + " tipsy=" + (swayHi - swayLo));

      // Scaredy Cat: a nearby monster sends you bolting but you cannot hit it; a cow changes nothing.
      this.clean();
      this.platform();
      String scaredInert = "{NoAI:1b,Silent:1b,Invulnerable:1b,PersistenceRequired:1b}";
      this.giveOnly("scaredy_cat");
      this.ticks(25);
      boolean calmAlone = !this.onPlayer(p -> p.hasEffect(MobEffects.SPEED));
      this.cmd("summon minecraft:cow " + (this.px + 0.5) + " 250 " + (this.pz + 5.5) + " " + scaredInert);
      this.ticks(25);
      boolean calmWithCow = !this.onPlayer(p -> p.hasEffect(MobEffects.SPEED));
      this.cmd("summon minecraft:zombie " + (this.px + 5.5) + " 250 " + (this.pz + 0.5) + " " + scaredInert);
      boolean bolting = this.waitServer(s -> {
         MobEffectInstance e = s.getPlayerList().getPlayers().get(0).getEffect(MobEffects.SPEED);
         return e != null && e.getAmplifier() == 1;
      }, 60);
      String verdicts = this.server.computeOnServer(s -> {
         ServerPlayer p = s.getPlayerList().getPlayers().get(0);
         net.minecraft.world.entity.Mob zombie = p.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, p.getBoundingBox().inflate(20.0),
            m -> net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(m.getType()).getPath().equals("zombie")).get(0);
         net.minecraft.world.entity.Mob cow = p.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, p.getBoundingBox().inflate(20.0),
            m -> net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(m.getType()).getPath().equals("cow")).get(0);
         var zombieHit = net.fabricmc.fabric.api.event.player.AttackEntityCallback.EVENT.invoker()
            .interact(p, p.level(), net.minecraft.world.InteractionHand.MAIN_HAND, zombie, null);
         var cowHit = net.fabricmc.fabric.api.event.player.AttackEntityCallback.EVENT.invoker()
            .interact(p, p.level(), net.minecraft.world.InteractionHand.MAIN_HAND, cow, null);
         return zombieHit + "/" + cowHit;
      });
      this.cmd("kill @e[type=!minecraft:player]");
      this.check("scaredy_cat: quiet alone and near a cow, bolts from a zombie and cannot hit it", calmAlone && calmWithCow && bolting && verdicts.startsWith("Fail") && verdicts.endsWith("Pass[]"),
         "alone=" + calmAlone + " cow=" + calmWithCow + " bolting=" + bolting + " attacks=" + verdicts);

      // Squeaky Shoes: squeaks per distance walked, silent while sneaking.
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz - 9.5) + " 0 0");
      this.giveOnly("squeaky_shoes");
      this.ticks(5);
      int s0 = this.ctx.computeOnClient(mc -> ClientProbe.squeakCount());
      this.ctx.getInput().holdKey(o -> o.keyUp);
      this.ticks(60);
      this.ctx.getInput().releaseKey(o -> o.keyUp);
      int s1 = this.ctx.computeOnClient(mc -> ClientProbe.squeakCount());
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz - 9.5) + " 0 0");
      this.ticks(5);
      this.ctx.getInput().holdKey(o -> o.keyShift);
      this.ctx.getInput().holdKey(o -> o.keyUp);
      this.ticks(60);
      this.ctx.getInput().releaseKey(o -> o.keyUp);
      this.ctx.getInput().releaseKey(o -> o.keyShift);
      int s2 = this.ctx.computeOnClient(mc -> ClientProbe.squeakCount());
      this.check("squeaky_shoes: squeaks while walking, silent while sneaking", s1 - s0 >= 4 && s2 == s1, "walking=" + (s1 - s0) + " sneaking=" + (s2 - s1));

      this.deathRemoval(id);
   }

   private float[] yawRangeWhileWalking(int ticks) {
      float lo = Float.MAX_VALUE;
      float hi = -Float.MAX_VALUE;
      this.ctx.getInput().holdKey(o -> o.keyUp);
      for (int i = 0; i < ticks; i++) {
         this.ticks(1);
         float y = this.ctx.computeOnClient(mc -> mc.player.getYRot());
         lo = Math.min(lo, y);
         hi = Math.max(hi, y);
      }

      this.ctx.getInput().releaseKey(o -> o.keyUp);
      return new float[]{lo, hi};
   }

   private void traitsAndCravings() {
      UUID id = this.id();
      net.minecraft.world.InteractionHand hand = net.minecraft.world.InteractionHand.MAIN_HAND;

      // Quadruple Amputee: control walks, the amputee cannot move, jump, use, attack or break.
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz - 9.5) + " 0 0");
      this.ticks(5);
      double z0 = this.onPlayer(p -> p.getZ());
      this.ctx.getInput().holdKey(o -> o.keyUp);
      this.ticks(40);
      this.ctx.getInput().releaseKey(o -> o.keyUp);
      double walked = this.onPlayer(p -> p.getZ()) - z0;
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz - 9.5) + " 0 0");
      this.giveOnly("quadruple_amputee");
      this.ticks(5);
      double z1 = this.onPlayer(p -> p.getZ());
      this.ctx.getInput().holdKey(o -> o.keyUp);
      this.ctx.getInput().holdKey(o -> o.keyJump);
      this.ticks(40);
      this.ctx.getInput().releaseKey(o -> o.keyUp);
      this.ctx.getInput().releaseKey(o -> o.keyJump);
      double stuck = this.onPlayer(p -> p.getZ()) - z1;
      double lift = this.onPlayer(p -> p.getY()) - 250.0;
      this.check("quadruple_amputee: control walks, amputee cannot move or jump", walked > 3.0 && stuck < 0.05 && lift < 0.05,
         "control=" + walked + " amputee=" + stuck + " lift=" + lift);
      boolean blocked = this.server.computeOnServer(s -> {
         ServerPlayer p = s.getPlayerList().getPlayers().get(0);
         net.minecraft.world.level.Level level = p.level();
         net.minecraft.core.BlockPos pos = p.blockPosition().below();
         net.minecraft.world.InteractionResult fail = net.minecraft.world.InteractionResult.FAIL;
         return net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT.invoker().interact(p, level, hand) == fail
            && net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.invoker().interact(p, level, hand,
               new net.minecraft.world.phys.BlockHitResult(p.position(), net.minecraft.core.Direction.UP, pos, false)) == fail
            && net.fabricmc.fabric.api.event.player.AttackBlockCallback.EVENT.invoker().interact(p, level, hand, pos, net.minecraft.core.Direction.UP) == fail
            && !net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, p, pos, level.getBlockState(pos), null);
      });
      this.check("quadruple_amputee: use, place, attack and break are all refused", blocked, "");
      this.cmd("item replace entity @a[limit=1] weapon.mainhand with minecraft:stick 1");
      this.ticks(2);
      this.onPlayer(p -> {
         p.drop(false);
         return null;
      });
      this.ticks(2);
      boolean cannotDrop = this.onPlayer(p -> !p.getMainHandItem().isEmpty());
      this.check("quadruple_amputee: cannot drop the held item either", cannotDrop, "");
      this.cmd("clear @a");
      this.cmd("disability give @a tinnitus");
      this.ticks(3);
      int left = this.respawnAfterKill();
      this.check("quadruple_amputee: dying cures it, other disabilities stay",
         !PlusState.has(id, PlusDisability.QUADRUPLE_AMPUTEE) && PlusState.has(id, PlusDisability.TINNITUS) && left == 1, "left=" + left);
      this.cmd("disability remove @a all");

      // Pregnancy: slower, and the baby kicks you backwards about a block.
      this.clean();
      this.platform();
      this.giveOnly("pregnancy");
      boolean slow = this.hasModifier(Attributes.MOVEMENT_SPEED, "pregnant_speed");
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.ticks(5);
      double kz0 = this.onPlayer(p -> p.getZ());
      int k0 = this.ctx.computeOnClient(mc -> ClientProbe.kickCount());
      this.ctx.runOnClient(mc -> ClientProbe.kick());
      this.ticks(25);
      double pushed = this.onPlayer(p -> p.getZ()) - kz0;
      int k1 = this.ctx.computeOnClient(mc -> ClientProbe.kickCount());
      this.check("pregnancy: speed penalty, and a kick pushes you back ~1 block", slow && k1 == k0 + 1 && pushed < -0.6 && pushed > -2.5,
         "slow=" + slow + " pushed=" + pushed);

      // Ginger: the picture really is sepia (R >= G >= B in the sky), unlike the control.
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.ticks(10);
      double[] plain = this.skyColour("ginger_control");
      this.giveOnly("ginger");
      this.ticks(15);
      double[] sepia = this.skyColour("ginger_sepia");
      this.check("ginger: sky is blue normally and sepia (R>G>B) with it",
         plain[2] > plain[0] && sepia[0] > sepia[1] && sepia[1] > sepia[2],
         "plain=" + java.util.Arrays.toString(plain) + " sepia=" + java.util.Arrays.toString(sepia));

      // Obesity: a weaker jump but no slowdown (slowness belongs to pregnancy); removed with the disability.
      this.clean();
      this.giveOnly("obesity");
      boolean heavy = this.hasModifier(Attributes.JUMP_STRENGTH, "obese_jump") && !this.hasModifier(Attributes.MOVEMENT_SPEED, "obese_speed");
      this.cmd("disability remove @a obesity");
      this.ticks(4);
      boolean light = !this.hasModifier(Attributes.JUMP_STRENGTH, "obese_jump");
      this.check("obesity: weaker jump only, removed with the disability", heavy && light, "");

      // Orphan: a spawn point set by command is wiped; control keeps it.
      this.clean();
      this.platform();
      this.cmd("spawnpoint @a " + this.px + " 250 " + this.pz);
      this.ticks(4);
      boolean kept = this.onPlayer(p -> p.getRespawnConfig() != null);
      this.giveOnly("orphan");
      this.ticks(4);
      boolean wiped = this.onPlayer(p -> p.getRespawnConfig() == null);
      this.cmd("spawnpoint @a " + this.px + " 250 " + this.pz);
      this.ticks(4);
      boolean wipedAgain = this.onPlayer(p -> p.getRespawnConfig() == null);
      this.check("orphan: control keeps a spawn point, orphan never keeps one", kept && wiped && wipedAgain, "kept=" + kept + " wiped=" + wiped + " again=" + wipedAgain);

      // Alcoholism: withdrawal when dry, a potion gives a buzz and resets the clock.
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.server.runOnServer(s -> PlusEffects.alcoholDryLimit = 20);
      this.giveOnly("alcoholism");
      boolean thirsty = this.waitServer(s -> s.getPlayerList().getPlayers().get(0).hasEffect(MobEffects.DARKNESS), 120);
      this.server.runOnServer(s -> PlusEffects.alcoholDryLimit = 4800);
      this.cmd("effect clear @a");
      this.cmd("item replace entity @a[limit=1] weapon.mainhand with minecraft:potion 1");
      this.eatFor(50);
      boolean buzz = this.hasEffect(MobEffects.RESISTANCE, 0);
      int dry = PlusEffects.of(id).alcohol.dryTicks;
      this.check("alcoholism: withdrawal blackouts when dry, a drink gives a buzz and resets", thirsty && buzz && dry < 100,
         "thirsty=" + thirsty + " buzz=" + buzz + " dry=" + dry);

      // Drug addiction: withdrawal when dry, a golden apple is the fix (strength + haste II), then the weakness crash.
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.server.runOnServer(s -> PlusEffects.drugDryLimit = 20);
      this.giveOnly("drug_addiction");
      boolean craving = this.waitServer(s -> s.getPlayerList().getPlayers().get(0).hasEffect(MobEffects.WEAKNESS), 120);
      this.server.runOnServer(s -> PlusEffects.drugDryLimit = 6000);
      this.cmd("effect clear @a");
      this.cmd("item replace entity @a[limit=1] weapon.mainhand with minecraft:golden_apple 1");
      this.eatFor(50);
      boolean rush = this.hasEffect(MobEffects.STRENGTH, 0) && this.hasEffect(MobEffects.HASTE, 1) && !this.onPlayer(p -> p.hasEffect(MobEffects.SPEED));
      this.server.runOnServer(s -> PlusEffects.of(id).drug.crashTicks = 2);
      this.ticks(6);
      boolean crash = this.onPlayer(p -> p.hasEffect(MobEffects.WEAKNESS) && p.hasEffect(MobEffects.MINING_FATIGUE));
      this.check("drug_addiction: withdrawal, a fix gives a rush, then the crash", craving && rush && crash,
         "craving=" + craving + " rush=" + rush + " crash=" + crash);

      // Paranoia: footsteps behind you (odds forced); none without it.
      this.clean();
      this.platform();
      this.ctx.runOnClient(mc -> ClientProbe.setParanoiaOdds(1));
      int p0 = this.ctx.computeOnClient(mc -> ClientProbe.paranoiaCount());
      this.ticks(20);
      int p1 = this.ctx.computeOnClient(mc -> ClientProbe.paranoiaCount());
      this.giveOnly("paranoia");
      this.ticks(30);
      int p2 = this.ctx.computeOnClient(mc -> ClientProbe.paranoiaCount());
      this.ctx.runOnClient(mc -> ClientProbe.setParanoiaOdds(200));
      this.check("paranoia: nothing without it, footsteps behind you with it", p1 == p0 && p2 - p1 >= 10, "without=" + (p1 - p0) + " with=" + (p2 - p1));

      // Blood Hungry: hitting a mob heals and feeds you; control does not.
      this.clean();
      this.platform();
      this.cmd("summon minecraft:cow " + (this.px + 3.5) + " 250 " + (this.pz + 0.5) + " {NoAI:1b,Silent:1b,PersistenceRequired:1b}");
      this.ticks(5);
      this.onPlayer(p -> {
         p.setHealth(10.0F);
         p.getFoodData().setFoodLevel(10);
         return null;
      });
      this.cmd("damage @e[type=minecraft:cow,limit=1] 2 minecraft:player_attack by @a[limit=1]");
      this.ticks(3);
      float plainHp = this.onPlayer(ServerPlayer::getHealth);
      this.giveOnly("blood_hungry");
      this.ticks(30);
      this.onPlayer(p -> {
         p.setHealth(10.0F);
         p.getFoodData().setFoodLevel(10);
         return null;
      });
      this.cmd("damage @e[type=minecraft:cow,limit=1] 2 minecraft:player_attack by @a[limit=1]");
      this.ticks(3);
      float biteHp = this.onPlayer(ServerPlayer::getHealth);
      int biteFood = this.onPlayer(p -> p.getFoodData().getFoodLevel());
      this.cmd("kill @e[type=minecraft:cow]");
      this.check("blood_hungry: a hit heals 2 and feeds, control does not", plainHp <= 10.1F && biteHp >= 11.9F && biteFood >= 11,
         "control=" + plainHp + " bite=" + biteHp + " food=" + biteFood);
      this.cmd("disability remove @a all");
   }

   /** Screenshots the view and returns the average RGB of the upper (sky) part. */
   private double[] skyColour(String name) {
      java.nio.file.Path img = this.ctx.takeScreenshot(name);
      try {
         java.awt.image.BufferedImage bi = javax.imageio.ImageIO.read(img.toFile());
         long r = 0;
         long g = 0;
         long b = 0;
         long n = 0;
         for (int y = 10; y < bi.getHeight() / 4; y += 4) {
            for (int x = 100; x < bi.getWidth() - 100; x += 8) {
               int rgb = bi.getRGB(x, y);
               r += rgb >> 16 & 0xFF;
               g += rgb >> 8 & 0xFF;
               b += rgb & 0xFF;
               n++;
            }
         }

         return new double[]{(double)r / n, (double)g / n, (double)b / n};
      } catch (java.io.IOException e) {
         throw new IllegalStateException(e);
      }
   }

   /** The reworked fears (each with its own reaction) and the nine episode/condition disabilities of 0.11. */
   private void fearsAndEpisodes() {
      UUID id = this.id();
      String inert = "{NoAI:1b,Silent:1b,Invulnerable:1b,PersistenceRequired:1b}";

      // Enderphobia: dread closes in as the Enderman nears and fades once it is gone.
      this.clean();
      this.platform();
      this.giveOnly("enderphobia");
      this.cmd("summon minecraft:enderman " + (this.px + 5.5) + " 250 " + (this.pz + 0.5) + " " + inert);
      this.ticks(45);
      float near = this.ctx.computeOnClient(mc -> ClientProbe.dread());
      this.cmd("kill @e[type=minecraft:enderman]");
      this.ticks(70);
      float far = this.ctx.computeOnClient(mc -> ClientProbe.dread());
      this.check("enderphobia: dread closes in when near and fades when it is gone", near > 0.3F && far < 0.05F, "near=" + near + " gone=" + far);

      // Apiphobia: a bee nearby buzzes at your ear.
      this.clean();
      this.platform();
      int b0 = this.ctx.computeOnClient(mc -> ClientProbe.buzzCount());
      this.cmd("summon minecraft:bee " + (this.px + 3.5) + " 251 " + (this.pz + 0.5) + " " + inert);
      this.giveOnly("apiphobia");
      this.ticks(70);
      int b1 = this.ctx.computeOnClient(mc -> ClientProbe.buzzCount());
      this.cmd("kill @e[type=minecraft:bee]");
      this.check("apiphobia: a bee nearby buzzes at your ear", b1 - b0 >= 2, "buzzes=" + (b1 - b0));

      // Aquaphobia: in water you thrash upwards.
      this.clean();
      this.platform();
      this.giveOnly("aquaphobia");
      this.cmd("fill " + (this.px - 2) + " 248 " + (this.pz - 2) + " " + (this.px + 2) + " 251 " + (this.pz + 2) + " minecraft:water");
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5));
      int t0 = this.ctx.computeOnClient(mc -> ClientProbe.thrashCount());
      this.ticks(40);
      int t1 = this.ctx.computeOnClient(mc -> ClientProbe.thrashCount());
      this.cmd("fill " + (this.px - 7) + " 248 " + (this.pz - 7) + " " + (this.px + 7) + " 253 " + (this.pz + 7) + " minecraft:air");
      this.platform();
      this.check("aquaphobia: in water you thrash upwards", t1 - t0 >= 5, "thrashes=" + (t1 - t0));

      // Pyrophobia: your gaze is drawn to lava to the east (yaw -90).
      this.clean();
      this.platform();
      this.giveOnly("pyrophobia");
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.cmd("setblock " + (this.px + 5) + " 250 " + this.pz + " minecraft:lava");
      this.ticks(60);
      float gaze = this.ctx.computeOnClient(mc -> mc.player.getYRot());
      this.cmd("fill " + (this.px + 1) + " 250 " + (this.pz - 6) + " " + (this.px + 12) + " 253 " + (this.pz + 6) + " minecraft:air");
      this.platform();
      this.check("pyrophobia: your gaze is pulled toward the flames", Math.abs(net.minecraft.util.Mth.wrapDegrees(gaze + 90.0F)) < 25.0F, "yaw=" + gaze);

      // Head Rush: standing up from a crouch blacks you out.
      this.clean();
      this.platform();
      this.giveOnly("head_rush");
      this.ticks(5);
      int blink0 = this.ctx.computeOnClient(mc -> ClientProbe.blinkTicks());
      this.ctx.getInput().holdKey(o -> o.keyShift);
      this.ticks(10);
      this.ctx.getInput().releaseKey(o -> o.keyShift);
      this.ticks(2);
      int blink1 = this.ctx.computeOnClient(mc -> ClientProbe.blinkTicks());
      this.check("head_rush: standing up from a crouch blacks you out", blink0 == 0 && blink1 > 10, "before=" + blink0 + " after=" + blink1);

      // IBS: eating can double you over (forced crouch).
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.giveOnly("ibs");
      this.cmd("item replace entity @a[limit=1] weapon.mainhand with minecraft:apple 1");
      int pain = 0;
      boolean crouched = false;
      this.ctx.getInput().holdKey(o -> o.keyUse);
      for (int i = 0; i < 25; i++) {
         this.ticks(2);
         pain = Math.max(pain, this.ctx.computeOnClient(mc -> ClientProbe.painTicks()));
         crouched |= this.onPlayer(p -> p.isShiftKeyDown());
      }

      this.ctx.getInput().releaseKey(o -> o.keyUse);
      this.check("ibs: eating sets off cramps that double you over", pain > 0 && crouched, "pain=" + pain + " crouched=" + crouched);

      // Kidney stones: an episode locks you up in agony (not doubled over) and costs health, without killing you.
      this.clean();
      this.platform();
      this.waitClient(mc -> ClientProbe.painTicks() == 0, 120);
      this.giveOnly("kidney_stones");
      this.server.runOnServer(s -> PlusEffects.of(id).stoneCooldown = 1);
      boolean locked = this.waitClient(mc -> ClientProbe.agonyTicks() > 0, 30);
      int lockedFor = this.ctx.computeOnClient(mc -> ClientProbe.freezeTicks());
      boolean notDoubled = this.ctx.computeOnClient(mc -> ClientProbe.painTicks()) == 0;
      float hp0 = this.onPlayer(ServerPlayer::getHealth);
      this.ticks(70);
      float hp1 = this.onPlayer(ServerPlayer::getHealth);
      this.check("kidney_stones: locks you up in agony, costs health, never kills",
         locked && lockedFor > 0 && notDoubled && hp1 <= hp0 - 2.0F && hp1 > 0.0F,
         "locked=" + locked + " freeze=" + lockedFor + " notDoubled=" + notDoubled + " before=" + hp0 + " after=" + hp1);

      // Stoned: slow swings (attack speed) and a steady view (no sway); the modifier goes with the disability.
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.giveOnly("stoned");
      this.ticks(5);
      boolean slowSwings = this.hasModifier(Attributes.ATTACK_SPEED, "stoned_reflex");
      float lo = Float.MAX_VALUE;
      float hi = -Float.MAX_VALUE;
      for (int i = 0; i < 60; i++) {
         this.ticks(1);
         float y = this.ctx.computeOnClient(mc -> mc.player.getYRot());
         lo = Math.min(lo, y);
         hi = Math.max(hi, y);
      }

      float exhaustionBefore = this.exhaustion();
      this.ticks(100);
      float exhaustionAfter = this.exhaustion();
      float munched = exhaustionAfter >= exhaustionBefore ? exhaustionAfter - exhaustionBefore : exhaustionAfter + 4.0F - exhaustionBefore;
      this.cmd("disability remove @a stoned");
      this.ticks(4);
      boolean swingsBack = !this.hasModifier(Attributes.ATTACK_SPEED, "stoned_reflex");
      this.check("stoned: swings are slower, the view does not sway and nothing drains faster", slowSwings && swingsBack && hi - lo < 0.5F && munched < 0.1F,
         "slow=" + slowSwings + " back=" + swingsBack + " sway=" + (hi - lo) + " drain=" + munched);

      // Lead poisoning: lower max health, and each Lead in the pack takes more and poisons.
      this.clean();
      this.cmd("clear @a");
      this.giveOnly("lead_poisoning");
      float lead0 = this.onPlayer(ServerPlayer::getMaxHealth);
      this.cmd("give @a minecraft:lead 2");
      this.ticks(5);
      float lead2 = this.onPlayer(ServerPlayer::getMaxHealth);
      boolean poisoned = this.waitServer(s -> s.getPlayerList().getPlayers().get(0).hasEffect(MobEffects.POISON), 100);
      this.cmd("clear @a");
      this.ticks(5);
      float lead00 = this.onPlayer(ServerPlayer::getMaxHealth);
      this.check("lead_poisoning: -1 heart base, -1 per Lead, poison, restored without Leads",
         Math.abs(lead0 - 18.0F) < 0.01F && Math.abs(lead2 - 14.0F) < 0.01F && poisoned && Math.abs(lead00 - 18.0F) < 0.01F,
         "base=" + lead0 + " two=" + lead2 + " poisoned=" + poisoned + " none=" + lead00);

      // Black lung: dust closes in as soot and past the limit you choke (no cough); open sky clears the dust.
      this.clean();
      this.platform();
      this.cmd("fill " + (this.px - 2) + " 249 " + (this.pz - 2) + " " + (this.px + 2) + " 253 " + (this.pz + 2) + " minecraft:stone hollow");
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5));
      this.giveOnly("black_lung");
      this.server.runOnServer(s -> {
         PlusEffects.of(id).dust = 11;
         ServerPlayer p = s.getPlayerList().getPlayers().get(0);
         PlusEffects.breatheDust(p, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), new net.minecraft.core.BlockPos(0, 40, 0));
      });
      boolean sooty = this.waitClient(mc -> ClientProbe.dustLevel() >= 12, 40);
      float soot = this.ctx.computeOnClient(mc -> ClientProbe.soot());
      this.ctx.takeScreenshot("black_lung_soot");
      float lungHp0 = this.onPlayer(ServerPlayer::getHealth);
      boolean choked = this.waitServer(s -> s.getPlayerList().getPlayers().get(0).getHealth() <= lungHp0 - 1.0F, 100);
      boolean noCough = this.ctx.computeOnClient(mc -> ClientProbe.coughTicks()) == 0;
      this.cmd("fill " + (this.px - 2) + " 250 " + (this.pz - 2) + " " + (this.px + 2) + " 253 " + (this.pz + 2) + " minecraft:air");
      this.server.runOnServer(s -> PlusEffects.of(id).dust = 5);
      this.ticks(90);
      int dustLeft = PlusEffects.of(id).dust;
      this.check("black_lung: soot closes in, past the limit you choke (no cough), open sky clears dust",
         sooty && soot > 0.7F && choked && noCough && dustLeft < 5, "sooty=" + sooty + " soot=" + soot + " choked=" + choked + " noCough=" + noCough + " dust=" + dustLeft);

      // Necrosis: the hands die first (mining -50%), then the legs (speed), then your strength; the offhand is untouched.
      this.clean();
      this.platform();
      this.giveOnly("necrosis");
      this.cmd("item replace entity @a[limit=1] weapon.offhand with minecraft:stick 1");
      this.server.runOnServer(s -> PlusEffects.of(id).necroTicks = 5999);
      this.ticks(8);
      double mine1 = this.modifierAmount(Attributes.BLOCK_BREAK_SPEED, "necro_mine");
      boolean offhandKept = this.onPlayer(p -> !p.getOffhandItem().isEmpty());
      int stage1 = this.ctx.computeOnClient(mc -> ClientProbe.necrosisStage());
      this.server.runOnServer(s -> PlusEffects.of(id).necroTicks = 5999);
      this.ticks(4);
      boolean legDead = this.hasModifier(Attributes.MOVEMENT_SPEED, "necro_speed");
      this.server.runOnServer(s -> PlusEffects.of(id).necroTicks = 5999);
      this.ticks(4);
      boolean weak = this.hasModifier(Attributes.ATTACK_DAMAGE, "necro_attack") && Math.abs(this.modifierAmount(Attributes.BLOCK_BREAK_SPEED, "necro_mine") + 0.75) < 1.0E-6;
      this.cmd("disability remove @a necrosis");
      this.ticks(5);
      boolean healed = !this.hasModifier(Attributes.MOVEMENT_SPEED, "necro_speed") && !this.hasModifier(Attributes.BLOCK_BREAK_SPEED, "necro_mine")
         && this.ctx.computeOnClient(mc -> ClientProbe.necrosisStage()) == 0;
      this.check("necrosis: hands (mining), then legs, then strength; no limb copied from the amputations; reversed on removal",
         Math.abs(mine1 + 0.5) < 1.0E-6 && offhandKept && stage1 == 1 && legDead && weak && healed,
         "mine1=" + mine1 + " offhand=" + offhandKept + " stage1=" + stage1 + " leg=" + legDead + " weak=" + weak + " healed=" + healed);

      // PTSD: a loud explosion throws you into a flashback; a quiet sound does not.
      this.clean();
      this.platform();
      this.giveOnly("ptsd");
      this.cmd("playsound minecraft:block.note_block.harp master @a " + this.px + " 250 " + this.pz + " 1");
      this.ticks(5);
      int calmFlash = this.ctx.computeOnClient(mc -> ClientProbe.flashbackTicks());
      this.cmd("playsound minecraft:entity.generic.explode master @a " + this.px + " 250 " + this.pz + " 4");
      this.ticks(8);
      int flash = this.ctx.computeOnClient(mc -> ClientProbe.flashbackTicks());
      float flashDread = this.ctx.computeOnClient(mc -> ClientProbe.dread());
      int ptsdDim = this.ctx.computeOnClient(mc -> ClientProbe.overloadNow());
      float ptsdDuck = this.ctx.computeOnClient(mc -> ClientProbe.soundDuck());
      this.check("ptsd: an explosion starts a flashback with dread and muffled sound, but never the autism dimming",
         calmFlash == 0 && flash > 50 && flashDread > 0.3F && ptsdDim == 0 && ptsdDuck < 0.5F,
         "quiet=" + calmFlash + " flashback=" + flash + " dread=" + flashDread + " dim=" + ptsdDim + " duck=" + ptsdDuck);

      // ARFID: only safe foods stay down (hunger is handed back for the rest).
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.giveOnly("arfid");
      this.onPlayer(p -> {
         p.getFoodData().setFoodLevel(10);
         return null;
      });
      this.cmd("item replace entity @a[limit=1] weapon.mainhand with minecraft:cooked_beef 1");
      this.eatFor(50);
      int unsafe = this.onPlayer(p -> p.getFoodData().getFoodLevel());
      this.onPlayer(p -> {
         p.getFoodData().setFoodLevel(10);
         return null;
      });
      this.cmd("item replace entity @a[limit=1] weapon.mainhand with minecraft:bread 1");
      this.eatFor(50);
      int safe = this.onPlayer(p -> p.getFoodData().getFoodLevel());
      this.check("arfid: an unsafe food is handed back, a safe food nourishes", unsafe == 10 && safe > 10, "unsafe=" + unsafe + " safe=" + safe);

      // Anorexia: food gives only about a third, and you are frail whenever hungry.
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.giveOnly("anorexia");
      this.onPlayer(p -> {
         p.getFoodData().setFoodLevel(6);
         return null;
      });
      this.cmd("item replace entity @a[limit=1] weapon.mainhand with minecraft:cooked_beef 1");
      this.eatFor(50);
      int fed = this.onPlayer(p -> p.getFoodData().getFoodLevel());
      boolean frail = this.hasModifier(Attributes.MAX_HEALTH, "frail_health");
      this.check("anorexia: a meal gives only a third (8 -> ~3) and you are frail while hungry", fed >= 7 && fed <= 10 && frail, "food=" + fed + " frail=" + frail);
      this.cmd("disability remove @a all");
   }

   /** Mechanic checks for the disabilities that were first added without a test of their own. */
   private void coverageGaps() {
      UUID id = this.id();
      String inert = "{NoAI:1b,Silent:1b,Invulnerable:1b,PersistenceRequired:1b}";

      // Thrill Seeker: a drop energises you, a cramped space bores you.
      this.clean();
      this.platform();
      this.giveOnly("thrill_seeker");
      int tx = this.px + 30;
      int tz = this.pz + 30;
      this.cmd("setblock " + tx + " 150 " + tz + " minecraft:stone");
      this.cmd("tp @a " + (tx + 0.5) + " 151 " + (tz + 0.5));
      boolean energised = this.waitServer(s -> {
         ServerPlayer p = s.getPlayerList().getPlayers().get(0);
         return p.hasEffect(MobEffects.SPEED) && p.hasEffect(MobEffects.HASTE);
      }, 60);
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5));
      this.ticks(3);
      this.cmd("setblock " + tx + " 150 " + tz + " minecraft:air");
      this.cmd("effect clear @a");
      this.cmd("fill " + (this.px - 2) + " 249 " + (this.pz - 2) + " " + (this.px + 2) + " 253 " + (this.pz + 2) + " minecraft:stone hollow");
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5));
      boolean bored = this.waitServer(s -> s.getPlayerList().getPlayers().get(0).hasEffect(MobEffects.MINING_FATIGUE), 60);
      this.cmd("fill " + (this.px - 2) + " 250 " + (this.pz - 2) + " " + (this.px + 2) + " 253 " + (this.pz + 2) + " minecraft:air");
      this.check("thrill_seeker: a drop gives speed and haste, a cramped room gives mining fatigue", energised && bored, "energised=" + energised + " bored=" + bored);

      // Night Owl: groggy in bright light, sharp in the dark.
      this.clean();
      this.platform();
      this.cmd("time set noon");
      this.giveOnly("night_owl");
      boolean groggy = this.waitServer(s -> {
         ServerPlayer p = s.getPlayerList().getPlayers().get(0);
         return p.hasEffect(MobEffects.SLOWNESS) && p.hasEffect(MobEffects.WEAKNESS);
      }, 80);
      this.cmd("effect clear @a");
      this.cmd("fill " + (this.px - 2) + " 249 " + (this.pz - 2) + " " + (this.px + 2) + " 253 " + (this.pz + 2) + " minecraft:stone hollow");
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5));
      boolean sharp = this.waitServer(s -> {
         ServerPlayer p = s.getPlayerList().getPlayers().get(0);
         return p.hasEffect(MobEffects.SPEED) && p.hasEffect(MobEffects.NIGHT_VISION);
      }, 80);
      this.cmd("fill " + (this.px - 2) + " 250 " + (this.pz - 2) + " " + (this.px + 2) + " 253 " + (this.pz + 2) + " minecraft:air");
      this.check("night_owl: slow and weak in daylight, fast with night vision in the dark", groggy && sharp, "groggy=" + groggy + " sharp=" + sharp);

      // Hoarder: 18 used slots is a light load, 28 a heavy one, and clearing the pack lifts it.
      this.clean();
      this.cmd("clear @a");
      this.giveOnly("hoarder");
      this.cmd("give @a minecraft:wooden_sword 18");
      this.ticks(4);
      boolean light = this.hasModifier(Attributes.MOVEMENT_SPEED, "hoarder_light") && !this.hasModifier(Attributes.MOVEMENT_SPEED, "hoarder_heavy");
      this.cmd("give @a minecraft:wooden_sword 10");
      this.ticks(4);
      boolean heavy = this.hasModifier(Attributes.MOVEMENT_SPEED, "hoarder_heavy") && !this.hasModifier(Attributes.MOVEMENT_SPEED, "hoarder_light");
      this.cmd("clear @a");
      this.ticks(4);
      boolean free = !this.hasModifier(Attributes.MOVEMENT_SPEED, "hoarder_light") && !this.hasModifier(Attributes.MOVEMENT_SPEED, "hoarder_heavy");
      this.check("hoarder: 18 slots light, 28 slots heavy, empty pack free", light && heavy && free, "light=" + light + " heavy=" + heavy + " free=" + free);

      // Slippery Grip: a hit you land and a block you break each wear the held tool one extra use.
      this.clean();
      this.platform();
      this.cmd("clear @a");
      this.cmd("summon minecraft:cow " + (this.px + 3.5) + " 250 " + (this.pz + 0.5) + " {NoAI:1b,Silent:1b,PersistenceRequired:1b}");
      this.ticks(5);
      this.cmd("item replace entity @a[limit=1] weapon.mainhand with minecraft:iron_pickaxe 1");
      this.ticks(2);
      this.cmd("damage @e[type=minecraft:cow,limit=1] 2 minecraft:player_attack by @a[limit=1]");
      this.ticks(30);
      int controlWear = this.onPlayer(p -> p.getMainHandItem().getDamageValue());
      int controlBreak = this.breakBlockWith("iron_pickaxe");
      this.giveOnly("slippery_grip");
      this.cmd("item replace entity @a[limit=1] weapon.mainhand with minecraft:iron_pickaxe 1");
      this.ticks(2);
      this.cmd("damage @e[type=minecraft:cow,limit=1] 2 minecraft:player_attack by @a[limit=1]");
      this.ticks(30);
      int hitWear = this.onPlayer(p -> p.getMainHandItem().getDamageValue());
      int slipBreak = this.breakBlockWith("iron_pickaxe");
      this.cmd("kill @e[type=minecraft:cow]");
      this.check("slippery_grip: a landed hit wears the tool one extra use (0 normally)", controlWear == 0 && hitWear == 1, "control=" + controlWear + " slippery=" + hitWear);
      this.check("slippery_grip: breaking a block wears it one extra use", slipBreak - controlBreak == 1, "control=" + controlBreak + " slippery=" + slipBreak);

      // Backwards Brain: the mouse's up/down is inverted during an episode, normal otherwise (forward keys are untouched).
      this.clean();
      this.platform();
      this.giveOnly("backwards_brain");
      this.ticks(3);
      this.ctx.runOnClient(mc -> mc.player.setXRot(0.0F));
      this.ctx.runOnClient(mc -> mc.player.turn(0.0, 10.0));
      float normal = this.ctx.computeOnClient(mc -> mc.player.getXRot());
      this.ctx.runOnClient(mc -> {
         mc.player.setXRot(0.0F);
         ClientProbe.startReverse(200);
      });
      this.ctx.runOnClient(mc -> mc.player.turn(0.0, 10.0));
      float inverted = this.ctx.computeOnClient(mc -> mc.player.getXRot());
      this.ctx.runOnClient(mc -> {
         mc.player.setXRot(0.0F);
         ClientProbe.startReverse(0);
      });
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.ticks(5);
      this.ctx.runOnClient(mc -> ClientProbe.startReverse(200));
      this.ticks(3);
      double fwd0 = this.onPlayer(p -> p.getZ());
      this.ctx.getInput().holdKeyFor(o -> o.keyUp, 15);
      this.ticks(5);
      double stillForward = this.onPlayer(p -> p.getZ()) - fwd0;
      this.ctx.runOnClient(mc -> ClientProbe.startReverse(0));
      this.check("backwards_brain: mouse look inverts in an episode, W still walks forward", normal > 1.0F && inverted < -1.0F && stillForward > 0.5,
         "normal=" + normal + " inverted=" + inverted + " forward=" + stillForward);

      // Heavy Eyelids: the lids creep shut while you idle and any jump jolts them open.
      this.clean();
      this.platform();
      this.giveOnly("heavy_eyelids");
      this.ticks(60);
      float creeping = this.ctx.computeOnClient(mc -> ClientProbe.lidLevel());
      this.ctx.runOnClient(mc -> ClientProbe.setLidLevel(0.7F));
      this.ticks(2);
      this.ctx.takeScreenshot("heavy_eyelids_drooping");
      this.ctx.getInput().holdKeyFor(o -> o.keyJump, 3);
      this.ticks(4);
      float jolted = this.ctx.computeOnClient(mc -> ClientProbe.lidLevel());
      this.check("heavy_eyelids: lids creep shut when idle, a jump jolts them open", creeping > 0.05F && creeping < 0.4F && jolted < 0.05F,
         "creeping=" + creeping + " afterJump=" + jolted);

      // Misophonia: chewing and drinking come back as three shrill echoes; a harp note does not, and nothing dims.
      this.clean();
      this.platform();
      this.giveOnly("misophonia");
      int echo0 = this.ctx.computeOnClient(mc -> ClientProbe.echoCount());
      this.cmd("playsound minecraft:block.note_block.harp master @a " + this.px + " 250 " + this.pz + " 1");
      this.ticks(20);
      int echoHarp = this.ctx.computeOnClient(mc -> ClientProbe.echoCount());
      this.cmd("playsound minecraft:entity.generic.eat master @a " + this.px + " 250 " + this.pz + " 1");
      this.ticks(25);
      int echoChew = this.ctx.computeOnClient(mc -> ClientProbe.echoCount());
      int dimmed = this.ctx.computeOnClient(mc -> ClientProbe.overloadNow());
      this.check("misophonia: chewing echoes three times, a harp note does not, the screen never dims", echoHarp == echo0 && echoChew - echoHarp == 3 && dimmed == 0,
         "harp=" + (echoHarp - echo0) + " chew=" + (echoChew - echoHarp) + " dim=" + dimmed);

      // Jumpy: an explosion makes you jump and flinch; a quiet sound does not.
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.giveOnly("jumpy");
      this.ticks(5);
      this.cmd("playsound minecraft:block.note_block.harp master @a " + this.px + " 250 " + this.pz + " 1");
      double quietLift = 0.0;
      for (int i = 0; i < 15; i++) {
         this.ticks(1);
         quietLift = Math.max(quietLift, this.onPlayer(p -> p.getY()) - 250.0);
      }

      float yaw0 = this.ctx.computeOnClient(mc -> mc.player.getYRot());
      this.cmd("playsound minecraft:entity.generic.explode master @a " + this.px + " 250 " + this.pz + " 4");
      double lift = 0.0;
      for (int i = 0; i < 15; i++) {
         this.ticks(1);
         lift = Math.max(lift, this.onPlayer(p -> p.getY()) - 250.0);
      }

      float flinch = Math.abs(net.minecraft.util.Mth.wrapDegrees(this.ctx.computeOnClient(mc -> mc.player.getYRot()) - yaw0));
      this.check("jumpy: an explosion makes you jump and flinch, a harp note does not", quietLift < 0.05 && lift > 0.5 && Math.abs(flinch - 30.0F) < 3.0F,
         "quiet=" + quietLift + " jump=" + lift + " flinch=" + flinch);

      // Blabbermouth: with the odds forced up you blurt your coordinates into chat.
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5));
      this.giveOnly("blabbermouth");
      this.ctx.runOnClient(mc -> ClientProbe.setBlabOdds(1));
      this.ticks(4);
      this.ctx.runOnClient(mc -> ClientProbe.setBlabOdds(2400));
      long blurts = this.chatLines().stream().filter(l -> l.contains("I'm at ")).count();
      this.check("blabbermouth: you blurt your coordinates into chat", blurts >= 1, "blurts=" + blurts);
      this.cmd("disability remove @a all");
   }

   private void deathRemoval(UUID id) {
      this.cmd("difficulty easy");
      this.clean();
      this.platform();
      this.check("deathremoval defaults to 0", PlusState.deathRemoval() == 0, "value=" + PlusState.deathRemoval());

      String[] five = {"anemia", "hangry", "moon_boots", "giant", "glass_cannon"};
      for (String n : five) {
         this.cmd("disability give @a " + n);
      }

      this.ticks(3);
      int kept = this.respawnAfterKill();
      this.check("default 0: dying removes nothing", kept == 5, "left=" + kept);

      this.cmd("disability deathremoval 2");
      this.ticks(2);
      this.check("deathremoval 2 is stored", PlusState.deathRemoval() == 2, "value=" + PlusState.deathRemoval());
      this.platform();
      int afterTwo = this.respawnAfterKill();
      this.check("deathremoval 2: dying lifts two of five", afterTwo == 3, "left=" + afterTwo);

      this.cmd("disability deathremoval 150");
      this.ticks(2);
      this.check("an out-of-range value is rejected, setting unchanged", PlusState.deathRemoval() == 2, "value=" + PlusState.deathRemoval());

      this.cmd("disability deathremoval 100");
      this.ticks(2);
      this.platform();
      int afterAll = this.respawnAfterKill();
      this.check("deathremoval 100: dying with fewer lifts them all", afterAll == 0, "left=" + afterAll);

      // Persistence: the value is written to and restored from the run file.
      this.cmd("disability deathremoval 4");
      this.ticks(2);
      Path file = this.server.computeOnServer(s -> {
         PlusState.save();
         return s.getWorldPath(LevelResource.ROOT).resolve("disabilities-plus.txt");
      });
      String text;
      try {
         text = Files.readString(file);
      } catch (java.io.IOException e) {
         throw new IllegalStateException(e);
      }

      this.check("deathRemoval is saved to the run file", text.contains("deathRemoval=4"), text.replace('\n', ' '));
      this.server.runOnServer(s -> {
         PlusState.setDeathRemoval(0);
         PlusState.load(s);
      });
      this.check("deathRemoval is restored on load", PlusState.deathRemoval() == 4, "value=" + PlusState.deathRemoval());
      this.cmd("disability deathremoval 0");
      this.cmd("disability remove @a all");
      this.ticks(2);
      this.check("deathremoval reset to 0", PlusState.deathRemoval() == 0, "");
   }

   /** Walks forward/backward on the platform in 60-tick legs; returns {max sideways drift, max travel along Z}. */
   private double[] walk(int maxTicks, double stopAtDrift) {
      double x0 = this.onPlayer(p -> p.getX());
      double z0 = this.onPlayer(p -> p.getZ());
      double drift = 0.0;
      double travelled = 0.0;
      boolean forward = true;
      for (int t = 0; t < maxTicks && drift < stopAtDrift; t += 2) {
         if (t % 60 == 0) {
            this.ctx.getInput().releaseKey(o -> o.keyUp);
            this.ctx.getInput().releaseKey(o -> o.keyDown);
            if (forward) {
               this.ctx.getInput().holdKey(o -> o.keyUp);
            } else {
               this.ctx.getInput().holdKey(o -> o.keyDown);
            }

            forward = !forward;
         }

         this.ticks(2);
         double x = this.onPlayer(p -> p.getX());
         double z = this.onPlayer(p -> p.getZ());
         drift = Math.max(drift, Math.abs(x - x0));
         travelled = Math.max(travelled, Math.abs(z - z0));
      }

      this.ctx.getInput().releaseKey(o -> o.keyUp);
      this.ctx.getInput().releaseKey(o -> o.keyDown);
      return new double[]{drift, travelled};
   }

   private void ataxia() {
      this.clean();
      this.cmd("gamemode survival @a");
      this.platform();
      boolean alive = this.onPlayer(p -> p.isAlive() && p.getY() > 249.0);
      this.check("ataxia: player alive on the platform before the walk", alive, "");
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz - 9.5) + " 0 0");
      this.ticks(10);

      double[] control = this.walk(240, 99.0);
      this.check(
         "ataxia control: really walked, and stayed straight",
         control[1] > 3.0 && control[0] < 0.15,
         "travelled=" + control[1] + " maxDrift=" + control[0]
      );

      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz - 9.5) + " 0 0");
      this.giveOnly("ataxia");
      double[] real = this.walk(900, 0.3);
      this.check(
         "ataxia: walking produces sideways stumbles",
         real[0] >= 0.3,
         "travelled=" + real[1] + " maxDrift=" + real[0]
      );
      this.ctx.takeScreenshot("ataxia_done");
   }

   // ---------------------------------------------------------------- the ten added in 1.1

   private boolean waitClient(java.util.function.Predicate<net.minecraft.client.Minecraft> p, int max) {
      for (int i = 0; i < max; i++) {
         if (this.ctx.computeOnClient(p::test)) {
            return true;
         }

         this.ticks(1);
      }

      return this.ctx.computeOnClient(p::test);
   }

   private static String kindOf(Mob m) {
      return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(m.getType()).getPath();
   }

   /** The amount of one of the add-on's attribute modifiers on the player, or NaN when it is absent. */
   private double modifierAmount(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, String path) {
      Identifier modifier = Identifier.fromNamespaceAndPath("disabilities_plus", path);
      return this.onPlayer(p -> {
         var m = p.getAttribute(attr).getModifier(modifier);
         return m == null ? Double.NaN : m.amount();
      });
   }

   /** Gives a fresh tool, breaks one block with it as the player, and returns how worn the tool is. */
   private int breakBlockWith(String tool) {
      this.cmd("item replace entity @a[limit=1] weapon.mainhand with minecraft:" + tool + " 1");
      this.cmd("setblock " + (this.px + 5) + " 250 " + this.pz + " minecraft:stone");
      this.ticks(3);
      int wear = this.server.computeOnServer(s -> {
         ServerPlayer p = s.getPlayerList().getPlayers().get(0);
         p.gameMode.destroyBlock(new net.minecraft.core.BlockPos(this.px + 5, 250, this.pz));
         return p.getMainHandItem().getDamageValue();
      });
      this.ticks(2);
      return wear;
   }

   private PlusConditions.State cond() {
      UUID id = this.id();
      return PlusEffects.of(id).cond;
   }

   private void holdItem(String item) {
      this.cmd("item replace entity @a[limit=1] weapon.mainhand with minecraft:" + item + " 1");
      this.ticks(2);
   }

   private void illnessesAndQuirks() {
      UUID id = this.id();
      this.cmd("gamemode survival @a");

      // ---- Alpha-gal: mammal meat arms a delayed reaction; chicken does not.
      this.clean();
      this.platform();
      this.cmd("effect give @a minecraft:saturation 1 0 true");
      this.giveOnly("alpha_gal");
      this.holdItem("cooked_chicken");
      this.eatFor(45);
      boolean chickenSafe = this.cond().allergyDelay == 0;
      this.holdItem("cooked_beef");
      this.eatFor(45);
      boolean armed = this.cond().allergyDelay > 0;
      this.server.runOnServer(s -> {
         PlusEffects.of(id).cond.allergyDelay = 2;
         ServerPlayer p = s.getPlayerList().getPlayers().get(0);
         p.setHealth(p.getMaxHealth());
         p.getFoodData().setFoodLevel(20);
      });
      this.ticks(6);
      float afterHealth = this.onPlayer(ServerPlayer::getHealth);
      int afterFood = this.onPlayer(p -> p.getFoodData().getFoodLevel());
      this.check("alpha_gal: chicken is safe, beef arms the reaction", chickenSafe && armed, "chickenSafe=" + chickenSafe + " armed=" + armed);
      boolean swollen = this.hasEffect(MobEffects.MINING_FATIGUE, 2) && this.hasEffect(MobEffects.WEAKNESS, 1);
      boolean notDoubledOver = this.ctx.computeOnClient(mc -> ClientProbe.painTicks()) == 0;
      this.check("alpha_gal: the reaction hurts, empties the stomach and swells your hands (no cramps)",
         afterHealth < 20.0F && afterHealth >= 2.0F && afterFood <= 12 && swollen && notDoubledOver,
         "health=" + afterHealth + " food=" + afterFood + " swollen=" + swollen + " notDoubledOver=" + notDoubledOver);
      this.server.runOnServer(s -> {
         PlusEffects.of(id).cond.allergyDelay = 2;
         s.getPlayerList().getPlayers().get(0).setHealth(3.0F);
      });
      this.ticks(6);
      boolean survived = this.onPlayer(p -> p.isAlive() && p.getHealth() >= 2.0F);
      this.check("alpha_gal: a reaction never drops you below 2 HP", survived, "");

      // ---- B12 deficiency: supply drains; empty means numb legs that cannot step up; animal food fixes it.
      this.clean();
      this.platform();
      this.cmd("clear @a");
      this.giveOnly("b12_deficiency");
      this.ticks(3);
      int supply = this.cond().b12Supply;
      double stepFull = this.onPlayer(p -> p.getAttributeValue(Attributes.STEP_HEIGHT));
      this.check("b12_deficiency: starts with a supply and full step height", supply > 0 && supply <= 6000 && Math.abs(stepFull - 0.6) < 1.0E-6,
         "supply=" + supply + " step=" + stepFull);
      this.server.runOnServer(s -> PlusEffects.of(id).cond.b12Supply = 1);
      this.ticks(4);
      double stepNumb = this.onPlayer(p -> p.getAttributeValue(Attributes.STEP_HEIGHT));
      this.check("b12_deficiency: with the supply empty your legs cannot step up (step height 0)", stepNumb < 1.0E-6, "step=" + stepNumb);
      this.holdItem("cooked_beef");
      this.eatFor(45);
      int refilled = this.cond().b12Supply;
      double stepBack = this.onPlayer(p -> p.getAttributeValue(Attributes.STEP_HEIGHT));
      this.check("b12_deficiency: eating meat refills the supply and restores the step", refilled >= 8000 && Math.abs(stepBack - 0.6) < 1.0E-6,
         "supply=" + refilled + " step=" + stepBack);

      // ---- COVID-19: coughing fits after the first minute, then fever spikes; taste is untouched.
      this.clean();
      this.platform();
      this.cmd("clear @a");
      this.giveOnly("covid_19");
      this.ticks(3);
      int started = this.cond().ill[0];
      this.server.runOnServer(s -> PlusConditions.coughOdds = 1);
      this.ticks(3);
      boolean quiet = this.ctx.computeOnClient(mc -> ClientProbe.coughTicks() == 0);
      this.server.runOnServer(s -> PlusEffects.of(id).cond.ill[0] = 8000);
      boolean coughed = this.waitClient(mc -> ClientProbe.coughTicks() > 0, 30);
      this.server.runOnServer(s -> {
         PlusConditions.coughOdds = 400;
         PlusEffects.of(id).cond.feverTimer = 2;
      });
      boolean fever = this.waitClient(mc -> ClientProbe.feverTicks() > 0, 30);
      this.ticks(60);
      this.ctx.takeScreenshot("covid_fever_flush");
      boolean weakened = this.waitServer(s -> s.getPlayerList().getPlayers().get(0).hasEffect(MobEffects.WEAKNESS), 60);
      this.check("covid_19: clock starts, coughs wait out the first minute, then coughing and fever spikes", started > 9000 && quiet && coughed && fever && weakened,
         "started=" + started + " quiet=" + quiet + " coughed=" + coughed + " fever=" + fever + " weak=" + weakened);
      this.server.runOnServer(s -> {
         ServerPlayer p = s.getPlayerList().getPlayers().get(0);
         p.getFoodData().setFoodLevel(4);
         p.getFoodData().setSaturation(0.0F);
      });
      this.holdItem("cooked_beef");
      this.eatFor(45);
      int tasteFood = this.onPlayer(p -> p.getFoodData().getFoodLevel());
      this.check("covid_19: food is no longer reduced (4 + 8 = 12)", tasteFood >= 11, "food=" + tasteFood);
      this.server.runOnServer(s -> PlusEffects.of(id).cond.ill[0] = 2);
      this.ticks(6);
      boolean recovered = !this.serverHas(PlusDisability.COVID_19) && this.cond().immune[0] > 0;
      this.check("covid_19: it runs its course, you recover and are immune for a while", recovered, "");

      // ---- Ebola: max health wasted away over time and weakness; no bleeding (that belongs to hemophilia).
      this.clean();
      this.platform();
      this.giveOnly("ebola");
      this.ticks(3);
      this.server.runOnServer(s -> PlusEffects.of(id).cond.ill[1] = 14400 - 1800);
      this.ticks(4);
      float maxHp = this.onPlayer(p -> p.getMaxHealth());
      boolean weak = this.waitServer(s -> s.getPlayerList().getPlayers().get(0).hasEffect(MobEffects.WEAKNESS), 40);
      this.ticks(40);
      int bleeding = this.onPlayer(p -> PlusEffects.of(id).bleedTicks);
      this.cmd("disability remove @a ebola");
      this.ticks(4);
      float restored = this.onPlayer(p -> p.getMaxHealth());
      this.check("ebola: 1800 ticks in costs two hearts and weakens you, never bleeds, and the health comes back", Math.abs(maxHp - 16.0F) < 0.01F && weak && bleeding == 0 && Math.abs(restored - 20.0F) < 0.01F,
         "max=" + maxHp + " weak=" + weak + " bleed=" + bleeding + " restored=" + restored);

      // ---- E. coli: raw food and long swims in dirty water drain your hunger (the Hunger effect), no cramps.
      this.clean();
      this.platform();
      this.cmd("clear @a");
      this.giveOnly("e_coli");
      this.server.runOnServer(s -> s.getPlayerList().getPlayers().get(0).getFoodData().setFoodLevel(16));
      this.holdItem("beef");
      this.eatFor(45);
      int foodAfterRaw = this.onPlayer(p -> p.getFoodData().getFoodLevel());
      boolean drained = this.hasEffect(MobEffects.HUNGER, 1);
      boolean noCramp = this.ctx.computeOnClient(mc -> ClientProbe.painTicks()) == 0;
      this.check("e_coli: raw beef costs more than it gives and drains your hunger, without cramps", foodAfterRaw < 16 && drained && noCramp,
         "food=" + foodAfterRaw + " hungerEffect=" + drained + " noCramp=" + noCramp);
      this.cmd("effect clear @a");
      this.cmd("fill " + (this.px - 1) + " 250 " + (this.pz - 1) + " " + (this.px + 1) + " 253 " + (this.pz + 1) + " minecraft:water");
      this.cmd("tp @a " + (this.px + 0.5) + " 251 " + (this.pz + 0.5));
      boolean swamped = this.waitServer(s -> PlusEffects.of(id).cond.waterCooldown > 0, 160);
      boolean waterDrain = this.hasEffect(MobEffects.HUNGER, 1);
      this.cmd("fill " + (this.px - 1) + " 250 " + (this.pz - 1) + " " + (this.px + 1) + " 253 " + (this.pz + 1) + " minecraft:air");
      this.check("e_coli: ~4 s in water drains your hunger", swamped && waterDrain, "swamped=" + swamped + " hunger=" + waterDrain);

      // ---- Mono: naps, and a hard hit ruptures the spleen (a heavy blow and a blackout, once per cooldown; no bleeding).
      this.clean();
      this.platform();
      this.giveOnly("mono");
      this.server.runOnServer(s -> PlusEffects.of(id).cond.napTimer = 2);
      boolean napped = this.waitClient(mc -> ClientProbe.napTicks() > 0, 20);
      float fade = this.ctx.computeOnClient(mc -> ClientProbe.napFade());
      this.ctx.takeScreenshot("mono_nap_overlay");
      this.check("mono: you drop off, the screen goes dark", napped && fade > 0.0F, "napped=" + napped + " fade=" + fade);
      this.clean();
      this.giveOnly("mono");
      this.cmd("damage @a[limit=1] 3 minecraft:generic");
      this.ticks(3);
      boolean light = this.onPlayer(p -> PlusEffects.of(id).cond.spleenCooldown == 0) && !this.onPlayer(p -> p.hasEffect(MobEffects.BLINDNESS));
      this.ticks(30);
      float before = this.onPlayer(ServerPlayer::getHealth);
      this.cmd("damage @a[limit=1] 7 minecraft:generic");
      this.ticks(3);
      float after = this.onPlayer(ServerPlayer::getHealth);
      boolean ruptured = this.onPlayer(p -> PlusEffects.of(id).cond.spleenCooldown > 0 && p.hasEffect(MobEffects.BLINDNESS) && PlusEffects.of(id).bleedTicks == 0);
      this.check("mono: a light hit is fine, a hard one ruptures the spleen (extra damage and a blackout, no bleed)",
         light && ruptured && after <= before - 9.0F, "light=" + light + " ruptured=" + ruptured + " before=" + before + " after=" + after);

      // ---- Erectile dysfunction: using an item keeps cutting out (odds forced to 1).
      this.clean();
      this.platform();
      this.giveOnly("erectile_dysfunction");
      this.server.runOnServer(s -> PlusConditions.sustainOdds = 1);
      this.holdItem("cooked_beef");
      this.eatFor(50);
      int stillThere = this.onPlayer(p -> p.getMainHandItem().getCount());
      this.server.runOnServer(s -> PlusConditions.sustainOdds = 120);
      this.cmd("disability remove @a erectile_dysfunction");
      this.holdItem("cooked_beef");
      this.eatFor(50);
      int eaten = this.onPlayer(p -> p.getMainHandItem().getCount());
      this.check("erectile_dysfunction: the meal never finishes, but does without it", stillThere == 1 && eaten == 0, "with=" + stillThere + " without=" + eaten);

      // ---- Baldness: no helmet in the cold chills you, a helmet stops it.
      this.clean();
      this.platform();
      this.cmd("item replace entity @a[limit=1] armor.head with minecraft:air");
      this.giveOnly("baldness");
      this.server.runOnServer(s -> PlusConditions.forceCold = true);
      this.ticks(10);
      int chilled = this.onPlayer(p -> p.getTicksFrozen());
      this.cmd("item replace entity @a[limit=1] armor.head with minecraft:iron_helmet");
      this.ticks(60);
      int warm = this.onPlayer(p -> p.getTicksFrozen());
      this.server.runOnServer(s -> PlusConditions.forceCold = false);
      this.cmd("item replace entity @a[limit=1] armor.head with minecraft:air");
      this.ctx.runOnClient(mc -> mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT));
      this.ticks(5);
      this.ctx.takeScreenshot("baldness_third_person");
      this.ctx.runOnClient(mc -> mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
      this.check("baldness: bare head chills in the cold, a helmet lets it thaw", chilled >= 8 && warm == 0, "chilled=" + chilled + " warm=" + warm);

      // ---- Evil: hostile and neutral mobs are set on you; passive ones are left alone.
      this.clean();
      this.platform();
      String inert = "{NoAI:1b,Silent:1b,Invulnerable:1b,PersistenceRequired:1b}";
      this.cmd("summon minecraft:zombie " + (this.px + 8.5) + " 250 " + (this.pz + 0.5) + " " + inert);
      this.cmd("summon minecraft:iron_golem " + (this.px - 8.5) + " 250 " + (this.pz + 0.5) + " " + inert);
      this.cmd("summon minecraft:cow " + (this.px + 0.5) + " 250 " + (this.pz + 8.5) + " " + inert);
      this.ticks(5);
      this.server.runOnServer(s -> s.getPlayerList().getPlayers().get(0).level().getEntitiesOfClass(Mob.class, s.getPlayerList().getPlayers().get(0).getBoundingBox().inflate(30.0)).forEach(m -> m.setTarget(null)));
      this.ticks(30);
      boolean calm = this.onPlayer(p -> p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(30.0)).stream().allMatch(m -> m.getTarget() == null));
      this.giveOnly("evil");
      this.ticks(45);
      boolean zombieMad = this.onPlayer(p -> p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(30.0), m -> kindOf(m).equals("zombie")).stream().allMatch(m -> m.getTarget() == p));
      boolean golemMad = this.onPlayer(p -> p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(30.0), m -> kindOf(m).equals("iron_golem")).stream().allMatch(m -> m.getTarget() == p));
      boolean cowCalm = this.onPlayer(p -> p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(30.0), m -> kindOf(m).equals("cow")).stream().allMatch(m -> m.getTarget() == null));
      this.cmd("kill @e[type=!minecraft:player]");
      this.check("evil: zombie and iron golem both target you, the cow does not", calm && zombieMad && golemMad && cowCalm, "calm=" + calm + " zombie=" + zombieMad + " golem=" + golemMad + " cow=" + cowCalm);

      // ---- Dysgraphia: writing is miswritten in text boxes, signs and books; chat (speech) and commands are not touched.
      this.clean();
      this.giveOnly("dysgraphia");
      String msg = "please bring the big pile of dark quartz and diamond blocks back";
      int boxChanged = 0;
      int penChanged = 0;
      for (int i = 0; i < 10; i++) {
         if (!msg.equals(this.ctx.computeOnClient(mc -> ClientProbe.typedIntoBox(msg)))) {
            boxChanged++;
         }

         if (!msg.equals(this.ctx.computeOnClient(mc -> ClientProbe.typedIntoPen(msg)))) {
            penChanged++;
         }
      }

      String chat = this.ctx.computeOnClient(mc -> ClientProbe.garble(msg));
      String command = this.ctx.computeOnClient(mc -> ClientProbe.typedIntoBox("/give @s stick 64"));
      this.cmd("disability remove @a all");
      this.ticks(4);
      String plainBox = this.ctx.computeOnClient(mc -> ClientProbe.typedIntoBox(msg));
      this.check("dysgraphia: text boxes and signs/books are miswritten, chat and commands are not, and healthy typing is exact",
         boxChanged >= 8 && penChanged >= 8 && chat.equals(msg) && command.equals("/give @s stick 64") && plainBox.equals(msg),
         "box=" + boxChanged + "/10 pen=" + penChanged + "/10 chatSame=" + chat.equals(msg) + " command=" + command);

      // ---- Sneezing: the server times a sneeze; it jerks the camera and wakes monsters nearby (a cow stays calm).
      this.clean();
      this.platform();
      String still = "{NoAI:1b,Silent:1b,Invulnerable:1b,PersistenceRequired:1b}";
      this.cmd("summon minecraft:zombie " + (this.px + 6.5) + " 250 " + (this.pz + 0.5) + " " + still);
      this.cmd("summon minecraft:cow " + (this.px - 6.5) + " 250 " + (this.pz + 0.5) + " " + still);
      this.ticks(3);
      this.server.runOnServer(s -> s.getPlayerList().getPlayers().get(0).level().getEntitiesOfClass(Mob.class,
         s.getPlayerList().getPlayers().get(0).getBoundingBox().inflate(30.0)).forEach(m -> m.setTarget(null)));
      this.giveOnly("sneezing");
      this.ticks(30);
      boolean unwoken = this.onPlayer(p -> p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(30.0)).stream().allMatch(m -> m.getTarget() == null));
      int sneezes0 = this.ctx.computeOnClient(mc -> ClientProbe.sneezeCount());
      this.server.runOnServer(s -> PlusEffects.of(id).cond.sneezeTimer = 2);
      boolean sneezed = this.waitClient(mc -> ClientProbe.sneezeCount() > sneezes0, 30);
      boolean zombieWoken = this.onPlayer(p -> p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(30.0), m -> kindOf(m).equals("zombie")).stream().allMatch(m -> m.getTarget() == p));
      boolean cowStays = this.onPlayer(p -> p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(30.0), m -> kindOf(m).equals("cow")).stream().allMatch(m -> m.getTarget() == null));
      this.cmd("kill @e[type=!minecraft:player]");
      this.check("sneezing: nothing happens between sneezes; the sneeze jerks you and wakes the zombie but not the cow", unwoken && sneezed && zombieWoken && cowStays,
         "unwoken=" + unwoken + " sneezed=" + sneezed + " zombie=" + zombieWoken + " cow=" + cowStays);

      // ---- Stack counts: readable under Dyslexia alone, unreadable under Dyscalculia.
      this.clean();
      this.cmd("clear @a");
      this.cmd("give @a minecraft:cobblestone 64");
      this.giveOnly("dyslexia");
      this.ticks(5);
      int obf0 = this.ctx.computeOnClient(mc -> ClientProbe.obfuscatedCountDraws());
      int read0 = this.ctx.computeOnClient(mc -> ClientProbe.readableCountDraws());
      this.ctx.takeScreenshot("dyslexia_stack_count_readable");
      int obf1 = this.ctx.computeOnClient(mc -> ClientProbe.obfuscatedCountDraws());
      int read1 = this.ctx.computeOnClient(mc -> ClientProbe.readableCountDraws());
      boolean exemptRestored = this.ctx.computeOnClient(mc -> !com.studiobing.disabilities.client.ClientState.dyslexiaExempt());
      this.check("dyslexia: stack counts stay readable, and the exemption is restored afterwards", read1 > read0 && obf1 == obf0 && exemptRestored, "read+" + (read1 - read0) + " obf+" + (obf1 - obf0) + " restored=" + exemptRestored);
      this.giveOnly("dyscalculia");
      this.ticks(5);
      int obf2 = this.ctx.computeOnClient(mc -> ClientProbe.obfuscatedCountDraws());
      this.ctx.takeScreenshot("dyscalculia_stack_count_unreadable");
      int obf3 = this.ctx.computeOnClient(mc -> ClientProbe.obfuscatedCountDraws());
      this.check("dyscalculia: stack counts are drawn unreadable", obf3 > obf2, "obf+" + (obf3 - obf2));
      this.cmd("clear @a");

      // ---- Dying cures the infectious illnesses (deathremoval is 0 here, so nothing else is lifted).
      this.clean();
      this.platform();
      this.cmd("disability give @a covid_19");
      this.cmd("disability give @a ebola");
      this.cmd("disability give @a tinnitus");
      this.ticks(4);
      int left = this.respawnAfterKill();
      boolean cleared = !this.serverHas(PlusDisability.COVID_19) && !this.serverHas(PlusDisability.EBOLA) && this.serverHas(PlusDisability.TINNITUS);
      this.check("death cures covid_19 and ebola but nothing else", cleared && left == 1, "left=" + left);
      this.cmd("disability remove @a all");
   }

   // ---------------------------------------------------------------- gimmicks with counterplay (1.3.0)

   private void gimmicks() {
      UUID id = this.id();
      this.cmd("gamemode survival @a");
      String inert = "{NoAI:1b,Silent:1b,PersistenceRequired:1b}";

      // ---- Anemia: healing is halved, unless you ate something iron-rich.
      this.clean();
      this.platform();
      this.cmd("clear @a");
      this.giveOnly("anemia");
      this.server.runOnServer(s -> s.getPlayerList().getPlayers().get(0).setHealth(8.0F));
      this.ticks(3);
      this.server.runOnServer(s -> s.getPlayerList().getPlayers().get(0).heal(4.0F));
      this.ticks(3);
      float thin = this.onPlayer(ServerPlayer::getHealth);
      this.holdItem("cooked_beef");
      this.eatFor(45);
      boolean ironFed = PlusEffects.of(id).ironTicks > 0;
      this.server.runOnServer(s -> s.getPlayerList().getPlayers().get(0).setHealth(8.0F));
      this.ticks(3);
      this.server.runOnServer(s -> s.getPlayerList().getPlayers().get(0).heal(4.0F));
      this.ticks(3);
      float thick = this.onPlayer(ServerPlayer::getHealth);
      this.check("anemia: healing is halved until you eat iron-rich food, then it is normal", Math.abs(thin - 10.0F) < 0.7F && ironFed && Math.abs(thick - 12.0F) < 0.7F,
         "thin=" + thin + " ironFed=" + ironFed + " thick=" + thick);

      // ---- Glass cannon: a light hit is fine, a hit of two hearts or more cracks you (one heart less).
      this.clean();
      this.giveOnly("glass_cannon");
      this.ticks(4);
      float whole = this.onPlayer(ServerPlayer::getMaxHealth);
      this.cmd("damage @a[limit=1] 3 minecraft:generic");
      this.ticks(3);
      int lightCracks = PlusEffects.of(id).cracks;
      this.server.runOnServer(s -> s.getPlayerList().getPlayers().get(0).setHealth(s.getPlayerList().getPlayers().get(0).getMaxHealth()));
      this.ticks(25);
      this.cmd("damage @a[limit=1] 5 minecraft:generic");
      this.ticks(3);
      int cracks = PlusEffects.of(id).cracks;
      float cracked = this.onPlayer(ServerPlayer::getMaxHealth);
      this.check("glass_cannon: a light hit is fine, a hard one cracks you (10 -> 8 max health)", Math.abs(whole - 10.0F) < 0.01F && lightCracks == 0 && cracks == 1 && Math.abs(cracked - 8.0F) < 0.01F,
         "whole=" + whole + " lightCracks=" + lightCracks + " cracks=" + cracks + " cracked=" + cracked);
      this.cmd("disability remove @a all");
      this.ticks(3);
      boolean mended = this.onPlayer(p -> PlusEffects.of(id).cracks == 0 && Math.abs(p.getMaxHealth() - 20.0F) < 0.01F);
      this.check("glass_cannon: cracks go with the disability", mended, "");

      // ---- Giant: a hard landing stomps a zombie beside you, shoves a cow, and wakes a husk 9 blocks away.
      this.clean();
      this.platform();
      this.cmd("summon minecraft:husk " + (this.px + 2.5) + " 250 " + (this.pz + 0.5) + " " + inert);
      this.cmd("summon minecraft:husk " + (this.px - 9.5) + " 250 " + (this.pz + 0.5) + " " + inert);
      this.cmd("summon minecraft:cow " + (this.px + 0.5) + " 250 " + (this.pz + 2.5) + " " + inert);
      this.ticks(3);
      this.server.runOnServer(s -> s.getPlayerList().getPlayers().get(0).level().getEntitiesOfClass(Mob.class,
         s.getPlayerList().getPlayers().get(0).getBoundingBox().inflate(40.0)).forEach(m -> m.setTarget(null)));
      this.giveOnly("giant");
      this.cmd("tp @a " + (this.px + 0.5) + " 262 " + (this.pz + 0.5) + " 0 0");
      boolean stomped = this.waitServer(s -> {
         ServerPlayer p = s.getPlayerList().getPlayers().get(0);
         return p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(12.0), m -> kindOf(m).equals("husk") && m.getHealth() < m.getMaxHealth()).size() == 1;
      }, 120);
      this.ticks(3);
      boolean farWoken = this.onPlayer(p -> p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(14.0),
         m -> kindOf(m).equals("husk") && m.getX() < p.getX() - 5.0).stream().allMatch(m -> m.getTarget() == p && m.getHealth() >= m.getMaxHealth()));
      boolean cowUnhurt = this.onPlayer(p -> p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(14.0), m -> kindOf(m).equals("cow"))
         .stream().allMatch(m -> m.getHealth() >= m.getMaxHealth()));
      this.cmd("kill @e[type=!minecraft:player]");
      this.check("giant: a stomp hurts the husk beside you, only wakes the far one, and leaves the cow unhurt", stomped && farWoken && cowUnhurt,
         "stomped=" + stomped + " farWoken=" + farWoken + " cowUnhurt=" + cowUnhurt);

      // ---- Moon boots: a hard landing bounces you back up; without them you stay down.
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 262 " + (this.pz + 0.5) + " 0 0");
      double plainBounce = this.bounceHeightAfterLanding();
      this.clean();
      this.platform();
      this.giveOnly("moon_boots");
      this.cmd("tp @a " + (this.px + 0.5) + " 262 " + (this.pz + 0.5) + " 0 0");
      double moonBounce = this.bounceHeightAfterLanding();
      this.check("moon_boots: landing bounces you up more than a metre, normal landing does not", plainBounce < 0.5 && moonBounce > 1.0,
         "plain=" + plainBounce + " moon=" + moonBounce);

      // ---- Hoarder: bulk is padding.
      this.clean();
      this.cmd("clear @a");
      this.giveOnly("hoarder");
      this.cmd("give @a minecraft:wooden_sword 18");
      this.ticks(4);
      double armorLight = this.onPlayer(p -> p.getAttributeValue(Attributes.ARMOR));
      this.cmd("give @a minecraft:wooden_sword 10");
      this.ticks(4);
      double armorHeavy = this.onPlayer(p -> p.getAttributeValue(Attributes.ARMOR));
      this.cmd("clear @a");
      this.ticks(4);
      double armorNone = this.onPlayer(p -> p.getAttributeValue(Attributes.ARMOR));
      this.check("hoarder: a fuller pack is better padded (0 / +2 / +6 armor)", Math.abs(armorNone) < 1.0E-6 && Math.abs(armorLight - 2.0) < 1.0E-6 && Math.abs(armorHeavy - 6.0) < 1.0E-6,
         "none=" + armorNone + " light=" + armorLight + " heavy=" + armorHeavy);

      // ---- Obesity: unmovable while sprinting, and a sprinting hit sends the target flying.
      this.clean();
      this.platform();
      this.cmd("summon minecraft:cow " + (this.px + 3.5) + " 250 " + (this.pz + 0.5) + " " + inert);
      this.cmd("summon minecraft:cow " + (this.px - 3.5) + " 250 " + (this.pz + 0.5) + " " + inert);
      this.giveOnly("obesity");
      this.server.runOnServer(s -> s.getPlayerList().getPlayers().get(0).setSprinting(true));
      this.ticks(4);
      boolean unshoved = this.hasModifier(Attributes.KNOCKBACK_RESISTANCE, "obese_ram");
      double[] launch = this.server.computeOnServer(s -> {
         ServerPlayer p = s.getPlayerList().getPlayers().get(0);
         var cows = p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(10.0), m -> kindOf(m).equals("cow"));
         p.setSprinting(false);
         cows.get(0).hurtServer(p.level(), p.level().damageSources().playerAttack(p), 1.0F);
         double walkingHit = cows.get(0).getDeltaMovement().horizontalDistance();
         p.setSprinting(true);
         cows.get(1).hurtServer(p.level(), p.level().damageSources().playerAttack(p), 1.0F);
         double sprintHit = cows.get(1).getDeltaMovement().horizontalDistance();
         return new double[]{walkingHit, sprintHit};
      });
      this.cmd("kill @e[type=!minecraft:player]");
      this.check("obesity: resists knockback while sprinting, and a sprinting hit launches the target", unshoved && launch[1] > launch[0] + 0.5,
         "unshoved=" + unshoved + " walking=" + launch[0] + " sprinting=" + launch[1]);

      // ---- Squeaky shoes: walking squeaks lure a zombie; sneaking does not.
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz - 3.5) + " 0 0");
      this.cmd("summon minecraft:zombie " + (this.px + 0.5) + " 250 " + (this.pz + 5.5) + " " + inert);
      this.giveOnly("squeaky_shoes");
      this.ticks(5);
      int lure0 = PlusEffects.squeakLures;
      this.ctx.getInput().holdKey(o -> o.keyUp);
      this.ticks(40);
      this.ctx.getInput().releaseKey(o -> o.keyUp);
      int lure1 = PlusEffects.squeakLures;
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz - 3.5) + " 0 0");
      this.ticks(5);
      this.ctx.getInput().holdKey(o -> o.keyShift);
      this.ctx.getInput().holdKey(o -> o.keyUp);
      this.ticks(40);
      this.ctx.getInput().releaseKey(o -> o.keyUp);
      this.ctx.getInput().releaseKey(o -> o.keyShift);
      int lure2 = PlusEffects.squeakLures;
      this.cmd("kill @e[type=!minecraft:player]");
      this.check("squeaky_shoes: walking lures the zombie, sneaking does not", lure1 - lure0 >= 2 && lure2 == lure1, "walking=" + (lure1 - lure0) + " sneaking=" + (lure2 - lure1));

      // ---- Night blindness: a torch in hand pushes the dark back.
      this.clean();
      this.platform();
      this.cmd("clear @a");
      this.cmd("fill " + (this.px - 2) + " 249 " + (this.pz - 2) + " " + (this.px + 2) + " 253 " + (this.pz + 2) + " minecraft:stone hollow");
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.giveOnly("night_blindness");
      this.ticks(70);
      float darkDim = this.ctx.computeOnClient(mc -> ClientProbe.nightDim());
      this.holdItem("torch");
      this.ticks(70);
      float torchDim = this.ctx.computeOnClient(mc -> ClientProbe.nightDim());
      this.ctx.takeScreenshot("night_blindness_with_torch");
      this.cmd("fill " + (this.px - 2) + " 250 " + (this.pz - 2) + " " + (this.px + 2) + " 253 " + (this.pz + 2) + " minecraft:air");
      this.check("night_blindness: a torch in hand cuts the dimming to a fifth", darkDim > 0.6F && torchDim < darkDim * 0.4F, "dark=" + darkDim + " torch=" + torchDim);

      // ---- Tinnitus: moving, the world is dulled; standing still for two seconds, you hear properly.
      this.clean();
      this.platform();
      this.cmd("tp @a " + (this.px + 0.5) + " 250 " + (this.pz + 0.5) + " 0 0");
      this.giveOnly("tinnitus");
      this.ticks(70);
      float listening = this.ctx.computeOnClient(mc -> ClientProbe.soundDuck());
      this.ctx.getInput().holdKey(o -> o.keyUp);
      this.ticks(8);
      float moving = this.ctx.computeOnClient(mc -> ClientProbe.soundDuck());
      this.ctx.getInput().releaseKey(o -> o.keyUp);
      this.check("tinnitus: dulled while moving (0.65), clear once you stand still", Math.abs(listening - 1.0F) < 0.01F && Math.abs(moving - 0.65F) < 0.01F,
         "still=" + listening + " moving=" + moving);

      // ---- Pregnancy: feed the craving for regeneration; another food does nothing.
      this.clean();
      this.cmd("clear @a");
      this.giveOnly("pregnancy");
      this.server.runOnServer(s -> {
         PlusEffects.of(id).cond.craving = net.minecraft.world.item.Items.COOKED_BEEF;
         PlusEffects.of(id).cond.cravingTimer = 5000;
      });
      this.holdItem("bread");
      this.eatFor(40);
      boolean breadNothing = !this.onPlayer(p -> p.hasEffect(MobEffects.REGENERATION)) && this.cond().craving != null;
      this.onPlayer(p -> {
         p.getFoodData().setFoodLevel(10);
         return null;
      });
      this.holdItem("cooked_beef");
      this.eatFor(45);
      boolean regen = this.hasEffect(MobEffects.REGENERATION, 1) && this.cond().craving == null;
      this.check("pregnancy: the wrong food does nothing, the craved one gives Regeneration II", breadNothing && regen, "breadNothing=" + breadNothing + " regen=" + regen);

      // ---- Hemophilia: paper is a bandage.
      this.clean();
      this.cmd("clear @a");
      this.giveOnly("hemophilia");
      this.cmd("item replace entity @a[limit=1] weapon.mainhand with minecraft:paper 2");
      this.ticks(2);
      String bandaged = this.server.computeOnServer(s -> {
         ServerPlayer p = s.getPlayerList().getPlayers().get(0);
         PlusEffects.of(id).bleedTicks = 0;
         var idle = net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT.invoker().interact(p, p.level(), net.minecraft.world.InteractionHand.MAIN_HAND);
         int keptWhenNotBleeding = p.getMainHandItem().getCount();
         PlusEffects.of(id).bleedTicks = 300;
         var used = net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT.invoker().interact(p, p.level(), net.minecraft.world.InteractionHand.MAIN_HAND);
         return idle + "/" + keptWhenNotBleeding + "/" + used + "/" + p.getMainHandItem().getCount() + "/" + PlusEffects.of(id).bleedTicks;
      });
      this.check("hemophilia: paper does nothing when you are not bleeding, and stops a bleed (one sheet used)", bandaged.matches("Pass.*/2/Success.*/1/0"), bandaged);

      // ---- Kidney stones: a drink hydrates; the stone then passes quickly and painlessly.
      this.clean();
      this.platform();
      this.cmd("clear @a");
      this.giveOnly("kidney_stones");
      int cooldown0 = PlusEffects.of(id).stoneCooldown;
      this.holdItem("potion");
      this.eatFor(50);
      boolean hydrated = PlusEffects.of(id).hydrated > 3000 && PlusEffects.of(id).stoneCooldown > cooldown0;
      this.waitClient(mc -> ClientProbe.agonyTicks() == 0, 60);
      this.server.runOnServer(s -> PlusEffects.of(id).stoneCooldown = 1);
      boolean quick = this.waitClient(mc -> ClientProbe.agonyTicks() > 0, 30);
      int agony = this.ctx.computeOnClient(mc -> ClientProbe.agonyTicks());
      float kh0 = this.onPlayer(ServerPlayer::getHealth);
      this.ticks(60);
      float kh1 = this.onPlayer(ServerPlayer::getHealth);
      this.check("kidney_stones: a drink hydrates and delays; the stone is short (<= 40 ticks) and painless", hydrated && quick && agony <= 40 && kh1 >= kh0,
         "hydrated=" + hydrated + " quick=" + quick + " agony=" + agony + " hp=" + kh0 + "->" + kh1);

      // ---- Lead poisoning: milk flushes it for five minutes.
      this.clean();
      this.cmd("clear @a");
      this.giveOnly("lead_poisoning");
      this.cmd("give @a minecraft:lead 2");
      this.ticks(5);
      float leaden = this.onPlayer(ServerPlayer::getMaxHealth);
      this.cmd("clear @a minecraft:lead");
      this.holdItem("milk_bucket");
      this.eatFor(45);
      boolean flushed = PlusEffects.of(id).chelated > 5000;
      this.cmd("give @a minecraft:lead 2");
      this.ticks(5);
      float flushedHp = this.onPlayer(ServerPlayer::getMaxHealth);
      this.check("lead_poisoning: milk restores full health and makes Leads harmless for a while", Math.abs(leaden - 14.0F) < 0.01F && flushed && Math.abs(flushedHp - 20.0F) < 0.01F,
         "leaden=" + leaden + " flushed=" + flushed + " after=" + flushedHp);
      this.cmd("clear @a");

      // ---- Necrosis: a golden apple pushes it back a stage, an enchanted one cures it.
      this.clean();
      this.platform();
      this.cmd("clear @a");
      this.giveOnly("necrosis");
      this.server.runOnServer(s -> PlusEffects.of(id).necroStage = 2);
      this.ticks(4);
      boolean legsDead = this.hasModifier(Attributes.MOVEMENT_SPEED, "necro_speed");
      this.holdItem("golden_apple");
      this.eatFor(45);
      int stageAfterApple = PlusEffects.of(id).necroStage;
      this.ticks(4);
      boolean legsBack = !this.hasModifier(Attributes.MOVEMENT_SPEED, "necro_speed");
      this.server.runOnServer(s -> PlusEffects.of(id).necroStage = 3);
      this.onPlayer(p -> {
         p.getFoodData().setFoodLevel(10);
         return null;
      });
      this.holdItem("enchanted_golden_apple");
      this.eatFor(45);
      int stageAfterEnchanted = PlusEffects.of(id).necroStage;
      this.check("necrosis: golden apple takes a stage back, enchanted cures it", legsDead && stageAfterApple == 1 && legsBack && stageAfterEnchanted == 0,
         "legsDead=" + legsDead + " afterApple=" + stageAfterApple + " legsBack=" + legsBack + " afterEnchanted=" + stageAfterEnchanted);
      this.cmd("disability remove @a all");
      this.cmd("effect clear @a");
   }


   // ---------------------------------------------------------------- playing with company (1.3.0)

   private void social() {
      UUID id = this.id();
      this.cmd("gamemode survival @a");
      this.clean();
      this.platform();
      this.cmd("clear @a");
      this.cmd("disability remove @a all");

      // ---- Feeding: a friend with a cared-for condition can be fed; a healthy one is left alone.
      this.giveOnly("anemia");
      this.onPlayer(p -> {
         p.getFoodData().setFoodLevel(10);
         return null;
      });
      String fedAnemic = this.server.computeOnServer(s -> {
         ServerPlayer real = s.getPlayerList().getPlayers().get(0);
         var fake = net.fabricmc.fabric.api.entity.FakePlayer.get(real.level());
         fake.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COOKED_BEEF, 2));
         var result = net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.invoker()
            .interact(fake, real.level(), net.minecraft.world.InteractionHand.MAIN_HAND, real, null);
         return result + "/" + fake.getMainHandItem().getCount();
      });
      this.ticks(3);
      boolean thickened = PlusEffects.of(id).ironTicks > 0;
      this.check("social: feeding an anemic friend red meat thickens their blood and uses one", fedAnemic.matches("Success.*/1") && thickened,
         "result=" + fedAnemic + " iron=" + thickened);
      this.cmd("disability remove @a all");
      this.ticks(3);
      String fedHealthy = this.server.computeOnServer(s -> {
         ServerPlayer real = s.getPlayerList().getPlayers().get(0);
         var fake = net.fabricmc.fabric.api.entity.FakePlayer.get(real.level());
         fake.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COOKED_BEEF, 2));
         var result = net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.invoker()
            .interact(fake, real.level(), net.minecraft.world.InteractionHand.MAIN_HAND, real, null);
         return result + "/" + fake.getMainHandItem().getCount();
      });
      this.check("social: a friend without such a condition is not force-fed", fedHealthy.matches("Pass.*/2"), fedHealthy);

      // ---- Feeding a pregnant friend their craving; a quad amputee can only eat this way.
      this.giveOnly("pregnancy");
      this.server.runOnServer(s -> {
         PlusEffects.of(id).cond.craving = net.minecraft.world.item.Items.COOKED_SALMON;
         PlusEffects.of(id).cond.cravingTimer = 5000;
         s.getPlayerList().getPlayers().get(0).getFoodData().setFoodLevel(10);
      });
      this.server.runOnServer(s -> {
         ServerPlayer real = s.getPlayerList().getPlayers().get(0);
         var fake = net.fabricmc.fabric.api.entity.FakePlayer.get(real.level());
         fake.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COOKED_SALMON, 1));
         net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.invoker()
            .interact(fake, real.level(), net.minecraft.world.InteractionHand.MAIN_HAND, real, null);
      });
      this.ticks(4);
      boolean craved = this.hasEffect(MobEffects.REGENERATION, 1) && this.cond().craving == null;
      this.check("social: feeding the craved food to a pregnant friend gives them Regeneration II", craved, "");

      this.cmd("disability remove @a all");
      this.giveOnly("quadruple_amputee");
      this.onPlayer(p -> {
         p.getFoodData().setFoodLevel(10);
         return null;
      });
      this.server.runOnServer(s -> {
         ServerPlayer real = s.getPlayerList().getPlayers().get(0);
         var fake = net.fabricmc.fabric.api.entity.FakePlayer.get(real.level());
         fake.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COOKED_BEEF, 1));
         net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.invoker()
            .interact(fake, real.level(), net.minecraft.world.InteractionHand.MAIN_HAND, real, null);
      });
      this.ticks(3);
      int quadFood = this.onPlayer(p -> p.getFoodData().getFoodLevel());
      this.check("social: a quadruple amputee can be fed by a friend (10 -> 18)", quadFood >= 17, "food=" + quadFood);

      // ---- Carrying: a friend with empty hands lifts a quad amputee, who then follows their back until they crouch.
      String carried = this.server.computeOnServer(s -> {
         ServerPlayer real = s.getPlayerList().getPlayers().get(0);
         var level = real.level();
         var fake = net.fabricmc.fabric.api.entity.FakePlayer.get(level, new com.mojang.authlib.GameProfile(UUID.randomUUID(), "Carrier"));
         fake.setPos(real.getX() + 3.0, real.getY(), real.getZ());
         fake.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
         level.addNewPlayer(fake);
         try {
            var result = net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.invoker()
               .interact(fake, level, net.minecraft.world.InteractionHand.MAIN_HAND, real, null);
            boolean taken = fake.getUUID().equals(PlusEffects.of(id).carriedBy);
            PlusSocial.updateCarried(real, level);
            double dist = Math.hypot(real.getX() - fake.getX(), real.getZ() - fake.getZ());
            boolean high = real.getY() > fake.getY() + 0.3;
            fake.setShiftKeyDown(true);
            PlusSocial.updateCarried(real, level);
            boolean released = PlusEffects.of(id).carriedBy == null;
            return result + "/" + taken + "/" + (dist < 0.6) + "/" + high + "/" + released;
         } finally {
            fake.setShiftKeyDown(false);
            level.removePlayerImmediately(fake, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
         }
      });
      this.check("social: a friend lifts a quadruple amputee onto their back, carries them, and puts them down when crouching",
         carried.matches("Success.*/true/true/true/true"), carried);
      this.cmd("disability remove @a all");
      this.ticks(3);

      // ---- Bandaging and patching friends.
      this.clean();
      this.giveOnly("hemophilia");
      String bandaged = this.server.computeOnServer(s -> {
         ServerPlayer real = s.getPlayerList().getPlayers().get(0);
         var fake = net.fabricmc.fabric.api.entity.FakePlayer.get(real.level());
         PlusEffects.of(id).bleedTicks = 300;
         fake.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.PAPER, 2));
         var result = net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.invoker()
            .interact(fake, real.level(), net.minecraft.world.InteractionHand.MAIN_HAND, real, null);
         return result + "/" + PlusEffects.of(id).bleedTicks + "/" + fake.getMainHandItem().getCount();
      });
      this.check("social: a friend can bandage your bleeding with paper", bandaged.matches("Success.*/0/1"), bandaged);

      this.clean();
      this.giveOnly("glass_cannon");
      this.server.runOnServer(s -> {
         PlusEffects.of(id).cracks = 2;
         PlusEffects.of(id).crackTicks = 3000;
      });
      this.ticks(3);
      float crackedHp = this.onPlayer(ServerPlayer::getMaxHealth);
      String patched = this.server.computeOnServer(s -> {
         ServerPlayer real = s.getPlayerList().getPlayers().get(0);
         var fake = net.fabricmc.fabric.api.entity.FakePlayer.get(real.level());
         fake.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GLASS, 2));
         var result = net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.invoker()
            .interact(fake, real.level(), net.minecraft.world.InteractionHand.MAIN_HAND, real, null);
         return result + "/" + PlusEffects.of(id).cracks + "/" + fake.getMainHandItem().getCount();
      });
      this.ticks(3);
      float patchedHp = this.onPlayer(ServerPlayer::getMaxHealth);
      this.check("social: a friend can patch one crack with a block of glass (+1 heart back)", patched.matches("Success.*/1/1") && patchedHp > crackedHp + 1.9F,
         patched + " " + crackedHp + "->" + patchedHp);

      // ---- Shared effects that need another player in the level: the stomp launch, the hoarder cover, the nearby lookup.
      this.clean();
      this.platform();
      String shared = this.server.computeOnServer(s -> {
         ServerPlayer real = s.getPlayerList().getPlayers().get(0);
         var level = real.level();
         var fake = net.fabricmc.fabric.api.entity.FakePlayer.get(level, new com.mojang.authlib.GameProfile(UUID.randomUUID(), "Friend"));
         fake.setPos(real.getX() + 2.0, real.getY(), real.getZ());
         level.addNewPlayer(fake);
         try {
            boolean near = PlusSocial.nearbyPlayers(level, real, 4.0).contains(fake);
            boolean farExcluded = PlusSocial.nearbyPlayers(level, real, 1.0).isEmpty();
            float fakeHp = fake.getHealth();
            PlusEffects.stomp(real, level, 8.0F);
            double launched = fake.getDeltaMovement().y;
            boolean unhurt = fake.getHealth() >= fakeHp;
            PlusEffects.of(fake.getUUID()).hoardArmor = 6.0;
            PlusSocial.applyHoarderCover(java.util.List.of(real, fake));
            double cover = real.getAttributeValue(Attributes.ARMOR);
            PlusEffects.of(fake.getUUID()).hoardArmor = 0.0;
            PlusSocial.applyHoarderCover(java.util.List.of(real, fake));
            double after = real.getAttributeValue(Attributes.ARMOR);
            return near + "/" + farExcluded + "/" + (launched > 0.8) + "/" + unhurt + "/" + cover + "/" + after;
         } finally {
            level.removePlayerImmediately(fake, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
         }
      });
      this.check("social: nearby lookup, stomp launches (not hurts) a friend, a hoarder's cover gives them +3 armor and then lifts",
         shared.equals("true/true/true/true/3.0/0.0"), shared);
      this.cmd("disability remove @a all");
      this.cmd("effect clear @a");
   }

   /** Lands from wherever the player was teleported to, and returns how high above the platform they get afterwards. */
   private double bounceHeightAfterLanding() {
      boolean landed = false;
      double best = 0.0;
      for (int i = 0; i < 160; i++) {
         this.ticks(1);
         double y = this.onPlayer(p -> p.getY());
         if (!landed && y <= 250.05) {
            landed = true;
            this.ticks(2);
            continue;
         }

         if (landed) {
            best = Math.max(best, y - 250.0);
            if (i > 60 && best > 0.0 && y <= 250.05) {
               break;
            }
         }
      }

      return best;
   }

   // ---------------------------------------------------------------- five real conditions about people and bodies (1.5.0)

   private net.fabricmc.fabric.api.entity.FakePlayer friend(ServerPlayer real, String name) {
      return net.fabricmc.fabric.api.entity.FakePlayer.get(real.level(), new com.mojang.authlib.GameProfile(UUID.randomUUID(), name));
   }

   private void bodies() {
      UUID id = this.id();
      String inert = "{NoAI:1b,Silent:1b,PersistenceRequired:1b}";
      this.cmd("gamemode survival @a");
      this.clean();
      this.platform();
      this.cmd("clear @a");
      this.cmd("kill @e[type=!minecraft:player]");
      this.cmd("disability remove @a all");

      // ---- Social anxiety: who is watching you, what a friend, a helmet and a pumpkin do, and the stages of the meter.
      this.giveOnly("social_anxiety");
      String gaze = this.server.computeOnServer(s -> {
         ServerPlayer real = s.getPlayerList().getPlayers().get(0);
         var level = real.level();
         var watcher = this.friend(real, "Watcher");
         watcher.setPos(real.getX(), real.getY(), real.getZ() + 8.0);
         watcher.setXRot(0.0F);
         level.addNewPlayer(watcher);
         try {
            watcher.setYRot(180.0F);
            watcher.setYHeadRot(180.0F);
            int facing = PlusAnxiety.scan(real, level).watchers();
            watcher.setYRot(0.0F);
            watcher.setYHeadRot(0.0F);
            int away = PlusAnxiety.scan(real, level).watchers();
            watcher.setYRot(180.0F);
            watcher.setYHeadRot(180.0F);
            real.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_HELMET));
            int helmet = PlusAnxiety.scan(real, level).watchers();
            real.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CARVED_PUMPKIN));
            int pumpkin = PlusAnxiety.scan(real, level).watchers();
            real.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.item.ItemStack.EMPTY);
            watcher.setPos(real.getX(), real.getY(), real.getZ() + 1.5);
            var close = PlusAnxiety.scan(real, level);
            return facing + "/" + away + "/" + helmet + "/" + pumpkin + "/" + close.watchers() + "/" + close.comfort();
         } finally {
            level.removePlayerImmediately(watcher, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
         }
      });
      this.check("social_anxiety: a player facing you counts, one looking away does not, a helmet halves it (rounded up, so one watcher stays one), a pumpkin hides you, a close friend is comfort",
         gaze.equals("1/0/1/0/0/true"), gaze);
      String stages = this.server.computeOnServer(s -> {
         ServerPlayer real = s.getPlayerList().getPlayers().get(0);
         var level = real.level();
         var fx = PlusEffects.of(real.getUUID());
         var watchers = new net.fabricmc.fabric.api.entity.FakePlayer[3];
         for (int i = 0; i < 3; i++) {
            watchers[i] = this.friend(real, "Crowd" + i);
            watchers[i].setPos(real.getX() + i, real.getY(), real.getZ() + 8.0);
            watchers[i].setXRot(0.0F);
            watchers[i].setYRot(180.0F);
            watchers[i].setYHeadRot(180.0F);
            level.addNewPlayer(watchers[i]);
         }

         int saved = real.tickCount;
         try {
            fx.anxiety.meter = 0.0F;
            fx.anxiety.stage = 0;
            fx.anxiety.freezeCooldown = 0;
            StringBuilder out = new StringBuilder();
            for (int second = 1; second <= 12; second++) {
               real.tickCount = 20 * second;
               PlusAnxiety.tick(real, level, fx);
               if (second == 3) {
                  out.append(fx.anxiety.stage).append('/').append(Math.round(fx.anxiety.meter)).append('/');
               }
            }

            boolean slowed = real.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS);
            out.append(fx.anxiety.stage).append('/').append(slowed).append('/').append(fx.anxiety.freezeCooldown > 0 ? "froze" : "calm");
            return out.toString();
         } finally {
            real.tickCount = saved;
            fx.anxiety.meter = 0.0F;
            for (var w : watchers) {
               level.removePlayerImmediately(w, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            }
         }
      });
      this.check("social_anxiety: three watchers build 9 a second: calm at 3 s (27), then nervous, panicked, slowed, and frozen up by 10 s", stages.equals("0/27/2/true/froze"), stages);
      this.cmd("disability remove @a all");
      this.ticks(2);
      this.giveOnly("social_anxiety");
      this.ctx.runOnClient(mc -> ClientProbe.setAnxiety(70));
      boolean lockedOut = !this.ctx.computeOnClient(mc -> ClientProbe.chatAllowed("hello everyone"));
      this.ticks(5);
      this.ctx.takeScreenshot("social_anxiety_panic");
      this.ctx.runOnClient(mc -> ClientProbe.setAnxiety(10));
      boolean canSpeak = this.ctx.computeOnClient(mc -> ClientProbe.chatAllowed("hello everyone"));
      this.ctx.runOnClient(mc -> ClientProbe.setAnxiety(0));
      this.check("social_anxiety: panicking you cannot send chat, calm you can", lockedOut && canSpeak, "locked=" + lockedOut + " canSpeak=" + canSpeak);
      this.cmd("disability remove @a all");
      this.ticks(3);

      // ---- Alien hand: it slaps what is nearest (gently when it has something to hold), drags you to people, swaps your hands.
      this.clean();
      this.platform();
      this.cmd("summon minecraft:cow " + (this.px + 1.5) + " 250 " + (this.pz + 0.5) + " " + inert);
      this.ticks(3);
      this.giveOnly("alien_hand");
      float slapEmpty = this.server.computeOnServer(s -> {
         ServerPlayer p = s.getPlayerList().getPlayers().get(0);
         var cow = p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(6.0), m -> kindOf(m).equals("cow")).get(0);
         float before = cow.getHealth();
         PlusAlienHand.act(p, p.level(), PlusAlienHand.Action.SLAP);
         return before - cow.getHealth();
      });
      this.ticks(25);
      this.cmd("item replace entity @a[limit=1] weapon.offhand with minecraft:torch 1");
      this.ticks(2);
      float slapBusy = this.server.computeOnServer(s -> {
         ServerPlayer p = s.getPlayerList().getPlayers().get(0);
         var cow = p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(6.0), m -> kindOf(m).equals("cow")).get(0);
         float before = cow.getHealth();
         PlusAlienHand.act(p, p.level(), PlusAlienHand.Action.SLAP);
         return before - cow.getHealth();
      });
      this.check("alien_hand: a slap hits the nearest creature for 2, and for 1 when the offhand is holding something", Math.abs(slapEmpty - 2.0F) < 0.01F && Math.abs(slapBusy - 1.0F) < 0.01F,
         "empty=" + slapEmpty + " busy=" + slapBusy);
      String swapped = this.server.computeOnServer(s -> {
         ServerPlayer p = s.getPlayerList().getPlayers().get(0);
         p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK));
         p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.TORCH));
         PlusAlienHand.act(p, p.level(), PlusAlienHand.Action.SWAP);
         return p.getMainHandItem().getItem() == net.minecraft.world.item.Items.TORCH && p.getOffhandItem().getItem() == net.minecraft.world.item.Items.STICK ? "swapped" : "no";
      });
      String pulled = this.server.computeOnServer(s -> {
         ServerPlayer real = s.getPlayerList().getPlayers().get(0);
         var level = real.level();
         var target = this.friend(real, "Pulled");
         target.setPos(real.getX() + 6.0, real.getY(), real.getZ());
         level.addNewPlayer(target);
         try {
            real.setDeltaMovement(0.0, 0.0, 0.0);
            PlusAlienHand.act(real, level, PlusAlienHand.Action.PULL);
            return real.getDeltaMovement().x > 0.5 ? "toward" : "no:" + real.getDeltaMovement().x;
         } finally {
            level.removePlayerImmediately(target, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
         }
      });
      this.check("alien_hand: it swaps your hands and drags you toward the nearest person", swapped.equals("swapped") && pulled.equals("toward"), swapped + " " + pulled);
      String gaps = this.server.computeOnServer(s -> {
         ServerPlayer p = s.getPlayerList().getPlayers().get(0);
         var fx = PlusEffects.of(p.getUUID());
         p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, net.minecraft.world.item.ItemStack.EMPTY);
         fx.alien.timer = 0;
         PlusAlienHand.tick(p, p.level(), fx);
         int open = fx.alien.timer;
         p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.TORCH));
         fx.alien.timer = 0;
         PlusAlienHand.tick(p, p.level(), fx);
         int held = fx.alien.timer;
         return open + "/" + held;
      });
      String[] g = gaps.split("/");
      this.check("alien_hand: with something to hold, the hand acts three times as seldom (400-1200 ticks vs 1200-3600)",
         Integer.parseInt(g[0]) >= 400 && Integer.parseInt(g[0]) < 1200 && Integer.parseInt(g[1]) >= 1200, gaps);
      this.cmd("kill @e[type=!minecraft:player]");
      this.cmd("disability remove @a all");
      this.cmd("clear @a");
      this.ticks(3);

      // ---- Cataplexy: laughter, finds and kills drop you; crouching braces; the knees stay buckled four seconds.
      this.clean();
      this.platform();
      this.giveOnly("cataplexy");
      String floored = this.server.computeOnServer(s -> {
         ServerPlayer p = s.getPlayerList().getPlayers().get(0);
         var level = p.level();
         var fx = PlusEffects.of(p.getUUID());
         fx.cata.collapse = 0;
         fx.cata.cooldown = 0;
         p.setShiftKeyDown(false);
         PlusCataplexy.onChat(s, "that was so funny lol");
         boolean laughed = fx.cata.collapse > 0 && p.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS);
         var cow = level.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(10.0));
         boolean handsDead = net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT.invoker().interact(p, level, net.minecraft.world.InteractionHand.MAIN_HAND)
            == net.minecraft.world.InteractionResult.FAIL;
         boolean noRepeat = !PlusCataplexy.trigger(p, "again");
         fx.cata.collapse = 0;
         fx.cata.cooldown = 0;
         PlusCataplexy.onChat(s, "good morning everyone");
         boolean calmChat = fx.cata.collapse == 0;
         PlusCataplexy.onFind(p, net.minecraft.world.level.block.Blocks.DIAMOND_ORE.defaultBlockState());
         boolean found = fx.cata.collapse > 0;
         fx.cata.collapse = 0;
         fx.cata.cooldown = 0;
         p.setShiftKeyDown(true);
         PlusCataplexy.onChat(s, "LMAO");
         boolean braced = fx.cata.collapse == 0 && fx.cata.cooldown > 0;
         p.setShiftKeyDown(false);
         fx.cata.collapse = 0;
         fx.cata.cooldown = 0;
         return laughed + "/" + handsDead + "/" + noRepeat + "/" + calmChat + "/" + found + "/" + braced;
      });
      this.check("cataplexy: laughter drops you and your hands go dead, nothing else in chat does, a find does, and crouched you only wobble",
         floored.equals("true/true/true/true/true/true"), floored);
      this.server.runOnServer(s -> PlusCataplexy.onChat(s, "rofl"));
      boolean sawIt = this.waitClient(mc -> ClientProbe.collapseTicks() > 0, 20);
      this.ctx.takeScreenshot("cataplexy_collapse");
      this.check("cataplexy: the client is told and drops you", sawIt, "");
      this.server.runOnServer(s -> {
         var fx = PlusEffects.of(id);
         PlusCataplexy.killOdds = 1;
         fx.cata.collapse = 0;
         fx.cata.cooldown = 0;
      });
      this.cmd("effect clear @a");
      this.ticks(90);
      this.cmd("summon minecraft:husk " + (this.px + 3.5) + " 250 " + (this.pz + 0.5) + " " + inert);
      this.ticks(3);
      boolean killed = this.server.computeOnServer(s -> {
         ServerPlayer p = s.getPlayerList().getPlayers().get(0);
         var level = p.level();
         var husk = level.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(10.0), m -> kindOf(m).equals("husk")).get(0);
         p.setShiftKeyDown(false);
         PlusEffects.of(p.getUUID()).cata.collapse = 0;
         PlusEffects.of(p.getUUID()).cata.cooldown = 0;
         husk.hurtServer(level, level.damageSources().playerAttack(p), 1000.0F);
         return PlusEffects.of(p.getUUID()).cata.collapse > 0;
      });
      this.server.runOnServer(s -> PlusCataplexy.killOdds = 3);
      this.check("cataplexy: the rush of killing a monster can drop you", killed, "");
      this.cmd("kill @e[type=!minecraft:player]");
      this.cmd("disability remove @a all");
      this.cmd("effect clear @a");
      this.ticks(3);

      // ---- Insensitivity to pain: no health bar, unfelt wounds, friends warned, checked over by a friend or yourself.
      this.clean();
      this.platform();
      this.giveOnly("pain_insensitivity");
      this.ticks(5);
      this.ctx.takeScreenshot("pain_insensitivity_no_health_bar");
      String wounds = this.server.computeOnServer(s -> {
         ServerPlayer p = s.getPlayerList().getPlayers().get(0);
         var level = p.level();
         var fx = PlusEffects.of(p.getUUID());
         fx.numb.wound = 0;
         fx.numb.checkCooldown = 0;
         p.setHealth(20.0F);
         p.hurtServer(level, level.damageSources().generic(), 2.0F);
         int light = fx.numb.wound;
         fx.numb.wound = 0;
         PlusNumbness.afterDamage(p, 4.0F);
         int hard = fx.numb.wound;
         fx.numb.wound = 41;
         float before = p.getHealth();
         PlusNumbness.tick(p, level, fx);
         float lost = before - p.getHealth();
         return light + "/" + hard + "/" + lost;
      });
      this.check("pain_insensitivity: a light hit leaves no wound, a hard one leaves a 10 second wound, and a wound keeps hurting", wounds.equals("0/200/1.0"), wounds);
      String checks = this.server.computeOnServer(s -> {
         ServerPlayer real = s.getPlayerList().getPlayers().get(0);
         var level = real.level();
         var fx = PlusEffects.of(real.getUUID());
         var doctor = this.friend(real, "Doctor");
         fx.numb.wound = 300;
         fx.numb.checkCooldown = 0;
         doctor.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
         var result = net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.invoker()
            .interact(doctor, level, net.minecraft.world.InteractionHand.MAIN_HAND, real, null);
         boolean treated = fx.numb.wound == 0;
         boolean again = PlusNumbness.examine(doctor, real);
         fx.numb.checkCooldown = 0;
         fx.numb.wound = 120;
         real.setShiftKeyDown(true);
         fx.numb.still.lastX = real.getX();
         fx.numb.still.lastZ = real.getZ();
         for (int i = 0; i < 62; i++) {
            PlusNumbness.tick(real, level, fx);
         }

         real.setShiftKeyDown(false);
         boolean self = fx.numb.wound == 0 && fx.numb.checkCooldown > 0;
         return result + "/" + treated + "/" + again + "/" + self;
      });
      this.check("pain_insensitivity: a friend's empty-hand check treats the wound (once per cooldown), and crouching still checks you yourself",
         checks.matches("Success.*/true/false/true"), checks);
      String warned = this.server.computeOnServer(s -> {
         ServerPlayer real = s.getPlayerList().getPlayers().get(0);
         var level = real.level();
         var near = this.friend(real, "Near");
         near.setPos(real.getX() + 5.0, real.getY(), real.getZ());
         var far = this.friend(real, "Far");
         far.setPos(real.getX() + 60.0, real.getY(), real.getZ());
         level.addNewPlayer(near);
         level.addNewPlayer(far);
         try {
            var told = PlusNumbness.warnFriends(real, level);
            return told.size() + "/" + told.contains(near) + "/" + told.contains(far);
         } finally {
            level.removePlayerImmediately(near, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            level.removePlayerImmediately(far, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
         }
      });
      this.check("pain_insensitivity: friends within 30 blocks are told you are hurt, distant ones are not", warned.equals("1/true/false"), warned);
      this.cmd("disability remove @a all");
      this.ticks(3);

      // ---- Ehlers-Danlos: hard hits and falls dislocate an arm or a leg; a friend or six seconds of crouching fixes it.
      this.clean();
      this.platform();
      this.giveOnly("ehlers_danlos");
      this.server.runOnServer(s -> {
         PlusJoints.forcedLimb = PlusJoints.ARM;
         s.getPlayerList().getPlayers().get(0).hurtServer(s.getPlayerList().getPlayers().get(0).level(),
            s.getPlayerList().getPlayers().get(0).level().damageSources().generic(), 3.0F);
      });
      this.ticks(3);
      boolean lightIsFine = PlusEffects.of(id).joints.limb == PlusJoints.NONE;
      this.ticks(25);
      this.server.runOnServer(s -> s.getPlayerList().getPlayers().get(0).hurtServer(s.getPlayerList().getPlayers().get(0).level(),
         s.getPlayerList().getPlayers().get(0).level().damageSources().generic(), 5.0F));
      this.ticks(4);
      boolean armOut = PlusEffects.of(id).joints.limb == PlusJoints.ARM && this.hasModifier(Attributes.ATTACK_DAMAGE, "joint_arm_attack")
         && this.hasModifier(Attributes.BLOCK_BREAK_SPEED, "joint_arm_mine") && !this.hasModifier(Attributes.MOVEMENT_SPEED, "joint_leg_speed");
      this.check("ehlers_danlos: a light hit is fine, a hard one dislocates the arm (attack and mining nearly gone)", lightIsFine && armOut, "light=" + lightIsFine + " arm=" + armOut);
      String fixed = this.server.computeOnServer(s -> {
         ServerPlayer real = s.getPlayerList().getPlayers().get(0);
         var level = real.level();
         var fx = PlusEffects.of(real.getUUID());
         var helper = this.friend(real, "Helper");
         helper.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
         real.setHealth(20.0F);
         var result = net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.invoker()
            .interact(helper, level, net.minecraft.world.InteractionHand.MAIN_HAND, real, null);
         boolean back = fx.joints.limb == PlusJoints.NONE;
         float cost = 20.0F - real.getHealth();
         // then alone, with a leg out: crouch still for six seconds
         fx.joints.limb = PlusJoints.LEG;
         real.setHealth(20.0F);
         real.setShiftKeyDown(true);
         fx.joints.still.lastX = real.getX();
         fx.joints.still.lastZ = real.getZ();
         int ticksNeeded = 0;
         while (fx.joints.limb != PlusJoints.NONE && ticksNeeded < 200) {
            PlusJoints.tick(real, level, fx);
            ticksNeeded++;
         }

         real.setShiftKeyDown(false);
         float alone = 20.0F - real.getHealth();
         // and a long fall dislocates a joint too
         fx.joints.limb = PlusJoints.NONE;
         fx.landedFall = 4.0F;
         PlusJoints.tick(real, level, fx);
         boolean fell = fx.joints.limb != PlusJoints.NONE;
         fx.landedFall = 0.0F;
         fx.joints.limb = PlusJoints.NONE;
         return result + "/" + back + "/" + cost + "/" + ticksNeeded + "/" + alone + "/" + fell;
      });
      this.check("ehlers_danlos: a friend pops it back (1 damage); alone it takes six seconds of crouching still and costs 2; a 4-block fall dislocates too",
         fixed.matches("Success.*/true/1.0/120/2.0/true"), fixed);
      this.cmd("disability remove @a all");
      this.server.runOnServer(s -> PlusJoints.forcedLimb = PlusJoints.NONE);
      this.cmd("effect clear @a");
   }
}
