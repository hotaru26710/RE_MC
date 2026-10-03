package org.hotaru.re_mc.deathreturn.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.hotaru.re_mc.Re_mc;
import org.hotaru.re_mc.deathreturn.network.ReturnTransitionPhase;

@Mod.EventBusSubscriber(modid = Re_mc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ReturnTransitionOverlay {
    private static final int REVEAL_DELAY_TICKS = 9;
    private static ReturnTransitionPhase transitionPhase = ReturnTransitionPhase.STOP;
    private static int transitionTicks;
    private static int transitionTotalTicks;
    private static float transitionIntensity = 1.0F;
    private static int revealDelayTicks;
    private static int auraTicks;
    private static int auraTotalTicks;
    private static float auraIntensity = 1.0F;
    private static int heartbeatTicker;

    private ReturnTransitionOverlay() {
    }

    public static void start(ReturnTransitionPhase phase, int durationTicks, float intensity) {
        float clampedIntensity = Math.max(0.0F, Math.min(1.5F, intensity));
        switch (phase) {
            case STOP -> stop();
            case BLACKOUT -> startBlackout(durationTicks, clampedIntensity);
            case REVEAL -> startReveal(durationTicks, clampedIntensity);
            case AURA -> startAura(durationTicks, clampedIntensity);
        }
    }

    private static void startBlackout(int durationTicks, float intensity) {
        transitionPhase = ReturnTransitionPhase.BLACKOUT;
        transitionTicks = Math.max(0, durationTicks);
        transitionTotalTicks = transitionTicks;
        transitionIntensity = intensity;
        revealDelayTicks = 0;
        heartbeatTicker = 0;
        playBlackoutSounds();
        showScreen();
    }

    private static void startReveal(int durationTicks, float intensity) {
        transitionPhase = ReturnTransitionPhase.REVEAL;
        transitionTicks = Math.max(1, durationTicks);
        transitionTotalTicks = transitionTicks;
        transitionIntensity = intensity;
        revealDelayTicks = REVEAL_DELAY_TICKS;
        showScreen();
    }

    private static void startAura(int durationTicks, float intensity) {
        auraTicks = Math.max(0, durationTicks);
        auraTotalTicks = auraTicks;
        auraIntensity = intensity;
    }

    public static void stop() {
        transitionPhase = ReturnTransitionPhase.STOP;
        transitionTicks = 0;
        transitionTotalTicks = 0;
        transitionIntensity = 0.0F;
        revealDelayTicks = 0;
        auraTicks = 0;
        auraTotalTicks = 0;
        auraIntensity = 0.0F;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof ReturnTransitionScreen) {
            minecraft.setScreen(null);
        }
    }

    private static void showScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof ReturnTransitionScreen)) {
            minecraft.setScreen(new ReturnTransitionScreen());
        }
    }

    public static float currentFade() {
        if (transitionPhase == ReturnTransitionPhase.BLACKOUT) {
            if (transitionTotalTicks <= 0) {
                return 1.0F;
            }
            float elapsed = transitionTotalTicks - Math.max(0, transitionTicks);
            return Math.min(1.0F, elapsed / 10.0F);
        }
        if (transitionPhase == ReturnTransitionPhase.REVEAL) {
            if (revealDelayTicks > 0) {
                return 1.0F;
            }
            return Math.max(0.0F, 1.0F - revealProgress());
        }
        return 0.0F;
    }

    public static float revealProgress() {
        if (transitionPhase != ReturnTransitionPhase.REVEAL || revealDelayTicks > 0 || transitionTotalTicks <= 0) {
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, 1.0F - transitionTicks / (float) transitionTotalTicks));
    }

    public static float cameraPulse() {
        if (transitionPhase != ReturnTransitionPhase.REVEAL || revealDelayTicks > 0) {
            return 0.0F;
        }
        float progress = revealProgress();
        float pulse = progress < 0.18F ? progress / 0.18F : Math.max(0.0F, 1.0F - (progress - 0.18F) / 0.82F);
        return pulse * transitionIntensity;
    }

    public static boolean isTransitionActive() {
        return transitionPhase == ReturnTransitionPhase.BLACKOUT || transitionPhase == ReturnTransitionPhase.REVEAL;
    }

    public static boolean isReveal() {
        return transitionPhase == ReturnTransitionPhase.REVEAL;
    }

    public static float transitionIntensity() {
        return transitionIntensity;
    }

    private static float auraAlpha() {
        if (auraTicks <= 0 || auraTotalTicks <= 0) {
            return 0.0F;
        }
        int elapsed = auraTotalTicks - auraTicks;
        float strongToAmbient = elapsed < 20 * 30 ? 1.0F : 0.42F;
        float fadeOut = Math.min(1.0F, auraTicks / (20.0F * 30.0F));
        return Math.max(0.0F, strongToAmbient * fadeOut * auraIntensity);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        stop();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (auraTicks > 0) {
            auraTicks--;
        }
        if (transitionPhase == ReturnTransitionPhase.BLACKOUT) {
            if (transitionTicks > 0) {
                transitionTicks--;
            }
            heartbeatTicker++;
            if (heartbeatTicker % 32 == 0) {
                playUi(SoundEvents.WARDEN_HEARTBEAT, 0.74F, 0.30F);
            }
            return;
        }
        if (transitionPhase != ReturnTransitionPhase.REVEAL) {
            return;
        }
        if (revealDelayTicks > 0) {
            heartbeatTicker++;
            revealDelayTicks--;
            if (revealDelayTicks == 0) {
                playRevealSounds();
            }
            return;
        }
        if (transitionTicks > 0) {
            transitionTicks--;
        }
        if (transitionTicks <= 0) {
            transitionPhase = ReturnTransitionPhase.STOP;
            transitionIntensity = 0.0F;
            revealDelayTicks = 0;
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof ReturnTransitionScreen) {
                minecraft.setScreen(null);
            }
        }
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!isTransitionActive()) {
            return;
        }
        if (event.getNewScreen() instanceof LevelLoadingScreen || event.getNewScreen() instanceof ReceivingLevelScreen) {
            event.setNewScreen(new ReturnTransitionScreen());
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (Minecraft.getInstance().screen instanceof ReturnTransitionScreen) {
            return;
        }
        if (isTransitionActive()) {
            ReturnTransitionScreen.renderWitchAura(event.getGuiGraphics(), currentFade(), revealProgress(), transitionIntensity);
            return;
        }
        float auraAlpha = auraAlpha();
        if (auraAlpha > 0.001F) {
            ReturnTransitionScreen.renderPersistentAura(event.getGuiGraphics(), auraAlpha);
        }
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float pulse = cameraPulse();
        if (pulse <= 0.001F) {
            return;
        }
        double wave = Math.sin(revealProgress() * Math.PI * 5.0D);
        event.setYaw(event.getYaw() + (float) (wave * 0.32D * pulse));
        event.setPitch(event.getPitch() + (float) (0.16D * pulse));
        event.setRoll(event.getRoll() + (float) (wave * 0.22D * pulse));
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        float pulse = cameraPulse();
        if (pulse <= 0.001F) {
            return;
        }
        event.setFOV(event.getFOV() * (1.0D - 0.075D * pulse));
    }

    private static void playBlackoutSounds() {
        playUi(SoundEvents.WARDEN_HEARTBEAT, 0.72F, 0.45F);
        playUi(SoundEvents.ELDER_GUARDIAN_CURSE, 0.58F, 0.18F);
        playUi(SoundEvents.AMBIENT_CAVE.value(), 0.66F, 0.22F);
    }

    private static void playRevealSounds() {
        playUi(SoundEvents.WARDEN_SONIC_BOOM, 1.08F, 0.72F);
        playUi(SoundEvents.PORTAL_TRAVEL, 0.62F, 0.78F);
        playUi(SoundEvents.BELL_RESONATE, 0.52F, 0.42F);
        playUi(SoundEvents.TOTEM_USE, 1.72F, 0.18F);
    }

    private static void playUi(SoundEvent sound, float pitch, float volume) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }
}