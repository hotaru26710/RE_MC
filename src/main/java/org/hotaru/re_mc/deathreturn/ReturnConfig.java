package org.hotaru.re_mc.deathreturn;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ReturnConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Enable the Death Return system.")
            .define("enabled", true);
    public static final ForgeConfigSpec.IntValue AUTO_INTERVAL_MINUTES = BUILDER
            .comment("Minutes between automatic checkpoint attempts.")
            .defineInRange("autoIntervalMinutes", 15, 1, 1440);
    public static final ForgeConfigSpec.DoubleValue SAFE_HEALTH_PERCENT = BUILDER
            .comment("All online players must be at or above this health fraction.")
            .defineInRange("safeHealthPercent", 0.5D, 0.0D, 1.0D);
    public static final ForgeConfigSpec.IntValue DAMAGE_FREE_SECONDS = BUILDER
            .comment("Seconds without player damage required before a checkpoint can become safe.")
            .defineInRange("damageFreeSeconds", 30, 0, 300);
    public static final ForgeConfigSpec.IntValue CANDIDATE_GRACE_SECONDS = BUILDER
            .comment("Seconds a candidate checkpoint must remain safe before promotion.")
            .defineInRange("candidateGraceSeconds", 30, 0, 300);
    public static final ForgeConfigSpec.IntValue CAPTURE_TIMEOUT_SECONDS = BUILDER
            .comment("Maximum time budget for copying a checkpoint before it is abandoned.")
            .defineInRange("captureTimeoutSeconds", 10, 1, 300);
    public static final ForgeConfigSpec.BooleanValue ALLOW_BOSS_AND_RAID = BUILDER
            .comment("Boss fights and raids do not block automatic checkpoints.")
            .define("allowBossAndRaid", true);
    public static final ForgeConfigSpec.BooleanValue RETAIN_ONLINE_MEMORY = BUILDER
            .comment("Online players retain Death Return logs across a rollback.")
            .define("retainOnlineMemory", true);
    public static final ForgeConfigSpec.BooleanValue SHOW_TRANSITION = BUILDER
            .comment("Show titles, sounds and the client-side black transition.")
            .define("showTransition", true);
    public static final ForgeConfigSpec.BooleanValue SPIRIT_ENABLED = BUILDER
            .comment("Enable the Spirit system.")
            .define("spiritEnabled", true);
    public static final ForgeConfigSpec.IntValue SPIRIT_RECOVERY_PER_MINUTE = BUILDER
            .comment("Spirit recovered per minute while the player is online.")
            .defineInRange("spiritRecoveryPerMinute", 1, 1, 100);
    public static final ForgeConfigSpec.BooleanValue SPIRIT_HUD_ENABLED = BUILDER
            .comment("Show the Spirit HUD above the player health bar.")
            .define("spiritHudEnabled", true);
    public static final ForgeConfigSpec.IntValue WITCH_SCENT_DECAY_PER_MINUTE = BUILDER
            .comment("Witch's Scent lost per minute while the player is online.")
            .defineInRange("witchScentDecayPerMinute", 1, 1, 100);
    public static final ForgeConfigSpec.IntValue SLEEP_SPIRIT_RESTORE = BUILDER
            .comment("Spirit restored after sleeping in a bed.")
            .defineInRange("sleepSpiritRestore", 20, 1, 100);
    public static final ForgeConfigSpec.IntValue MEMORY_HISTORY_LIMIT = BUILDER
            .comment("Maximum number of death records retained per player.")
            .defineInRange("memoryHistoryLimit", 100, 1, 1000);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private ReturnConfig() {
    }
}
