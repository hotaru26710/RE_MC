package org.hotaru.re_mc.deathreturn.memory;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerMemoryTest {
    @Test
    void keepsNewestHundredDeathsFirst() {
        PlayerMemory memory = new PlayerMemory(UUID.randomUUID());
        for (int i = 1; i <= 120; i++) {
            memory.addDeath(new DeathRecord(i, i, "cause-" + i, "minecraft:overworld", i, i, i));
        }

        assertEquals(100, memory.getDeaths().size());
        assertEquals(120, memory.getDeaths().get(0).getDeathCount());
        assertEquals(21, memory.getDeaths().get(99).getDeathCount());
    }

    @Test
    void missingSpiritDefaultsToFull() {
        UUID id = UUID.randomUUID();
        PlayerMemory memory = new PlayerMemory(id);
        var tag = memory.toTag();
        tag.remove("spirit");
        PlayerMemory restored = PlayerMemory.fromTag(tag, id);
        assertEquals(100.0F, restored.getSpirit());
    }

    @Test
    void nbtRoundTripPreservesState() {
        UUID id = UUID.randomUUID();
        PlayerMemory memory = new PlayerMemory(id);
        memory.setProfileName("Tester");
        memory.setTotalReturns(4);
        memory.setLastCheckpointTime(12345L);
        memory.setLastCheckpointDimension("minecraft:overworld");
        memory.setSpirit(42.5F);
        memory.addDeath(new DeathRecord(99L, 1, "fall", "minecraft:overworld", 1.0D, 2.0D, 3.0D));

        PlayerMemory restored = PlayerMemory.fromTag(memory.toTag(), id);

        assertEquals(4, restored.getTotalReturns());
        assertEquals("Tester", restored.getProfileName());
        assertEquals(12345L, restored.getLastCheckpointTime());
        assertEquals(42.5F, restored.getSpirit());
        assertEquals(1, restored.getDeaths().size());
    }
}
