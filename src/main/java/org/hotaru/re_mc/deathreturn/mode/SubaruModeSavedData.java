package org.hotaru.re_mc.deathreturn.mode;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public final class SubaruModeSavedData extends SavedData {
    private static final String ENABLED_TAG = "enabled";
    private static final String ALLOW_COMMANDS_TAG = "allowCommands";

    private boolean enabled;
    private boolean allowCommands;

    public boolean isEnabled() {
        return enabled;
    }

    public boolean allowCommands() {
        return allowCommands;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        setDirty();
    }

    public void setAllowCommands(boolean allowCommands) {
        this.allowCommands = allowCommands;
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean(ENABLED_TAG, enabled);
        tag.putBoolean(ALLOW_COMMANDS_TAG, allowCommands);
        return tag;
    }

    public static SubaruModeSavedData load(CompoundTag tag) {
        SubaruModeSavedData data = new SubaruModeSavedData();
        data.enabled = tag.getBoolean(ENABLED_TAG);
        data.allowCommands = tag.getBoolean(ALLOW_COMMANDS_TAG);
        return data;
    }
}
