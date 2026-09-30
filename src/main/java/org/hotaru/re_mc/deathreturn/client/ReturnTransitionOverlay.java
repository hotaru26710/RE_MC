package org.hotaru.re_mc.deathreturn.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.hotaru.re_mc.Re_mc;

@Mod.EventBusSubscriber(modid = Re_mc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ReturnTransitionOverlay {
    private static int ticks;
    private static int totalTicks;
    private static boolean reveal;

    private ReturnTransitionOverlay() {
    }

    public static void start(int durationTicks, boolean revealMode) {
        if (durationTicks <= 0) {
            stop();
            return;
        }
        ticks = durationTicks;
        totalTicks = durationTicks;
        reveal = revealMode;
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof ReturnTransitionScreen)) {
            minecraft.setScreen(new ReturnTransitionScreen());
        }
    }

    public static void stop() {
        ticks = 0;
        totalTicks = 0;
        reveal = false;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof ReturnTransitionScreen) {
            minecraft.setScreen(null);
        }
    }

    public static float currentFade() {
        if (ticks <= 0) {
            return 0.0F;
        }
        if (totalTicks <= 0) {
            return 1.0F;
        }
        return Math.min(1.0F, ticks / 10.0F);
    }

    public static float revealProgress() {
        if (!reveal || totalTicks <= 0) {
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, 1.0F - ticks / (float) totalTicks));
    }

    public static boolean isReveal() {
        return reveal;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ticks <= 0) {
            return;
        }
        ticks--;
        if (ticks <= 0) {
            stop();
        }
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (ticks <= 0 || reveal) {
            return;
        }
        if (event.getNewScreen() instanceof LevelLoadingScreen || event.getNewScreen() instanceof ReceivingLevelScreen) {
            event.setNewScreen(new ReturnTransitionScreen());
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (ticks <= 0 || Minecraft.getInstance().screen instanceof ReturnTransitionScreen) {
            return;
        }
        ReturnTransitionScreen.renderWitchAura(event.getGuiGraphics(), currentFade(), revealProgress());
    }
}
