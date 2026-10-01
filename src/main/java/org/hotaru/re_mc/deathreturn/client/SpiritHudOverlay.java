package org.hotaru.re_mc.deathreturn.client;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.hotaru.re_mc.Re_mc;

@Mod.EventBusSubscriber(modid = Re_mc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SpiritHudOverlay {
    private static final int SEGMENTS = 10;
    private static final int SEGMENT_WIDTH = 8;
    private static final int SEGMENT_HEIGHT = 8;
    private static final int SEGMENT_GAP = 0;
    private static float spirit = 100.0F;
    private static boolean visible;

    private SpiritHudOverlay() {
    }

    public static void setSpirit(float value, boolean enabled) {
        spirit = Math.max(0.0F, Math.min(100.0F, value));
        visible = enabled;
    }

    public static void clear() {
        spirit = 100.0F;
        visible = false;
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (!visible || player == null || player.isCreative() || player.isSpectator() || minecraft.screen instanceof ReturnTransitionScreen) {
            return;
        }

        int width = event.getGuiGraphics().guiWidth();
        int height = event.getGuiGraphics().guiHeight();
        int totalWidth = SEGMENTS * SEGMENT_WIDTH + (SEGMENTS - 1) * SEGMENT_GAP;
        int x = width / 2 - 91;
        int y = height - 50 - (player.getArmorValue() > 0 ? 10 : 0);
        render(event.getGuiGraphics(), x, y, totalWidth);
    }

    private static void render(GuiGraphics graphics, int x, int y, int totalWidth) {
        float time = Util.getMillis() / 1000.0F;
        boolean strain = spirit <= 75.0F;
        boolean danger = spirit < 30.0F;
        int jitter = danger ? (int) Math.round(Math.sin(time * 24.0F)) : 0;
        int trackX = x + jitter;
        int trackY = y + jitter;

        drawPixelMark(graphics, x + jitter, y - 10, danger, strain, time);

        graphics.fill(trackX - 2, trackY - 2, trackX + trackWidth() + 2, trackY + SEGMENT_HEIGHT + 2, 0xE0100D18);
        graphics.fill(trackX - 1, trackY - 1, trackX + trackWidth() + 1, trackY + SEGMENT_HEIGHT + 1, danger && ((int) (time * 14.0F) & 1) == 0 ? 0xFFFF4C73 : 0xFF30294B);
        graphics.fill(trackX, trackY, trackX + trackWidth(), trackY + SEGMENT_HEIGHT, 0xFF171426);

        float pulse = strain ? 0.84F + 0.20F * (float) Math.abs(Math.sin(time * (danger ? 11.0F : 5.0F))) : 1.0F;
        int sweep = spirit < 50.0F ? (int) (time * (danger ? 15.0F : 7.0F)) % SEGMENTS : -1;

        for (int i = 0; i < SEGMENTS; i++) {
            int segmentX = trackX + i * (SEGMENT_WIDTH + SEGMENT_GAP);
            int segmentY = trackY;
            drawSegment(graphics, segmentX, segmentY, 0.0F, false);
            float value = Math.max(0.0F, Math.min(10.0F, spirit - i * 10.0F));
            if (value > 0.0F) {
                float brightness = pulse;
                if (danger && (i + (int) (time * 8.0F)) % 3 == 0) {
                    brightness *= 1.35F;
                } else if (strain && i % 2 == 0) {
                    brightness *= 1.12F;
                }
                drawSegment(graphics, segmentX, segmentY, value / 10.0F, true);
                if (brightness > 1.0F) {
                    int overlayAlpha = (int) Math.min(105.0F, (brightness - 1.0F) * 180.0F);
                    graphics.fill(segmentX + 1, segmentY + 1, segmentX + SEGMENT_WIDTH - 1, segmentY + SEGMENT_HEIGHT - 1, overlayAlpha << 24 | 0x00FFFFFF);
                }
                if (i == sweep) {
                    graphics.fill(segmentX + 1, segmentY + 1, segmentX + SEGMENT_WIDTH - 1, segmentY + SEGMENT_HEIGHT - 1, danger ? 0x77FF9AB4 : 0x55FFFFFF);
                }
            }
        }
    }

    private static void drawPixelMark(GuiGraphics graphics, int x, int y, boolean danger, boolean strain, float time) {
        int color = colorFor(spirit);
        int alpha = danger && ((int) (time * 12.0F) & 1) == 0 ? 0x80 : 0xFF;
        int c = alpha << 24 | (color & 0x00FFFFFF);
        int shadow = 0xAA000000;
        graphics.fill(x + 3, y, x + 5, y + 2, c);
        graphics.fill(x + 1, y + 2, x + 3, y + 4, c);
        graphics.fill(x + 3, y + 2, x + 5, y + 4, 0xFFFFFFFF);
        graphics.fill(x + 5, y + 2, x + 7, y + 4, c);
        graphics.fill(x, y + 4, x + 2, y + 6, c);
        graphics.fill(x + 2, y + 4, x + 4, y + 6, c);
        graphics.fill(x + 4, y + 4, x + 6, y + 6, c);
        graphics.fill(x + 6, y + 4, x + 8, y + 6, c);
        graphics.fill(x + 2, y + 6, x + 4, y + 8, c);
        graphics.fill(x + 4, y + 6, x + 6, y + 8, c);
        if (strain) {
            graphics.fill(x + 1, y + 6, x + 7, y + 7, shadow);
        }
    }

    private static void drawSegment(GuiGraphics graphics, int x, int y, float fill, boolean filled) {
        int base = filled ? colorFor(spirit) : 0xFF25223A;
        graphics.fill(x, y, x + SEGMENT_WIDTH, y + SEGMENT_HEIGHT, 0xFF0B0913);
        graphics.fill(x + 1, y + 1, x + SEGMENT_WIDTH - 1, y + SEGMENT_HEIGHT - 1, base);
        if (filled && fill > 0.0F) {
            int innerWidth = SEGMENT_WIDTH - 2;
            int filledWidth = Math.max(1, Math.round(innerWidth * fill));
            graphics.fill(x + 1, y + 1, x + 1 + filledWidth, y + SEGMENT_HEIGHT - 1, base);
            graphics.fill(x + 1, y + 1, x + 1 + filledWidth, y + 2, 0x66FFFFFF);
            graphics.fill(x + 1, y + SEGMENT_HEIGHT - 2, x + 1 + filledWidth, y + SEGMENT_HEIGHT - 1, 0x44000000);
        }
    }

    private static int trackWidth() {
        return SEGMENTS * SEGMENT_WIDTH + (SEGMENTS - 1) * SEGMENT_GAP;
    }

    private static int colorFor(float value) {
        float t = Math.max(0.0F, Math.min(1.0F, value / 100.0F));
        int low = 0xFF3B5C;
        int mid = 0x9B5CFF;
        int high = 0x33E6FF;
        if (t < 0.5F) {
            return lerpColor(low, mid, t * 2.0F);
        }
        return lerpColor(mid, high, (t - 0.5F) * 2.0F);
    }

    private static int lerpColor(int from, int to, float t) {
        int r = (int) (((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * t);
        int g = (int) (((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * t);
        int b = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return (r << 16) | (g << 8) | b;
    }
}
