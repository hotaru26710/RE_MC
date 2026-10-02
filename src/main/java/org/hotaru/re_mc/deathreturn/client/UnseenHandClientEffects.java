package org.hotaru.re_mc.deathreturn.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.hotaru.re_mc.Re_mc;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Re_mc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class UnseenHandClientEffects {
    private static final List<Integer> TARGET_IDS = new ArrayList<>();
    private static long visualUntilMillis;
    private static int cooldownTicks;
    private static boolean cooldownEnabled;

    private UnseenHandClientEffects() {
    }

    public static void show(List<Integer> entityIds, int durationTicks) {
        TARGET_IDS.clear();
        TARGET_IDS.addAll(entityIds);
        visualUntilMillis = Util.getMillis() + Math.max(0, durationTicks) * 50L;
    }

    public static void setCooldown(int remainingTicks, boolean enabled) {
        cooldownTicks = Math.max(0, remainingTicks);
        cooldownEnabled = enabled;
    }

    public static void clear() {
        TARGET_IDS.clear();
        visualUntilMillis = 0L;
        cooldownTicks = 0;
        cooldownEnabled = false;
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
        if (Util.getMillis() > visualUntilMillis) {
            TARGET_IDS.clear();
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || TARGET_IDS.isEmpty() || Util.getMillis() > visualUntilMillis) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Player player = minecraft.player;
        if (level == null || player == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        VertexConsumer consumer = minecraft.renderBuffers().bufferSource().getBuffer(RenderType.lines());
        int index = 0;
        for (int entityId : TARGET_IDS) {
            Entity target = level.getEntity(entityId);
            if (target == null) {
                continue;
            }
            renderHand(poseStack, consumer, player, target, index++, event.getPartialTick());
        }
        minecraft.renderBuffers().bufferSource().endBatch(RenderType.lines());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!cooldownEnabled) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || player.isCreative() || player.isSpectator() || minecraft.screen instanceof ReturnTransitionScreen) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int x = width / 2 - 91;
        int y = height - 50 - (player.getArmorValue() > 0 ? 10 : 0) - 20;
        int barWidth = 82;
        int barHeight = 4;
        boolean ready = cooldownTicks <= 0;
        graphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xCC06030B);
        graphics.fill(x, y, x + barWidth, y + barHeight, 0xFF22152F);
        if (ready) {
            graphics.fill(x, y, x + barWidth, y + barHeight, 0xFF5AF2FF);
            graphics.fill(x, y, x + barWidth, y + 1, 0xFFE8FFFF);
        } else {
            float progress = 1.0F - Math.min(1.0F, cooldownTicks / (20.0F * 60.0F * 5.0F));
            int filled = Math.round(barWidth * progress);
            graphics.fill(x, y, x + filled, y + barHeight, 0xFF9A4DFF);
            graphics.fill(x, y, x + Math.max(1, filled), y + 1, 0xFFE0B7FF);
        }
        Component label = ready
                ? Component.translatable("hud.re_mc.unseen_hand.ready")
                : Component.translatable("hud.re_mc.unseen_hand.cooldown", formatCooldown(cooldownTicks));
        graphics.drawString(minecraft.font, label, x + barWidth + 5, y - 3, ready ? 0xFF8FFBFF : 0xFFD8B4FF, true);
    }

    private static void renderHand(PoseStack poseStack, VertexConsumer consumer, Player caster, Entity target, int index, float partialTick) {
        Vec3 start = caster.getPosition(partialTick).add(0.0D, caster.getBbHeight() * 0.62D, 0.0D);
        Vec3 end = target.getPosition(partialTick).add(0.0D, target.getBbHeight() * 0.62D, 0.0D);
        Vec3 direction = end.subtract(start);
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = caster.getLookAngle();
        }
        direction = direction.normalize();
        Vec3 side = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (side.lengthSqr() < 1.0E-4D) {
            side = new Vec3(1.0D, 0.0D, 0.0D);
        }
        side = side.normalize();
        Vec3 up = side.cross(direction).normalize();
        double phase = (Util.getMillis() % 10000L) / 1000.0D + index * 0.91D;
        int alpha = 205;
        int coreColor = color(170, 59, 255, alpha);
        int edgeColor = color(84, 246, 255, alpha);
        int shadowColor = color(16, 4, 28, alpha);

        Vec3 previous = start;
        for (int segment = 1; segment <= 9; segment++) {
            double t = segment / 9.0D;
            double wave = Math.sin(phase + t * Math.PI * 4.0D) * 0.22D;
            double twist = Math.cos(phase * 1.3D + t * Math.PI * 3.0D) * 0.18D;
            Vec3 point = lerp(start, end, t).add(side.scale(wave)).add(up.scale(twist));
            drawLine(poseStack, consumer, previous, point, coreColor);
            drawLine(poseStack, consumer, previous.add(side.scale(0.035D)), point.add(side.scale(0.035D)), edgeColor);
            drawLine(poseStack, consumer, previous.add(side.scale(-0.035D)), point.add(side.scale(-0.035D)), shadowColor);
            previous = point;
        }

        Vec3 palm = end.subtract(direction.scale(0.18D));
        drawRing(poseStack, consumer, palm, side, up, 0.52D, coreColor, 18, phase);
        drawRing(poseStack, consumer, palm, side, up, 0.34D, edgeColor, 12, phase + 1.6D);
        for (int finger = 0; finger < 5; finger++) {
            double offset = (finger - 2) * 0.13D;
            double spread = (finger - 2) * 0.16D;
            Vec3 base = palm.add(side.scale(offset)).add(up.scale(-0.10D + Math.abs(offset) * 0.4D));
            Vec3 knuckle = base.add(direction.scale(0.32D)).add(side.scale(spread)).add(up.scale(0.20D));
            Vec3 tip = knuckle.add(direction.scale(0.35D)).add(side.scale(spread * 0.85D)).add(up.scale(0.08D));
            drawLine(poseStack, consumer, base, knuckle, coreColor);
            drawLine(poseStack, consumer, knuckle, tip, edgeColor);
            drawLine(poseStack, consumer, base.add(side.scale(0.025D)), knuckle.add(side.scale(0.025D)), shadowColor);
        }
    }

    private static void drawRing(PoseStack poseStack, VertexConsumer consumer, Vec3 center, Vec3 side, Vec3 up, double radius, int color, int segments, double phase) {
        Vec3 previous = ringPoint(center, side, up, radius, 0.0D, phase);
        for (int i = 1; i <= segments; i++) {
            Vec3 point = ringPoint(center, side, up, radius, i / (double) segments, phase);
            drawLine(poseStack, consumer, previous, point, color);
            previous = point;
        }
    }

    private static Vec3 ringPoint(Vec3 center, Vec3 side, Vec3 up, double radius, double t, double phase) {
        double angle = t * Math.PI * 2.0D + phase;
        return center.add(side.scale(Math.cos(angle) * radius)).add(up.scale(Math.sin(angle) * radius * 0.72D));
    }

    private static Vec3 lerp(Vec3 from, Vec3 to, double t) {
        return from.add(to.subtract(from).scale(t));
    }

    private static void drawLine(PoseStack poseStack, VertexConsumer consumer, Vec3 from, Vec3 to, int color) {
        Vec3 normal = to.subtract(from);
        if (normal.lengthSqr() < 1.0E-4D) {
            normal = new Vec3(0.0D, 1.0D, 0.0D);
        }
        normal = normal.normalize();
        addVertex(poseStack, consumer, from, normal, color);
        addVertex(poseStack, consumer, to, normal, color);
    }

    private static void addVertex(PoseStack poseStack, VertexConsumer consumer, Vec3 pos, Vec3 normal, int color) {
        consumer.vertex(poseStack.last().pose(), (float) pos.x, (float) pos.y, (float) pos.z)
                .color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >> 24) & 0xFF)
                .normal(poseStack.last().normal(), (float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }

    private static int color(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static String formatCooldown(int ticks) {
        int totalSeconds = Math.max(0, ticks / 20);
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }
}