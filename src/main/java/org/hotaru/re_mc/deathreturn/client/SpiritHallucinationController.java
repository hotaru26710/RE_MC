package org.hotaru.re_mc.deathreturn.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.hotaru.re_mc.Re_mc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = Re_mc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SpiritHallucinationController {
    private static final RandomSource RANDOM = RandomSource.create();
    private static final List<FakeZombie> ZOMBIES = new ArrayList<>();
    private static float spirit = 100.0F;
    private static boolean visible;
    private static int tickCounter;

    private SpiritHallucinationController() {
    }

    public static void setSpirit(float value, boolean enabled) {
        spirit = Math.max(0.0F, Math.min(100.0F, value));
        visible = enabled;
        if (!enabled || spirit >= 65.0F) {
            ZOMBIES.clear();
        }
    }

    public static void clear() {
        spirit = 100.0F;
        visible = false;
        ZOMBIES.clear();
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
        tickCounter++;
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;

        Iterator<FakeZombie> iterator = ZOMBIES.iterator();
        while (iterator.hasNext()) {
            FakeZombie fake = iterator.next();
            if (player == null || level == null || --fake.ticks <= 0) {
                iterator.remove();
                continue;
            }
            moveFakeZombie(fake, player);
            if (fake.zombie.position().distanceToSqr(player.position()) < 1.75D) {
                iterator.remove();
            }
        }

        if (!visible || spirit >= 65.0F || player == null || level == null || minecraft.screen instanceof ReturnTransitionScreen) {
            return;
        }
        float severity = Math.max(0.0F, Math.min(1.0F, (65.0F - spirit) / 65.0F));
        int interval = Math.max(100, Math.round(300.0F - severity * 220.0F));
        if (tickCounter % interval != 0 || RANDOM.nextFloat() > 0.25F + severity * 0.75F || ZOMBIES.size() >= 2) {
            return;
        }
        spawnFakeZombie(level, player, severity);
    }

    private static void spawnFakeZombie(ClientLevel level, Player player, float severity) {
        double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
        double distance = 10.0D + RANDOM.nextDouble() * 6.0D;
        double x = player.getX() + Math.cos(angle) * distance;
        double z = player.getZ() + Math.sin(angle) * distance;
        double y = player.getY() + RANDOM.nextDouble() * 3.0D - 1.0D;
        BlockPos feet = BlockPos.containing(x, y, z);
        if (!level.hasChunkAt(feet) || !level.getBlockState(feet).isAir() || !level.getBlockState(feet.above()).isAir()) {
            return;
        }
        Zombie zombie = EntityType.ZOMBIE.create(level);
        if (zombie == null) {
            return;
        }
        zombie.setPos(x, y, z);
        zombie.setNoAi(true);
        zombie.setInvulnerable(true);
        zombie.setPersistenceRequired();
        ZOMBIES.add(new FakeZombie(zombie, 120 + Math.round(severity * 160.0F), 0.055D + severity * 0.035D));
    }

    private static void moveFakeZombie(FakeZombie fake, Player player) {
        Zombie zombie = fake.zombie;
        Vec3 delta = player.position().subtract(zombie.position());
        if (delta.lengthSqr() > 1.0E-4D) {
            Vec3 movement = delta.normalize().scale(fake.speed);
            zombie.setOldPosAndRot();
            zombie.setPos(zombie.position().add(movement));
            float yaw = (float) (Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0D);
            zombie.setYRot(yaw);
            zombie.setYHeadRot(yaw);
            zombie.setYBodyRot(yaw);
            zombie.yRotO = yaw;
        }
        zombie.tickCount++;
        zombie.walkAnimation.update(fake.speed > 0.07D ? 0.85F : 0.55F, 0.4F);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || ZOMBIES.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        Vec3 camera = event.getCamera().getPosition();
        var bufferSource = minecraft.renderBuffers().bufferSource();
        event.getPoseStack().pushPose();
        event.getPoseStack().translate(-camera.x, -camera.y, -camera.z);
        for (FakeZombie fake : ZOMBIES) {
            Zombie zombie = fake.zombie;
            dispatcher.render(
                    zombie,
                    zombie.getX(),
                    zombie.getY(),
                    zombie.getZ(),
                    0.0F,
                    event.getPartialTick(),
                    event.getPoseStack(),
                    bufferSource,
                    LightTexture.FULL_BRIGHT
            );
        }
        event.getPoseStack().popPose();
        bufferSource.endBatch();
    }

    private static final class FakeZombie {
        private final Zombie zombie;
        private int ticks;
        private final double speed;

        private FakeZombie(Zombie zombie, int ticks, double speed) {
            this.zombie = zombie;
            this.ticks = ticks;
            this.speed = speed;
        }
    }
}
