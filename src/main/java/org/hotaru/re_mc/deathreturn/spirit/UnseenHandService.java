package org.hotaru.re_mc.deathreturn.spirit;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.hotaru.re_mc.Re_mc;
import org.hotaru.re_mc.deathreturn.ReturnConfig;
import org.hotaru.re_mc.deathreturn.ReturnManager;
import org.hotaru.re_mc.deathreturn.memory.PlayerMemory;
import org.hotaru.re_mc.deathreturn.memory.ReturnMemoryService;
import org.hotaru.re_mc.deathreturn.mode.SubaruModeManager;
import org.hotaru.re_mc.deathreturn.network.ReturnNetwork;
import org.hotaru.re_mc.deathreturn.network.UnseenHandCooldownPacket;
import org.hotaru.re_mc.deathreturn.network.UnseenHandVisualPacket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Re_mc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class UnseenHandService {
    public static final double RANGE = 10.0D;
    public static final int DURATION_TICKS = 100;
    public static final int COOLDOWN_TICKS = 20 * 60 * 5;
    public static final float ACTIVE_SPIRIT_COST = 10.0F;
    public static final float PASSIVE_SPIRIT_COST = 20.0F;
    public static final float PASSIVE_HEALTH_THRESHOLD = 6.0F;
    public static final int PASSIVE_ENEMY_THRESHOLD = 4;
    private static final int DAMAGE_INTERVAL_TICKS = 10;
    private static final float ATTACK_DAMAGE = 3.0F;
    private static final List<HandAttack> ATTACKS = new ArrayList<>();

    private UnseenHandService() {
    }

    public static void tickAll(MinecraftServer server) {
        if (!SubaruModeManager.isEnabled(server)) {
            ATTACKS.clear();
            return;
        }
        if (isReturnInProgress(server)) {
            ATTACKS.clear();
            return;
        }
        long gameTime = server.overworld().getGameTime();
        tickAttacks(server, gameTime);
        for (ServerPlayer player : List.copyOf(server.getPlayerList().getPlayers())) {
            checkPassive(player, gameTime);
        }
    }

    public static void activate(ServerPlayer player) {
        MinecraftServer server = player.server;
        if (!SubaruModeManager.isEnabled(server) || !SpiritService.isEligible(player) || isReturnInProgress(server)) {
            return;
        }
        long gameTime = server.overworld().getGameTime();
        int remainingCooldown = remainingCooldownTicks(player, gameTime);
        if (remainingCooldown > 0) {
            player.displayClientMessage(Component.translatable("message.re_mc.unseen_hand.cooldown", formatCooldown(remainingCooldown)).withStyle(ChatFormatting.GRAY), true);
            return;
        }
        List<LivingEntity> targets = findEnemies(player);
        if (targets.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.re_mc.unseen_hand.no_target").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        SpiritService.drainSpiritNoScent(player, ACTIVE_SPIRIT_COST);
        startAttack(player, targets, gameTime);
    }

    private static void checkPassive(ServerPlayer player, long gameTime) {
        if (!SpiritService.isEligible(player) || isReturnInProgress(player.server) || player.getHealth() >= PASSIVE_HEALTH_THRESHOLD) {
            return;
        }
        if (remainingCooldownTicks(player, gameTime) > 0) {
            return;
        }
        List<LivingEntity> enemies = findEnemies(player);
        if (enemies.size() < PASSIVE_ENEMY_THRESHOLD) {
            return;
        }
        SpiritService.drainSpiritNoScent(player, PASSIVE_SPIRIT_COST);
        player.displayClientMessage(Component.translatable("message.re_mc.unseen_hand.passive").withStyle(ChatFormatting.DARK_PURPLE), true);
        startAttack(player, enemies, gameTime);
    }

    private static void startAttack(ServerPlayer player, List<LivingEntity> targets, long gameTime) {
        PlayerMemory memory = ReturnMemoryService.getOrCreate(player.server, player.getUUID());
        if (!memory.isUnseenHandDebugReady()) {
            memory.setUnseenHandCooldownEnd(gameTime + COOLDOWN_TICKS);
            ReturnMemoryService.save(player.server, player.getUUID(), memory);
        }
        syncCooldown(player);

        List<Integer> targetIds = targets.stream().map(Entity::getId).toList();
        ATTACKS.removeIf(attack -> attack.casterId.equals(player.getUUID()));
        ATTACKS.add(new HandAttack(player.getUUID(), targetIds, player.serverLevel().dimension(), gameTime + DURATION_TICKS));

        ReturnNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UnseenHandVisualPacket(targetIds, DURATION_TICKS));
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.75F, 0.55F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 0.55F);
        level.sendParticles(player, ParticleTypes.REVERSE_PORTAL, false, player.getX(), player.getY() + 1.0D, player.getZ(), 80, 1.4D, 0.8D, 1.4D, 0.18D);
        level.sendParticles(player, ParticleTypes.SCULK_SOUL, false, player.getX(), player.getY() + 1.0D, player.getZ(), 35, 1.1D, 0.8D, 1.1D, 0.08D);
    }

    private static void tickAttacks(MinecraftServer server, long gameTime) {
        Iterator<HandAttack> iterator = ATTACKS.iterator();
        while (iterator.hasNext()) {
            HandAttack attack = iterator.next();
            ServerPlayer caster = server.getPlayerList().getPlayer(attack.casterId);
            if (caster == null || !caster.isAlive() || gameTime >= attack.endGameTime) {
                iterator.remove();
                continue;
            }
            ServerLevel level = server.getLevel(attack.dimension);
            if (level == null) {
                iterator.remove();
                continue;
            }
            boolean damageTick = gameTime % DAMAGE_INTERVAL_TICKS == 0;
            for (int entityId : attack.targetIds) {
                Entity entity = level.getEntity(entityId);
                if (!(entity instanceof LivingEntity target) || !target.isAlive() || target.isSpectator()) {
                    continue;
                }
                if (damageTick) {
                    applyHit(caster, target);
                }
                renderParticles(level, caster, target);
            }
        }
    }

    private static void applyHit(ServerPlayer caster, LivingEntity target) {
        Vec3 push = target.position().subtract(caster.position());
        if (push.lengthSqr() < 1.0E-4D) {
            push = caster.getLookAngle();
        }
        push = push.normalize().scale(1.05D).add(0.0D, 0.42D, 0.0D);
        target.setDeltaMovement(target.getDeltaMovement().add(push));
        target.hurtMarked = true;
        target.invulnerableTime = 0;
        target.hurt(caster.damageSources().playerAttack(caster), ATTACK_DAMAGE);
    }

    private static void renderParticles(ServerLevel level, ServerPlayer caster, LivingEntity target) {
        Vec3 start = caster.getEyePosition().add(0.0D, -0.25D, 0.0D);
        Vec3 end = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
        for (int i = 0; i < 5; i++) {
            double t = (i + 1) / 6.0D;
            double x = start.x + (end.x - start.x) * t;
            double y = start.y + (end.y - start.y) * t;
            double z = start.z + (end.z - start.z) * t;
            level.sendParticles(caster, ParticleTypes.SCULK_SOUL, false, x, y, z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
        }
        level.sendParticles(caster, ParticleTypes.SOUL_FIRE_FLAME, false, end.x, end.y, end.z, 3, 0.18D, 0.22D, 0.18D, 0.01D);
        level.sendParticles(caster, ParticleTypes.END_ROD, false, end.x, end.y, end.z, 2, 0.12D, 0.18D, 0.12D, 0.005D);
    }

    public static List<LivingEntity> findEnemies(ServerPlayer player) {
        return player.serverLevel()
                .getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RANGE),
                        entity -> entity != player && entity instanceof Enemy && entity.isAlive() && !entity.isSpectator())
                .stream()
                .sorted((left, right) -> Double.compare(player.distanceToSqr(left), player.distanceToSqr(right)))
                .toList();
    }

    public static int remainingCooldownTicks(ServerPlayer player) {
        return remainingCooldownTicks(player, player.server.overworld().getGameTime());
    }

    private static int remainingCooldownTicks(ServerPlayer player, long gameTime) {
        PlayerMemory memory = ReturnMemoryService.getOrCreate(player.server, player.getUUID());
        if (memory.isUnseenHandDebugReady()) {
            return 0;
        }
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, memory.getUnseenHandCooldownEnd() - gameTime));
    }

    public static boolean toggleDebugReady(ServerPlayer player) {
        PlayerMemory memory = ReturnMemoryService.getOrCreate(player.server, player.getUUID());
        memory.setUnseenHandDebugReady(!memory.isUnseenHandDebugReady());
        ReturnMemoryService.save(player.server, player.getUUID(), memory);
        syncCooldown(player);
        return memory.isUnseenHandDebugReady();
    }

    public static void syncCooldown(ServerPlayer player) {
        ReturnNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UnseenHandCooldownPacket(remainingCooldownTicks(player), ReturnConfig.SPIRIT_ENABLED.get() && ReturnConfig.SPIRIT_HUD_ENABLED.get() && SubaruModeManager.isEnabled(player.server) && SpiritService.isEligible(player)));
    }

    public static void resetCooldown(ServerPlayer player) {
        ATTACKS.removeIf(attack -> attack.casterId.equals(player.getUUID()));
        PlayerMemory memory = ReturnMemoryService.getOrCreate(player.server, player.getUUID());
        memory.setUnseenHandCooldownEnd(0L);
        ReturnMemoryService.save(player.server, player.getUUID(), memory);
        syncCooldown(player);
    }

    private static boolean isReturnInProgress(MinecraftServer server) {
        ReturnManager manager = ReturnManager.getOrNull();
        return manager != null && manager.isReturnInProgress();
    }

    private static String formatCooldown(int ticks) {
        int totalSeconds = Math.max(0, ticks / 20);
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!SubaruModeManager.isEnabled(player.server) || !SpiritService.isEligible(player)) {
            return;
        }
        SpiritService.restoreSpiritNoScent(player, ReturnConfig.SLEEP_SPIRIT_RESTORE.get());
        resetCooldown(player);
        player.displayClientMessage(Component.translatable("message.re_mc.unseen_hand.sleep_reset").withStyle(ChatFormatting.AQUA), true);
    }

    private static final class HandAttack {
        private final UUID casterId;
        private final List<Integer> targetIds;
        private final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;
        private final long endGameTime;

        private HandAttack(UUID casterId, List<Integer> targetIds, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension, long endGameTime) {
            this.casterId = casterId;
            this.targetIds = List.copyOf(targetIds);
            this.dimension = dimension;
            this.endGameTime = endGameTime;
        }
    }
}