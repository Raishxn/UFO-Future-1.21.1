package com.raishxn.ufo.api.multiblock.port;

import com.raishxn.ufocore.api.port.ItemPort;
import com.raishxn.ufocore.api.port.ItemPortGroup;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemPortGroupTest {
    @Test
    void aggregatesPartialExtractionWithoutExceedingRequest() {
        ItemPortGroup<String> group = new ItemPortGroup<>(List.of(
                fixedPort(30L, 0L),
                fixedPort(50L, 0L),
                fixedPort(200L, 0L)));

        assertEquals(100L, group.extract("minecraft:iron_ingot", 100L, true));
    }

    @Test
    void forwardsKeySimulationAndRemainingAmountForInsertion() {
        List<String> calls = new ArrayList<>();
        ItemPortGroup<String> group = new ItemPortGroup<>(List.of(
                recordingPort(calls, 0L, 40L),
                recordingPort(calls, 0L, 100L)));

        assertEquals(75L, group.insert("minecraft:copper_ingot", 75L, true));
        assertEquals(List.of(
                "insert:minecraft:copper_ingot:75:true",
                "insert:minecraft:copper_ingot:35:true"), calls);
    }

    @Test
    void transactionalExtractionSimulatesBeforeCommitAndMutatesOnlyOnce() {
        MutablePort port = new MutablePort("minecraft:diamond", 80L, 100L);
        ItemPortGroup<String> group = new ItemPortGroup<>(List.of(port));

        assertEquals(60L, group.extractTransactional("minecraft:diamond", 60L));
        assertEquals(20L, port.stored);
        assertEquals(List.of(true, false), port.extractSimulations);
    }

    @Test
    void transactionalInsertionPreservesUnacceptedRemainder() {
        MutablePort port = new MutablePort("minecraft:diamond", 80L, 100L);
        ItemPortGroup<String> group = new ItemPortGroup<>(List.of(port));

        assertEquals(20L, group.insertTransactional("minecraft:diamond", 60L));
        assertEquals(100L, port.stored);
        assertEquals(List.of(true, false), port.insertSimulations);
    }

    @Test
    void commitMaySafelyReturnLessThanSimulation() {
        ItemPort<String> changingPort = new ItemPort<>() {
            @Override
            public long extractItem(String item, long maxAmount, boolean simulate) {
                return simulate ? maxAmount : Math.min(7L, maxAmount);
            }

            @Override
            public long insertItem(String item, long maxAmount, boolean simulate) {
                return 0L;
            }
        };

        assertEquals(7L, new ItemPortGroup<>(List.of(changingPort))
                .extractTransactional("minecraft:gold_ingot", 20L));
    }

    @Test
    void emptyGroupAndInvalidRequestsTransferNothing() {
        ItemPortGroup<String> group = ItemPortGroup.empty();

        assertEquals(0L, group.extractTransactional("minecraft:stone", 10L));
        assertEquals(0L, group.insertTransactional(null, 10L));
        assertEquals(0L, group.insertTransactional("minecraft:stone", 0L));
    }

    private static ItemPort<String> fixedPort(long extract, long insert) {
        return new ItemPort<>() {
            @Override
            public long extractItem(String item, long maxAmount, boolean simulate) {
                return Math.min(maxAmount, extract);
            }

            @Override
            public long insertItem(String item, long maxAmount, boolean simulate) {
                return Math.min(maxAmount, insert);
            }
        };
    }

    private static ItemPort<String> recordingPort(List<String> calls, long extract, long insert) {
        return new ItemPort<>() {
            @Override
            public long extractItem(String item, long maxAmount, boolean simulate) {
                calls.add("extract:" + item + ":" + maxAmount + ":" + simulate);
                return Math.min(maxAmount, extract);
            }

            @Override
            public long insertItem(String item, long maxAmount, boolean simulate) {
                calls.add("insert:" + item + ":" + maxAmount + ":" + simulate);
                return Math.min(maxAmount, insert);
            }
        };
    }

    private static final class MutablePort implements ItemPort<String> {
        private final String item;
        private final long capacity;
        private final List<Boolean> extractSimulations = new ArrayList<>();
        private final List<Boolean> insertSimulations = new ArrayList<>();
        private long stored;

        private MutablePort(String item, long stored, long capacity) {
            this.item = item;
            this.stored = stored;
            this.capacity = capacity;
        }

        @Override
        public long extractItem(String item, long maxAmount, boolean simulate) {
            this.extractSimulations.add(simulate);
            if (!this.item.equals(item)) {
                return 0L;
            }
            long extracted = Math.min(maxAmount, this.stored);
            if (!simulate) {
                this.stored -= extracted;
            }
            return extracted;
        }

        @Override
        public long insertItem(String item, long maxAmount, boolean simulate) {
            this.insertSimulations.add(simulate);
            if (!this.item.equals(item)) {
                return 0L;
            }
            long inserted = Math.min(maxAmount, this.capacity - this.stored);
            if (!simulate) {
                this.stored += inserted;
            }
            return inserted;
        }
    }
}
