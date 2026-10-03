package org.hotaru.re_mc.deathreturn;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.hotaru.re_mc.Re_mc;
import com.mojang.logging.LogUtils;

import java.util.ArrayList;
import java.util.List;

public final class HoldingLevelService {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceKey<Level> RETURN_GAP = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation(Re_mc.MODID, "return_gap")
    );

    private final MinecraftServer server;
    private ServerLevel holdingLevel;
    private final List<ServerPlayer> players = new ArrayList<>();

    public HoldingLevelService(MinecraftServer server) {
        this.server = server;
    }

    public ServerLevel createAndMovePlayers(List<ServerPlayer> onlinePlayers) throws Exception {
        LevelStem stem = server.registryAccess()
                .registryOrThrow(Registries.LEVEL_STEM)
                .getOrThrow(LevelStem.OVERWORLD);
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
        this.holdingLevel = new ServerLevel(
                server,
                Runnable::run,
                server.storageSource,
                server.getWorldData().overworldData(),
                RETURN_GAP,
                stem,
                listener,
                false,
                server.overworld().getSeed(),
                List.of(),
                true,
                null
        );
        this.holdingLevel.noSave = true;
        this.holdingLevel.getChunk(0, 0, ChunkStatus.FULL, true);

        this.players.clear();
        for (ServerPlayer player : onlinePlayers) {
            moveToHolding(player, holdingLevel);
        }
        return holdingLevel;
    }

    public void releasePlayer(ServerPlayer player) {
        player.setInvulnerable(false);
        player.setNoGravity(false);
        player.setDeltaMovement(Vec3.ZERO);
    }

    public void close() {
        for (ServerPlayer player : players) {
            releasePlayer(player);
        }
        players.clear();
        if (holdingLevel != null) {
            try {
                holdingLevel.noSave = true;
                holdingLevel.close();
            } catch (Exception exception) {
                LOGGER.warn("Failed to close Death Return holding level", exception);
            }
            holdingLevel = null;
        }
    }

    public ServerLevel holdingLevel() {
        return holdingLevel;
    }

    private static void clearEndBossBar(ServerPlayer player, ServerLevel oldLevel) {
        if (oldLevel.dimension() != Level.END || oldLevel.getDragonFight() == null) {
            return;
        }
        oldLevel.getDragonFight().removePlayer(player);
    }

    private void moveToHolding(ServerPlayer player, ServerLevel target) {
        ServerLevel oldLevel = player.serverLevel();
        LevelData levelData = target.getLevelData();
        clearEndBossBar(player, oldLevel);
        player.connection.send(new ClientboundRespawnPacket(
                target.dimensionTypeId(),
                target.dimension(),
                BiomeManager.obfuscateSeed(target.getSeed()),
                player.gameMode.getGameModeForPlayer(),
                player.gameMode.getPreviousGameModeForPlayer(),
                target.isDebug(),
                target.isFlat(),
                (byte) 3,
                player.getLastDeathLocation(),
                player.getPortalCooldown()
        ));
        player.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
        oldLevel.removePlayerImmediately(player, Entity.RemovalReason.CHANGED_DIMENSION);
        player.setServerLevel(target);
        target.addDuringCommandTeleport(player);
        player.moveTo(0.5D, target.getMinBuildHeight() + 100.0D, 0.5D, player.getYRot(), player.getXRot());
        player.setInvulnerable(true);
        player.setNoGravity(true);
        player.setDeltaMovement(Vec3.ZERO);
        player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        player.connection.resetPosition();
        players.add(player);
    }
}

