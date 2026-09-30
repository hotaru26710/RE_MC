package org.hotaru.re_mc.deathreturn;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public final class CheckpointService {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> ROOT_EXCLUSIONS = Set.of("re_mc", "session.lock", "logs", "crash-reports");

    private final MinecraftServer server;
    private final Path worldDir;
    private final Path returnDir;
    private final Path stableDir;
    private final Path candidateDir;
    private final Path preRestoreDir;
    private final Path manifestFile;

    private CheckpointManifest stableManifest;

    public CheckpointService(MinecraftServer server) {
        this.server = server;
        this.worldDir = server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
        this.returnDir = worldDir.resolve("re_mc").resolve("return");
        this.stableDir = returnDir.resolve("stable");
        this.candidateDir = returnDir.resolve("candidate");
        this.preRestoreDir = returnDir.resolve("pre_restore");
        this.manifestFile = returnDir.resolve("stable.nbt");
    }

    public void ensureDirectories() throws IOException {
        Files.createDirectories(returnDir);
        Files.createDirectories(returnDir.resolve("memory"));
    }

    public void loadManifest() {
        stableManifest = null;
        if (!Files.isRegularFile(manifestFile)) {
            return;
        }
        try {
            CompoundTag tag = NbtIo.readCompressed(manifestFile.toFile());
            stableManifest = CheckpointManifest.load(tag);
        } catch (Exception exception) {
            LOGGER.error("Failed to read Death Return stable manifest {}", manifestFile, exception);
        }
    }

    public boolean hasStableCheckpoint() {
        return stableManifest != null && Files.isDirectory(stableDir);
    }

    public CheckpointManifest stableManifest() {
        return stableManifest;
    }

    public long worldGameTime() {
        ServerLevel overworld = server.overworld();
        return overworld == null ? 0L : overworld.getGameTime();
    }

    public boolean createCandidate(String source, boolean force) {
        long started = System.nanoTime();
        try {
            ensureDirectories();
            deleteTree(candidateDir);
            Files.createDirectories(candidateDir);
            server.saveEverything(true, true, true);
            copyWorldTree(candidateDir);
            long elapsedSeconds = (System.nanoTime() - started) / 1_000_000_000L;
            if (!force && elapsedSeconds > ReturnConfig.CAPTURE_TIMEOUT_SECONDS.get()) {
                LOGGER.warn("Abandoning Death Return checkpoint candidate: copy took {}s", elapsedSeconds);
                deleteTree(candidateDir);
                return false;
            }
            Files.writeString(candidateDir.resolve(".source"), source);
            return true;
        } catch (Exception exception) {
            LOGGER.error("Failed to create Death Return checkpoint candidate", exception);
            try {
                deleteTree(candidateDir);
            } catch (IOException ignored) {
            }
            return false;
        }
    }

    public void promoteCandidate(String source) throws IOException {
        if (!Files.isDirectory(candidateDir)) {
            throw new IOException("Candidate checkpoint directory does not exist");
        }
        CheckpointManifest manifest = new CheckpointManifest(System.currentTimeMillis(), worldGameTime(), source);
        deleteTree(stableDir);
        moveTree(candidateDir, stableDir);
        NbtIo.writeCompressed(manifest.save(), manifestFile.toFile());
        stableManifest = manifest;
    }

    public void discardCandidate() {
        try {
            deleteTree(candidateDir);
        } catch (IOException exception) {
            LOGGER.warn("Failed to discard Death Return candidate", exception);
        }
    }

    public void backupCurrentWorld() throws IOException {
        deleteTree(preRestoreDir);
        Files.createDirectories(preRestoreDir);
        copyWorldTree(preRestoreDir);
    }

    public void restoreStableWorld() throws IOException {
        if (!hasStableCheckpoint()) {
            throw new IOException("No stable Death Return checkpoint is available");
        }
        clearWorldForRestore();
        copyTree(stableDir, worldDir, false);
    }

    public void restorePreRestoreWorld() throws IOException {
        if (!Files.isDirectory(preRestoreDir)) {
            throw new IOException("No pre-restore world is available");
        }
        clearWorldForRestore();
        copyTree(preRestoreDir, worldDir, false);
    }

    public void cleanupAfterSuccessfulRestore() {
        try {
            deleteTree(preRestoreDir);
            deleteTree(candidateDir);
        } catch (IOException exception) {
            LOGGER.warn("Failed to clean up Death Return temporary backups", exception);
        }
    }

    public Path returnDir() {
        return returnDir;
    }

    private void clearWorldForRestore() throws IOException {
        if (!Files.isDirectory(worldDir)) {
            Files.createDirectories(worldDir);
            return;
        }
        try (var children = Files.list(worldDir)) {
            for (Path child : children.toList()) {
                String name = child.getFileName().toString();
                if (ROOT_EXCLUSIONS.contains(name)) {
                    continue;
                }
                deleteTree(child);
            }
        }
    }

    private void copyWorldTree(Path target) throws IOException {
        copyTree(worldDir, target, true);
    }

    private void copyTree(Path source, Path target, boolean excludeReturnDirectory) throws IOException {
        Files.createDirectories(target);
        Path returnRelative = worldDir.relativize(returnDir);
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relative = source.relativize(dir);
                if (excludeReturnDirectory && !relative.toString().isEmpty() && relative.startsWith(returnRelative)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                String name = dir.getFileName() == null ? "" : dir.getFileName().toString();
                if (relative.toString().isEmpty() || !ROOT_EXCLUSIONS.contains(name)) {
                    Files.createDirectories(target.resolve(relative));
                } else {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relative = source.relativize(file);
                if (excludeReturnDirectory && relative.startsWith(returnRelative)) {
                    return FileVisitResult.CONTINUE;
                }
                if (ROOT_EXCLUSIONS.contains(relative.getName(0).toString())) {
                    return FileVisitResult.CONTINUE;
                }
                Path destination = target.resolve(relative);
                Files.createDirectories(destination.getParent());
                Files.copy(file, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void moveTree(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicFailure) {
            Files.move(source, target);
        }
    }

    public static void deleteTree(Path root) throws IOException {
        if (root == null || !Files.exists(root)) {
            return;
        }
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
