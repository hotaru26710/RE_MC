package org.hotaru.re_mc.deathreturn.spirit;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.hotaru.re_mc.Re_mc;
import org.hotaru.re_mc.deathreturn.memory.PlayerMemory;
import org.hotaru.re_mc.deathreturn.memory.ReturnMemoryService;
import org.hotaru.re_mc.deathreturn.mode.SubaruModeManager;
import org.hotaru.re_mc.deathreturn.network.ReturnNetwork;
import org.hotaru.re_mc.deathreturn.network.WitchCursePacket;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Re_mc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WitchScentService {
    private static final List<String> FORBIDDEN_PHRASES = List.of(
            "死亡回归",
            "死归",
            "重生",
            "死亡回归",
            "return by death",
            "invisible providence"
    );
    private static final Map<UUID, CurseSequence> CURSES = new HashMap<>();
    private static int tickCounter;

    private WitchScentService() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = event.getServer();
        if (!SubaruModeManager.isEnabled(server)) {
            CURSES.clear();
            return;
        }
        tickCounter++;
        processCurses(server);
        if (tickCounter % 100 == 0) {
            processSpawnBoost(server);
        }
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (!SubaruModeManager.isEnabled(player.server) || CURSES.containsKey(player.getUUID())) {
            return;
        }
        PlayerMemory memory = ReturnMemoryService.getOrCreate(player.server, player.getUUID());
        if (memory.getWitchScent() <= 30.0F) {
            return;
        }
        String text = event.getRawText().toLowerCase(Locale.ROOT);
        if (FORBIDDEN_PHRASES.stream().anyMatch(text::contains)) {
            CURSES.put(player.getUUID(), new CurseSequence(0));
            player.displayClientMessage(Component.translatable("message.re_mc.witch_curse.trigger").withStyle(ChatFormatting.DARK_PURPLE), false);
        }
    }

    public static void cancelCurse(ServerPlayer player) {
        CURSES.remove(player.getUUID());
        clearCurseEffects(player);
    }

    private static void clearCurseEffects(ServerPlayer player) {
        player.removeEffect(MobEffects.DARKNESS);
        player.removeEffect(MobEffects.BLINDNESS);
        ReturnNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new WitchCursePacket(0.0F, false));
    }

    private static void processCurses(MinecraftServer server) {
        Iterator<Map.Entry<UUID, CurseSequence>> iterator = CURSES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, CurseSequence> entry = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                iterator.remove();
                continue;
            }
            if (!player.isAlive()) {
                clearCurseEffects(player);
                iterator.remove();
                continue;
            }
            CurseSequence sequence = entry.getValue();
            sequence.ticks++;
            float progress = Math.min(1.0F, sequence.ticks / 80.0F);
            float darkness = progress * 0.88F;
            if (sequence.ticks % 5 == 0) {
                ReturnNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new WitchCursePacket(darkness, true));
            }
            if (sequence.ticks % 4 == 0) {
                int count = 3 + Math.round(progress * 18.0F);
                player.serverLevel().sendParticles(player, ParticleTypes.PORTAL, false,
                        player.getX(), player.getY() + 0.7D, player.getZ(),
                        count, 0.7D + progress, 0.9D, 0.7D + progress, 0.015D);
                player.serverLevel().sendParticles(player, ParticleTypes.WITCH, false,
                        player.getX(), player.getY() + 0.7D, player.getZ(),
                        Math.max(1, count / 3), 0.6D, 0.8D, 0.6D, 0.01D);
            }
            if (sequence.ticks == 80) {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0, false, true, true));
            }
            if (sequence.ticks > 80) {
                int drainTick = sequence.ticks - 80;
                float drainProgress = Math.min(1.0F, drainTick / 40.0F);
                if (drainTick < 40) {
                    float nextHealth = Math.max(0.01F, player.getMaxHealth() * (1.0F - drainProgress));
                    player.setHealth(nextHealth);
                } else if (player.isAlive() && player.getHealth() > 0.0F) {
                    player.invulnerableTime = 0;
                    player.hurt(player.damageSources().magic(), Float.MAX_VALUE);
                }
            }
            if (!player.isAlive() || player.getHealth() <= 0.0F) {
                clearCurseEffects(player);
                iterator.remove();
            }
        }
    }

    private static void processSpawnBoost(MinecraftServer server) {
        for (ServerPlayer player : List.copyOf(server.getPlayerList().getPlayers())) {
            if (player.isCreative() || player.isSpectator()) {
                continue;
            }
            PlayerMemory memory = ReturnMemoryService.getOrCreate(server, player.getUUID());
            float scent = memory.getWitchScent();
            if (scent <= 30.0F || player.serverLevel().getDifficulty() == Difficulty.PEACEFUL) {
                continue;
            }
            int attempts = 18 + Math.round((scent - 30.0F) * 0.6F);
            int spawns = 1 + Math.round((scent - 30.0F) / 22.0F);
            int spawned = 0;
            for (int attempt = 0; attempt < attempts && spawned < spawns; attempt++) {
                if (trySpawnHostile(player.serverLevel(), player, scent)) {
                    spawned++;
                }
            }
        }
    }

    private static boolean trySpawnHostile(ServerLevel level, ServerPlayer player, float scent) {
        double radius = 12.0D + Math.min(18.0D, scent * 0.22D);
        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        double distance = radius * (0.65D + level.random.nextDouble() * 0.35D);
        BlockPos pos = BlockPos.containing(
                player.getX() + Math.cos(angle) * distance,
                player.getY() + level.random.nextInt(9) - 4,
                player.getZ() + Math.sin(angle) * distance
        );
        if (!level.hasChunkAt(pos)) {
            return false;
        }
        Holder<Biome> biome = level.getBiome(pos);
        MobSpawnSettings.SpawnerData spawnData = biome.value()
                .getMobSettings()
                .getMobs(MobCategory.MONSTER)
                .getRandom(level.random)
                .orElse(null);
        if (spawnData == null || !spawnData.type.canSummon()) {
            return false;
        }
        EntityType<?> type = spawnData.type;
        SpawnPlacements.Type placement = SpawnPlacements.getPlacementType(type);
        if (!NaturalSpawner.isSpawnPositionOk(placement, level, pos, type)) {
            return false;
        }
        if (!SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, pos, level.random)) {
            return false;
        }
        if (!level.noCollision(type.getAABB(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D))) {
            return false;
        }
        if (!(type.create(level) instanceof Mob mob)) {
            return false;
        }
        mob.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        if (!ForgeEventFactory.checkSpawnPosition(mob, level, MobSpawnType.NATURAL)) {
            return false;
        }
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null, null);
        return level.addFreshEntity(mob);
    }

    private static final class CurseSequence {
        private int ticks;

        private CurseSequence(int ticks) {
            this.ticks = ticks;
        }
    }
}
