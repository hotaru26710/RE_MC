package org.hotaru.re_mc.deathreturn.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
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
    private static final ResourceLocation HEART_EMPTY = new ResourceLocation(Re_mc.MODID, "textures/gui/spirit_heart_empty.png");
    private static final ResourceLocation HEART_FULL = new ResourceLocation(Re_mc.MODID, "textures/gui/spirit_heart_full.png");
    private static final ResourceLocation HEART_HALF = new ResourceLocation(Re_mc.MODID, "textures/gui/spirit_heart_half.png");
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
            drawHeart(graphics, heartX, heartY, mode, color);
        }
    }

    private static void drawHeart(GuiGraphics graphics, int x, int y, int mode, int color) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(HEART_EMPTY, x, y, 0, 0, HEART_SIZE, HEART_SIZE, HEART_SIZE, HEART_SIZE);
        if (mode <= 0) {
            return;
        }
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        RenderSystem.setShaderColor(red, green, blue, 1.0F);
        graphics.blit(mode == 2 ? HEART_FULL : HEART_HALF, x, y, 0, 0, HEART_SIZE, HEART_SIZE, HEART_SIZE, HEART_SIZE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
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
