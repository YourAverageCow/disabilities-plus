package com.disabilitiesplus.client;

import com.disabilitiesplus.PlusDisability;
import com.studiobing.disabilities.Disability;
import com.studiobing.disabilities.client.ClientState;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/** The HUD list of everything you have, from both the original mod and this add-on, wrapped into columns to fit the screen. */
public final class PlusHudList {
   private static final int LINE_HEIGHT = 10;
   private static final int COLUMN_GAP = 8;
   /** Space kept free at the bottom: the hotbar and hearts, plus the timer line. */
   private static final int BOTTOM_RESERVE = 54;
   private static final int TOOLTIP_WIDTH = 180;

   private PlusHudList() {
   }

   private record Entry(String line, String name, String color, String description) {
   }

   public static void render(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null || mc.gui.hud.isHidden() || !ClientState.listVisible()) {
         return;
      }

      ClientState.setDyslexiaExempt(true);
      try {
         Font font = mc.font;
         boolean hidden = ClientState.has(Disability.ANOSOGNOSIA);
         List<Entry> entries = new ArrayList<>();

         for (Disability d : ClientState.mine()) {
            entries.add(new Entry(d.color + "\u2022 " + d.displayName, d.displayName, d.color, d.description));
         }

         for (PlusDisability d : PlusClientState.mine()) {
            entries.add(new Entry(d.listLine, d.displayName, d.color, d.description));
         }

         int x = 4;
         int y = 4;
         ctx.text(font, Component.literal("§c§lDisabilities §7(" + (hidden ? "?" : entries.size()) + ")"), x, y, -1);
         y += 12;

         Entry hovered = null;
         int hiddenMore = 0;
         if (!hidden) {
            int rows = Math.max(1, (mc.getWindow().getGuiScaledHeight() - y - BOTTOM_RESERVE) / LINE_HEIGHT);
            int guiWidth = mc.getWindow().getGuiScaledWidth();
            int[] widths = new int[entries.size()];
            int columns = (entries.size() + rows - 1) / rows;
            int[] columnWidth = new int[Math.max(1, columns)];

            for (int i = 0; i < entries.size(); i++) {
               widths[i] = font.width(entries.get(i).line());
               columnWidth[i / rows] = Math.max(columnWidth[i / rows], widths[i]);
            }

            // Only draw the columns that fit on screen; the rest are summarised instead of being clipped mid-word.
            int fit = 0;
            int used = x;
            for (int c = 0; c < columns; c++) {
               if (used + columnWidth[c] > guiWidth - 4) {
                  break;
               }

               used += columnWidth[c] + COLUMN_GAP;
               fit++;
            }

            fit = Math.max(1, fit);
            int shown = Math.min(entries.size(), fit * rows);
            hiddenMore = entries.size() - shown;
            int[] columnX = new int[fit];
            int cursor = x;
            for (int c = 0; c < fit; c++) {
               columnX[c] = cursor;
               cursor += columnWidth[c] + COLUMN_GAP;
            }

            for (int i = 0; i < shown; i++) {
               int colX = columnX[i / rows];
               int lineY = y + (i % rows) * LINE_HEIGHT;
               ctx.text(font, Component.literal(entries.get(i).line()), colX, lineY, -1);
               if (mouseX >= colX && mouseX <= colX + widths[i] && mouseY >= lineY - 1 && mouseY < lineY + LINE_HEIGHT - 1) {
                  hovered = entries.get(i);
               }
            }

            y += Math.min(shown, rows) * LINE_HEIGHT;
         }

         if (hiddenMore > 0) {
            ctx.text(font, Component.literal("\u00a78+" + hiddenMore + " more (use /disability list)"), x, y + 2, -1);
            y += LINE_HEIGHT;
         }

         int everything = Disability.values().length + PlusDisability.ALL.length;
         int ticks = ClientState.ticksUntilNext();
         if (!hidden && entries.size() >= everything) {
            ctx.text(font, Component.literal("§8All acquired"), x, y + 2, -1);
         } else if (!ClientState.timerEnabled()) {
            ctx.text(font, Component.literal("§8Timer off"), x, y + 2, -1);
         } else if (ticks >= 0) {
            int secs = ticks / 20;
            ctx.text(font, Component.literal("§7Next in §f" + secs / 60 + ":" + String.format("%02d", secs % 60)), x, y + 2, -1);
         }

         if (hovered != null) {
            List<FormattedCharSequence> tooltip = new ArrayList<>();
            tooltip.add(Component.literal(hovered.color() + hovered.name()).getVisualOrderText());
            tooltip.addAll(font.split(Component.literal("§7" + hovered.description()), TOOLTIP_WIDTH));
            ctx.setTooltipForNextFrame(font, tooltip, mouseX, mouseY);
            ctx.extractDeferredElements(mouseX, mouseY, 0.0F);
         }
      } finally {
         ClientState.setDyslexiaExempt(false);
      }
   }
}
