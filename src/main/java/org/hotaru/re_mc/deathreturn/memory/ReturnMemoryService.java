package org.hotaru.re_mc.deathreturn.memory;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Persists return memory outside vanilla world snapshot data.
 */
public final class ReturnMemoryService {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String MEMORY_DIRECTORY = "re_mc/return/memory";

    private ReturnMemoryService() {
    }

    public static PlayerMemory load(MinecraftServer server, UUID playerId) {
        Path memoryFile = memoryFile(server, playerId);
        if (!Files.exists(memoryFile)) {
            return new PlayerMemory(playerId);
        }

        try {
            return PlayerMemory.fromTag(NbtIo.readCompressed(memoryFile.toFile()), playerId);
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("Failed to load return memory for {}", playerId, exception);
            return new PlayerMemory(playerId);
        }
    }

    public static void save(MinecraftServer server, UUID playerId, PlayerMemory memory) {
        Path memoryFile = memoryFile(server, playerId);
        try {
            Files.createDirectories(memoryFile.getParent());
            NbtIo.writeCompressed(memory.toTag(), memoryFile.toFile());
        } catch (IOException exception) {
            LOGGER.error("Failed to save return memory for {}", playerId, exception);
        }
    }

    public static DeathRecord appendDeath(MinecraftServer server, ServerPlayer player, String cause, ResourceKey<Level> dimension, Vec3 pos) {
        UUID playerId = player.getUUID();
        PlayerMemory memory = getOrCreate(server, playerId);
        memory.setProfileName(player.getGameProfile().getName());

        DeathRecord deathRecord = new DeathRecord(
                player.serverLevel().getGameTime(),
                memory.nextDeathCount(),
                cause == null ? "" : cause,
                dimension.location().toString(),
                pos.x(),
                pos.y(),
                pos.z()
        );
        memory.addDeath(deathRecord);
        save(server, playerId, memory);
        return deathRecord;
    }

    public static PlayerMemory recordReturn(MinecraftServer server, ServerPlayer player, long checkpointGameTime, ResourceKey<Level> checkpointDimension) {
        UUID playerId = player.getUUID();
        PlayerMemory memory = getOrCreate(server, playerId);
        memory.setProfileName(player.getGameProfile().getName());
        memory.setTotalReturns(memory.getTotalReturns() + 1);
        memory.setLastCheckpointTime(checkpointGameTime);
        memory.setLastCheckpointDimension(checkpointDimension.location().toString());
        save(server, playerId, memory);
        return memory;
    }

    public static PlayerMemory getOrCreate(MinecraftServer server, UUID playerId) {
        return load(server, playerId);
    }

    private static Path memoryFile(MinecraftServer server, UUID playerId) {
        return server.getWorldPath(LevelResource.ROOT)
                .resolve(MEMORY_DIRECTORY)
                .resolve(playerId + ".nbt");
    }
}
