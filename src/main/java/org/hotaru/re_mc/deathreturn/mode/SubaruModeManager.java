package org.hotaru.re_mc.deathreturn.mode;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public final class SubaruModeManager {
    private static final String DATA_NAME = "re_mc_subaru_mode";

    private SubaruModeManager() {
    }

    public static SubaruModeSavedData data(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(SubaruModeSavedData::load, SubaruModeSavedData::new, DATA_NAME);
    }

    public static boolean isEnabled(MinecraftServer server) {
        return data(server).isEnabled();
    }

    public static boolean allowCommands(MinecraftServer server) {
        return data(server).allowCommands();
    }

    public static void setEnabled(MinecraftServer server, boolean enabled, boolean allowCommands) {
        SubaruModeSavedData data = data(server);
        data.setEnabled(enabled);
        data.setAllowCommands(allowCommands);
        applyCommandPolicy(server, enabled, allowCommands);
    }

    public static void setAllowCommands(MinecraftServer server, boolean allowCommands) {
        SubaruModeSavedData data = data(server);
        data.setAllowCommands(allowCommands);
        applyCommandPolicy(server, data.isEnabled(), allowCommands);
    }

    public static void applySavedCommandPolicy(MinecraftServer server) {
        SubaruModeSavedData data = data(server);
        applyCommandPolicy(server, data.isEnabled(), data.allowCommands());
    }

    private static void applyCommandPolicy(MinecraftServer server, boolean enabled, boolean allowCommands) {
        if (enabled) {
            server.getWorldData().getLevelSettings().allowCommands = allowCommands;
        }
    }
}
