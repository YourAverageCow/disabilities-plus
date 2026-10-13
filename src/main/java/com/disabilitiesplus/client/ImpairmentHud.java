package com.disabilitiesplus.client;

import com.disabilitiesplus.PlusDisability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ImpairmentHud {
    private static final float VIGNETTE_DEPTH = 0.3F;
    private static final int VIGNETTE_EDGE_ALPHA = 235;
    private static final float MIGRAINE_MAX_ALPHA = 0.59F;
    private static final float OVERLOAD_MAX_ALPHA = 0.5F;
    private static final float BLINK_MAX_ALPHA = 0.95F;
    private static final float PAIN_MAX_ALPHA = 0.3F;
    private static final float SOOT_MAX_STRENGTH = 0.9F;
    private static final int SOOT_RGB = 0x1A1410;
    private static final float FEVER_MAX_ALPHA = 0.28F;
    private static final int FEVER_RGB = 0xFF7A00;
    private static final float LID_MAX_COVER = 0.92F;
    private static final float LID_ALPHA = 0.97F;

    private ImpairmentHud() {
    }

    private static int argb(float alpha, int rgb) {
        return (int)(alpha * 255.0F) << 24 | rgb;
    }

    public static void render(GuiGraphicsExtractor ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        float dim = PlusClientState.nightDim();
        if (dim > 0.0F) {
            ctx.fill(0, 0, w, h, argb(dim, 0));
        }

        float strength = Math.max(PlusClientState.has(PlusDisability.TUNNEL_VISION) ? 1.0F : 0.0F, PlusClientState.dread());
        if (strength > 0.01F) {
            renderVignette(ctx, w, h, strength, 0);
        }

        float soot = PlusClientState.soot() * SOOT_MAX_STRENGTH;
        if (soot > 0.01F) {
            renderVignette(ctx, w, h, soot, SOOT_RGB);
        }

        float fever = PlusClientState.feverFade();
        if (fever > 0.0F) {
            float flush = 0.5F + 0.5F * (float)Math.sin(PlusClientState.feverTicks() * 0.08);
            ctx.fill(0, 0, w, h, argb(FEVER_MAX_ALPHA * fever * (0.6F + 0.4F * flush), FEVER_RGB));
        }

        int hurting = Math.max(PlusClientState.painTicks(), PlusClientState.agonyTicks());
        if (hurting > 0) {
            float pulse = 0.5F + 0.5F * (float)Math.sin(hurting * 0.5);
            ctx.fill(0, 0, w, h, argb(PAIN_MAX_ALPHA * pulse, 11141120));
        }

        float overload = PlusClientState.overloadFade();
        if (overload > 0.0F) {
            ctx.fill(0, 0, w, h, argb(overload * OVERLOAD_MAX_ALPHA, 0));
        }

        float blink = PlusClientState.blinkFade();
        if (blink > 0.0F) {
            ctx.fill(0, 0, w, h, argb(blink * BLINK_MAX_ALPHA, 0));
        }

        float lid = PlusClientState.lidLevel();
        if (lid > 0.0F) {
            int band = (int)(h * 0.5F * lid * LID_MAX_COVER);
            ctx.fill(0, 0, w, band, argb(LID_ALPHA, 0));
            ctx.fill(0, h - band, w, h, argb(LID_ALPHA, 0));
        }

        float nap = PlusClientState.napFade();
        if (nap > 0.0F) {
            ctx.fill(0, 0, w, h, argb(nap, 0));
        }

        float pulse = PlusClientState.migrainePulse();
        if (pulse > 0.0F) {
            ctx.fill(0, 0, w, h, argb(pulse * MIGRAINE_MAX_ALPHA, 16777215));
        }
    }

    private static void renderVignette(GuiGraphicsExtractor ctx, int w, int h, float strength, int rgb) {
        int depth = (int)(Math.min(w, h) * VIGNETTE_DEPTH * (0.6F + 0.4F * strength));
        int edgeAlpha = (int)(VIGNETTE_EDGE_ALPHA * strength);
        int edge = edgeAlpha << 24 | rgb;
        ctx.fillGradient(0, 0, w, depth, edge, 0);
        ctx.fillGradient(0, h - depth, w, h, 0, edge);

        for (int i = 0; i < depth; i += 2) {
            int c = (int)(edgeAlpha * (1.0F - (float)i / depth)) << 24 | rgb;
            ctx.fill(i, 0, i + 2, h, c);
            ctx.fill(w - i - 2, 0, w - i, h, c);
        }
    }
}
