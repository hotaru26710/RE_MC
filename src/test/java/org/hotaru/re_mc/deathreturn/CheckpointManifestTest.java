package org.hotaru.re_mc.deathreturn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CheckpointManifestTest {
    @Test
    void nbtRoundTripPreservesManifest() {
        CheckpointManifest manifest = new CheckpointManifest(100L, 200L, "test");
        CheckpointManifest restored = CheckpointManifest.load(manifest.save());

        assertEquals(100L, restored.createdAtMillis());
        assertEquals(200L, restored.worldGameTime());
        assertEquals("test", restored.source());
    }
}
