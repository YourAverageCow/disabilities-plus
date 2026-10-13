package com.disabilitiesplus.client;

import com.disabilitiesplus.PlusDisability;
import com.studiobing.disabilities.Disability;
import com.studiobing.disabilities.client.ClientState;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public final class PlusClientEffects {
    private static final Random RNG = new Random();
    private static final String[] LOUD_SOUNDS = new String[]{"explode", "thunder", "anvil", "firework", "sonic_boom", "dragon"};
    private static final double LOUD_RADIUS = 32.0;
    private static final int OVERLOAD_TICKS = 120;
    private static final double STEP_SQUEAK_DISTANCE = 1.5;
    private static final String[] CHEWING_SOUNDS = new String[]{"generic.eat", "generic.drink"};
    private static final double CHEWING_RADIUS = 16.0;
    private static final double KICK_STRENGTH = 0.5;

    static int kickOdds = 600;
    static int blabOdds = 2400;
    static int paranoiaOdds = 200;
    private static final SoundEvent[] STEPS = new SoundEvent[]{SoundEvents.GRASS_STEP, SoundEvents.STONE_STEP, SoundEvents.WOOD_STEP, SoundEvents.GRAVEL_STEP};
    private static final int LID_CLOSE_TICKS = 600;

    public static final int CHAT_LOCK_ANXIETY = com.disabilitiesplus.MindRules.ANXIETY_PANIC;
    private static final float COLLAPSE_PITCH = 55.0F;

    private static final java.util.Set<Item> LIGHT_ITEMS = java.util.Set.of(
        Items.TORCH, Items.SOUL_TORCH, Items.LANTERN,
        Items.SOUL_LANTERN, Items.GLOWSTONE, Items.SEA_LANTERN,
        Items.JACK_O_LANTERN, Items.SHROOMLIGHT, Items.REDSTONE_TORCH,
        Items.CAMPFIRE, Items.SOUL_CAMPFIRE, Items.GLOW_BERRIES
    );
    private static final float HELD_LIGHT_FACTOR = 0.2F;
    private static final double FRIEND_LIGHT_RANGE = 6.0;
    private static final double COMPANY_RANGE = 3.0;

    private static final int STILL_TICKS_TO_LISTEN = 40;
    private static final int ECHOES = 3;
    private static final int ECHO_GAP_TICKS = 4;

    private PlusClientEffects() {
    }

    public static void tick(Minecraft mc) {
        if (mc.player == null || mc.isPaused()) {
            return;
        }

        if (PlusClientState.cpMoveTicks > 0) {
            PlusClientState.cpMoveTicks--;
        }

        stillnessTick(mc);
        nightBlindnessTick(mc);
        migraineTick();
        tinnitusTick(mc);
        amnesiaTick(mc);
        ataxiaTick(mc);
        overloadTick();
        confusionTick();
        jerkTick(mc);
        pogoTick(mc);
        squeakTick(mc);
        pregnancyTick(mc);
        paranoiaTick(mc);
        reverseTick();
        blinkTick(mc);
        blabTick(mc);
        episodeTick(mc);
        echoTick(mc);
        FearEffects.tick(mc);
        PlusClientState.soundReactive = PlusClientState.has(PlusDisability.AUTISM)
            || PlusClientState.has(PlusDisability.JUMPY)
            || PlusClientState.has(PlusDisability.PTSD)
            || PlusClientState.has(PlusDisability.IBS)
            || PlusClientState.has(PlusDisability.MISOPHONIA);
        PlusClientState.soundDuck = (ringing() ? 0.65F : 1.0F)
           * (PlusClientState.migraineTicks > 0 ? 0.45F : 1.0F)
           * (PlusClientState.overloadTicks > 0 ? 0.35F : 1.0F)
           * (PlusClientState.flashbackTicks > 0 ? 0.35F : 1.0F);
    }

    private static boolean holdingLight(Minecraft mc) {
        if (carriesLight(mc.player)) {
            return true;
        }

        for (Player other : mc.level.players()) {
            if (other != mc.player && other.distanceToSqr(mc.player) <= FRIEND_LIGHT_RANGE * FRIEND_LIGHT_RANGE && carriesLight(other)) {
                return true;
            }
        }

        return false;
    }

    private static boolean carriesLight(Player player) {
        return LIGHT_ITEMS.contains(player.getMainHandItem().getItem()) || LIGHT_ITEMS.contains(player.getOffhandItem().getItem());
    }

    private static void nightBlindnessTick(Minecraft mc) {
        float target = 0.0F;
        if (mc.level != null && PlusClientState.has(PlusDisability.NIGHT_BLINDNESS)) {
            int light = mc.level.getMaxLocalRawBrightness(BlockPos.containing(mc.player.getEyePosition()));
            target = Mth.clamp((11.0F - light) / 7.0F, 0.0F, 1.0F) * 0.8F;
            if (holdingLight(mc)) {
                target *= HELD_LIGHT_FACTOR;
            }
        }

        PlusClientState.nightDim += (target - PlusClientState.nightDim) * 0.1F;
        if (PlusClientState.nightDim < 0.01F) {
            PlusClientState.nightDim = 0.0F;
        }
    }

    private static void stillnessTick(Minecraft mc) {
        double moved = Math.hypot(mc.player.getX() - PlusClientState.stillX, mc.player.getZ() - PlusClientState.stillZ);
        PlusClientState.stillX = mc.player.getX();
        PlusClientState.stillZ = mc.player.getZ();
        PlusClientState.stillTicks = moved < 0.01 && !mc.player.isFallFlying() ? PlusClientState.stillTicks + 1 : 0;
        boolean company = false;
        for (Player other : mc.level.players()) {
            company |= other != mc.player && !other.isSpectator() && other.distanceToSqr(mc.player) <= COMPANY_RANGE * COMPANY_RANGE;
        }

        PlusClientState.companyNearby = company;
    }

    private static void migraineTick() {
        if (!PlusClientState.has(PlusDisability.MIGRAINE)) {
            PlusClientState.migraineTicks = 0;
        } else if (PlusClientState.migraineTicks > 0) {
            PlusClientState.migraineTicks--;
        } else if (RNG.nextInt(3600) == 0) {
            PlusClientState.migraineTicks = 240 + RNG.nextInt(160);
        }
    }

    private static void tinnitusTick(Minecraft mc) {
        if (mc.level == null || !ringing() || ClientState.has(Disability.DEAF)) {
            PlusClientState.tinnitusTimer = 0;
        } else if (++PlusClientState.tinnitusTimer >= 30) {
            PlusClientState.tinnitusTimer = 0;
            mc.level
                .playSound(
                    mc.player, mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.AMBIENT, 0.18F, 2.0F
                );
        }
    }

    private static boolean ringing() {
        return PlusClientState.has(PlusDisability.TINNITUS) && PlusClientState.stillTicks < STILL_TICKS_TO_LISTEN && !PlusClientState.companyNearby;
    }

    private static int nextAmnesiaWipe() {
        return 1200 + RNG.nextInt(1800);
    }

    private static void amnesiaTick(Minecraft mc) {
        if (!PlusClientState.has(PlusDisability.AMNESIA)) {
            PlusClientState.amnesiaTimer = -1;
        } else if (PlusClientState.amnesiaTimer < 0) {
            PlusClientState.amnesiaTimer = nextAmnesiaWipe();
        } else if (--PlusClientState.amnesiaTimer == 0) {
            PlusClientState.amnesiaTimer = nextAmnesiaWipe();
            mc.gui.hud.getChat().clearMessages(false);
        }
    }

    private static void ataxiaTick(Minecraft mc) {
        if (PlusClientState.has(PlusDisability.ATAXIA)
            && PlusClientState.cpMoveTicks == 0
            && mc.gui.screen() == null
            && !ClientState.isSeizing()
            && !ClientState.isAsleep()
            && (mc.options.keyUp.isDown() || mc.options.keyDown.isDown())
            && RNG.nextInt(90) == 0) {
            PlusClientState.cpMoveDir = 2 + RNG.nextInt(2);
            PlusClientState.cpMoveTicks = 6 + RNG.nextInt(6);
        }
    }

    private static void overloadTick() {
        if (!PlusClientState.has(PlusDisability.AUTISM)) {
            PlusClientState.overloadTicks = 0;
        } else if (PlusClientState.overloadTicks > 0) {
            PlusClientState.overloadTicks--;
        }
    }

    private static boolean matchesAny(String path, String[] needles) {
        for (String needle : needles) {
            if (path.contains(needle)) {
                return true;
            }
        }

        return false;
    }

    public static void onSoundPlayed(String path, double x, double y, double z) {
        Minecraft mc = Minecraft.getInstance();
        if (PlusClientState.soundReactive && !PlusClientState.echoing && mc.player != null) {
            double distSq = mc.player.distanceToSqr(x, y, z);
            if (distSq <= LOUD_RADIUS * LOUD_RADIUS && matchesAny(path, LOUD_SOUNDS)) {
                if (PlusClientState.has(PlusDisability.AUTISM)) {
                    PlusClientState.overloadTicks = OVERLOAD_TICKS;
                }

                if (PlusClientState.has(PlusDisability.JUMPY) && mc.gui.screen() == null) {
                    startle(mc);
                }

                if (PlusClientState.has(PlusDisability.PTSD)) {
                    FearEffects.startFlashback(mc);
                }
            }

            if (PlusClientState.has(PlusDisability.IBS) && distSq <= 9.0 && matchesAny(path, CHEWING_SOUNDS) && RNG.nextInt(100) < 40) {
                FearEffects.cramp(60);
            }

            if (PlusClientState.has(PlusDisability.MISOPHONIA)
                && distSq <= CHEWING_RADIUS * CHEWING_RADIUS
                && matchesAny(path, CHEWING_SOUNDS)
                && PlusClientState.echoesLeft == 0) {
                PlusClientState.echoesLeft = ECHOES;
                PlusClientState.echoDelay = ECHO_GAP_TICKS;
            }
        }
    }

    static void startle(Minecraft mc) {
        PlusClientState.cpJump = true;
        mc.player.setYRot(mc.player.getYRot() + (RNG.nextBoolean() ? 30.0F : -30.0F));
    }

    private static void reverseTick() {
        if (!PlusClientState.has(PlusDisability.BACKWARDS_BRAIN)) {
            PlusClientState.swapTicks = 0;
        } else if (PlusClientState.swapTicks > 0) {
            PlusClientState.swapTicks--;
        } else if (RNG.nextInt(2400) == 0) {
            PlusClientState.swapTicks = 200 + RNG.nextInt(200);
        }
    }

    static void startNap(int ticks) {
        PlusClientState.napTicks = Math.max(PlusClientState.napTicks, ticks);
        PlusClientState.freezeTicks = Math.max(PlusClientState.freezeTicks, ticks);
    }

    private static void episodeTick(Minecraft mc) {
        if (!PlusClientState.has(PlusDisability.SOCIAL_ANXIETY)) {
            PlusClientState.anxiety = 0;
        }

        if (!PlusClientState.has(PlusDisability.CATAPLEXY)) {
            PlusClientState.collapseTicks = 0;
        } else if (PlusClientState.collapseTicks > 0) {
            PlusClientState.collapseTicks--;
            PlusClientState.freezeTicks = Math.max(PlusClientState.freezeTicks, 2);
            PlusClientState.cpSneak = true;
            mc.player.setXRot(Mth.approachDegrees(mc.player.getXRot(), COLLAPSE_PITCH, 3.0F));
        }

        if (!PlusClientState.has(PlusDisability.MONO)) {
            PlusClientState.napTicks = 0;
        } else if (PlusClientState.napTicks > 0) {
            PlusClientState.napTicks--;
        }

        if (!PlusClientState.has(PlusDisability.COVID_19)) {
            PlusClientState.feverTicks = 0;
        } else if (PlusClientState.feverTicks > 0) {
            PlusClientState.feverTicks--;
        }

        if (!PlusClientState.has(PlusDisability.KIDNEY_STONES)) {
            PlusClientState.agonyTicks = 0;
        } else if (PlusClientState.agonyTicks > 0) {
            PlusClientState.agonyTicks--;
            PlusClientState.freezeTicks = Math.max(PlusClientState.freezeTicks, 2);
        }

        if (!PlusClientState.has(PlusDisability.BLACK_LUNG)) {
            PlusClientState.dustLevel = 0;
        }
    }

    private static void echoTick(Minecraft mc) {
        if (!PlusClientState.has(PlusDisability.MISOPHONIA) || mc.level == null) {
            PlusClientState.echoesLeft = 0;
        } else if (PlusClientState.echoesLeft > 0 && --PlusClientState.echoDelay <= 0) {
            PlusClientState.echoesLeft--;
            PlusClientState.echoDelay = ECHO_GAP_TICKS;
            PlusClientState.echoCount++;
            PlusClientState.echoing = true;
            try {
                mc.level.playSound(
                    mc.player, mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.GENERIC_EAT.value(), SoundSource.PLAYERS, 1.4F, 1.9F
                );
            } finally {
                PlusClientState.echoing = false;
            }

            mc.player.setXRot(Mth.clamp(mc.player.getXRot() + 3.0F, -90.0F, 90.0F));
        }
    }

    private static void blinkTick(Minecraft mc) {
        boolean stoned = PlusClientState.has(PlusDisability.STONED);
        if (!stoned && !PlusClientState.has(PlusDisability.HEAD_RUSH)) {
            PlusClientState.blinkTicks = 0;
        } else if (PlusClientState.blinkTicks > 0) {
            PlusClientState.blinkTicks--;
        } else if (stoned && RNG.nextInt(150) == 0) {
            PlusClientState.blinkTicks = 20;
        }

        if (!PlusClientState.has(PlusDisability.HEAVY_EYELIDS)) {
            PlusClientState.lidLevel = 0.0F;
        } else if (mc.gui.screen() == null && (mc.options.keyJump.isDown() || mc.options.keyAttack.isDown() || mc.options.keyShift.isDown())) {
            PlusClientState.lidLevel = 0.0F;
        } else {
            PlusClientState.lidLevel = Math.min(1.0F, PlusClientState.lidLevel + 1.0F / LID_CLOSE_TICKS);
        }
    }

    private static void blabTick(Minecraft mc) {
        if (mc.level != null
            && mc.gui.screen() == null
            && PlusClientState.has(PlusDisability.BLABBERMOUTH)
            && !ClientState.has(Disability.MUTE)
            && RNG.nextInt(blabOdds) == 0) {
            blurt(mc);
        }
    }

    static void blurt(Minecraft mc) {
        mc.player.connection
            .sendChat("I'm at " + mc.player.blockPosition().getX() + " " + mc.player.blockPosition().getY() + " " + mc.player.blockPosition().getZ());
    }

    private static void confusionTick() {
        if (!PlusClientState.has(PlusDisability.LEFT_RIGHT_CONFUSION)) {
            PlusClientState.confuseTicks = 0;
        } else if (PlusClientState.confuseTicks > 0) {
            PlusClientState.confuseTicks--;
        } else if (RNG.nextInt(2400) == 0) {
            PlusClientState.confuseTicks = 200 + RNG.nextInt(200);
        }
    }

    private static void jerkTick(Minecraft mc) {
        if (mc.level != null && mc.gui.screen() == null) {
            if (PlusClientState.has(PlusDisability.HICCUPS) && RNG.nextInt(500) == 0) {
                hiccup(mc);
            }
        }
    }

    static void hiccup(Minecraft mc) {
        mc.player.setXRot(Mth.clamp(mc.player.getXRot() - 5.0F, -90.0F, 90.0F));
        mc.level.playSound(mc.player, mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, 1.7F);
    }

    static void sneezeNow() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            sneeze(mc);
            PlusClientState.sneezeCount++;
        }
    }

    static void startAgony(int ticks) {
        PlusClientState.agonyTicks = Math.max(PlusClientState.agonyTicks, ticks);
        PlusClientState.freezeTicks = Math.max(PlusClientState.freezeTicks, ticks);
    }

    static void sneeze(Minecraft mc) {
        mc.player.setYRot(mc.player.getYRot() + (RNG.nextBoolean() ? 14.0F : -14.0F));
        mc.player.setXRot(Mth.clamp(mc.player.getXRot() + 9.0F, -90.0F, 90.0F));
        mc.level.playSound(mc.player, mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.PANDA_SNEEZE, SoundSource.PLAYERS, 0.7F, 1.0F);
    }

    private static boolean walking(Minecraft mc) {
        return mc.options.keyUp.isDown() || mc.options.keyDown.isDown();
    }

    private static void pogoTick(Minecraft mc) {
        if (!PlusClientState.has(PlusDisability.POGO)) {
            PlusClientState.pogoTimer = 0;
        } else if (++PlusClientState.pogoTimer >= 20
            && mc.player.onGround()
            && mc.gui.screen() == null
            && !ClientState.isSeizing()
            && !ClientState.isAsleep()) {
            PlusClientState.pogoTimer = 0;
            PlusClientState.cpJump = true;
        }
    }

    private static void squeakTick(Minecraft mc) {
        double x = mc.player.getX();
        double z = mc.player.getZ();
        double moved = Math.hypot(x - PlusClientState.lastStepX, z - PlusClientState.lastStepZ);
        PlusClientState.lastStepX = x;
        PlusClientState.lastStepZ = z;
        if (mc.level == null || !PlusClientState.has(PlusDisability.SQUEAKY_SHOES)) {
            PlusClientState.squeakDistance = 0.0;
        } else if (moved < 3.0 && mc.player.onGround() && !mc.player.isShiftKeyDown()) {
            PlusClientState.squeakDistance += moved;
            if (PlusClientState.squeakDistance >= STEP_SQUEAK_DISTANCE) {
                PlusClientState.squeakDistance = 0.0;
                PlusClientState.squeakCount++;
                mc.level
                    .playSound(
                        mc.player, x, mc.player.getY(), z, SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.PLAYERS, 0.5F, 1.9F + RNG.nextFloat() * 0.2F
                    );
            }
        }
    }

    private static void pregnancyTick(Minecraft mc) {
        if (mc.level != null && mc.gui.screen() == null && PlusClientState.has(PlusDisability.PREGNANCY) && RNG.nextInt(kickOdds) == 0) {
            babyKick(mc);
        }
    }

    static void babyKick(Minecraft mc) {
        Vec3 look = mc.player.getLookAngle();
        double len = Math.hypot(look.x, look.z);
        if (len > 1.0E-4) {
            double s = KICK_STRENGTH / len;
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(-look.x * s, 0.1, -look.z * s));
        }

        mc.player.setXRot(Mth.clamp(mc.player.getXRot() + 6.0F, -90.0F, 90.0F));
        mc.level.playSound(mc.player, mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 0.8F, 0.6F);
        PlusClientState.kickCount++;
    }

    private static void paranoiaTick(Minecraft mc) {
        if (mc.level != null && PlusClientState.has(PlusDisability.PARANOIA)) {
            if (RNG.nextInt(paranoiaOdds) == 0) {
                double yaw = Math.toRadians(mc.player.getYRot());
                double dist = 1.5 + RNG.nextDouble() * 1.5;
                double x = mc.player.getX() + Math.sin(yaw) * dist;
                double z = mc.player.getZ() - Math.cos(yaw) * dist;
                mc.level.playSound(mc.player, x, mc.player.getY(), z, STEPS[RNG.nextInt(STEPS.length)], SoundSource.VOICE, 0.8F, 0.9F + RNG.nextFloat() * 0.2F);
                PlusClientState.paranoiaCount++;
            }

            if (mc.gui.screen() == null && RNG.nextInt(900) == 0) {
                mc.player.setYRot(mc.player.getYRot() + (RNG.nextBoolean() ? 25.0F : -25.0F));
            }
        }
    }
}
