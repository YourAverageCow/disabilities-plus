package com.disabilitiesplus;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.TeleportRandomlyConsumeEffect;

final class PlusSocial {
    private static final double COVER_RANGE = 6.0;
    private static final Identifier COVER_ID = Identifier.fromNamespaceAndPath(PlusNet.MOD_ID, "hoarder_cover");

    private static final PlusDisability[] CARED_FOR = {
        PlusDisability.ANEMIA, PlusDisability.PREGNANCY, PlusDisability.KIDNEY_STONES, PlusDisability.LEAD_POISONING,
        PlusDisability.NECROSIS, PlusDisability.QUADRUPLE_AMPUTEE
    };

    private PlusSocial() {
    }

    private static boolean has(Player player, PlusDisability d) {
        return PlusState.has(player.getUUID(), d);
    }

    static List<ServerPlayer> nearbyPlayers(ServerLevel level, Player center, double radius) {
        return level.players().stream()
            .filter(p -> p != center && !p.isSpectator() && p.isAlive() && p.distanceToSqr(center) <= radius * radius)
            .toList();
    }

    static InteractionResult interact(Player actor, InteractionHand hand, Entity target) {
        if (hand != InteractionHand.MAIN_HAND || !(actor instanceof ServerPlayer giver) || !(target instanceof ServerPlayer other) || giver == other) {
            return null;
        }

        ItemStack held = giver.getItemInHand(hand);
        if (held.isEmpty()) {
            if (PlusJoints.helpRelocate(giver, other) || PlusNumbness.examine(giver, other)) {
                return InteractionResult.SUCCESS;
            }

            return carry(giver, other);
        }

        PlusEffects.Effects fx = PlusEffects.of(other.getUUID());
        if (held.is(Items.PAPER) && has(other, PlusDisability.HEMOPHILIA) && fx.bleedTicks > 0) {
            held.shrink(1);
            fx.bleedTicks = 0;
            PlusEffects.hint(giver, "§aYou bandage " + other.getName().getString() + ".");
            PlusEffects.hint(other, "§a" + giver.getName().getString() + " bandages your wound.");
            return InteractionResult.SUCCESS;
        }

        if ((held.is(Items.GLASS) || held.is(Items.GLASS_PANE)) && has(other, PlusDisability.GLASS_CANNON) && fx.cracks > 0) {
            held.shrink(1);
            fx.cracks--;
            fx.crackTicks = fx.cracks > 0 ? fx.crackTicks : 0;
            PlusEffects.hint(giver, "§bYou patch up " + other.getName().getString() + ".");
            PlusEffects.hint(other, "§b" + giver.getName().getString() + " patches a crack in you.");
            return InteractionResult.SUCCESS;
        }

        if (canFeed(other, held)) {
            feed(giver, other, held, fx);
            return InteractionResult.SUCCESS;
        }

        return null;
    }

    private static InteractionResult carry(ServerPlayer giver, ServerPlayer other) {
        PlusEffects.Effects fx = PlusEffects.of(other.getUUID());
        if (has(other, PlusDisability.QUADRUPLE_AMPUTEE) && fx.carriedBy == null && !has(giver, PlusDisability.QUADRUPLE_AMPUTEE)) {
            fx.carriedBy = giver.getUUID();
            PlusEffects.hint(giver, "§aYou lift " + other.getName().getString() + " onto your back. Crouch to put them down.");
            PlusEffects.hint(other, "§a" + giver.getName().getString() + " carries you.");
            return InteractionResult.SUCCESS;
        }

        return null;
    }

    static void updateCarried(ServerPlayer carried, ServerLevel level) {
        PlusEffects.Effects fx = PlusEffects.of(carried.getUUID());
        if (fx.carriedBy == null) {
            return;
        }

        Player carrier = level.getPlayerByUUID(fx.carriedBy);
        if (carrier == null || carrier == carried || !carrier.isAlive() || carrier.isSpectator() || carrier.isShiftKeyDown()
            || !has(carried, PlusDisability.QUADRUPLE_AMPUTEE)) {
            fx.carriedBy = null;
            PlusEffects.hint(carried, "§7You are put down.");
            return;
        }

        double yaw = Math.toRadians(carrier.getYRot());
        carried.connection.teleport(carrier.getX() + Math.sin(yaw) * 0.4, carrier.getY() + 0.5, carrier.getZ() - Math.cos(yaw) * 0.4,
            carrier.getYRot(), carried.getXRot());
        carried.resetFallDistance();
    }

