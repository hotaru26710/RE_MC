package org.hotaru.re_mc.deathreturn.spirit;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.network.PacketDistributor;
import org.hotaru.re_mc.deathreturn.ReturnConfig;
import org.hotaru.re_mc.deathreturn.memory.PlayerMemory;
import org.hotaru.re_mc.deathreturn.memory.ReturnMemoryService;
import org.hotaru.re_mc.deathreturn.network.ReturnNetwork;
import org.hotaru.re_mc.deathreturn.network.SpiritSyncPacket;
import org.hotaru.re_mc.deathreturn.mode.SubaruModeManager;

import java.util.List;

public final class SpiritService {
    private static final int DEBUFF_REFRESH_INTERVAL_TICKS = 100;
    private static final int RECOVERY_INTERVAL_TICKS = 1200;
    private static int tickCounter;

    private SpiritService() {
    }

    public static void tickAll(MinecraftServer server) {
        if (!ReturnConfig.SPIRIT_ENABLED.get()) {
            return;
        }
        tickCounter++;
        boolean refreshDebuffs = tickCounter % DEBUFF_REFRESH_INTERVAL_TICKS == 0;
        boolean recover = tickCounter % RECOVERY_INTERVAL_TICKS == 0;
        for (ServerPlayer player : List.copyOf(server.getPlayerList().getPlayers())) {
            if (!isEligible(player)) {
                continue;
            }
            PlayerMemory memory = ReturnMemoryService.getOrCreate(server, player.getUUID());
            if (recover) {
                boolean changed = false;
                if (memory.isSpiritKeepFull()) {
                    if (memory.getSpirit() < 100.0F) {
                        memory.setSpirit(100.0F);
                        changed = true;
                    }
                } else if (memory.getSpirit() < 100.0F) {
                    memory.setSpirit(memory.getSpirit() + ReturnConfig.SPIRIT_RECOVERY_PER_MINUTE.get());
                    changed = true;
                }
                if (memory.getWitchScent() > 0.0F) {
                    memory.setWitchScent(memory.getWitchScent() - ReturnConfig.WITCH_SCENT_DECAY_PER_MINUTE.get());
                    changed = true;
                }
                if (changed) {
                    ReturnMemoryService.save(server, player.getUUID(), memory);
                    sync(player, memory.getSpirit());
                }
            }
            if (refreshDebuffs) {
                refreshDebuffs(player, memory.getSpirit());
            }
        }
    }

    public static void sync(ServerPlayer player) {
        PlayerMemory memory = ReturnMemoryService.getOrCreate(player.server, player.getUUID());
        sync(player, memory.getSpirit());
        refreshDebuffs(player, memory.getSpirit());
    }

