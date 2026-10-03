package org.hotaru.re_mc.deathreturn.client;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.hotaru.re_mc.Re_mc;

@Mod.EventBusSubscriber(modid = Re_mc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class CombinedStatusHud {
    private static final float COLLAPSED_WIDTH = 46.0F;
    private static final float EXPANDED_WIDTH = 116.0F;
    private static final int HEIGHT = 12;
    private static final int COOLDOWN_TOTAL_TICKS = 20 * 60 * 5;
    private static float animatedWidth = COLLAPSED_WIDTH;
    private static int lastCooldownTicks;
    private static int readyFlashTicks;

    private CombinedStatusHud() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        int cooldown = UnseenHandClientEffects.cooldownTicks();
        if (UnseenHandClientEffects.cooldownEnabled() && lastCooldownTicks > 0 && cooldown <= 0) {
            readyFlashTicks = 20;
        }
        lastCooldownTicks = cooldown;
        if (readyFlashTicks > 0) {
            readyFlashTicks--;
        }

        boolean expanded = SpiritHudOverlay.spiritValue() < 99.5F
                || SpiritHudOverlay.witchScentValue() > 0.5F
                || (UnseenHandClientEffects.cooldownEnabled() && cooldown > 0)
                || readyFlashTicks > 0;
        float target = SpiritHudOverlay.isVisible() && expanded ? EXPANDED_WIDTH : COLLAPSED_WIDTH;
        animatedWidth += (target - animatedWidth) * 0.32F;
        if (Math.abs(target - animatedWidth) < 0.25F) {
            animatedWidth = target;
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (!SpiritHudOverlay.isVisible() || player == null || player.isCreative() || player.isSpectator() || minecraft.screen instanceof ReturnTransitionScreen) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int right = width / 2 + 91;
        int y = height - 50 - (player.getArmorValue() > 0 ? 10 : 0) - 13;
        if (y < 3) {
            return;
        }

        float spirit = SpiritHudOverlay.spiritValue();
        float scent = SpiritHudOverlay.witchScentValue();
        int cooldown = UnseenHandClientEffects.cooldownTicks();
        boolean cooldownEnabled = UnseenHandClientEffects.cooldownEnabled();
        float time = Util.getMillis() / 1000.0F;
        int jitter = spirit <= 25.0F && ((int) (time * 18.0F) & 1) == 0 ? -1 : 0;
        int panelWidth = Math.round(animatedWidth);
        int left = right - panelWidth + jitter;
        boolean expanded = panelWidth > 82;

        int outer = 0xD9040610;
        int border = 0xFF151A24;
        int inner = 0xE00B0D16;
        graphics.fill(left - 1, y - 1, right + 1, y + HEIGHT + 1, outer);
        graphics.fill(left, y, right, y + HEIGHT, border);
        graphics.fill(left + 1, y + 1, right - 1, y + HEIGHT - 1, inner);
        graphics.fill(left + 1, y + 1, right - 1, y + 2, 0x804AF7FF);

        int contentLeft = left + 2;
        int contentWidth = Math.max(12, panelWidth - 4);
        int segmentWidth = Math.max(10, contentWidth / 3);
        renderSegment(graphics, minecraft, expanded, contentLeft, y, segmentWidth, Segment.SPIRIT, spirit / 100.0F, spiritColor(spirit), number(spirit), time, spirit <= 25.0F, false);
        renderSegment(graphics, minecraft, expanded, contentLeft + segmentWidth, y, segmentWidth, Segment.SCENT, scent / 100.0F, 0xFFB45CFF, number(scent), time, false, scent > 30.0F);
        boolean cooling = cooldownEnabled && cooldown > 0;
        float handRatio = cooling ? 1.0F - Math.min(1.0F, cooldown / (float) COOLDOWN_TOTAL_TICKS) : 1.0F;
        String handText = cooling ? formatCooldown(cooldown) : "OK";
        renderSegment(graphics, minecraft, expanded, contentLeft + segmentWidth * 2, y, contentWidth - segmentWidth * 2, Segment.HAND, handRatio, 0xFF5AF2FF, handText, time, false, readyFlashTicks > 0);
    }

    private static void renderSegment(GuiGraphics graphics, Minecraft minecraft, boolean expanded, int x, int y, int width, Segment segment, float ratio, int color, String text, float time, boolean dangerPulse, boolean sweep) {
        ratio = Math.max(0.0F, Math.min(1.0F, ratio));
        int iconX = x + 1;
        int iconY = y + 4;
        drawIcon(graphics, segment, iconX, iconY, color);
        int barX = x + (expanded ? 6 : 0);
        int barY = y + (expanded ? 9 : 7);
        int barWidth = Math.max(6, width - (expanded ? 8 : 2));
        int barColor = dangerPulse && ((int) (time * 12.0F) & 1) == 0 ? 0xFFFF4E6C : color;
        graphics.fill(barX, barY, barX + barWidth, barY + 2, 0xFF171A25);
        int filled = Math.round(barWidth * ratio);
        graphics.fill(barX, barY, barX + filled, barY + 2, barColor);
        if (dangerPulse) {
            int alpha = 45 + (int) (55.0F * (0.5F + 0.5F * (float) Math.sin(time * 8.0D)));
            graphics.fill(x, y + 2, x + width, y + HEIGHT - 2, (alpha << 24) | 0xFF3A4D);
        }
        if (sweep) {
            int scanWidth = Math.max(4, width / 3);
            int scan = x + (int) ((time * 36.0F) % Math.max(1, width));
            graphics.fill(scan, y + 2, Math.min(x + width, scan + scanWidth), y + HEIGHT - 2, 0x4AFFFFFF & 0x4AFFFFFF);
        }
        if (expanded) {
            graphics.drawString(minecraft.font, text, x + 6, y + 2, color, true);
        }
    }

    private static void drawIcon(GuiGraphics graphics, Segment segment, int x, int y, int color) {
        switch (segment) {
            case SPIRIT -> {
                graphics.fill(x + 1, y, x + 3, y + 1, color);
                graphics.fill(x, y + 1, x + 4, y + 2, color);
                graphics.fill(x + 1, y + 2, x + 3, y + 3, color);
            }
            case SCENT -> {
                graphics.fill(x + 1, y, x + 2, y + 1, color);
                graphics.fill(x, y + 1, x + 3, y + 3, color);
                graphics.fill(x + 1, y + 3, x + 2, y + 4, color);
            }
            case HAND -> {
                graphics.fill(x, y, x + 1, y + 4, color);
                graphics.fill(x + 2, y - 1, x + 3, y + 4, color);
                graphics.fill(x + 4, y, x + 5, y + 4, color);
                graphics.fill(x, y + 4, x + 5, y + 5, color);
            }
        }
    }

    private static int spiritColor(float spirit) {
        float t = Math.max(0.0F, Math.min(1.0F, spirit / 100.0F));
        int low = 0x668F;
        int mid = 0xC47CFF;
        int high = 0x4FF5FF;
        return t < 0.5F ? lerpColor(low, mid, t * 2.0F) : lerpColor(mid, high, (t - 0.5F) * 2.0F);
    }

    private static int lerpColor(int from, int to, float t) {
        int r = (int) (((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * t);
        int g = (int) (((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * t);
        int b = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return (r << 16) | (g << 8) | b;
    }

    private static String number(float value) {
        return Integer.toString(Math.round(value));
    }

    private static String formatCooldown(int ticks) {
        int totalSeconds = Math.max(0, ticks / 20);
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    private enum Segment {
        SPIRIT,
        SCENT,
        HAND
    }
}