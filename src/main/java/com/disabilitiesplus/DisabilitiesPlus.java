package com.disabilitiesplus;

import com.studiobing.disabilities.Disability;
import com.studiobing.disabilities.RunState;
import java.util.UUID;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

public class DisabilitiesPlus implements ModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(PlusNet.StatePayload.ID, PlusNet.StatePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PlusNet.EpisodePayload.ID, PlusNet.EpisodePayload.CODEC);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            PlusState.server = server;
            PlusState.load(server);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            PlusState.save();
            PlusState.server = null;
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            PlusEffects.tick(server);
            PlusState.tick(server);
        });
        ServerLivingEntityEvents.AFTER_DAMAGE.register(PlusEffects::afterDamage);
        ServerLivingEntityEvents.AFTER_DEATH.register(DisabilitiesPlus::onDeath);
        ServerLivingEntityEvents.AFTER_DEATH.register(PlusCataplexy::onKill);
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            MinecraftServer chatServer = sender.level().getServer();
            if (chatServer != null) {
                PlusCataplexy.onChat(chatServer, message.signedContent());
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!ServerPlayNetworking.canSend(handler, PlusNet.StatePayload.ID)) {
                handler.disconnect(Component.literal("This server runs the Disabilities Plus add-on.\nYou need it installed to join."));
            } else {
                PlusState.markDirty();
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PlusEffects.forget(handler.player.getUUID());
            PlusState.save();
            PlusState.markDirty();
        });
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer sp) {
                PlusEffects.breatheDust(sp, state, pos);
                PlusEffects.wearTool(sp);
                PlusCataplexy.onFind(sp, state);
            }
        });

        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (handsUseless(player)) {
                return InteractionResult.FAIL;
            }

            return player instanceof ServerPlayer sp && PlusEffects.bandage(sp, hand) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        });
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (handsUseless(player)) {
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (handsUseless(player)) {
                return InteractionResult.FAIL;
            }

            InteractionResult social = level.isClientSide() ? null : PlusSocial.interact(player, hand, entity);
            return social != null ? social : InteractionResult.PASS;
        });
        AttackBlockCallback.EVENT.register((player, level, hand, pos, dir) -> handsUseless(player) ? InteractionResult.FAIL : InteractionResult.PASS);
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) ->
            handsUseless(player) || frozenWithFear(player, entity) ? InteractionResult.FAIL : InteractionResult.PASS);
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> !handsUseless(player));

        Identifier afterOriginal = Identifier.fromNamespaceAndPath(PlusNet.MOD_ID, "after_original");
        CommandRegistrationCallback.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, afterOriginal);
        CommandRegistrationCallback.EVENT.register(afterOriginal, (dispatcher, access, env) -> PlusCommands.integrate(dispatcher));
    }

    private static boolean frozenWithFear(Player player, Entity target) {
        return target instanceof Enemy && PlusView.has(player, PlusDisability.SCAREDY_CAT);
    }

    private static boolean handsUseless(Player player) {
        return limbless(player) || player instanceof ServerPlayer sp && PlusCataplexy.down(sp);
    }

    private static boolean limbless(Player player) {
        return PlusView.has(player, PlusDisability.QUADRUPLE_AMPUTEE);
    }

    private static void onDeath(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof ServerPlayer sp)) {
            return;
        }

        UUID id = sp.getUUID();
        boolean hidden = RunState.has(id, Disability.ANOSOGNOSIA);
        if (PlusState.remove(id, PlusDisability.QUADRUPLE_AMPUTEE) && !hidden) {
            sp.sendSystemMessage(Component.literal("§aDying cured your Quadruple Amputee."));
        }

        PlusConditions.cureOnDeath(sp);
        int count = PlusState.deathRemoval();
        if (count > 0) {
            StringBuilder names = new StringBuilder();

            for (int i = 0; i < count; i++) {
                String name = PlusState.removeRandomAny(id);
                if (name == null) {
                    break;
                }

                names.append(names.isEmpty() ? "" : "§7, ").append(name);
            }

            if (!names.isEmpty()) {
                RunState.markDirty();
                PlusState.markDirty();
                if (!hidden) {
                    sp.sendSystemMessage(Component.literal("§aDying cured you of: " + names + "§a."));
                }
            }
        }
    }
}
