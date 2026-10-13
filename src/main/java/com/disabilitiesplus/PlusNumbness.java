package com.disabilitiesplus;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

final class PlusNumbness {
    private static final double WARN_RANGE = 30.0;
    private static final int WOUND_INTERVAL_TICKS = 40;
    private static final int WARN_INTERVAL_TICKS = 100;
    private static final int SELF_CHECK_TICKS = 60;
    private static final int CHECK_COOLDOWN_TICKS = 100;
    private static final float BADLY_HURT = 0.5F;

    private PlusNumbness() {
    }

    static final class State {
        int wound = 0;
        int checkCooldown = 0;
        int warnTimer = 0;
        final StillTracker still = new StillTracker();
    }

    private static boolean has(ServerPlayer sp) {
        return PlusState.has(sp.getUUID(), PlusDisability.PAIN_INSENSITIVITY);
    }

    static void afterDamage(ServerPlayer sp, float taken) {
        if (has(sp)) {
            State c = PlusEffects.of(sp.getUUID()).numb;
            c.wound = MindRules.woundAfter(c.wound, taken);
        }
    }

    private static String hearts(ServerPlayer sp) {
        return String.format(java.util.Locale.ROOT, "%.1f", sp.getHealth() / 2.0F);
    }

    static void tick(ServerPlayer sp, ServerLevel sw, PlusEffects.Effects fx) {
        State c = fx.numb;
        if (!has(sp) || !PlusEffects.afflictable(sp)) {
            c.wound = 0;
            c.still.reset();
            return;
        }

        if (c.checkCooldown > 0) {
            c.checkCooldown--;
        }

        if (c.wound > 0) {
            c.wound--;
            if (c.wound % WOUND_INTERVAL_TICKS == 0) {
                PlusEffects.bleed(sp, sw, 1.0F);
            }
        }

        if (c.still.update(sp) >= SELF_CHECK_TICKS && c.checkCooldown == 0) {
            c.still.reset();
            examine(sp, sp);
        }

        if (--c.warnTimer <= 0) {
            c.warnTimer = WARN_INTERVAL_TICKS;
            if (c.wound > 0 || sp.getHealth() <= sp.getMaxHealth() * BADLY_HURT) {
                warnFriends(sp, sw);
            }
        }
    }

    static boolean examine(ServerPlayer examiner, ServerPlayer patient) {
        State c = PlusEffects.of(patient.getUUID()).numb;
        if (!has(patient) || c.checkCooldown > 0) {
            return false;
        }

        c.checkCooldown = CHECK_COOLDOWN_TICKS;
        boolean treated = c.wound > 0;
        c.wound = 0;
        String note = hearts(patient) + " hearts" + (treated ? " and a wound, now dressed" : "");
        if (examiner == patient) {
            PlusEffects.hint(patient, "§7You check yourself over: " + note + ".");
        } else {
            PlusEffects.hint(patient, "§7" + examiner.getName().getString() + " checks you over: " + note + ".");
            PlusEffects.hint(examiner, "§7" + patient.getName().getString() + " has " + note + ".");
        }

        return true;
    }

    static List<ServerPlayer> warnFriends(ServerPlayer sp, ServerLevel sw) {
        List<ServerPlayer> told = PlusSocial.nearbyPlayers(sw, sp, WARN_RANGE);
        if (told.isEmpty()) {
            return told;
        }

        String message = "§c" + sp.getName().getString() + " is hurt and cannot feel it (" + hearts(sp) + " hearts). Check them.";
        for (ServerPlayer friend : told) {
            PlusEffects.hint(friend, message);
        }

        return told;
    }
}
