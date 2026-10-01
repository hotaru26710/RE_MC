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
    private static final int HEARTS = 10;
    private static final int HEART_SIZE = 8;
    private static final int HEART_STEP = 8;
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
        int y = height - 50 - (player.getArmorValue() > 0 ? 10 : 0);
        render(event.getGuiGraphics(), x, y);
    }

    private static void render(GuiGraphics graphics, int x, int y) {
        float time = Util.getMillis() / 1000.0F;
        boolean strain = spirit <= 75.0F;
        boolean danger = spirit < 30.0F;
        int jitter = danger ? (int) Math.round(Math.sin(time * 24.0D)) : 0;
        int color = colorFor(spirit);
        int outline = danger && ((int) (time * 16.0F) & 1) == 0 ? 0xFFFF6B8E : 0xFF0B0913;

        for (int i = 0; i < HEARTS; i++) {
            int heartX = x + i * HEART_STEP + jitter;
            int heartY = y;
            if (danger && (i + (int) (time * 10.0F)) % 2 == 0) {
                heartY -= 1;
            } else if (strain && (i + (int) (time * 7.0F)) % 3 == 0) {
                heartY -= 1;
            }

            float remaining = Math.max(0.0F, Math.min(10.0F, spirit - i * 10.0F));
            int mode = remaining >= 10.0F ? 2 : remaining >= 5.0F ? 1 : 0;
            drawHeart(graphics, heartX + 1, heartY + 1, mode == 0 ? 0xFF12101F : 0xFF08060C, 2);
            drawHeart(graphics, heartX, heartY, mode == 0 ? 0xFF25223A : color, mode);
            if (mode == 2 && (danger || (strain && i % 2 == 0))) {
                drawHeart(graphics, heartX, heartY, 0x66FFFFFF, 2);
            }
        }
    }

    private static void drawHeart(GuiGraphics graphics, int x, int y, int color, int mode) {
        if (mode == 0) {
            drawHeartShape(graphics, x, y, color, false);
            return;
        }
        if (mode == 2) {
            drawHeartShape(graphics, x, y, color, false);
            return;
        }
        drawHeartShape(graphics, x, y, color, true);
    }

    private static void drawHeartShape(GuiGraphics graphics, int x, int y, int color, boolean half) {
        for (int gx = 0; gx < 4; gx++) {
            for (int gy = 0; gy < 4; gy++) {
                if (!isHeartPixel(gx, gy)) {
                    continue;
                }
                if (half && gx >= 2) {
                    continue;
                }
                int px = x + gx * 2;
                int py = y + gy * 2;
                graphics.fill(px, py, px + 2, py + 2, color);
            }
        }
    }

    private static boolean isHeartPixel(int x, int y) {
        return switch (y) {
            case 0 -> x == 1 || x == 2;
            case 1, 2 -> x >= 0 && x <= 3;
            case 3 -> x == 1 || x == 2;
            default -> false;
        };
    }

    private static int colorFor(float value) {
        float t = Math.max(0.0F, Math.min(1.0F, value / 100.0F));
        int low = 0xFF668F;
        int mid = 0xC47CFF;
        int high = 0x4FF5FF;
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
