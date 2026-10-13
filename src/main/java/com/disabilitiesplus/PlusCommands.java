package com.disabilitiesplus;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.studiobing.disabilities.Disability;
import com.studiobing.disabilities.RunState;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Extends the original mod's {@code /disability} command with the add-on's disabilities. Rather than patching the
 * original, it merges nodes into the existing tree: for give/remove/next/list the original's executor is kept and
 * delegated to for anything that is not ours.
 */
public final class PlusCommands {
   private PlusCommands() {
   }

   static void integrate(CommandDispatcher<CommandSourceStack> dispatcher) {
      CommandNode<CommandSourceStack> root = dispatcher.getRoot().getChild("disability");
      if (root == null) {
         return;
      }

      Command<CommandSourceStack> origGive = leaf(root, "give", "players", "name");
      Command<CommandSourceStack> origRemove = leaf(root, "remove", "players", "name");
      Command<CommandSourceStack> origList = leaf(root, "list");
      Command<CommandSourceStack> origListSel = leaf(root, "list", "players");
      Command<CommandSourceStack> origNext = leaf(root, "next");
      Command<CommandSourceStack> origNextSel = leaf(root, "next", "players");

      // Tab completion of our names alongside the original's.
      extendSuggestions(walk(root, "give", "players", "name"));
      extendSuggestions(walk(root, "remove", "players", "name"));

      if (origGive != null) {
         root.addChild(
            Commands.literal("give")
               .then(Commands.argument("players", EntityArgument.players()).then(Commands.argument("name", StringArgumentType.word()).executes(ctx -> give(ctx, origGive))))
               .build()
         );
      }

      if (origRemove != null) {
         root.addChild(
            Commands.literal("remove")
               .then(Commands.argument("players", EntityArgument.players()).then(Commands.argument("name", StringArgumentType.word()).executes(ctx -> remove(ctx, origRemove))))
               .build()
         );
      }

      if (origNext != null && origNextSel != null) {
         root.addChild(
            Commands.literal("next")
               .executes(ctx -> next(ctx, ctx.getSource().getServer().getPlayerList().getPlayers()))
               .then(Commands.argument("players", EntityArgument.players()).executes(ctx -> next(ctx, EntityArgument.getPlayers(ctx, "players"))))
               .build()
         );
      }

      if (origList != null && origListSel != null) {
         root.addChild(
            Commands.literal("list")
               .executes(ctx -> {
                  int r = origList.run(ctx);
                  listExtras(ctx, List.of(ctx.getSource().getPlayerOrException()));
                  return r;
               })
               .then(Commands.argument("players", EntityArgument.players()).executes(ctx -> {
                  int r = origListSel.run(ctx);
                  listExtras(ctx, EntityArgument.getPlayers(ctx, "players"));
                  return r;
               }))
               .build()
         );
      }

      root.addChild(
         Commands.literal("deathremoval")
            .executes(ctx -> {
               int n = PlusState.deathRemoval();
               feedback(ctx, "Each death currently removes " + n + (n == 1 ? " random disability." : " random disabilities."));
               return n;
            })
            .then(Commands.argument("count", IntegerArgumentType.integer(0, PlusState.MAX_DEATH_REMOVAL)).executes(ctx -> {
               int n = IntegerArgumentType.getInteger(ctx, "count");
               PlusState.setDeathRemoval(n);
               feedback(
                  ctx,
                  n == 0 ? "Dying no longer removes any disabilities." : "Each death will now remove " + n + (n == 1 ? " random disability." : " random disabilities.")
               );
               return n;
            }))
            .build()
      );
   }

   // ------------------------------------------------------------------ tree helpers

   private static CommandNode<CommandSourceStack> walk(CommandNode<CommandSourceStack> from, String... path) {
      CommandNode<CommandSourceStack> node = from;

      for (String step : path) {
         node = node == null ? null : node.getChild(step);
      }

      return node;
   }

   private static Command<CommandSourceStack> leaf(CommandNode<CommandSourceStack> from, String... path) {
      CommandNode<CommandSourceStack> node = walk(from, path);
      return node == null ? null : node.getCommand();
   }

   @SuppressWarnings("unchecked")
   private static void extendSuggestions(CommandNode<CommandSourceStack> node) {
      if (node instanceof ArgumentCommandNode<?, ?> argument) {
         try {
            ArgumentCommandNode<CommandSourceStack, ?> arg = (ArgumentCommandNode<CommandSourceStack, ?>)argument;
            SuggestionProvider<CommandSourceStack> original = arg.getCustomSuggestions();
            Field field = ArgumentCommandNode.class.getDeclaredField("customSuggestions");
            field.setAccessible(true);
            SuggestionProvider<CommandSourceStack> wrapped = (ctx, builder) -> {
               for (PlusDisability d : PlusDisability.ALL) {
                  builder.suggest(d.name().toLowerCase());
               }

               return original == null ? builder.buildFuture() : original.getSuggestions(ctx, builder);
            };
            field.set(arg, wrapped);
         } catch (ReflectiveOperationException | RuntimeException e) {
            // Only tab completion of the add-on's names is lost; the commands themselves still work.
         }
      }
   }

