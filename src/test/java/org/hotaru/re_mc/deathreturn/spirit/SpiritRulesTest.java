package org.hotaru.re_mc.deathreturn.spirit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SpiritRulesTest {
    @Test
    void classifiesFiveLossDamageTypes() {
        assertAll(
                () -> assertEquals(5, SpiritRules.lossForDamageType("fall")),
                () -> assertEquals(5, SpiritRules.lossForDamageType("suffocation")),
                () -> assertEquals(5, SpiritRules.lossForDamageType("inWall")),
                () -> assertEquals(5, SpiritRules.lossForDamageType("cactus")),
                () -> assertEquals(5, SpiritRules.lossForDamageType("crush")),
                () -> assertEquals(5, SpiritRules.lossForDamageType("cramming")),
                () -> assertEquals(5, SpiritRules.lossForDamageType("fallingBlock")),
                () -> assertEquals(5, SpiritRules.lossForDamageType("minecraft:falling_block")),
                () -> assertEquals(5, SpiritRules.lossForDamageType("flyIntoWall"))
        );
    }

    @Test
    void classifiesTenLossDamageTypes() {
        assertAll(
                () -> assertEquals(10, SpiritRules.lossForDamageType("drown")),
                () -> assertEquals(10, SpiritRules.lossForDamageType("starve")),
                () -> assertEquals(10, SpiritRules.lossForDamageType("freeze")),
                () -> assertEquals(10, SpiritRules.lossForDamageType("mob")),
                () -> assertEquals(10, SpiritRules.lossForDamageType("mob_attack")),
                () -> assertEquals(10, SpiritRules.lossForDamageType("player")),
                () -> assertEquals(10, SpiritRules.lossForDamageType("player_attack"))
        );
    }

    @Test
    void classifiesFifteenLossDamageTypes() {
        assertAll(
                () -> assertEquals(15, SpiritRules.lossForDamageType("inFire")),
                () -> assertEquals(15, SpiritRules.lossForDamageType("onFire")),
                () -> assertEquals(15, SpiritRules.lossForDamageType("fire")),
                () -> assertEquals(15, SpiritRules.lossForDamageType("magic")),
                () -> assertEquals(15, SpiritRules.lossForDamageType("indirectMagic")),
                () -> assertEquals(15, SpiritRules.lossForDamageType("wither")),
                () -> assertEquals(15, SpiritRules.lossForDamageType("witherSkull")),
                () -> assertEquals(15, SpiritRules.lossForDamageType("lightningBolt")),
                () -> assertEquals(15, SpiritRules.lossForDamageType("minecraft:lightning_bolt"))
        );
    }

    @Test
    void classifiesTwentyLossDamageTypes() {
        assertAll(
                () -> assertEquals(20, SpiritRules.lossForDamageType("explosion")),
                () -> assertEquals(20, SpiritRules.lossForDamageType("explosion.player")),
                () -> assertEquals(20, SpiritRules.lossForDamageType("player_explosion"))
        );
    }

    @Test
    void classifiesTwentyFiveLossDamageTypes() {
        assertEquals(25, SpiritRules.lossForDamageType("lava"));
    }

    @Test
    void classifiesThirtyLossDamageTypes() {
        assertAll(
                () -> assertEquals(30, SpiritRules.lossForDamageType("outOfWorld")),
                () -> assertEquals(30, SpiritRules.lossForDamageType("minecraft:out_of_world")),
                () -> assertEquals(30, SpiritRules.lossForDamageType("genericKill"))
        );
    }

    @Test
    void fallsBackToTenForUnknownDamageTypes() {
        assertAll(
                () -> assertEquals(10, SpiritRules.lossForDamageType("dragonBreath")),
                () -> assertEquals(10, SpiritRules.lossForDamageType("not_a_damage_type")),
                () -> assertEquals(10, SpiritRules.lossForDamageType("")),
                () -> assertEquals(10, SpiritRules.lossForDamageType(null))
        );
    }

    @Test
    void matchingIsCaseInsensitiveAndNamespaceTolerant() {
        assertAll(
                () -> assertEquals(5, SpiritRules.lossForDamageType("  MINECRAFT:FALL  ")),
                () -> assertEquals(15, SpiritRules.lossForDamageType("Minecraft:IN_FIRE")),
                () -> assertEquals(20, SpiritRules.lossForDamageType("minecraft:explosion.player")),
                () -> assertEquals(30, SpiritRules.lossForDamageType("GENERIC_KILL"))
        );
    }
}