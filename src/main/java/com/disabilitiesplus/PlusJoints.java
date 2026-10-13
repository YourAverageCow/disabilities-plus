package com.disabilitiesplus;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;

final class PlusJoints {
    static final int NONE = 0;
    static final int ARM = 1;
    static final int LEG = 2;
    private static final float HIT_THRESHOLD = 4.0F;
    private static final float FALL_THRESHOLD = 3.0F;
    private static final int SELF_FIX_TICKS = 120;
    private static final Identifier ARM_ATTACK_ID = Identifier.fromNamespaceAndPath(PlusNet.MOD_ID, "joint_arm_attack");
    private static final Identifier ARM_MINE_ID = Identifier.fromNamespaceAndPath(PlusNet.MOD_ID, "joint_arm_mine");
    private static final Identifier LEG_SPEED_ID = Identifier.fromNamespaceAndPath(PlusNet.MOD_ID, "joint_leg_speed");
    private static final Identifier LEG_JUMP_ID = Identifier.fromNamespaceAndPath(PlusNet.MOD_ID, "joint_leg_jump");

    static int forcedLimb = NONE;

    private PlusJoints() {
    }

    static final class State {
        int limb = NONE;

        boolean applied = false;
        final StillTracker still = new StillTracker();
    }

    private static boolean has(ServerPlayer sp) {
        return PlusState.has(sp.getUUID(), PlusDisability.EHLERS_DANLOS);
    }

    static void tick(ServerPlayer sp, ServerLevel sw, PlusEffects.Effects fx) {
        State c = fx.joints;
        if (!has(sp) || !PlusEffects.afflictable(sp)) {
            c.limb = NONE;
            c.still.reset();
            if (!c.applied) {
                return;
            }
        } else if (c.limb == NONE && fx.landedFall >= FALL_THRESHOLD) {
            dislocate(sp, sw, c);
        }

        boolean arm = c.limb == ARM;
        boolean leg = c.limb == LEG;
        PlusEffects.toggleModifier(sp.getAttribute(Attributes.ATTACK_DAMAGE), ARM_ATTACK_ID, arm, -0.9, Operation.ADD_MULTIPLIED_TOTAL);
        PlusEffects.toggleModifier(sp.getAttribute(Attributes.BLOCK_BREAK_SPEED), ARM_MINE_ID, arm, -0.9, Operation.ADD_MULTIPLIED_TOTAL);
        PlusEffects.toggleModifier(sp.getAttribute(Attributes.MOVEMENT_SPEED), LEG_SPEED_ID, leg, -0.5, Operation.ADD_MULTIPLIED_TOTAL);
        PlusEffects.toggleModifier(sp.getAttribute(Attributes.JUMP_STRENGTH), LEG_JUMP_ID, leg, -1.0, Operation.ADD_MULTIPLIED_TOTAL);
        c.applied = c.limb != NONE;
        if (c.limb != NONE && c.still.update(sp) >= SELF_FIX_TICKS) {
            relocate(sp, sw, c, 2.0F, "You grit your teeth and pop it back in.");
        }
    }

    static void afterDamage(ServerPlayer sp, ServerLevel sw, float taken) {
        State c = PlusEffects.of(sp.getUUID()).joints;
        if (has(sp) && PlusEffects.afflictable(sp) && c.limb == NONE && taken >= HIT_THRESHOLD) {
            dislocate(sp, sw, c);
        }
    }

    static void dislocate(ServerPlayer sp, ServerLevel sw, State c) {
        c.limb = forcedLimb != NONE ? forcedLimb : (PlusEffects.RNG.nextBoolean() ? ARM : LEG);
        c.still.reset();
        sw.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.SKELETON_HURT, SoundSource.PLAYERS, 1.0F, 0.6F);
        PlusEffects.hint(sp, c.limb == ARM ? "§cYour shoulder pops out of its socket! Crouch still for six seconds, or ask a friend."
            : "§cYour knee pops out of place! Crouch still for six seconds, or ask a friend.");
    }

    private static void relocate(ServerPlayer patient, ServerLevel sw, State c, float cost, String message) {
        c.limb = NONE;
        c.still.reset();
        PlusEffects.bleed(patient, sw, cost);
        patient.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, false, false, false));
        sw.playSound(null, patient.getX(), patient.getY(), patient.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.8F, 0.7F);
        PlusEffects.hint(patient, "§a" + message);
    }

    static boolean helpRelocate(ServerPlayer helper, ServerPlayer patient) {
        State c = PlusEffects.of(patient.getUUID()).joints;
        if (helper == patient || !has(patient) || c.limb == NONE || !(patient.level() instanceof ServerLevel sw)) {
            return false;
        }

        relocate(patient, sw, c, 1.0F, helper.getName().getString() + " pops it back in for you.");
        PlusEffects.hint(helper, "§aYou pop " + patient.getName().getString() + "'s joint back in.");
        return true;
    }
}
