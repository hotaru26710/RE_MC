package org.hotaru.re_mc.deathreturn.memory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Server-side memory for one player's death/return history.
 */
public final class PlayerMemory {
    public static final int MAX_DEATH_RECORDS = 100;

    private static final String UUID_TAG = "uuid";
    private static final String PROFILE_NAME_TAG = "profileName";
    private static final String TOTAL_RETURNS_TAG = "totalReturns";
    private static final String LAST_CHECKPOINT_TIME_TAG = "lastCheckpointTime";
    private static final String LAST_CHECKPOINT_DIMENSION_TAG = "lastCheckpointDimension";
    private static final String SPIRIT_TAG = "spirit";
    private static final String SCENT_TAG = "witchScent";
    private static final String UNSEEN_HAND_COOLDOWN_TAG = "unseenHandCooldownEnd";
    private static final String UNSEEN_HAND_DEBUG_TAG = "unseenHandDebugReady";
    private static final String DEATHS_TAG = "deaths";

    private final UUID playerId;
    private String profileName = "";
    private int totalReturns;
    private long lastCheckpointTime;
    private String lastCheckpointDimension = "";
    private float spirit = 100.0F;
    private float witchScent;
    private long unseenHandCooldownEnd;
    private boolean unseenHandDebugReady;
    private final List<DeathRecord> deaths = new ArrayList<>();

    public PlayerMemory(UUID playerId) {
        this.playerId = Objects.requireNonNull(playerId, "playerId");
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName == null ? "" : profileName;
    }

    public int getTotalReturns() {
        return totalReturns;
    }

    public void setTotalReturns(int totalReturns) {
        this.totalReturns = Math.max(0, totalReturns);
    }

    public long getLastCheckpointTime() {
        return lastCheckpointTime;
    }

    public void setLastCheckpointTime(long lastCheckpointTime) {
        this.lastCheckpointTime = lastCheckpointTime;
    }

    public String getLastCheckpointDimension() {
        return lastCheckpointDimension;
    }

    public void setLastCheckpointDimension(String lastCheckpointDimension) {
        this.lastCheckpointDimension = lastCheckpointDimension == null ? "" : lastCheckpointDimension;
    }

    public float getSpirit() {
        return spirit;
    }

    public void setSpirit(float spirit) {
        this.spirit = Math.max(0.0F, Math.min(100.0F, spirit));
    }

    public float getWitchScent() {
        return witchScent;
    }

    public void setWitchScent(float witchScent) {
        this.witchScent = Math.max(0.0F, Math.min(100.0F, witchScent));
    }

    public long getUnseenHandCooldownEnd() {
        return unseenHandCooldownEnd;
    }

    public void setUnseenHandCooldownEnd(long unseenHandCooldownEnd) {
        this.unseenHandCooldownEnd = Math.max(0L, unseenHandCooldownEnd);
    }

    public boolean isUnseenHandDebugReady() {
        return unseenHandDebugReady;
    }

    public void setUnseenHandDebugReady(boolean unseenHandDebugReady) {
        this.unseenHandDebugReady = unseenHandDebugReady;
    }

    public List<DeathRecord> getDeaths() {
        return Collections.unmodifiableList(deaths);
    }

    /**
     * Adds a death record at the front and keeps only the newest 100 records.
     */
    public void addDeath(DeathRecord deathRecord) {
        deaths.add(0, Objects.requireNonNull(deathRecord, "deathRecord"));
        while (deaths.size() > MAX_DEATH_RECORDS) {
            deaths.remove(deaths.size() - 1);
        }
    }

    public int nextDeathCount() {
        int highest = 0;
        for (DeathRecord death : deaths) {
            highest = Math.max(highest, death.getDeathCount());
        }
        return highest + 1;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(UUID_TAG, playerId);
        tag.putString(PROFILE_NAME_TAG, profileName);
        tag.putInt(TOTAL_RETURNS_TAG, totalReturns);
        tag.putLong(LAST_CHECKPOINT_TIME_TAG, lastCheckpointTime);
        tag.putString(LAST_CHECKPOINT_DIMENSION_TAG, lastCheckpointDimension);
        tag.putFloat(SPIRIT_TAG, spirit);
        tag.putFloat(SCENT_TAG, witchScent);
        tag.putLong(UNSEEN_HAND_COOLDOWN_TAG, unseenHandCooldownEnd);
        tag.putBoolean(UNSEEN_HAND_DEBUG_TAG, unseenHandDebugReady);

        ListTag deathsTag = new ListTag();
        for (DeathRecord death : deaths) {
            deathsTag.add(death.toTag());
        }
        tag.put(DEATHS_TAG, deathsTag);
        return tag;
    }

    public static PlayerMemory fromTag(CompoundTag tag, UUID fallbackPlayerId) {
        UUID playerId = tag.contains(UUID_TAG) ? tag.getUUID(UUID_TAG) : fallbackPlayerId;
        PlayerMemory memory = new PlayerMemory(playerId);
        memory.setProfileName(tag.getString(PROFILE_NAME_TAG));
        memory.setTotalReturns(tag.getInt(TOTAL_RETURNS_TAG));
        memory.setLastCheckpointTime(tag.getLong(LAST_CHECKPOINT_TIME_TAG));
        memory.setLastCheckpointDimension(tag.getString(LAST_CHECKPOINT_DIMENSION_TAG));
        memory.setSpirit(tag.contains(SPIRIT_TAG) ? tag.getFloat(SPIRIT_TAG) : 100.0F);
        memory.setWitchScent(tag.contains(SCENT_TAG) ? tag.getFloat(SCENT_TAG) : 0.0F);
        memory.setUnseenHandCooldownEnd(tag.contains(UNSEEN_HAND_COOLDOWN_TAG) ? tag.getLong(UNSEEN_HAND_COOLDOWN_TAG) : 0L);
        memory.setUnseenHandDebugReady(tag.contains(UNSEEN_HAND_DEBUG_TAG) && tag.getBoolean(UNSEEN_HAND_DEBUG_TAG));

        ListTag deathsTag = tag.getList(DEATHS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < deathsTag.size() && i < MAX_DEATH_RECORDS; i++) {
            memory.deaths.add(DeathRecord.fromTag(deathsTag.getCompound(i)));
        }
        return memory;
    }
}
