package org.hotaru.re_mc.deathreturn;

import com.mojang.logging.LogUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import org.hotaru.re_mc.Re_mc;
import org.hotaru.re_mc.deathreturn.memory.PlayerMemory;
import org.hotaru.re_mc.deathreturn.memory.ReturnMemoryService;
import org.hotaru.re_mc.deathreturn.network.ReturnNetwork;
import org.hotaru.re_mc.deathreturn.network.ReturnTransitionPacket;
import org.hotaru.re_mc.deathreturn.network.ReturnTransitionPhase;
import org.hotaru.re_mc.deathreturn.mode.SubaruModeManager;
import org.hotaru.re_mc.deathreturn.mode.SubaruModePending;
import org.hotaru.re_mc.deathreturn.spirit.SpiritRules;
import org.hotaru.re_mc.deathreturn.spirit.SpiritService;
import org.hotaru.re_mc.deathreturn.spirit.UnseenHandService;
import org.hotaru.re_mc.deathreturn.spirit.WitchScentService;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ReturnManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static ReturnManager instance;

    private final MinecraftServer server;
    private final CheckpointService checkpoints;
    private final WorldReloadService reloadService;
    private ReturnState state = ReturnState.IDLE;
    private long lastDamageMillis;
    private long nextAutoCheckpointMillis;
    private long firstCheckpointMillis;
    private long candidateCreatedMillis;
    private long candidateReadyMillis;
    private boolean returnScheduled;
    private ServerPlayer triggerPlayer;
    private final Map<UUID, Long> auraUntil = new HashMap<>();
    private long auraParticleTick;
    private int pendingSpiritLoss = 10;

    private ReturnManager(MinecraftServer server) {
        this.server = server;
        this.checkpoints = new CheckpointService(server);
        this.reloadService = new WorldReloadService(server, checkpoints);
        try {
            checkpoints.ensureDirectories();
            checkpoints.loadManifest();
        } catch (Exception exception) {
            LOGGER.error("Failed to initialize Death Return checkpoint storage", exception);
            state = ReturnState.FAILED;
        }
        long now = System.currentTimeMillis();
        this.firstCheckpointMillis = now + 5000L;
        this.nextAutoCheckpointMillis = now + minutes(ReturnConfig.AUTO_INTERVAL_MINUTES.get());
        this.lastDamageMillis = now;
    }

    public static ReturnManager get() {
        return instance;
    }

    public static ReturnManager getOrNull() {
        return instance;
    }

    public static boolean isActive(MinecraftServer server) {
        if (!ReturnConfig.ENABLED.get()) {
            return false;
        }
        if (server == null) {
            return false;
        }
        return SubaruModeManager.isEnabled(server);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        if (SubaruModePending.consumePending()) {
            SubaruModeManager.setEnabled(server, true, server.getWorldData().getLevelSettings().allowCommands);
            LOGGER.info("Subaru Mode enabled for newly created world");
        } else {
            SubaruModeManager.applySavedCommandPolicy(server);
        }
        instance = new ReturnManager(server);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        if (instance != null) {
            instance.saveMemory();
            instance = null;
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && instance != null) {
            instance.tick();
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (instance == null || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        instance.onPlayerDamage(player, event.getSource(), event.getAmount());
        if (isActive(player.server) && event.getAmount() >= player.getHealth() - 0.001F) {
            event.setCanceled(true);
            player.setHealth(Math.max(1.0F, player.getHealth()));
            instance.startReturn(player, event.getSource());
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (instance == null || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (isActive(player.server)) {
            event.setCanceled(true);
            player.setHealth(Math.max(1.0F, player.getHealth()));
            instance.startReturn(player, event.getSource());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (instance != null && event.getEntity() instanceof ServerPlayer player) {
            SpiritService.sync(player);
            UnseenHandService.syncCooldown(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (instance != null && event.getEntity() instanceof ServerPlayer player) {
            instance.saveMemory(player);
        }
    }

    private void tick() {
        tickAura();
        if (isActive(server)) {
            SpiritService.tickAll(server);
            UnseenHandService.tickAll(server);
        }
        if (!isActive(server) || state == ReturnState.RETURNING || state == ReturnState.RECOVERING) {
            return;
        }
        long now = System.currentTimeMillis();
        if (!checkpoints.hasStableCheckpoint() && now >= firstCheckpointMillis) {
            if (checkpoints.createCandidate("initial", true)) {
                try {
                    checkpoints.promoteCandidate("initial");
                    LOGGER.info("Death Return initial checkpoint created at game time {}", checkpoints.worldGameTime());
                } catch (Exception exception) {
                    LOGGER.error("Failed to promote initial Death Return checkpoint", exception);
                    state = ReturnState.FAILED;
                }
            }
            return;
        }

        if (state == ReturnState.CANDIDATE_READY) {
            if (now < candidateReadyMillis) {
                if (!isSafeNow()) {
                    discardCandidate();
                }
                return;
            }
            if (isSafeNow()) {
                try {
                    checkpoints.promoteCandidate("auto");
                    state = ReturnState.IDLE;
                    LOGGER.info("Death Return checkpoint promoted at game time {}", checkpoints.worldGameTime());
                } catch (Exception exception) {
                    LOGGER.error("Failed to promote Death Return checkpoint", exception);
                    discardCandidate();
                    state = ReturnState.FAILED;
                }
            } else {
                discardCandidate();
            }
            return;
        }

        if (state == ReturnState.IDLE && now >= nextAutoCheckpointMillis) {
            firstCheckpointMillis = now;
            nextAutoCheckpointMillis = now + minutes(ReturnConfig.AUTO_INTERVAL_MINUTES.get());
            if (isSafeNow() && checkpoints.createCandidate("auto", false)) {
                state = ReturnState.CANDIDATE_READY;
                candidateCreatedMillis = now;
                candidateReadyMillis = now + seconds(ReturnConfig.CANDIDATE_GRACE_SECONDS.get());
            }
        }
    }

    private void onPlayerDamage(ServerPlayer player, DamageSource source, float amount) {
        lastDamageMillis = System.currentTimeMillis();
        if (state == ReturnState.CANDIDATE_READY) {
            discardCandidate();
        }
    }

    private void startReturn(ServerPlayer player, DamageSource source) {
        if (!isActive(server) || returnScheduled || state == ReturnState.RETURNING || state == ReturnState.RECOVERING) {
            return;
        }
        if (!checkpoints.hasStableCheckpoint()) {
            if (!checkpoints.createCandidate("emergency", true)) {
                LOGGER.error("Death Return could not create an emergency checkpoint");
                return;
            }
            try {
                checkpoints.promoteCandidate("emergency");
            } catch (Exception exception) {
                LOGGER.error("Death Return could not promote an emergency checkpoint", exception);
                return;
            }
        }
        if (state == ReturnState.CANDIDATE_READY) {
            discardCandidate();
        }
        WitchScentService.cancelCurse(player);
        returnScheduled = true;
        triggerPlayer = player;
        pendingSpiritLoss = SpiritRules.lossForDamageSource(source);
        state = ReturnState.RETURNING;
        String cause = source.getLocalizedDeathMessage(player).getString();
        ReturnMemoryService.appendDeath(server, player, cause, player.level().dimension(), player.position());
        showTransition(player);
        server.execute(this::performReturn);
    }

    private void performReturn() {
        if (!returnScheduled) {
            return;
        }
        try {
            List<ServerPlayer> players = List.copyOf(server.getPlayerList().getPlayers());
            reloadService.reloadToStableCheckpoint(players);
            if (triggerPlayer != null && checkpoints.stableManifest() != null) {
                ReturnMemoryService.recordReturn(
                        server,
                        triggerPlayer,
                        checkpoints.stableManifest().worldGameTime(),
                        Level.OVERWORLD
                );
                SpiritService.applyReturnLoss(triggerPlayer, pendingSpiritLoss);
                UnseenHandService.resetCooldown(triggerPlayer);
                beginPostReturnReveal(triggerPlayer);
                beginReturnAura(triggerPlayer);
                SpiritService.sync(triggerPlayer);
                if (ReturnConfig.SHOW_TRANSITION.get()) {
                    playReturnSound(triggerPlayer);
                }
                auraUntil.put(triggerPlayer.getUUID(), System.currentTimeMillis() + minutes(5));
            }
            state = ReturnState.IDLE;
            nextAutoCheckpointMillis = System.currentTimeMillis() + minutes(ReturnConfig.AUTO_INTERVAL_MINUTES.get());
            broadcast(Component.literal("Death Return completed. The world was restored to the last checkpoint."));
        } catch (Throwable exception) {
            LOGGER.error("Death Return failed", exception);
            stopTransition(triggerPlayer);
            state = ReturnState.FAILED;
            broadcast(Component.literal("Death Return failed. Check the server log immediately."));
        } finally {
            returnScheduled = false;
            triggerPlayer = null;
        }
    }

    public String manualCreate(ServerPlayer player) {
        if (!isActive(server)) {
            return "Death Return is disabled outside Hardcore mode.";
        }
        if (state != ReturnState.IDLE) {
            return "A checkpoint candidate or return is already in progress.";
        }
        if (!isSafeNow()) {
            return "The world is not in a safe checkpoint state.";
        }
        if (!checkpoints.createCandidate("manual", false)) {
            return "Checkpoint capture failed or timed out.";
        }
        state = ReturnState.CANDIDATE_READY;
        long now = System.currentTimeMillis();
        candidateCreatedMillis = now;
        candidateReadyMillis = now + seconds(ReturnConfig.CANDIDATE_GRACE_SECONDS.get());
        return "Candidate checkpoint created. It will be promoted after the grace period.";
    }

    public String manualForce(ServerPlayer player) {
        if (!isActive(server)) {
            return "Death Return is disabled outside Hardcore mode.";
        }
        if (state == ReturnState.RETURNING || state == ReturnState.RECOVERING) {
            return "Cannot force a checkpoint during a return.";
        }
        if (state == ReturnState.CANDIDATE_READY) {
            discardCandidate();
        }
        if (!checkpoints.createCandidate("force", true)) {
            state = ReturnState.FAILED;
            return "Forced checkpoint capture failed.";
        }
        try {
            checkpoints.promoteCandidate("force");
            state = ReturnState.IDLE;
            return "Forced checkpoint promoted.";
        } catch (Exception exception) {
            LOGGER.error("Failed to force-promote Death Return checkpoint", exception);
            state = ReturnState.FAILED;
            return "Forced checkpoint promotion failed.";
        }
    }

    public String recover() {
        if (state != ReturnState.FAILED) {
            return "Death Return is not in a failed state.";
        }
        state = ReturnState.IDLE;
        return "Death Return state reset to idle.";
    }

    public boolean isReturnInProgress() {
        return returnScheduled || state == ReturnState.RETURNING || state == ReturnState.RECOVERING;
    }

    public String status() {
        if (!isActive(server)) {
            return "Death Return: disabled (Subaru Mode is not enabled).";
        }
        String checkpoint = checkpoints.hasStableCheckpoint()
                ? "stable at game time " + checkpoints.stableManifest().worldGameTime()
                : "none";
        return "Death Return: " + state + ", checkpoint=" + checkpoint
                + ", safe=" + isSafeNow()
                + ", nextAutoIn=" + Math.max(0L, nextAutoCheckpointMillis - System.currentTimeMillis()) / 1000L + "s";
    }

    private boolean isSafeNow() {
        long now = System.currentTimeMillis();
        if (now - lastDamageMillis < seconds(ReturnConfig.DAMAGE_FREE_SECONDS.get())) {
            return false;
        }
        double required = ReturnConfig.SAFE_HEALTH_PERCENT.get();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.isAlive() || player.getMaxHealth() <= 0.0F) {
                return false;
            }
            if (player.getHealth() / player.getMaxHealth() < required) {
                return false;
            }
        }
        return true;
    }

    private void discardCandidate() {
        checkpoints.discardCandidate();
        state = ReturnState.IDLE;
        candidateCreatedMillis = 0L;
        candidateReadyMillis = 0L;
    }

    private void saveMemory() {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            saveMemory(player);
        }
    }

    private void saveMemory(ServerPlayer player) {
        PlayerMemory memory = ReturnMemoryService.getOrCreate(server, player.getUUID());
        ReturnMemoryService.save(server, player.getUUID(), memory);
    }

    private void broadcast(Component component) {
        server.getPlayerList().broadcastSystemMessage(component, false);
    }

    private void showTransition(ServerPlayer player) {
        if (!ReturnConfig.SHOW_TRANSITION.get()) {
            return;
        }
        ReturnNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ReturnTransitionPacket(ReturnTransitionPhase.BLACKOUT, 20 * 120, 1.0F));
    }

    private void beginPostReturnReveal(ServerPlayer player) {
        if (!ReturnConfig.SHOW_TRANSITION.get()) {
            return;
        }
        ReturnNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ReturnTransitionPacket(ReturnTransitionPhase.REVEAL, 20 * 4 + 5, 1.0F));
    }

    private void beginReturnAura(ServerPlayer player) {
        if (!ReturnConfig.SHOW_TRANSITION.get()) {
            return;
        }
        ReturnNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ReturnTransitionPacket(ReturnTransitionPhase.AURA, 20 * 300, 1.0F));
    }

    private void stopTransition(ServerPlayer player) {
        if (player == null || player.connection == null) {
            return;
        }
        ReturnNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ReturnTransitionPacket(ReturnTransitionPhase.STOP, 0, 0.0F));
    }

    private void tickAura() {
        if (auraUntil.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        auraParticleTick++;
        Iterator<Map.Entry<UUID, Long>> iterator = auraUntil.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (entry.getValue() < now) {
                iterator.remove();
                continue;
            }
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null || !player.isAlive() || auraParticleTick % 5 != 0) {
                continue;
            }
            spawnAuraParticles(player);
        }
    }

    private void spawnAuraParticles(ServerPlayer player) {
        double time = (System.currentTimeMillis() % 100000L) / 1000.0D;
        for (int i = 0; i < 8; i++) {
            double angle = time * 0.7D + i * Math.PI / 4.0D;
            double radius = 0.65D + 0.18D * Math.sin(time * 1.3D + i);
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            double y = player.getY() + 0.55D + 0.35D * Math.sin(time * 0.9D + i * 0.8D);
            player.serverLevel().sendParticles(ParticleTypes.PORTAL, x, y, z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
            player.serverLevel().sendParticles(ParticleTypes.WITCH, x, y + 0.15D, z, 1, 0.01D, 0.01D, 0.01D, 0.0D);
            if (i % 2 == 0) {
                player.serverLevel().sendParticles(ParticleTypes.DRAGON_BREATH, x, y, z, 1, 0.0D, 0.03D, 0.0D, 0.005D);
            }
        }
    }

    private void playReturnSound(ServerPlayer player) {
        player.connection.send(new ClientboundSoundPacket(
                Re_mc.DEATH_RETURN.getHolder().orElseThrow(),
                SoundSource.PLAYERS,
                player.getX(),
                player.getY(),
                player.getZ(),
                1.0F,
                1.0F,
                player.serverLevel().getRandom().nextLong()
        ));
    }

    private static long seconds(long value) {
        return value * 1000L;
    }

    private static long minutes(long value) {
        return value * 60_000L;
    }
}
