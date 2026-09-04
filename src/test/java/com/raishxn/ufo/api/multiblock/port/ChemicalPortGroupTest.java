package com.raishxn.ufo.api.multiblock.port;

import com.raishxn.ufocore.api.port.ChemicalPort;
import com.raishxn.ufocore.api.port.ChemicalPortGroup;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChemicalPortGroupTest {
    @Test
    void aggregatesPartialExtractionWithoutExceedingRequest() {
        ChemicalPortGroup<String> group = new ChemicalPortGroup<>(List.of(
                fixedPort(30L, 0L),
                fixedPort(50L, 0L),
                fixedPort(200L, 0L)));

        assertEquals(100L, group.extract("mekanism:oxygen", 100L, true));
    }

    @Test
    void forwardsKeySimulationAndRemainingAmountForInsertion() {
        List<String> calls = new ArrayList<>();
        ChemicalPort<String> first = recordingPort(calls, 0L, 40L);
        ChemicalPort<String> second = recordingPort(calls, 0L, 100L);

        ChemicalPortGroup<String> group = new ChemicalPortGroup<>(List.of(first, second));

        assertEquals(75L, group.insert("mekanism:hydrogen", 75L, true));
        assertEquals(List.of(
                "insert:mekanism:hydrogen:75:true",
                "insert:mekanism:hydrogen:35:true"), calls);
    }

    @Test
    void transactionalExtractionSimulatesBeforeCommitAndMutatesOnlyOnce() {
        MutablePort port = new MutablePort("mekanism:oxygen", 80L, 100L);
        ChemicalPortGroup<String> group = new ChemicalPortGroup<>(List.of(port));

        assertEquals(60L, group.extractTransactional("mekanism:oxygen", 60L));
        assertEquals(20L, port.stored);
        assertEquals(List.of(true, false), port.extractSimulations);
    }

    @Test
    void transactionalInsertionRejectsDifferentChemicalAndPreservesStorage() {
        MutablePort port = new MutablePort("mekanism:oxygen", 20L, 100L);
        ChemicalPortGroup<String> group = new ChemicalPortGroup<>(List.of(port));

        assertEquals(0L, group.insertTransactional("mekanism:hydrogen", 60L));
        assertEquals(20L, port.stored);
        assertEquals(List.of(true), port.insertSimulations);
    }

    @Test
    void emptyGroupAndInvalidRequestsTransferNothing() {
        ChemicalPortGroup<String> group = ChemicalPortGroup.empty();

        assertEquals(0L, group.extractTransactional("mekanism:oxygen", 10L));
        assertEquals(0L, group.insertTransactional(null, 10L));
        assertEquals(0L, group.insertTransactional("mekanism:oxygen", 0L));
    }

    private static ChemicalPort<String> fixedPort(long extract, long insert) {
        return new ChemicalPort<>() {
            @Override
            public long extractChemical(String chemical, long maxAmount, boolean simulate) {
                return Math.min(maxAmount, extract);
            }

            @Override
            public long insertChemical(String chemical, long maxAmount, boolean simulate) {
                return Math.min(maxAmount, insert);
            }
        };
    }

    private static ChemicalPort<String> recordingPort(List<String> calls, long extract, long insert) {
        return new ChemicalPort<>() {
            @Override
            public long extractChemical(String chemical, long maxAmount, boolean simulate) {
                calls.add("extract:" + chemical + ":" + maxAmount + ":" + simulate);
                return Math.min(maxAmount, extract);
            }

            @Override
            public long insertChemical(String chemical, long maxAmount, boolean simulate) {
                calls.add("insert:" + chemical + ":" + maxAmount + ":" + simulate);
                return Math.min(maxAmount, insert);
            }
        };
    }

    private static final class MutablePort implements ChemicalPort<String> {
        private final String chemical;
        private final long capacity;
        private final List<Boolean> extractSimulations = new ArrayList<>();
        private final List<Boolean> insertSimulations = new ArrayList<>();
        private long stored;

        private MutablePort(String chemical, long stored, long capacity) {
            this.chemical = chemical;
            this.stored = stored;
            this.capacity = capacity;
        }

        @Override
        public long extractChemical(String chemical, long maxAmount, boolean simulate) {
            this.extractSimulations.add(simulate);
            if (!this.chemical.equals(chemical)) {
                return 0L;
            }
            long extracted = Math.min(maxAmount, this.stored);
            if (!simulate) {
                this.stored -= extracted;
            }
            return extracted;
        }

        @Override
        public long insertChemical(String chemical, long maxAmount, boolean simulate) {
            this.insertSimulations.add(simulate);
            if (!this.chemical.equals(chemical)) {
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
