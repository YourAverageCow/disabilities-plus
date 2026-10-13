package com.disabilitiesplus.client;

import com.disabilitiesplus.PlusDisability;
import com.disabilitiesplus.PlusNet;
import com.disabilitiesplus.PlusState;
import com.disabilitiesplus.PlusView;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class DisabilitiesPlusClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(PlusNet.StatePayload.ID, (payload, context) -> context.client().execute(() -> {
            Map<UUID, EnumSet<PlusDisability>> all = new HashMap<>();

            for (int i = 0; i < payload.players().size(); i++) {
                all.put(payload.players().get(i), PlusState.fromMask(payload.masks().get(i)));
            }

            PlusView.setClient(all);
        }));
        ClientPlayNetworking.registerGlobalReceiver(PlusNet.EpisodePayload.ID, (payload, context) -> context.client().execute(() -> {
            switch (payload.kind()) {
                case PlusNet.EpisodePayload.PAIN -> PlusClientState.painTicks = payload.ticks();
                case PlusNet.EpisodePayload.COUGH -> PlusClientState.coughTicks = payload.ticks();
                case PlusNet.EpisodePayload.NECROSIS -> PlusClientState.necrosisStage = payload.ticks();
                case PlusNet.EpisodePayload.NAP -> PlusClientEffects.startNap(payload.ticks());
                case PlusNet.EpisodePayload.DUST -> PlusClientState.dustLevel = payload.ticks();
                case PlusNet.EpisodePayload.FEVER -> PlusClientState.feverTicks = payload.ticks();
                case PlusNet.EpisodePayload.AGONY -> PlusClientEffects.startAgony(payload.ticks());
                case PlusNet.EpisodePayload.ANXIETY -> PlusClientState.anxiety = payload.ticks();
                case PlusNet.EpisodePayload.LOCK -> PlusClientState.freezeTicks = Math.max(PlusClientState.freezeTicks, payload.ticks());
                case PlusNet.EpisodePayload.COLLAPSE -> PlusClientState.collapseTicks = payload.ticks();
                case PlusNet.EpisodePayload.SNEEZE -> PlusClientEffects.sneezeNow();
                default -> {
                }
            }
        }));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> PlusClientState.clear());
        ClientTickEvents.END_CLIENT_TICK.register(PlusClientEffects::tick);
        ClientSendMessageEvents.MODIFY_CHAT.register(DisabilitiesPlusClient::garbleChat);
        HudElementRegistry.addFirst(Identifier.fromNamespaceAndPath(PlusNet.MOD_ID, "impairments"), (ctx, tick) -> ImpairmentHud.render(ctx));

        HudElementRegistry.replaceElement(VanillaHudElements.HEALTH_BAR, original -> (ctx, tick) -> {
            if (!PlusClientState.has(PlusDisability.PAIN_INSENSITIVITY)) {
                original.extractRenderState(ctx, tick);
            }
        });

        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (PlusClientState.has(PlusDisability.SOCIAL_ANXIETY) && PlusClientState.anxiety >= com.disabilitiesplus.client.PlusClientEffects.CHAT_LOCK_ANXIETY) {
                Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(Component.literal("§8You cannot get the words out."));
                return false;
            }

            return true;
        });
    }

    static String garbleChat(String message) {
        return PlusClientState.has(PlusDisability.APHASIA) ? Aphasia.scramble(message) : message;
    }
}
