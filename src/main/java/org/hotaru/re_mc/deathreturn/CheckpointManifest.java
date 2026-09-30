package org.hotaru.re_mc.deathreturn;

import net.minecraft.nbt.CompoundTag;

public record CheckpointManifest(long createdAtMillis, long worldGameTime, String source) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("createdAtMillis", createdAtMillis);
        tag.putLong("worldGameTime", worldGameTime);
        tag.putString("source", source);
        return tag;
    }

    public static CheckpointManifest load(CompoundTag tag) {
        return new CheckpointManifest(
                tag.getLong("createdAtMillis"),
                tag.getLong("worldGameTime"),
                tag.getString("source")
        );
    }
}
