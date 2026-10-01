package org.hotaru.re_mc.deathreturn.spirit;

import net.minecraft.world.damagesource.DamageSource;

import java.util.Locale;
import java.util.Set;

/**
 * Pure death-loss classification rules.
 */
public final class SpiritRules {
    private static final int UNKNOWN_LOSS = 10;

    private static final Set<String> LOSS_5_DAMAGE_TYPES = Set.of(
            "fall",
            "suffocation",
            "suffocate",
            "inwall",
            "cactus",
            "crush",
            "cramming",
            "fallingblock",
            "fallinganvil",
            "anvil",
            "fallingstalactite",
            "flyintowall"
    );

    private static final Set<String> LOSS_10_DAMAGE_TYPES = Set.of(
            "drown",
            "starve",
            "freeze",
            "mob",
            "mobattack",
            "player",
            "playerattack"
    );

    private static final Set<String> LOSS_15_DAMAGE_TYPES = Set.of(
            "fire",
            "infire",
            "onfire",
            "magic",
            "indirectmagic",
            "wither",
            "witherskull",
            "lightning",
            "lightningbolt",
            "hotfloor",
            "fireball"
    );

    private static final Set<String> LOSS_20_DAMAGE_TYPES = Set.of(
            "explosion",
            "explosionplayer",
            "playerexplosion"
    );

    private static final Set<String> LOSS_25_DAMAGE_TYPES = Set.of(
            "lava"
    );

    private static final Set<String> LOSS_30_DAMAGE_TYPES = Set.of(
            "outofworld",
            "felloutofworld",
            "generickill"
    );

    private SpiritRules() {
    }

    public static int lossForDamageType(String damageTypeId) {
        String normalizedId = normalize(damageTypeId);

        if (LOSS_5_DAMAGE_TYPES.contains(normalizedId)) {
            return 5;
        }
        if (LOSS_10_DAMAGE_TYPES.contains(normalizedId)) {
            return 10;
        }
        if (LOSS_15_DAMAGE_TYPES.contains(normalizedId)) {
            return 15;
        }
        if (LOSS_20_DAMAGE_TYPES.contains(normalizedId)) {
            return 20;
        }
        if (LOSS_25_DAMAGE_TYPES.contains(normalizedId)) {
            return 25;
        }
        if (LOSS_30_DAMAGE_TYPES.contains(normalizedId)) {
            return 30;
        }

        return UNKNOWN_LOSS;
    }

    public static int lossForDamageSource(DamageSource source) {
        if (source == null) {
            return UNKNOWN_LOSS;
        }
        return lossForDamageType(source.getMsgId());
    }

    private static String normalize(String damageTypeId) {
        if (damageTypeId == null) {
            return "";
        }

        String lowerCaseId = damageTypeId.trim().toLowerCase(Locale.ROOT);
        int namespaceSeparator = lowerCaseId.lastIndexOf(':');
        if (namespaceSeparator >= 0) {
            lowerCaseId = lowerCaseId.substring(namespaceSeparator + 1);
        }

        StringBuilder normalized = new StringBuilder(lowerCaseId.length());
        for (int i = 0; i < lowerCaseId.length(); i++) {
            char character = lowerCaseId.charAt(i);
            if ((character >= 'a' && character <= 'z') || (character >= '0' && character <= '9')) {
                normalized.append(character);
            }
        }
        return normalized.toString();
    }
}