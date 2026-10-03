package org.hotaru.re_mc.deathreturn.client;

import com.mojang.blaze3d.shaders.AbstractUniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.hotaru.re_mc.Re_mc;

@Mod.EventBusSubscriber(modid = Re_mc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SpiritHudOverlay {
    private static final ResourceLocation DESATURATE_EFFECT = new ResourceLocation("minecraft", "shaders/post/desaturate.json");
    private static float spirit = 100.0F;
    private static float witchScent;
    private static boolean visible;

    private SpiritHudOverlay() {
    }

    public static void setSpirit(float value, float scent, boolean enabled) {
        spirit = Math.max(0.0F, Math.min(100.0F, value));
        witchScent = Math.max(0.0F, Math.min(100.0F, scent));
        visible = enabled;
        SpiritGlitchController.setSpirit(spirit, witchScent, enabled);
    }

    public static void clear() {
        spirit = 100.0F;
        witchScent = 0.0F;
        visible = false;
        SpiritGlitchController.clear();
        shutdownSpiritDesaturation();
    }

    public static float spiritValue() {
        return spirit;
    }

    public static float witchScentValue() {
        return witchScent;
    }

    public static boolean isVisible() {
        return visible;
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        updateSpiritDesaturation();
    }

    private static void updateSpiritDesaturation() {
        Minecraft minecraft = Minecraft.getInstance();
        GameRenderer renderer = minecraft.gameRenderer;
        PostChain current = renderer.currentEffect();
        boolean ours = current != null && DESATURATE_EFFECT.toString().equals(current.getName());
        Player player = minecraft.player;
        boolean shouldApply = visible && player != null && !player.isCreative() && !player.isSpectator()
                && !(minecraft.screen instanceof ReturnTransitionScreen) && spirit < 99.5F;

        if (!shouldApply) {
            if (ours) {
                renderer.shutdownEffect();
            }
            return;
        }
        if (current != null && !ours) {
            return;
        }
        if (!ours) {
            renderer.loadEffect(DESATURATE_EFFECT);
            current = renderer.currentEffect();
        }
        if (current == null) {
            return;
        }

        float t = Math.max(0.0F, Math.min(1.0F, spirit / 100.0F));
        float saturation = 0.15F + 0.85F * t;
        float brightness = 0.72F + 0.28F * t;
        for (PostPass pass : current.passes) {
            if (!"color_convolve".equals(pass.getName())) {
                continue;
            }
            AbstractUniform saturationUniform = pass.getEffect().safeGetUniform("Saturation");
            saturationUniform.set(saturation);
            AbstractUniform colorScale = pass.getEffect().safeGetUniform("ColorScale");
            colorScale.set(brightness, brightness, brightness);
            AbstractUniform offset = pass.getEffect().safeGetUniform("Offset");
            offset.set(0.0F, 0.0F, 0.0F);
            return;
        }
    }

    private static void shutdownSpiritDesaturation() {
        GameRenderer renderer = Minecraft.getInstance().gameRenderer;
        PostChain current = renderer.currentEffect();
        if (current != null && DESATURATE_EFFECT.toString().equals(current.getName())) {
            renderer.shutdownEffect();
        }
    }
}