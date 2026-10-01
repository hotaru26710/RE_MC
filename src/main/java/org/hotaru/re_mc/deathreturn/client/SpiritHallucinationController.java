package org.hotaru.re_mc.deathreturn.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
    private static final float[] STEERING_ANGLES = {0.0F, 18.0F, -18.0F, 36.0F, -36.0F, 58.0F, -58.0F, 82.0F, -82.0F};
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
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        removeLookedAtZombie();
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide()) {
            removeLookedAtZombie();
        }
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
            moveLikeHostileMob(fake, player, level);
        }

        if (!visible || spirit >= 65.0F || player == null || level == null || minecraft.screen instanceof ReturnTransitionScreen) {
            return;
        }
        float severity = Math.max(0.0F, Math.min(1.0F, (65.0F - spirit) / 65.0F));
        int interval = Math.max(120, Math.round(340.0F - severity * 240.0F));
        if (tickCounter % interval != 0 || RANDOM.nextFloat() > 0.25F + severity * 0.7F || ZOMBIES.size() >= 2) {
            return;
        }
        spawnFakeZombie(level, player, severity);
    }

    private static void spawnFakeZombie(ClientLevel level, Player player, float severity) {
        double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
        double distance = 11.0D + RANDOM.nextDouble() * 6.0D;
        double x = player.getX() + Math.cos(angle) * distance;
        double z = player.getZ() + Math.sin(angle) * distance;
        BlockPos column = BlockPos.containing(x, player.getY(), z);
        if (!level.hasChunkAt(column)) {
            return;
        }
        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
        if (Math.abs(groundY - player.getY()) > 7.0D) {
            return;
        }
        Zombie zombie = EntityType.ZOMBIE.create(level);
        if (zombie == null) {
            return;
        }
        zombie.setPos(x, groundY, z);
        zombie.setNoAi(true);
        zombie.setInvulnerable(true);
        zombie.setPersistenceRequired();
        zombie.setYRot(0.0F);
        zombie.setYHeadRot(0.0F);
        zombie.setYBodyRot(0.0F);
        ZOMBIES.add(new FakeZombie(zombie, 220 + Math.round(severity * 260.0F), 0.035D + severity * 0.025D));
    }

    private static void moveLikeHostileMob(FakeZombie fake, Player player, ClientLevel level) {
        Zombie zombie = fake.zombie;
        Vec3 current = zombie.position();
        Vec3 direct = player.position().subtract(current);
        double distance = direct.length();
        if (distance < 1.4D) {
            zombie.setDeltaMovement(Vec3.ZERO);
            fake.velocity = Vec3.ZERO;
            if (tickCounter % 22 == (zombie.getId() & 7)) {
                zombie.swing(InteractionHand.MAIN_HAND);
            }
            updateAnimation(zombie, 0.0F, direct);
            return;
        }

        Vec3 desired = chooseSteeringDirection(level, current, direct, distance);
        Vec3 targetVelocity = desired.scale(fake.maxSpeed);
        fake.velocity = fake.velocity.scale(0.68D).add(targetVelocity.scale(0.32D));
        if (fake.velocity.lengthSqr() > fake.maxSpeed * fake.maxSpeed) {
            fake.velocity = fake.velocity.normalize().scale(fake.maxSpeed);
        }

        Vec3 next = current.add(fake.velocity);
        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(next.x), Mth.floor(next.z));
        if (Math.abs(groundY - current.y) <= 1.2D) {
            next = new Vec3(next.x, groundY, next.z);
        }
        zombie.setOldPosAndRot();
        zombie.setPos(next);
        updateAnimation(zombie, (float) fake.velocity.length(), direct);
    }

    private static Vec3 chooseSteeringDirection(ClientLevel level, Vec3 current, Vec3 direct, double distance) {
        Vec3 horizontal = new Vec3(direct.x, 0.0D, direct.z).normalize();
        float baseYaw = (float) Math.toDegrees(Math.atan2(horizontal.z, horizontal.x));
        for (float offset : STEERING_ANGLES) {
            float yaw = (float) Math.toRadians(baseYaw + offset);
            Vec3 direction = new Vec3(Math.cos(yaw), 0.0D, Math.sin(yaw));
            Vec3 probe = current.add(direction.scale(0.7D));
            BlockPos ground = BlockPos.containing(probe.x, current.y - 0.2D, probe.z);
            BlockPos feet = ground.above();
            BlockPos head = feet.above();
            if (isWalkable(level, ground, feet, head)) {
                return direction;
            }
        }
        return horizontal.scale(-1.0D);
    }

    private static boolean isWalkable(ClientLevel level, BlockPos ground, BlockPos feet, BlockPos head) {
        BlockState groundState = level.getBlockState(ground);
        BlockState feetState = level.getBlockState(feet);
        BlockState headState = level.getBlockState(head);
        boolean supported = groundState.isSolidRender(level, ground) || groundState.is(BlockTags.DIRT) || groundState.is(BlockTags.BASE_STONE_OVERWORLD);
        return supported && feetState.getCollisionShape(level, feet).isEmpty() && headState.getCollisionShape(level, head).isEmpty();
    }

    private static void updateAnimation(Zombie zombie, float speed, Vec3 lookDirection) {
        float targetYaw = (float) (Math.toDegrees(Math.atan2(lookDirection.z, lookDirection.x)) - 90.0D);
        float yawDelta = Mth.wrapDegrees(targetYaw - zombie.getYRot());
        float turn = Mth.clamp(yawDelta, -5.0F, 5.0F);
        zombie.yRotO = zombie.getYRot();
        zombie.setYRot(zombie.getYRot() + turn);
        zombie.setYBodyRot(zombie.getYRot());
        zombie.setYHeadRot(targetYaw);
        zombie.tickCount++;
        zombie.walkAnimation.update(Math.min(0.85F, speed * 13.0F), 0.42F);
    }

    private static void removeLookedAtZombie() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        Vec3 eyes = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        FakeZombie closest = null;
        double closestScore = Double.MAX_VALUE;
        for (FakeZombie fake : ZOMBIES) {
            Vec3 toward = fake.zombie.position().add(0.0D, 0.9D, 0.0D).subtract(eyes);
            double projection = toward.dot(look);
            if (projection <= 0.0D || projection > 4.5D) {
                continue;
            }
            double perpendicular = toward.subtract(look.scale(projection)).lengthSqr();
            if (perpendicular < 0.45D && projection < closestScore) {
                closest = fake;
                closestScore = projection;
            }
        }
        if (closest != null) {
            closest.zombie.playSound(net.minecraft.sounds.SoundEvents.ZOMBIE_HURT, 0.8F, 1.0F);
            ZOMBIES.remove(closest);
        }
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
        private final double maxSpeed;
        private Vec3 velocity = Vec3.ZERO;

        private FakeZombie(Zombie zombie, int ticks, double maxSpeed) {
            this.zombie = zombie;
            this.ticks = ticks;
            this.maxSpeed = maxSpeed;
        }
    }
}
