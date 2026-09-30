package org.hotaru.re_mc.deathreturn.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.hotaru.re_mc.Re_mc;

public final class ReturnTransitionScreen extends Screen {
    private static final ResourceLocation FOG = new ResourceLocation(Re_mc.MODID, "textures/gui/death_return_fog.png");
    private static final ResourceLocation MASK = new ResourceLocation(Re_mc.MODID, "textures/gui/death_return_mask.png");
    private static final int TEXTURE_SIZE = 256;

    public ReturnTransitionScreen() {
        super(Component.empty());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderWitchAura(guiGraphics, ReturnTransitionOverlay.currentFade(), ReturnTransitionOverlay.revealProgress());
    }

    public static void renderWitchAura(GuiGraphics guiGraphics, float fade, float revealProgress) {
        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        float minSize = Math.min(width, height);
        float maxSize = Math.max(width, height);
        int blackAlpha = (int) (255.0F * Math.max(0.0F, Math.min(1.0F, fade))) << 24;

        if (revealProgress <= 0.0F) {
            guiGraphics.fill(0, 0, width, height, blackAlpha);
        } else {
            float blast = 1.0F - (float) Math.pow(1.0F - revealProgress, 3.0D);
            int maskSize = (int) (minSize * (0.52F + blast * 2.05F));
            float holeCenterX = width / 2.0F;
            float holeCenterY = height * (0.80F - blast * 0.08F);
            int left = (int) (holeCenterX - maskSize / 2.0F);
            int top = (int) (holeCenterY - maskSize / 2.0F);
            int right = left + maskSize;
            int bottom = top + maskSize;
            int edgeAlpha = (int) (255.0F * fade * (1.0F - blast * 0.55F)) << 24;

            guiGraphics.fill(0, 0, width, Math.max(0, top), edgeAlpha);
            guiGraphics.fill(0, Math.min(height, bottom), width, height, edgeAlpha);
            guiGraphics.fill(0, Math.max(0, top), Math.max(0, left), Math.min(height, bottom), edgeAlpha);
            guiGraphics.fill(Math.min(width, right), Math.max(0, top), width, Math.min(height, bottom), edgeAlpha);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, fade * (1.0F - blast * 0.72F));
            guiGraphics.blit(MASK, left, top, 0, 0, maskSize, maskSize, TEXTURE_SIZE, TEXTURE_SIZE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }

        renderPurpleWisps(guiGraphics, width, height, minSize, fade, revealProgress);
    }

    private static void renderPurpleWisps(GuiGraphics guiGraphics, int width, int height, float minSize, float fade, float revealProgress) {
        float time = Util.getMillis() / 1000.0F;
        float blast = 1.0F - (float) Math.pow(1.0F - revealProgress, 3.0D);
        float centerX = width / 2.0F;
        float centerY = revealProgress > 0.0F ? height * (0.80F - blast * 0.08F) : height / 2.0F;
        float baseSize = minSize * (revealProgress > 0.0F ? 0.24F + blast * 0.18F : 0.36F);
        float orbitBase = revealProgress > 0.0F ? minSize * (0.06F + blast * 0.36F) : minSize * 0.16F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, centerY, 0.0F);

        for (int i = 0; i < 14; i++) {
            float phase = i * 0.55F;
            float angle = time * (0.55F + blast * 1.1F) + phase;
            float orbit = orbitBase * (0.78F + 0.16F * (i % 7));
            float x = (float) Math.cos(angle) * orbit;
            float y = (float) Math.sin(angle * 1.2F + 0.4F) * orbit * 1.25F;
            float scale = 0.62F + 0.18F * (float) Math.sin(time * 1.0F + phase);
            float alpha = (0.16F + 0.10F * (float) Math.sin(time * 1.5F + phase)) * fade * (1.0F - revealProgress * 0.65F);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x, y, 0.0F);
            guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees((float) Math.toDegrees(angle) + 90.0F));
            guiGraphics.pose().scale(0.42F * scale, 1.35F * scale, 1.0F);
            RenderSystem.setShaderColor(0.78F, 0.38F, 1.0F, alpha);
            int size = (int) baseSize;
            guiGraphics.blit(FOG, -size / 2, -size / 2, 0, 0, size, size, TEXTURE_SIZE, TEXTURE_SIZE);
            guiGraphics.pose().popPose();
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.pose().popPose();
        RenderSystem.disableBlend();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
