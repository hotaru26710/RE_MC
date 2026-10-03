package org.hotaru.re_mc.deathreturn;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.common.MinecraftForge;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class WorldReloadService {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final MinecraftServer server;
    private final CheckpointService checkpoints;

    public WorldReloadService(MinecraftServer server, CheckpointService checkpoints) {
        this.server = server;
        this.checkpoints = checkpoints;
    }

    public void reloadToStableCheckpoint(List<ServerPlayer> onlinePlayers) throws Exception {
        server.saveEverything(true, true, true);
        checkpoints.backupCurrentWorld();
        HoldingLevelService holding = new HoldingLevelService(server);
        holding.createAndMovePlayers(onlinePlayers);
        try {
            closeWorldLevels();
            checkpoints.restoreStableWorld();
            readRestoredWorldData();
            rebuildLevels();
            rebindPlayers(onlinePlayers);
            holding.close();
            checkpoints.cleanupAfterSuccessfulRestore();
        } catch (Throwable failure) {
            LOGGER.error("Death Return world reload failed; attempting pre-restore rollback", failure);
            rollbackPreRestore(onlinePlayers);
            holding.close();
            throw new Exception("Death Return rollback failed and pre-restore world was used", failure);
        }
    }

    private void rollbackPreRestore(List<ServerPlayer> onlinePlayers) {
        try {
            closeWorldLevels();
            checkpoints.restorePreRestoreWorld();
            try {
                readRestoredWorldData();
            } catch (Throwable dataFailure) {
                LOGGER.error("Failed to reread pre-restore level.dat; using the in-memory world data to rebuild levels", dataFailure);
            }
            rebuildLevels();
            rebindPlayers(onlinePlayers);
        } catch (Throwable rollbackFailure) {
            LOGGER.error("Failed to recover pre-restore world after Death Return failure", rollbackFailure);
        }
    }

    private void rebuildLevels() {
        ChunkProgressListener listener = new ChunkProgressListener() {
            @Override
            public void updateSpawnPos(ChunkPos pos) {
            }

            @Override
            public void onStatusChange(ChunkPos pos, ChunkStatus status) {
            }

            @Override
            public void start() {
            }

            @Override
            public void stop() {
            }
        };
        server.createLevels(listener);
        server.markWorldsDirty();
    }
    private void closeWorldLevels() throws Exception {
        List<ServerLevel> levels = new ArrayList<>(server.levels.values());
        for (ServerLevel level : levels) {
            if (level == null) {
                continue;
            }
            if (level.dimension() == Level.END && level.getDragonFight() != null) {
                for (ServerPlayer player : List.copyOf(level.players())) {
                    level.getDragonFight().removePlayer(player);
                }
            }
            level.noSave = true;
            MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.level.LevelEvent.Unload(level));
            level.close();
        }
        server.levels.clear();
        server.markWorldsDirty();
    }

    private void readRestoredWorldData() throws Exception {
        RegistryOps<net.minecraft.nbt.Tag> registryOps = RegistryOps.create(NbtOps.INSTANCE, server.registryAccess());
        Pair<WorldData, net.minecraft.world.level.levelgen.WorldDimensions.Complete> pair = server.storageSource.getDataTag(
                registryOps,
                server.getWorldData().getDataConfiguration(),
                server.registryAccess().registryOrThrow(Registries.LEVEL_STEM),
                server.registryAccess().allRegistriesLifecycle()
        );
        server.worldData = pair.getFirst();
    }

    private void rebindPlayers(List<ServerPlayer> onlinePlayers) {
        for (ServerPlayer player : onlinePlayers) {
            try {
                UUID uuid = player.getUUID();
                CompoundTag tag = server.playerDataStorage.load(player);
                ResourceKey<Level> dimension = Level.OVERWORLD;
                if (tag != null && tag.contains("Dimension", 8)) {
                    dimension = DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, tag.get("Dimension")))
                            .resultOrPartial(LOGGER::error)
                            .orElse(Level.OVERWORLD);
                }
                ServerLevel target = server.getLevel(dimension);
                if (target == null) {
                    target = server.overworld();
                }
                reloadStatsAndAdvancements(player, uuid);
                player.setInvulnerable(false);
                player.setNoGravity(false);
                player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                if (tag == null) {
                    resetNewPlayer(player, target);
                } else {
                    player.teleportTo(target, player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
                    sendPlayerSync(player);
                }
            } catch (Throwable exception) {
                LOGGER.error("Failed to rebind player {} after Death Return", player.getGameProfile().getName(), exception);
            }
        }
    }

    private void resetNewPlayer(ServerPlayer player, ServerLevel target) {
        player.getInventory().clearContent();
        player.setExperienceLevels(0);
        player.setExperiencePoints(0);
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(5.0F);
        player.getFoodData().setExhaustion(0.0F);
        player.removeAllEffects();
        var spawn = target.getSharedSpawnPos();
        player.teleportTo(target, spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D, player.getYRot(), player.getXRot());
        sendPlayerSync(player);
    }

    private void reloadStatsAndAdvancements(ServerPlayer player, UUID uuid) {
        try {
            player.advancements.stopListening();
            Path statsPath = server.getWorldPath(LevelResource.PLAYER_STATS_DIR).resolve(uuid + ".json");
            ServerStatsCounter stats = new ServerStatsCounter(server, statsPath.toFile());
            stats.parseLocal(server.getFixerUpper(), player.getGameProfile().getName());
            Path advancementsPath = server.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).resolve(uuid + ".json");
            PlayerAdvancements advancements = new PlayerAdvancements(
                    server.getFixerUpper(),
                    server.getPlayerList(),
                    server.getAdvancements(),
                    advancementsPath,
                    player
            );
            player.stats = stats;
            player.advancements = advancements;
            server.getPlayerList().stats.put(uuid, stats);
            server.getPlayerList().advancements.put(uuid, advancements);
        } catch (Throwable exception) {
            LOGGER.warn("Failed to reload stats/advancements for {}", player.getGameProfile().getName(), exception);
        }
    }

    private void sendPlayerSync(ServerPlayer player) {
        player.connection.send(new ClientboundSetHealthPacket(player.getHealth(), player.getFoodData().getFoodLevel(), player.getFoodData().getSaturationLevel()));
        player.connection.send(new ClientboundSetExperiencePacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
        player.connection.send(new ClientboundSetCarriedItemPacket(player.getInventory().selected));
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
        server.getPlayerList().sendAllPlayerInfo(player);
    }
}






