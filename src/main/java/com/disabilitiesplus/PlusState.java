package com.disabilitiesplus;

import com.studiobing.disabilities.Disability;
import com.studiobing.disabilities.RunState;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

public final class PlusState {
    public static final int MAX_DEATH_REMOVAL = 100;
    private static final Random RNG = new Random();
    private static final Map<UUID, EnumSet<PlusDisability>> ACTIVE = new ConcurrentHashMap<>();

    public static volatile MinecraftServer server;
    private static int deathRemoval = 0;
    private static Path saveFile = null;
    private static boolean dirty = false;
    private static int saveCounter = 0;
    private static int syncCounter = 0;

    public record Grant(Disability original, PlusDisability extra) {
    }

    private PlusState() {
    }

    public static boolean has(UUID id, PlusDisability d) {
        EnumSet<PlusDisability> s = ACTIVE.get(id);
        return s != null && s.contains(d);
    }

    public static Set<PlusDisability> active(UUID id) {
        EnumSet<PlusDisability> s = ACTIVE.get(id);
        return s == null ? Set.of() : Collections.unmodifiableSet(s);
    }

    public static int totalCount(UUID id) {
        return active(id).size() + RunState.active(id).size();
    }

    public static int deathRemoval() {
        return deathRemoval;
    }

    private static EnumSet<PlusDisability> setFor(UUID id) {
        return ACTIVE.computeIfAbsent(id, k -> EnumSet.noneOf(PlusDisability.class));
    }

    public static boolean give(UUID id, PlusDisability d) {
        if (setFor(id).add(d)) {
            dirty = true;
            return true;
        }

        return false;
    }

    public static int giveAll(UUID id) {
        int n = 0;

        for (PlusDisability d : PlusDisability.ALL) {
            if (setFor(id).add(d)) {
                n++;
            }
        }

        if (n > 0) {
            dirty = true;
        }

        return n;
    }

    public static boolean remove(UUID id, PlusDisability d) {
        EnumSet<PlusDisability> s = ACTIVE.get(id);
        if (s != null && s.remove(d)) {
            dirty = true;
            return true;
        }

        return false;
    }

    public static int removeAll(UUID id) {
        EnumSet<PlusDisability> s = ACTIVE.get(id);
        int n = s == null ? 0 : s.size();
        if (s != null) {
            s.clear();
        }

        if (n > 0) {
            dirty = true;
        }

        return n;
    }

    public static void clearEveryone() {
        ACTIVE.clear();
        dirty = true;
    }

    public static void setDeathRemoval(int count) {
        deathRemoval = Math.max(0, Math.min(MAX_DEATH_REMOVAL, count));
        dirty = true;
    }

    public static void markDirty() {
        dirty = true;
    }

    static PlusDisability pick(List<PlusDisability> pool, double roll) {
        double total = 0.0;

        for (PlusDisability d : pool) {
            total += d.weight;
        }

        double target = roll * total;

        for (PlusDisability d : pool) {
            target -= d.weight;
            if (target < 0.0) {
                return d;
            }
        }

        return pool.get(pool.size() - 1);
    }

    public static Grant grantRandom(UUID id) {
        Set<Disability> held = RunState.active(id);
        List<Disability> originals = new ArrayList<>();

        for (Disability d : Disability.values()) {
            if (!held.contains(d)) {
                originals.add(d);
            }
        }

        List<PlusDisability> extras = new ArrayList<>();
        EnumSet<PlusDisability> mine = ACTIVE.get(id);

        for (PlusDisability d : PlusDisability.ALL) {
            if (mine == null || !mine.contains(d)) {
                extras.add(d);
            }
        }

        double total = originals.size();

        for (PlusDisability d : extras) {
            total += d.weight;
        }

        if (total <= 0.0) {
            return null;
        }

        double roll = RNG.nextDouble() * total;
        if (roll < originals.size()) {
            Disability d = originals.get((int)roll);
            RunState.give(id, d);
            return new Grant(d, null);
        }

        PlusDisability d = pick(extras, (roll - originals.size()) / (total - originals.size()));
        give(id, d);
        return new Grant(null, d);
    }