    public static void sync(ServerPlayer player, float spirit) {
        PlayerMemory memory = ReturnMemoryService.getOrCreate(player.server, player.getUUID());
        ReturnNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SpiritSyncPacket(spirit, memory.getWitchScent(), ReturnConfig.SPIRIT_ENABLED.get() && ReturnConfig.SPIRIT_HUD_ENABLED.get() && SubaruModeManager.isEnabled(player.server))
        );
    }

    public static boolean hasSpirit(ServerPlayer player, float amount) {
        PlayerMemory memory = ReturnMemoryService.getOrCreate(player.server, player.getUUID());
        return memory.isSpiritKeepFull() || memory.getSpirit() + 0.001F >= amount;
    }

    public static boolean spendSpirit(ServerPlayer player, float amount, float scentGain) {
        PlayerMemory memory = ReturnMemoryService.getOrCreate(player.server, player.getUUID());
        if (!memory.isSpiritKeepFull() && memory.getSpirit() + 0.001F < amount) {
            return false;
        }
        setSpiritAfterCost(memory, amount);
        memory.setWitchScent(memory.getWitchScent() + scentGain);
        ReturnMemoryService.save(player.server, player.getUUID(), memory);
        sync(player, memory.getSpirit());
        refreshDebuffs(player, memory.getSpirit());
        return true;
    }
    public static void addWitchScent(ServerPlayer player, float amount) {
        PlayerMemory memory = ReturnMemoryService.getOrCreate(player.server, player.getUUID());
        memory.setWitchScent(memory.getWitchScent() + amount);
        ReturnMemoryService.save(player.server, player.getUUID(), memory);
        sync(player, memory.getSpirit());
    }

    public static void restoreSpirit(ServerPlayer player, float amount, float scentGain) {
        PlayerMemory memory = ReturnMemoryService.getOrCreate(player.server, player.getUUID());
        restoreSpiritValue(memory, amount);
        memory.setWitchScent(memory.getWitchScent() + scentGain);
        ReturnMemoryService.save(player.server, player.getUUID(), memory);
        sync(player, memory.getSpirit());
        refreshDebuffs(player, memory.getSpirit());
    }

    public static void restoreSpiritNoScent(ServerPlayer player, float amount) {
        PlayerMemory memory = ReturnMemoryService.getOrCreate(player.server, player.getUUID());
        restoreSpiritValue(memory, amount);
        ReturnMemoryService.save(player.server, player.getUUID(), memory);
        sync(player, memory.getSpirit());
        refreshDebuffs(player, memory.getSpirit());
    }

    public static boolean spendSpiritNoScent(ServerPlayer player, float amount) {
        PlayerMemory memory = ReturnMemoryService.getOrCreate(player.server, player.getUUID());
        if (!memory.isSpiritKeepFull() && memory.getSpirit() + 0.001F < amount) {
            return false;
        }
        setSpiritAfterCost(memory, amount);
        ReturnMemoryService.save(player.server, player.getUUID(), memory);
        sync(player, memory.getSpirit());
        refreshDebuffs(player, memory.getSpirit());
        return true;
    }

    public static void drainSpiritNoScent(ServerPlayer player, float amount) {
        PlayerMemory memory = ReturnMemoryService.getOrCreate(player.server, player.getUUID());
        setSpiritAfterCost(memory, amount);
        ReturnMemoryService.save(player.server, player.getUUID(), memory);
        sync(player, memory.getSpirit());
        refreshDebuffs(player, memory.getSpirit());
    }

    public static void applyReturnLoss(ServerPlayer player, int lossPercent) {
        PlayerMemory memory = ReturnMemoryService.getOrCreate(player.server, player.getUUID());
        setSpiritAfterCost(memory, lossPercent);
        memory.setWitchScent(memory.getWitchScent() + 8.0F + lossPercent * 0.5F);
        ReturnMemoryService.save(player.server, player.getUUID(), memory);
        sync(player, memory.getSpirit());
        refreshDebuffs(player, memory.getSpirit());
    }

    public static boolean toggleKeepFull(ServerPlayer player) {
        PlayerMemory memory = ReturnMemoryService.getOrCreate(player.server, player.getUUID());
        memory.setSpiritKeepFull(!memory.isSpiritKeepFull());
        if (memory.isSpiritKeepFull()) {
            memory.setSpirit(100.0F);
        }
        ReturnMemoryService.save(player.server, player.getUUID(), memory);
        sync(player, memory.getSpirit());
        refreshDebuffs(player, memory.getSpirit());
        return memory.isSpiritKeepFull();
    }

    public static void refreshDebuffs(ServerPlayer player, float spirit) {
        if (!ReturnConfig.SPIRIT_ENABLED.get() || !isEligible(player)) {
            return;
        }
        if (spirit <= 75.0F) {
            addOrRefresh(player, MobEffects.DIG_SLOWDOWN, 0);
        }
        if (spirit <= 50.0F) {
            addOrRefresh(player, MobEffects.WEAKNESS, 0);
        }
        if (spirit < 30.0F) {
            addOrRefresh(player, MobEffects.MOVEMENT_SLOWDOWN, 0);
        }
        if (spirit <= 25.0F) {
            addOrRefresh(player, MobEffects.DARKNESS, 0);
        }
    }

    private static void setSpiritAfterCost(PlayerMemory memory, float amount) {
        if (memory.isSpiritKeepFull()) {
            memory.setSpirit(100.0F);
            return;
        }
        memory.setSpirit(memory.getSpirit() - amount);
    }

    private static void restoreSpiritValue(PlayerMemory memory, float amount) {
        if (memory.isSpiritKeepFull()) {
            memory.setSpirit(100.0F);
            return;
        }
        memory.setSpirit(memory.getSpirit() + amount);
    }

    private static void addOrRefresh(ServerPlayer player, MobEffect effect, int amplifier) {
        MobEffectInstance existing = player.getEffect(effect);
        if (existing != null && (existing.getAmplifier() > amplifier || existing.getDuration() > 140)) {
            return;
        }
        player.addEffect(new MobEffectInstance(effect, 120, amplifier, false, true, true));
    }

    public static boolean isEligible(ServerPlayer player) {
        return !player.isCreative() && !player.isSpectator() && player.isAlive();
    }
}
