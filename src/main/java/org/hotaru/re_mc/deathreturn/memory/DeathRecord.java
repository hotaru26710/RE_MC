package org.hotaru.re_mc.deathreturn.memory;

import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

/**
 * One death event retained by player return memory.
 */
public final class DeathRecord {
    private static final String GAME_TIME_TAG = "gameTime";
    private static final String DEATH_COUNT_TAG = "deathCount";
    private static final String CAUSE_TAG = "cause";
    private static final String DIMENSION_TAG = "dimension";
    private static final String X_TAG = "x";
    private static final String Y_TAG = "y";
    private static final String Z_TAG = "z";

    private final long gameTime;
    private final int deathCount;
    private final String cause;
    private final String dimension;
    private final double x;
    private final double y;
    private final double z;

    public DeathRecord(long gameTime, int deathCount, String cause, String dimension, double x, double y, double z) {
        this.gameTime = gameTime;
        this.deathCount = deathCount;
        this.cause = Objects.requireNonNull(cause, "cause");
        this.dimension = Objects.requireNonNull(dimension, "dimension");
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public long getGameTime() {
        return gameTime;
    }

    public int getDeathCount() {
        return deathCount;
    }

    public String getCause() {
        return cause;
    }

    public String getDimension() {
        return dimension;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putLong(GAME_TIME_TAG, gameTime);
        tag.putInt(DEATH_COUNT_TAG, deathCount);
        tag.putString(CAUSE_TAG, cause);
        tag.putString(DIMENSION_TAG, dimension);
        tag.putDouble(X_TAG, x);
        tag.putDouble(Y_TAG, y);
        tag.putDouble(Z_TAG, z);
        return tag;
    }

    public static DeathRecord fromTag(CompoundTag tag) {
        return new DeathRecord(
                tag.getLong(GAME_TIME_TAG),
                tag.getInt(DEATH_COUNT_TAG),
                tag.getString(CAUSE_TAG),
                tag.getString(DIMENSION_TAG),
                tag.getDouble(X_TAG),
                tag.getDouble(Y_TAG),
                tag.getDouble(Z_TAG)
        );
    }
}
