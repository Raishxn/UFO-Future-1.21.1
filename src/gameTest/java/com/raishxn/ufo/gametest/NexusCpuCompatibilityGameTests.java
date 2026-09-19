package com.raishxn.ufo.gametest;

import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.core.definitions.AEBlocks;
import com.raishxn.ufo.api.ae.NexusCraftingUnitOwnership;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.QuantumComputationNexusControllerBlock;
import com.raishxn.ufo.block.entity.QuantumComputationNexusControllerBE;
import com.raishxn.ufo.block.entity.pattern.QuantumComputationNexusPatternFactory;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Regression coverage for normal AE2/addon CPUs sharing a world with the Nexus. */
@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class NexusCpuCompatibilityGameTests {
    private static final BlockPos CONTROLLER = new BlockPos(20, 5, 20);

    @GameTest(template = "event_driven_structure", timeoutTicks = 200)
    public static void nexusDoesNotClaimForeignCraftingCpu(GameTestHelper helper) {
        Fixture fixture = assemble(helper);
        BlockPos foreignPos = fixture.definition().pattern()
                .getExpectedPositions(fixture.controllerPos(), Direction.NORTH, 'I').getFirst();
        helper.getLevel().setBlockAndUpdate(
                foreignPos, AEBlocks.CRAFTING_STORAGE_1K.block().defaultBlockState());

        CraftingBlockEntity foreignCpu = (CraftingBlockEntity) helper.getLevel().getBlockEntity(foreignPos);
        fixture.controller().scanStructure(helper.getLevel());
        helper.assertTrue(fixture.controller().isAssembled(), "Nexus did not form around a foreign CPU");
        helper.assertTrue(((NexusCraftingUnitOwnership) foreignCpu).ufo$getNexusController() == null,
                "Nexus claimed a normal AE2/addon crafting CPU");

        // Simulate the persistent marker written by affected UFO 3.0 worlds. A rescan must heal
        // it and immediately let AE2 rebuild the CPU instead of leaving 'no available CPU'.
        ((NexusCraftingUnitOwnership) foreignCpu).ufo$setNexusController(fixture.controllerPos());
        if (foreignCpu.getCluster() != null) foreignCpu.breakCluster();
        fixture.controller().scanStructure(helper.getLevel());
        helper.assertTrue(((NexusCraftingUnitOwnership) foreignCpu).ufo$getNexusController() == null,
                "Nexus did not clear legacy ownership from a foreign crafting CPU");
        helper.assertTrue(foreignCpu.getCluster() != null,
                "Foreign crafting CPU did not reform after legacy ownership was cleared");
        helper.succeed();
    }

    @GameTest(template = "event_driven_structure", timeoutTicks = 200)
    public static void dismantlingNexusReleasesItsComputeModules(GameTestHelper helper) {
        Fixture fixture = assemble(helper);
        BlockPos modulePos = fixture.definition().pattern()
                .getExpectedPositions(fixture.controllerPos(), Direction.NORTH, 'I').getFirst();
        helper.getLevel().setBlockAndUpdate(modulePos,
                ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1B)
                        .get().defaultBlockState());
        CraftingBlockEntity module = (CraftingBlockEntity) helper.getLevel().getBlockEntity(modulePos);
        fixture.controller().scanStructure(helper.getLevel());
        helper.assertTrue(fixture.controllerPos().equals(
                        ((NexusCraftingUnitOwnership) module).ufo$getNexusController()),
                "Nexus did not claim its own compute module");

        BlockPos casingPos = fixture.definition().pattern()
                .getExpectedPositions(fixture.controllerPos(), Direction.NORTH, 'C').getFirst();
        helper.getLevel().setBlockAndUpdate(casingPos, Blocks.AIR.defaultBlockState());
        fixture.controller().scanStructure(helper.getLevel());
        helper.assertTrue(!fixture.controller().isAssembled(), "Broken Nexus remained formed");
        helper.assertTrue(((NexusCraftingUnitOwnership) module).ufo$getNexusController() == null,
                "Dismantled Nexus retained compute-module ownership");
        helper.assertTrue(module.getCluster() != null,
                "Released compute module did not reform as a normal AE2 CPU");
        helper.succeed();
    }

    private static Fixture assemble(GameTestHelper helper) {
        var definition = QuantumComputationNexusPatternFactory.getDefinition();
        BlockPos controllerPos = helper.absolutePos(CONTROLLER);
        helper.getLevel().setBlockAndUpdate(controllerPos,
                MultiblockBlocks.QUANTUM_COMPUTATION_NEXUS_CONTROLLER.get().defaultBlockState()
                        .setValue(QuantumComputationNexusControllerBlock.FACING, Direction.NORTH));
        definition.pattern().assembleAsCreative(
                helper.getLevel(), controllerPos, Direction.NORTH, definition.defaultCreativeStates());
        return new Fixture(controllerPos,
                (QuantumComputationNexusControllerBE) helper.getLevel().getBlockEntity(controllerPos), definition);
    }

    private record Fixture(BlockPos controllerPos, QuantumComputationNexusControllerBE controller,
                           com.raishxn.ufo.api.multiblock.MultiblockDefinition definition) {
    }
}