    private static boolean caredFor(ServerPlayer other) {
        for (PlusDisability d : CARED_FOR) {
            if (has(other, d)) {
                return true;
            }
        }

        return false;
    }

    private static boolean canFeed(ServerPlayer other, ItemStack held) {
        boolean cureMilk = held.is(Items.MILK_BUCKET) && has(other, PlusDisability.LEAD_POISONING);
        if (!caredFor(other) || harmful(held) && !cureMilk) {
            return false;
        }

        ItemUseAnimation how = held.getUseAnimation();
        if (how == ItemUseAnimation.DRINK) {
            return true;
        }

        return how == ItemUseAnimation.EAT
            && (other.getFoodData().needsFood() || held.is(Items.GOLDEN_APPLE) || held.is(Items.ENCHANTED_GOLDEN_APPLE));
    }

    private static boolean harmful(ItemStack held) {
        var consumable = held.get(DataComponents.CONSUMABLE);
        if (consumable != null) {
            for (ConsumeEffect effect : consumable.onConsumeEffects()) {
                if (effect instanceof ClearAllStatusEffectsConsumeEffect || effect instanceof RemoveStatusEffectsConsumeEffect
                    || effect instanceof TeleportRandomlyConsumeEffect) {
                    return true;
                }

                if (effect instanceof ApplyStatusEffectsConsumeEffect apply && anyHarmful(apply.effects())) {
                    return true;
                }
            }
        }

        var potion = held.get(DataComponents.POTION_CONTENTS);
        return potion != null && anyHarmful(potion.getAllEffects());
    }

    private static boolean anyHarmful(Iterable<MobEffectInstance> effects) {
        for (MobEffectInstance applied : effects) {
            if (!applied.getEffect().value().isBeneficial()) {
                return true;
            }
        }

        return false;
    }

    private static void feed(ServerPlayer giver, ServerPlayer other, ItemStack held, PlusEffects.Effects fx) {
        ItemStack meal = held.copyWithCount(1);
        Item item = held.getItem();
        ItemStack leftover = meal.finishUsingItem(other.level(), other);
        if (!giver.hasInfiniteMaterials()) {
            held.shrink(1);
        }

        if (!leftover.isEmpty() && !leftover.is(item)) {
            if (!giver.getInventory().add(leftover) && !leftover.isEmpty()) {
                ItemEntity drop =
                    new ItemEntity(giver.level(), giver.getX(), giver.getY() + 0.5, giver.getZ(), leftover);
                drop.setDefaultPickUpDelay();
                giver.level().addFreshEntity(drop);
            }
        }

        fx.fedItem = item;
        other.level().playSound(null, other.getX(), other.getY(), other.getZ(), SoundEvents.GENERIC_EAT.value(), SoundSource.PLAYERS, 0.8F, 1.0F);
        PlusEffects.hint(giver, "§aYou feed " + other.getName().getString() + ".");
        PlusEffects.hint(other, "§a" + giver.getName().getString() + " feeds you.");
    }

    static void applyHoarderCover(List<ServerPlayer> players) {
        for (ServerPlayer p : players) {
            double cover = 0.0;
            for (ServerPlayer q : players) {
                if (q != p && q.level() == p.level() && q.distanceToSqr(p) <= COVER_RANGE * COVER_RANGE) {
                    cover = Math.max(cover, PlusEffects.of(q.getUUID()).hoardArmor / 2.0);
                }
            }

            PlusEffects.setModifier(p.getAttribute(Attributes.ARMOR), COVER_ID, cover, Operation.ADD_VALUE);
        }
    }
}