   // ------------------------------------------------------------------ executors

   private static void feedback(CommandContext<CommandSourceStack> ctx, String message) {
      ctx.getSource().sendSuccess(() -> Component.literal("§7" + message), true);
   }

   private static String names(Collection<ServerPlayer> players) {
      List<String> out = new ArrayList<>();

      for (ServerPlayer p : players) {
         out.add(p.getName().getString());
      }

      return String.join(", ", out);
   }

   private static int give(CommandContext<CommandSourceStack> ctx, Command<CommandSourceStack> original) throws CommandSyntaxException {
      Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "players");
      String name = StringArgumentType.getString(ctx, "name");
      if (name.equalsIgnoreCase("random")) {
         return giveRandom(ctx, targets);
      }

      PlusDisability d = PlusDisability.find(name);
      if (d != null) {
         int n = 0;

         for (ServerPlayer p : targets) {
            if (PlusState.give(p.getUUID(), d)) {
               n++;
               PlusState.announce(p, d, "been given");
            }
         }

         feedback(ctx, n == 0 ? names(targets) + " already had " + d.displayName + "." : "Gave " + d.displayName + " to " + names(targets) + ".");
         return n;
      }

      if (name.equalsIgnoreCase("all")) {
         for (ServerPlayer p : targets) {
            PlusState.giveAll(p.getUUID());
         }
      }

      return original.run(ctx);
   }

   private static int giveRandom(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets) {
      List<String> given = new ArrayList<>();

      for (ServerPlayer p : targets) {
         PlusState.Grant g = PlusState.grantRandom(p.getUUID());
         if (g == null) {
            continue;
         }

         if (g.original() != null) {
            RunState.announce(p, g.original(), "been given");
            given.add(p.getName().getString() + ": " + g.original().displayName);
         } else {
            PlusState.announce(p, g.extra(), "been given");
            given.add(p.getName().getString() + ": " + g.extra().displayName);
         }
      }

      feedback(ctx, given.isEmpty() ? names(targets) + " already had everything." : "Gave " + String.join(", ", given) + ".");
      return given.size();
   }

   private static int remove(CommandContext<CommandSourceStack> ctx, Command<CommandSourceStack> original) throws CommandSyntaxException {
      Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "players");
      String name = StringArgumentType.getString(ctx, "name");
      if (name.equalsIgnoreCase("random")) {
         List<String> cured = new ArrayList<>();

         for (ServerPlayer p : targets) {
            String removed = PlusState.removeRandomAny(p.getUUID());
            if (removed != null) {
               cured.add(p.getName().getString() + ": " + removed + "§7");
            }
         }

         feedback(ctx, cured.isEmpty() ? names(targets) + " had nothing to remove." : "Removed " + String.join(", ", cured) + ".");
         return cured.size();
      }

      PlusDisability d = PlusDisability.find(name);
      if (d != null) {
         int n = 0;

         for (ServerPlayer p : targets) {
            if (PlusState.remove(p.getUUID(), d)) {
               n++;
            }
         }

         feedback(ctx, n == 0 ? names(targets) + " didn't have " + d.displayName + "." : "Removed " + d.displayName + " from " + names(targets) + ".");
         return n;
      }

      if (name.equalsIgnoreCase("all")) {
         for (ServerPlayer p : targets) {
            PlusState.removeAll(p.getUUID());
         }
      }

      return original.run(ctx);
   }

   private static int next(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets) {
      int n = 0;

      for (ServerPlayer p : targets) {
         PlusState.Grant g = PlusState.grantRandom(p.getUUID());
         if (g != null) {
            n++;
            if (g.original() != null) {
               RunState.announce(p, g.original(), "developed");
            } else {
               PlusState.announce(p, g.extra(), "developed");
            }
         }
      }

      feedback(ctx, n == 0 ? "Nothing to give — they already have everything." : "Skipped the timer for " + n + (n == 1 ? " player." : " players."));
      return n;
   }

   private static void listExtras(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets) {
      for (ServerPlayer p : targets) {
         Set<PlusDisability> extras = PlusState.active(p.getUUID());
         if (!extras.isEmpty()) {
            StringBuilder sb = new StringBuilder(p.getName().getString() + " (+" + extras.size() + " from Disabilities Plus): ");
            boolean first = true;

            for (PlusDisability d : extras) {
               sb.append(first ? "" : "§7, ").append(d.color).append(d.displayName);
               first = false;
            }

            feedback(ctx, sb.toString());
         }
      }
   }
}
