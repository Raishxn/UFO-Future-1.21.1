package com.raishxn.ufo.crafting;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AggregateRecipeBatchTest {
    @Test
    void oneMasterCrystalProcessesAnEntireEssenceBatchAndReturnsOnce() {
        var batch = AggregateRecipeBatch.of(Map.of("essence", 4L, "crystal", 1L),
                Map.of("upgraded", 1L), Map.of("crystal", 1L));
        assertEquals(Map.of("essence", 4L), batch.consumedInputs());
        long copies = batch.maximumCopies(key -> key.equals("essence") ? 3_999_996L : 0L);
        assertEquals(1_000_000L, copies);
        assertEquals(Map.of("upgraded", 1_000_000L, "crystal", 1L), batch.products(copies));
    }

    @Test
    void returnedCrystalCanSeedEveryStageOfTheEssenceChain() {
        long essence = 4_096L;
        long crystals = 1L;
        for (int stage = 0; stage < 5; stage++) {
            var batch = AggregateRecipeBatch.of(Map.of("essence", 4L, "crystal", 1L),
                    Map.of("upgraded", 1L), Map.of("crystal", 1L));
            assertTrue(crystals >= 1L);
            long available = essence - 4L;
            long copies = batch.maximumCopies(key -> key.equals("essence") ? available : 0L);
            var result = batch.products(copies);
            essence = result.get("upgraded");
            crystals = result.get("crystal");
        }
        assertEquals(4L, essence);
        assertEquals(1L, crystals);
    }

    @Test
    void bucketsAndDamagedToolsRemainConsumedPerCopy() {
        var batch = AggregateRecipeBatch.of(Map.of("water_bucket", 1L, "tool", 1L),
                Map.of("output", 2L), Map.of("bucket", 1L, "damaged_tool", 1L));
        assertEquals(3L, batch.maximumCopies(key -> key.equals("tool") ? 2L : 100L));
        assertEquals(Map.of("output", 6L, "bucket", 3L, "damaged_tool", 3L), batch.products(3L));
    }

    @Test
    void onlyTheReturnedPartOfAnInputIsReusable() {
        var batch = AggregateRecipeBatch.of(Map.of("input", 4L),
                Map.of("output", 1L), Map.of("input", 1L));
        assertEquals(Map.of("input", 3L), batch.consumedInputs());
        assertEquals(4L, batch.maximumCopies(key -> 9L));
        assertEquals(Map.of("output", 4L, "input", 1L), batch.products(4L));
    }

    @Test
    void billionCopyBatchesAndSharedOutputKeysDoNotOverflow() {
        var batch = AggregateRecipeBatch.of(Map.of("input", 1L),
                Map.of("input", 8L), Map.of("input", 1L));
        // This entirely reusable recipe is restricted to the validated first copy.
        assertEquals(1L, batch.maximumCopies(key -> Long.MAX_VALUE));
        assertEquals(Map.of("input", 9L), batch.products(1L));

        var consumed = AggregateRecipeBatch.of(Map.of("fuel", 1L, "input", 1L),
                Map.of("input", 8L), Map.of("input", 1L));
        long copies = consumed.maximumCopies(key -> Long.MAX_VALUE);
        assertEquals((Long.MAX_VALUE - 1L) / 8L, copies);
        assertEquals(copies * 8L + 1L, consumed.products(copies).get("input"));
        assertTrue(copies > Integer.MAX_VALUE);
    }
}
