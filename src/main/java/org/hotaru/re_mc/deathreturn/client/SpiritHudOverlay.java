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
    private static final int SEGMENT_WIDTH = 7;
    private static final int SEGMENT_HEIGHT = 5;
    private static final int SEGMENT_GAP = 1;
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
        int x = width / 2 - 91;
        int y = height - 49 - (player.getArmorValue() > 0 ? 10 : 0);
        render(event.getGuiGraphics(), x, y);
    }

    private static void render(GuiGraphics graphics, int x, int y) {
        int totalWidth = SEGMENTS * SEGMENT_WIDTH + (SEGMENTS - 1) * SEGMENT_GAP;
        graphics.fill(x - 1, y - 1, x + totalWidth + 1, y + SEGMENT_HEIGHT + 1, 0xAA000000);
        graphics.fill(x, y, x + totalWidth, y + 1, 0x66442A66);

        float time = Util.getMillis() / 1000.0F;
        float pulse = spirit < 75.0F ? 0.78F + 0.22F * (float) Math.sin(time * 4.0F) : 1.0F;
        int sweep = spirit < 50.0F ? (int) (time * 7.0F) % SEGMENTS : -1;
        int jitter = spirit <= 25.0F ? (int) Math.round(Math.sin(time * 17.0F)) : 0;
        int color = colorFor(spirit);

        for (int i = 0; i < SEGMENTS; i++) {
            int segmentX = x + i * (SEGMENT_WIDTH + SEGMENT_GAP) + jitter;
            float segmentValue = Math.max(0.0F, Math.min(10.0F, spirit - i * 10.0F));
            int background = 0x5524122A;
            graphics.fill(segmentX, y, segmentX + SEGMENT_WIDTH, y + SEGMENT_HEIGHT, background);
            if (segmentValue > 0.0F) {
                int filledWidth = Math.max(1, Math.round(SEGMENT_WIDTH * segmentValue / 10.0F));
                int alpha = (int) (255.0F * pulse);
                int segmentColor = (alpha << 24) | (color & 0x00FFFFFF);
                graphics.fill(segmentX, y, segmentX + filledWidth, y + SEGMENT_HEIGHT, segmentColor);
            }
            if (i == sweep) {
                graphics.fill(segmentX, y, segmentX + SEGMENT_WIDTH, y + SEGMENT_HEIGHT, 0x44FFFFFF);
            }
        }
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
