package com.disabilitiesplus;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Where code that runs on both sides asks "does this player have that add-on disability?". On the server it reads
 * {@link PlusState}; on a client it reads the sets the server last synced. Holds no client-only classes.
 */
public final class PlusView {
   private static final Map<UUID, EnumSet<PlusDisability>> CLIENT = new ConcurrentHashMap<>();

   private PlusView() {
   }

   public static boolean has(Player player, PlusDisability d) {
      if (player == null) {
         return false;
      }

      return player instanceof ServerPlayer ? PlusState.has(player.getUUID(), d) : clientHas(player.getUUID(), d);
   }

   public static boolean clientHas(UUID id, PlusDisability d) {
      EnumSet<PlusDisability> s = CLIENT.get(id);
      return s != null && s.contains(d);
   }

   public static Set<PlusDisability> clientSet(UUID id) {
      EnumSet<PlusDisability> s = CLIENT.get(id);
      return s == null ? Set.of() : Collections.unmodifiableSet(s);
   }

   public static void setClient(Map<UUID, EnumSet<PlusDisability>> all) {
      CLIENT.keySet().retainAll(all.keySet());
      CLIENT.putAll(all);
   }

   public static void clearClient() {
      CLIENT.clear();
   }
}
