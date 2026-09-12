package com.raishxn.ufo.gametest;

import appeng.core.definitions.AEBlocks;
import appeng.api.networking.GridHelper;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.api.multiblock.MultiblockDefinition;
import com.raishxn.ufo.block.InfinityFabricationSingularityControllerBlock;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.QuantumComputationNexusControllerBlock;
import com.raishxn.ufo.block.QuantumPatternFabricationMatrixControllerBlock;
import com.raishxn.ufo.block.entity.InfinityFabricationSingularityControllerBE;
import com.raishxn.ufo.block.entity.QuantumComputationNexusControllerBE;
import com.raishxn.ufo.block.entity.QuantumGridLinkBE;
import com.raishxn.ufo.block.entity.QuantumPatternFabricationMatrixControllerBE;
import com.raishxn.ufo.block.entity.pattern.InfinityFabricationSingularityPatternFactory;
import com.raishxn.ufo.block.entity.pattern.QuantumComputationNexusPatternFactory;
import com.raishxn.ufo.block.entity.pattern.QuantumPatternFabricationMatrixPatternFactory;
import com.raishxn.ufo.diagnostic.MachinePerformanceRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Real structures proving that topology scans only run after lifecycle invalidation. */
@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class EndgameStructureLifecycleGameTests {
    private static final BlockPos CONTROLLER = new BlockPos(20, 5, 20);
    private static final int IDLE_TICKS = 80;

    @GameTest(template = "event_driven_structure", timeoutTicks = 300)
    public static void computationNexusIsEventDriven(GameTestHelper helper) {
        BlockState state = MultiblockBlocks.QUANTUM_COMPUTATION_NEXUS_CONTROLLER.get()
                .defaultBlockState().setValue(QuantumComputationNexusControllerBlock.FACING, Direction.NORTH);
        exercise(helper, state, QuantumComputationNexusPatternFactory.getDefinition(), Direction.NORTH,
                QuantumComputationNexusControllerBE.class);
    }

    @GameTest(template = "event_driven_structure", timeoutTicks = 300)
    public static void patternMatrixIsEventDriven(GameTestHelper helper) {
        BlockState state = MultiblockBlocks.QUANTUM_PATTERN_FABRICATION_MATRIX_CONTROLLER.get()
                .defaultBlockState().setValue(QuantumPatternFabricationMatrixControllerBlock.FACING, Direction.NORTH);
        exercise(helper, state, QuantumPatternFabricationMatrixPatternFactory.getDefinition(), Direction.EAST,
                QuantumPatternFabricationMatrixControllerBE.class);
    }

    @GameTest(template = "event_driven_structure", timeoutTicks = 300)
    public static void fabricationSingularityIsEventDriven(GameTestHelper helper) {
        BlockState state = MultiblockBlocks.INFINITY_FABRICATION_SINGULARITY_CONTROLLER.get()
                .defaultBlockState().setValue(InfinityFabricationSingularityControllerBlock.FACING, Direction.NORTH);
        exercise(helper, state, InfinityFabricationSingularityPatternFactory.getDefinition(), Direction.EAST,
                InfinityFabricationSingularityControllerBE.class);
    }

    private static <T extends BlockEntity & IMultiblockController> void exercise(
            GameTestHelper helper, BlockState controllerState, MultiblockDefinition definition,
            Direction patternFacing, Class<T> controllerType) {
        BlockPos controllerPos = helper.absolutePos(CONTROLLER);
        helper.getLevel().setBlockAndUpdate(controllerPos, controllerState);
        definition.pattern().assembleAsCreative(
                helper.getLevel(), controllerPos, patternFacing, definition.defaultCreativeStates());
        T controller = controllerType.cast(helper.getLevel().getBlockEntity(controllerPos));
        controller.scanStructure(helper.getLevel());

        helper.runAfterDelay(10, () -> {
            helper.assertTrue(controller.isAssembled(), controllerType.getSimpleName() + " did not form");
            long initialScans = scanCount(helper, controllerPos);
            helper.runAfterDelay(IDLE_TICKS, () -> {
                assertScanCount(helper, controllerPos, initialScans,
                        controllerType.getSimpleName() + " rescanned while idle");
                exerciseStructureChange(helper, controller, definition, patternFacing, controllerPos,
                        controllerType.getSimpleName(), initialScans);
            });
        });
    }

    private static void exerciseStructureChange(
            GameTestHelper helper, IMultiblockController controller, MultiblockDefinition definition,
            Direction patternFacing, BlockPos controllerPos, String machineName, long initialScans) {
        BlockPos casingPos = definition.pattern().getExpectedPositions(controllerPos, patternFacing, 'C').getFirst();
        BlockState casingState = definition.defaultCreativeStates().get('C');
        helper.getLevel().destroyBlock(casingPos, false);
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(!controller.isAssembled(), machineName + " ignored a broken casing");
            assertScanCount(helper, controllerPos, initialScans + 1,
                    machineName + " did not debounce the break to one scan");
            helper.getLevel().setBlockAndUpdate(casingPos, casingState);
            helper.runAfterDelay(5, () -> {
                helper.assertTrue(controller.isAssembled(), machineName + " did not reform after casing restoration");
                assertScanCount(helper, controllerPos, initialScans + 2,
                        machineName + " did not debounce restoration to one scan");
                exerciseNetworkReconnect(helper, controller, definition, patternFacing, controllerPos,
                        machineName, initialScans + 2);
            });
        });
    }

    private static void exerciseNetworkReconnect(
            GameTestHelper helper, IMultiblockController controller, MultiblockDefinition definition,
            Direction patternFacing, BlockPos controllerPos, String machineName, long structuralScans) {
        BlockPos linkPos = definition.pattern().getExpectedPositions(controllerPos, patternFacing, 'L').getFirst();
        QuantumGridLinkBE link = (QuantumGridLinkBE) helper.getLevel().getBlockEntity(linkPos);
        BlockPos powerPos = helper.absolutePos(new BlockPos(38, 1, 38));
        helper.assertTrue(helper.getLevel().getBlockState(powerPos).isAir(),
                machineName + " network fixture position is obstructed by the structure");
        helper.getLevel().setBlockAndUpdate(powerPos, AEBlocks.CREATIVE_ENERGY_CELL.block().defaultBlockState());
        helper.runAfterDelay(2, () -> {
            var power = (AENetworkedBlockEntity) helper.getLevel().getBlockEntity(powerPos);
            var connection = GridHelper.createConnection(link.getMainNode().getNode(), power.getMainNode().getNode());
            helper.runAfterDelay(25, () -> {
                helper.assertTrue(link.isNetworkReady(), machineName + " did not connect to the ME network");
                assertScanCount(helper, controllerPos, structuralScans,
                        machineName + " used a structural scan for network connection");
                connection.destroy();
                helper.runAfterDelay(10, () -> {
                    helper.assertTrue(!link.isNetworkReady(), machineName + " stayed connected after network removal");
                    assertScanCount(helper, controllerPos, structuralScans,
                            machineName + " used a structural scan for network loss");
                    GridHelper.createConnection(link.getMainNode().getNode(), power.getMainNode().getNode());
                    helper.runAfterDelay(25, () -> {
                        helper.assertTrue(link.isNetworkReady(), machineName + " did not reconnect to the ME network");
                        assertScanCount(helper, controllerPos, structuralScans,
                                machineName + " used a structural scan for network reconnection");
                        helper.runAfterDelay(IDLE_TICKS, () -> {
                            assertScanCount(helper, controllerPos, structuralScans,
                                    machineName + " resumed polling after network reconnection");
                            helper.succeed();
                        });
                    });
                });
            });
        });
    }

    private static long scanCount(GameTestHelper helper, BlockPos controllerPos) {
        String dimension = helper.getLevel().dimension().location().toString();
        var snapshot = MachinePerformanceRegistry.INSTANCE.snapshot(dimension, controllerPos.asLong());
        helper.assertTrue(snapshot.isPresent(), "missing structure scan metrics for controller");
        return snapshot.orElseThrow().scanCount();
    }

    private static void assertScanCount(GameTestHelper helper, BlockPos controllerPos, long expected, String message) {
        long actual = scanCount(helper, controllerPos);
        helper.assertTrue(actual == expected, message + ": expected " + expected + ", got " + actual);
    }
}