    public static String removeRandomAny(UUID id) {
        List<Disability> originals = new ArrayList<>(RunState.active(id));
        List<PlusDisability> extras = new ArrayList<>(active(id));
        int total = originals.size() + extras.size();
        if (total == 0) {
            return null;
        }

        int i = RNG.nextInt(total);
        if (i < originals.size()) {
            Disability d = originals.get(i);
            RunState.remove(id, d);
            return d.color + d.displayName;
        }

        PlusDisability d = extras.get(i - originals.size());
        remove(id, d);
        return d.color + d.displayName;
    }

    public static void announce(ServerPlayer p, PlusDisability d, String verb) {
        if (!RunState.has(p.getUUID(), Disability.ANOSOGNOSIA)) {
            p.sendSystemMessage(Component.literal(d.color + "You have " + verb + " " + d.displayName + "."));
        }
    }

    public static int maskWords() {
        return (PlusDisability.ALL.length + 63) / 64;
    }

    public static long[] toMask(Set<PlusDisability> set) {
        long[] mask = new long[maskWords()];

        for (PlusDisability d : set) {
            mask[d.ordinal() >> 6] |= 1L << (d.ordinal() & 63);
        }

        return mask;
    }

    public static EnumSet<PlusDisability> fromMask(long[] mask) {
        EnumSet<PlusDisability> set = EnumSet.noneOf(PlusDisability.class);

        for (PlusDisability d : PlusDisability.ALL) {
            int word = d.ordinal() >> 6;
            if (word < mask.length && (mask[word] & 1L << (d.ordinal() & 63)) != 0L) {
                set.add(d);
            }
        }

        return set;
    }

    public static void tick(MinecraftServer server) {
        if (dirty || ++syncCounter >= 20) {
            syncCounter = 0;
            dirty = false;
            sync(server);
        }

        if (++saveCounter >= 400) {
            saveCounter = 0;
            save();
        }
    }

    public static void sync(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        List<UUID> ids = new ArrayList<>();
        List<long[]> masks = new ArrayList<>();

        for (ServerPlayer p : players) {
            ids.add(p.getUUID());
            masks.add(toMask(active(p.getUUID())));
        }

        PlusNet.StatePayload payload = new PlusNet.StatePayload(ids, masks);

        for (ServerPlayer p : players) {
            if (ServerPlayNetworking.canSend(p, PlusNet.StatePayload.ID)) {
                ServerPlayNetworking.send(p, payload);
            }
        }
    }

    public static void load(MinecraftServer server) {
        ACTIVE.clear();
        deathRemoval = 0;

        try {
            saveFile = server.getWorldPath(LevelResource.ROOT).resolve("disabilities-plus.txt");
        } catch (Exception e) {
            saveFile = null;
            return;
        }

        if (!Files.exists(saveFile)) {
            return;
        }

        try {
            for (String line : Files.readAllLines(saveFile)) {
                if (line.startsWith("deathRemoval=")) {
                    deathRemoval = Math.max(0, Math.min(MAX_DEATH_REMOVAL, parseInt(line.substring(13).trim())));
                } else if (line.startsWith("p=")) {
                    String[] parts = line.substring(2).split("\\|", -1);
                    if (parts.length >= 2) {
                        EnumSet<PlusDisability> set = EnumSet.noneOf(PlusDisability.class);

                        for (String n : parts[1].split(",")) {
                            PlusDisability d = n.isBlank() ? null : PlusDisability.find(n.trim());
                            if (d != null) {
                                set.add(d);
                            }
                        }

                        try {
                            ACTIVE.put(UUID.fromString(parts[0].trim()), set);
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        if (saveFile == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("deathRemoval=").append(deathRemoval).append('\n');

        for (Map.Entry<UUID, EnumSet<PlusDisability>> e : new HashMap<>(ACTIVE).entrySet()) {
            sb.append("p=").append(e.getKey()).append('|');
            boolean first = true;

            for (PlusDisability d : e.getValue()) {
                if (!first) {
                    sb.append(',');
                }

                sb.append(d.name());
                first = false;
            }

            sb.append('\n');
        }

        try {
            Path tmp = saveFile.resolveSibling(saveFile.getFileName() + ".tmp");
            Files.writeString(tmp, sb.toString());
            try {
                Files.move(tmp, saveFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(tmp, saveFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ignored) {
        }
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
