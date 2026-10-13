package com.disabilitiesplus;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

final class PlusAnxiety {
    private static final double PLAYER_RANGE = 30.0;
    private static final double MOB_RANGE = 16.0;
    private static final double COMFORT_RANGE = 2.5;
    private static final int FREEZE_TICKS = 60;
    private static final int FREEZE_COOLDOWN_TICKS = 200;
    private static final float AFTER_FREEZE_METER = 70.0F;
    private static final Identifier SHAKY_HANDS_ID = Identifier.fromNamespaceAndPath(PlusNet.MOD_ID, "anxiety_hands");

    private PlusAnxiety() {
    }

    static final class State {
        float meter = 0.0F;
        int stage = 0;
        int freezeCooldown = 0;
        int sent = 0;
    }

    record Gaze(int watchers, boolean comfort) {
    }

    private static boolean has(ServerPlayer sp) {
        return PlusState.has(sp.getUUID(), PlusDisability.SOCIAL_ANXIETY);
    }

    private static boolean sees(LivingEntity watcher, ServerPlayer target) {
        Vec3 from = watcher.getEyePosition();
        Vec3 toMe = target.getEyePosition().subtract(from);
        Vec3 view = watcher.getViewVector(1.0F);
        return MindRules.facing(view.x, view.y, view.z, toMe.x, toMe.y, toMe.z) && watcher.hasLineOfSight(target);
    }

    static Gaze scan(ServerPlayer sp, ServerLevel sw) {
        int watchers = 0;
        boolean comfort = false;
        var helmet = sp.getItemBySlot(EquipmentSlot.HEAD);
        boolean hidden = helmet.is(Items.CARVED_PUMPKIN);
        for (ServerPlayer other : PlusSocial.nearbyPlayers(sw, sp, PLAYER_RANGE)) {
            if (other.distanceToSqr(sp) <= COMFORT_RANGE * COMFORT_RANGE) {
                comfort = true;
            } else if (!hidden && sees(other, sp)) {
                watchers++;
            }
        }

        if (!hidden) {
            for (Mob mob : sw.getEntitiesOfClass(Mob.class, sp.getBoundingBox().inflate(MOB_RANGE),
                m -> m.isAlive() && (m instanceof Enemy || m instanceof AbstractVillager))) {
                if (sees(mob, sp)) {
                    watchers++;
                }
            }

            if (!helmet.isEmpty()) {
                watchers = (watchers + 1) / 2;
            }
        }

        return new Gaze(watchers, comfort);
    }

    static void tick(ServerPlayer sp, ServerLevel sw, PlusEffects.Effects fx) {
        State c = fx.anxiety;
        if (!has(sp) || !PlusEffects.afflictable(sp)) {
            boolean shaking = c.stage != 0;
            c.meter = 0.0F;
            c.stage = 0;
            if (shaking) {
                PlusEffects.setModifier(sp.getAttribute(Attributes.BLOCK_BREAK_SPEED), SHAKY_HANDS_ID, 0.0, Operation.ADD_MULTIPLIED_TOTAL);
            }

            if (c.sent != 0) {
                c.sent = 0;
                PlusEffects.sendEpisode(sp, PlusNet.EpisodePayload.ANXIETY, 0);
            }

            return;
        }

        if (c.freezeCooldown > 0) {
            c.freezeCooldown--;
        }

        if (c.stage >= 2) {
            sp.setSprinting(false);
        }

        if (sp.tickCount % 20 != 0) {
            return;
        }

        Gaze gaze = scan(sp, sw);
        c.meter = MindRules.anxietyNext(c.meter, gaze.watchers(), gaze.comfort());
        int stage = MindRules.anxietyStage(c.meter);
        if (stage > c.stage) {
            PlusEffects.hint(sp, stage == 1 ? "§9Eyes on you. Your hands start to shake."
                : stage == 2 ? "§8You cannot get the words out." : "§cYou freeze.");
        }

        c.stage = stage;
        PlusEffects.setModifier(sp.getAttribute(Attributes.BLOCK_BREAK_SPEED), SHAKY_HANDS_ID, stage >= 1 ? -0.2 : 0.0, Operation.ADD_MULTIPLIED_TOTAL);
        if (stage >= 2) {
            PlusEffects.applyQuiet(sp, MobEffects.SLOWNESS, 60, 0);
        }

        if (stage >= 3 && c.freezeCooldown == 0) {
            c.freezeCooldown = FREEZE_COOLDOWN_TICKS;
            c.meter = AFTER_FREEZE_METER;
            PlusEffects.sendEpisode(sp, PlusNet.EpisodePayload.LOCK, FREEZE_TICKS);
            sp.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, FREEZE_TICKS, 3, false, false, false));
        }

        int shown = Math.round(c.meter);
        if (shown != c.sent) {
            c.sent = shown;
            PlusEffects.sendEpisode(sp, PlusNet.EpisodePayload.ANXIETY, shown);
        }
    }
}
