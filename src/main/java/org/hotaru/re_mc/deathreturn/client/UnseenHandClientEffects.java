package org.hotaru.re_mc.deathreturn.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.hotaru.re_mc.Re_mc;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Re_mc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class UnseenHandClientEffects {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Re_mc.MODID, "textures/entity/unseen_hand_atlas.png");
    private static final int FULL_DETAIL_HANDS = 8;
    private static final int PUNCH_CYCLE_MILLIS = 500;
    private static final int EXTENSION_MILLIS = 220;
    private static final int FADE_MILLIS = 500;
    private static final List<Integer> TARGET_IDS = new ArrayList<>();
    private static long visualStartMillis;
    private static long visualUntilMillis;
    private static long emptySinceMillis;
    private static int cooldownTicks;
    private static boolean cooldownEnabled;

    private UnseenHandClientEffects() {
    }

    public static void show(List<Integer> entityIds, int durationTicks) {
        TARGET_IDS.clear();
        TARGET_IDS.addAll(entityIds);
        visualStartMillis = Util.getMillis();
        visualUntilMillis = visualStartMillis + Math.max(0, durationTicks) * 50L;
        emptySinceMillis = 0L;
    }

    public static void setCooldown(int remainingTicks, boolean enabled) {
        cooldownTicks = Math.max(0, remainingTicks);
        cooldownEnabled = enabled;
    }

    public static void clear() {
        TARGET_IDS.clear();
        visualStartMillis = 0L;
        visualUntilMillis = 0L;
        emptySinceMillis = 0L;
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
        long now = Util.getMillis();
        long end = effectiveEndMillis();
        if (now > end + FADE_MILLIS) {
            TARGET_IDS.clear();
            emptySinceMillis = 0L;
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || TARGET_IDS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Player player = minecraft.player;
        if (level == null || player == null) {
            return;
        }

        List<Entity> visibleTargets = new ArrayList<>();
        for (int entityId : TARGET_IDS) {
            Entity target = level.getEntity(entityId);
            if (target != null && target.isAlive()) {
                visibleTargets.add(target);
            }
        }
        if (visibleTargets.isEmpty()) {
            if (emptySinceMillis == 0L) {
                emptySinceMillis = Util.getMillis();
            }
        } else {
            emptySinceMillis = 0L;
        }

        double alpha = overallAlpha();
        if (alpha <= 0.001D) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        VertexConsumer consumer = minecraft.renderBuffers().bufferSource().getBuffer(RenderType.entityTranslucent(TEXTURE));
        double phase = (Util.getMillis() - visualStartMillis) / 1000.0D;
        renderBodyShadow(poseStack, consumer, player, event.getPartialTick(), phase, alpha);
        int detailIndex = 0;
        for (Entity target : visibleTargets) {
            renderHand(poseStack, consumer, player, target, detailIndex, detailIndex < FULL_DETAIL_HANDS, event.getPartialTick(), phase, alpha);
            detailIndex++;
        }
        minecraft.renderBuffers().bufferSource().endBatch(RenderType.entityTranslucent(TEXTURE));
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        double punch = currentPunch();
        if (punch <= 0.001D) {
            return;
        }
        double wave = Math.sin(((Util.getMillis() % PUNCH_CYCLE_MILLIS) / (double) PUNCH_CYCLE_MILLIS) * Math.PI * 2.0D);
        event.setYaw(event.getYaw() + (float) (wave * 0.16D * punch));
        event.setPitch(event.getPitch() + (float) (0.08D * punch));
        event.setRoll(event.getRoll() + (float) (wave * 0.10D * punch));
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || player.isCreative() || player.isSpectator() || minecraft.screen instanceof ReturnTransitionScreen) {
            return;
        }
        renderImpactPulse(event.getGuiGraphics());
        if (!cooldownEnabled) {
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

    private static void renderImpactPulse(GuiGraphics graphics) {
        double punch = currentPunch();
        if (punch <= 0.01D) {
            return;
        }
        int alpha = (int) (70.0D * punch);
        int color = (alpha << 24) | 0x5A104E;
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int edge = Math.max(8, Math.min(width, height) / 28);
        graphics.fill(0, 0, width, edge, color);
        graphics.fill(0, height - edge, width, height, color);
        graphics.fill(0, edge, edge, height - edge, color);
        graphics.fill(width - edge, edge, width, height - edge, color);
    }

    private static void renderBodyShadow(PoseStack poseStack, VertexConsumer consumer, Player player, float partialTick, double phase, double alpha) {
        Vec3 feet = player.getPosition(partialTick).add(0.0D, 0.04D, 0.0D);
        Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
        for (int i = 0; i < 16; i++) {
            double angle0 = i / 16.0D * Math.PI * 2.0D;
            double angle1 = (i + 1) / 16.0D * Math.PI * 2.0D;
            double radius = 0.62D + Math.sin(phase * 2.2D + i) * 0.08D;
            Vec3 side = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 forward = new Vec3(0.0D, 0.0D, 1.0D);
            Vec3 p0 = feet;
            Vec3 p1 = feet.add(side.scale(Math.cos(angle0) * radius)).add(forward.scale(Math.sin(angle0) * radius));
            Vec3 p2 = feet.add(side.scale(Math.cos(angle1) * radius)).add(forward.scale(Math.sin(angle1) * radius));
            drawQuad(poseStack, consumer, p0, p0, p2, p1, uv(0, 0, 0.25, 0.25), color(0, 0, 0, (int) (105.0D * alpha)));
        }
        for (int i = 0; i < 10; i++) {
            double angle = phase * 1.1D + i / 10.0D * Math.PI * 2.0D;
            Vec3 base = feet.add(Math.cos(angle) * 0.62D, 0.0D, Math.sin(angle) * 0.62D);
            Vec3 end = feet.add(Math.cos(angle + 0.5D) * 1.1D, 1.55D + Math.sin(phase + i) * 0.42D, Math.sin(angle + 0.5D) * 1.1D);
            drawSmokeRibbon(poseStack, consumer, base, end, up, phase + i * 0.7D, alpha * 0.72D, false);
        }
    }

    private static void renderHand(PoseStack poseStack, VertexConsumer consumer, Player caster, Entity target, int index, boolean fullDetail, float partialTick, double phase, double alpha) {
        Vec3 start = caster.getPosition(partialTick).add(0.0D, caster.getBbHeight() * 0.58D, 0.0D);
        Vec3 end = target.getPosition(partialTick).add(0.0D, target.getBbHeight() * 0.58D, 0.0D);
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
        double elapsed = Math.max(0L, Util.getMillis() - visualStartMillis);
        double reach = Math.min(1.0D, elapsed / (double) EXTENSION_MILLIS);
        reach = reach * (2.0D - reach);
        double punch = punchStrength(elapsed);
        Vec3 handEnd = reach < 1.0D ? lerp(start, end, reach) : end.subtract(direction.scale(1.25D * (1.0D - punch)));

        int segments = fullDetail ? 14 : 8;
        drawArmLayer(poseStack, consumer, start, handEnd, side, up, phase + index * 0.77D, index, segments, 0.72D, 178, alpha * 0.86D, uv(0.0D, 0.0D, 0.25D, 0.25D), color(0, 0, 0, 255));
        drawArmLayer(poseStack, consumer, start, handEnd, side, up, phase + index * 0.77D, index, segments, 0.96D, 92, alpha * 0.66D, uv(0.25D, 0.0D, 0.5D, 0.25D), color(150, 47, 255, 255));
        if (fullDetail) {
            drawArmLayer(poseStack, consumer, start, handEnd, side, up, phase + index * 0.77D, index, segments, 0.20D, 124, alpha * 0.82D, uv(0.5D, 0.0D, 0.75D, 0.25D), color(255, 44, 78, 255));
        }

        Vec3 palm = handEnd.subtract(direction.scale(0.18D * reach));
        drawPalm(poseStack, consumer, palm, direction, side, up, punch, fullDetail, alpha, index);
        if (fullDetail) {
            drawFingers(poseStack, consumer, palm, direction, side, up, punch, alpha, index, phase);
        }
    }

    private static void drawArmLayer(PoseStack poseStack, VertexConsumer consumer, Vec3 start, Vec3 end, Vec3 side, Vec3 up, double phase, int index, int segments, double widthScale, int alpha, double globalAlpha, UvRect uv, int tint) {
        Vec3 previous = curvedPoint(start, end, side, up, 0.0D, phase, index);
        for (int segment = 1; segment <= segments; segment++) {
            double t = segment / (double) segments;
            double t0 = (segment - 1) / (double) segments;
            Vec3 point = curvedPoint(start, end, side, up, t, phase, index);
            Vec3 direction = point.subtract(previous);
            if (direction.lengthSqr() < 1.0E-4D) {
                direction = end.subtract(start);
            }
            direction = direction.normalize();
            Vec3 basisSide = direction.cross(up);
            if (basisSide.lengthSqr() < 1.0E-4D) {
                basisSide = side;
            }
            basisSide = basisSide.normalize();
            Vec3 basisUp = basisSide.cross(direction).normalize();
            double width0 = widthScale * (0.44D - t0 * 0.18D) * (0.88D + Math.sin(phase * 3.0D + t0 * 5.0D) * 0.12D);
            double width1 = widthScale * (0.44D - t * 0.18D) * (0.88D + Math.sin(phase * 3.0D + t * 5.0D) * 0.12D);
            for (int plane = 0; plane < 4; plane++) {
                double angle = plane * Math.PI / 4.0D + phase * 0.18D;
                Vec3 normal = basisSide.scale(Math.cos(angle)).add(basisUp.scale(Math.sin(angle))).normalize();
                drawQuad(poseStack, consumer,
                        previous.add(normal.scale(width0)), point.add(normal.scale(width1)),
                        point.subtract(normal.scale(width1)), previous.subtract(normal.scale(width0)),
                        uvSegment(uv, t0, t), color((tint >> 16) & 0xFF, (tint >> 8) & 0xFF, tint & 0xFF, (int) (alpha * globalAlpha)));
            }
            previous = point;
        }
    }

    private static void drawSmokeRibbon(PoseStack poseStack, VertexConsumer consumer, Vec3 start, Vec3 end, Vec3 up, double phase, double alpha, boolean detailed) {
        Vec3 direction = end.subtract(start).normalize();
        Vec3 side = direction.cross(up);
        if (side.lengthSqr() < 1.0E-4D) {
            side = new Vec3(1.0D, 0.0D, 0.0D);
        }
        side = side.normalize();
        Vec3 bend = side.cross(direction).normalize();
        int segments = detailed ? 9 : 6;
        Vec3 previous = start;
        for (int segment = 1; segment <= segments; segment++) {
            double t = segment / (double) segments;
            double t0 = (segment - 1) / (double) segments;
            Vec3 point = lerp(start, end, t).add(side.scale(Math.sin(phase + t * Math.PI * 2.7D) * 0.16D * t)).add(bend.scale(Math.cos(phase * 1.3D + t * Math.PI * 2.1D) * 0.13D * t));
            Vec3 segDir = point.subtract(previous).normalize();
            Vec3 basis = segDir.cross(up);
            if (basis.lengthSqr() < 1.0E-4D) {
                basis = side;
            }
            basis = basis.normalize();
            double width0 = 0.18D * (1.0D - t0 * 0.55D);
            double width1 = 0.18D * (1.0D - t * 0.55D);
            drawQuad(poseStack, consumer, previous.add(basis.scale(width0)), point.add(basis.scale(width1)), point.subtract(basis.scale(width1)), previous.subtract(basis.scale(width0)), uvSegment(uv(0.0D, 0.0D, 0.25D, 0.25D), t0, t), color(0, 0, 0, (int) (alpha * 110.0D)));
            drawQuad(poseStack, consumer, previous.add(basis.scale(width0 * 1.55D)), point.add(basis.scale(width1 * 1.55D)), point.subtract(basis.scale(width1 * 1.55D)), previous.subtract(basis.scale(width0 * 1.55D)), uvSegment(uv(0.25D, 0.0D, 0.5D, 0.25D), t0, t), color(150, 47, 255, (int) (alpha * 45.0D)));
            previous = point;
        }
    }

    private static void drawPalm(PoseStack poseStack, VertexConsumer consumer, Vec3 palm, Vec3 direction, Vec3 side, Vec3 up, double punch, boolean fullDetail, double alpha, int index) {
        double size = (fullDetail ? 0.54D : 0.42D) * (0.92D + punch * 0.12D);
        Vec3 center = palm.add(direction.scale(punch * 0.12D)).add(side.scale(Math.sin(index * 1.7D) * 0.08D));
        drawCrossedPlane(poseStack, consumer, center, side, up, size, uv(0.75D, 0.0D, 1.0D, 0.25D), color(0, 0, 0, (int) (alpha * 185.0D)));
        drawCrossedPlane(poseStack, consumer, center, side, up, size * 1.25D, uv(0.75D, 0.0D, 1.0D, 0.25D), color(150, 47, 255, (int) (alpha * 55.0D)));
        if (fullDetail) {
            drawCrossedPlane(poseStack, consumer, center.add(direction.scale(0.02D)), side, up, size * 0.34D, uv(0.5D, 0.0D, 0.75D, 0.25D), color(255, 44, 78, (int) (alpha * (80.0D + punch * 80.0D))));
        }
    }

    private static void drawFingers(PoseStack poseStack, VertexConsumer consumer, Vec3 palm, Vec3 direction, Vec3 side, Vec3 up, double punch, double alpha, int index, double phase) {
        for (int finger = 0; finger < 5; finger++) {
            double spread = (finger - 2) * 0.16D;
            double curl = 0.55D - punch * 0.38D;
            Vec3 base = palm.add(side.scale(spread)).add(up.scale(-0.10D + Math.abs(spread) * 0.28D));
            Vec3 knuckle = base.add(direction.scale(0.32D)).add(side.scale(spread * 0.35D)).add(up.scale(curl * 0.18D));
            Vec3 tip = knuckle.add(direction.scale(0.34D - punch * 0.12D)).add(side.scale(spread * 0.25D)).add(up.scale(-curl * 0.22D));
            drawTaperedSegment(poseStack, consumer, base, knuckle, side, up, 0.105D, 0.075D, uv(0.0D, 0.25D, 0.25D, 0.5D), color(0, 0, 0, (int) (alpha * 190.0D)));
            drawTaperedSegment(poseStack, consumer, knuckle, tip, side, up, 0.075D, 0.045D, uv(0.0D, 0.25D, 0.25D, 0.5D), color(0, 0, 0, (int) (alpha * 180.0D)));
            drawTaperedSegment(poseStack, consumer, base.add(direction.scale(0.02D)), tip, side, up, 0.035D, 0.018D, uv(0.25D, 0.0D, 0.5D, 0.25D), color(150, 47, 255, (int) (alpha * 55.0D)));
        }
    }

    private static void drawTaperedSegment(PoseStack poseStack, VertexConsumer consumer, Vec3 from, Vec3 to, Vec3 side, Vec3 up, double widthFrom, double widthTo, UvRect uv, int color) {
        Vec3 direction = to.subtract(from);
        if (direction.lengthSqr() < 1.0E-4D) {
            return;
        }
        direction = direction.normalize();
        Vec3 basisSide = direction.cross(up);
        if (basisSide.lengthSqr() < 1.0E-4D) {
            basisSide = side;
        }
        basisSide = basisSide.normalize();
        Vec3 basisUp = basisSide.cross(direction).normalize();
        for (int plane = 0; plane < 2; plane++) {
            Vec3 normal = plane == 0 ? basisSide : basisUp;
            drawQuad(poseStack, consumer, from.add(normal.scale(widthFrom)), to.add(normal.scale(widthTo)), to.subtract(normal.scale(widthTo)), from.subtract(normal.scale(widthFrom)), uv, color);
        }
    }

    private static void drawCrossedPlane(PoseStack poseStack, VertexConsumer consumer, Vec3 center, Vec3 side, Vec3 up, double size, UvRect uv, int color) {
        drawQuad(poseStack, consumer, center.add(side.scale(size)).subtract(up.scale(size)), center.add(side.scale(size)).add(up.scale(size)), center.subtract(side.scale(size)).add(up.scale(size)), center.subtract(side.scale(size)).subtract(up.scale(size)), uv, color);
        drawQuad(poseStack, consumer, center.add(up.scale(size)).subtract(side.scale(size * 0.55D)), center.add(up.scale(size)).add(side.scale(size * 0.55D)), center.subtract(up.scale(size)).add(side.scale(size * 0.55D)), center.subtract(up.scale(size)).subtract(side.scale(size * 0.55D)), uv, color);
    }

    private static Vec3 curvedPoint(Vec3 start, Vec3 end, Vec3 side, Vec3 up, double t, double phase, int index) {
        double taper = Math.sin(Math.PI * Math.min(1.0D, Math.max(0.0D, t)));
        double sideWave = Math.sin(phase * 1.7D + index * 0.91D + t * Math.PI * 3.2D) * 0.28D * taper;
        double upWave = Math.cos(phase * 1.2D + index * 0.63D + t * Math.PI * 4.1D) * 0.24D * taper;
        return lerp(start, end, t).add(side.scale(sideWave)).add(up.scale(upWave));
    }

    private static UvRect uv(double u0, double v0, double u1, double v1) {
        return new UvRect(u0, v0, u1, v1);
    }

    private static UvRect uvSegment(UvRect base, double t0, double t1) {
        return new UvRect(base.u0, base.v0 + (base.v1 - base.v0) * t0, base.u1, base.v0 + (base.v1 - base.v0) * t1);
    }

    private static void drawQuad(PoseStack poseStack, VertexConsumer consumer, Vec3 a, Vec3 b, Vec3 c, Vec3 d, UvRect uv, int color) {
        Vec3 normal = b.subtract(a).cross(c.subtract(a));
        if (normal.lengthSqr() < 1.0E-4D) {
            normal = new Vec3(0.0D, 1.0D, 0.0D);
        }
        normal = normal.normalize();
        vertex(poseStack, consumer, a, uv.u0, uv.v0, normal, color);
        vertex(poseStack, consumer, b, uv.u0, uv.v1, normal, color);
        vertex(poseStack, consumer, c, uv.u1, uv.v1, normal, color);
        vertex(poseStack, consumer, d, uv.u1, uv.v0, normal, color);
    }

    private static void vertex(PoseStack poseStack, VertexConsumer consumer, Vec3 pos, double u, double v, Vec3 normal, int color) {
        consumer.vertex(poseStack.last().pose(), (float) pos.x, (float) pos.y, (float) pos.z)
                .color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >> 24) & 0xFF)
                .uv((float) u, (float) v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(poseStack.last().normal(), (float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }

    private static Vec3 lerp(Vec3 from, Vec3 to, double t) {
        return from.add(to.subtract(from).scale(t));
    }

    private static double punchStrength(double elapsedMillis) {
        if (elapsedMillis < EXTENSION_MILLIS) {
            return 0.0D;
        }
        double phase = ((elapsedMillis - EXTENSION_MILLIS) % PUNCH_CYCLE_MILLIS) / (double) PUNCH_CYCLE_MILLIS;
        if (phase < 0.35D) {
            double t = phase / 0.35D;
            return t * t * (3.0D - 2.0D * t);
        }
        double t = (phase - 0.35D) / 0.65D;
        double eased = t * t * (3.0D - 2.0D * t);
        return 1.0D - eased;
    }

    private static double currentPunch() {
        if (TARGET_IDS.isEmpty() || visualStartMillis == 0L || Util.getMillis() > effectiveEndMillis() + FADE_MILLIS) {
            return 0.0D;
        }
        return punchStrength(Math.max(0L, Util.getMillis() - visualStartMillis)) * overallAlpha();
    }

    private static long effectiveEndMillis() {
        if (emptySinceMillis > 0L) {
            return Math.min(visualUntilMillis, emptySinceMillis + FADE_MILLIS);
        }
        return visualUntilMillis;
    }

    private static double overallAlpha() {
        long now = Util.getMillis();
        long end = effectiveEndMillis();
        if (now >= end) {
            return 0.0D;
        }
        double fadeIn = Math.min(1.0D, Math.max(0.0D, (now - visualStartMillis) / 260.0D));
        double fadeOut = Math.min(1.0D, Math.max(0.0D, (end - now) / (double) FADE_MILLIS));
        return fadeIn * fadeOut;
    }

    private static int color(int r, int g, int b, int a) {
        return (Math.max(0, Math.min(255, a)) << 24) | (Math.max(0, Math.min(255, r)) << 16) | (Math.max(0, Math.min(255, g)) << 8) | Math.max(0, Math.min(255, b));
    }

    private static String formatCooldown(int ticks) {
        int totalSeconds = Math.max(0, ticks / 20);
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    private record UvRect(double u0, double v0, double u1, double v1) {
    }
}