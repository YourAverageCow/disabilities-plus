package com.disabilitiesplus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public final class PlusNet {
    public static final String MOD_ID = "disabilities_plus";

    private PlusNet() {
    }

    public record StatePayload(List<UUID> players, List<long[]> masks) implements CustomPacketPayload {
        private static final int MAX_WORDS = 16;
        public static final Type<StatePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "state"));
        public static final StreamCodec<RegistryFriendlyByteBuf, StatePayload> CODEC = StreamCodec.of((buf, p) -> {
            buf.writeVarInt(p.players.size());

            for (int i = 0; i < p.players.size(); i++) {
                buf.writeUUID(p.players.get(i));
                long[] mask = p.masks.get(i);
                buf.writeVarInt(mask.length);

                for (long word : mask) {
                    buf.writeLong(word);
                }
            }
        }, buf -> {
            int n = buf.readVarInt();
            List<UUID> ids = new ArrayList<>(n);
            List<long[]> masks = new ArrayList<>(n);

            for (int i = 0; i < n; i++) {
                ids.add(buf.readUUID());
                int words = buf.readVarInt();
                if (words < 0 || words > MAX_WORDS) {
                    throw new IllegalArgumentException("Bad disability mask length " + words);
                }

                long[] mask = new long[words];
                for (int w = 0; w < words; w++) {
                    mask[w] = buf.readLong();
                }

                masks.add(mask);
            }

            return new StatePayload(ids, masks);
        });

        @Override
        public Type<StatePayload> type() {
            return ID;
        }
    }

    public record EpisodePayload(int kind, int ticks) implements CustomPacketPayload {
        public static final int PAIN = 0;
        public static final int COUGH = 1;
        public static final int NECROSIS = 2;
        public static final int NAP = 3;
        public static final int DUST = 4;
        public static final int FEVER = 5;
        public static final int SNEEZE = 6;
        public static final int AGONY = 7;
        public static final int ANXIETY = 8;
        public static final int LOCK = 9;
        public static final int COLLAPSE = 10;
        public static final Type<EpisodePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "episode"));
        public static final StreamCodec<RegistryFriendlyByteBuf, EpisodePayload> CODEC = StreamCodec.of((buf, p) -> {
            buf.writeVarInt(p.kind);
            buf.writeVarInt(p.ticks);
        }, buf -> new EpisodePayload(buf.readVarInt(), buf.readVarInt()));

        @Override
        public Type<EpisodePayload> type() {
            return ID;
        }
    }
}
