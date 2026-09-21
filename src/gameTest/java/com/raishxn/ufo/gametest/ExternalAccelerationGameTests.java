package com.raishxn.ufo.gametest;

import com.raishxn.ufo.UFOConfig;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.DimensionalMatterAssemblerBlockEntity;
import com.raishxn.ufo.block.entity.QuantumEnergyCellBlockEntity;
import com.raishxn.ufo.block.entity.QuantumInterfaceBlockEntity;
import com.raishxn.ufo.block.entity.QuantumComputationNexusControllerBE;
import com.raishxn.ufo.block.entity.StellarNexusControllerBE;
import com.raishxn.ufo.block.entity.UfoEnergyCellBlockEntity;
import com.raishxn.ufo.block.entity.processing.TickAccelerationLimiter;
import com.raishxn.ufo.diagnostic.MachinePerformanceRegistry;
import com.raishxn.ufo.init.ModBlockEntities;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Reproduces what time accelerators do: call a block's ticker several times while Minecraft's
 * {@code gameTime} is unchanged. The test uses detached real controllers so the world's normal
 * ticker cannot consume part of the same-tick allowance before the assertion.
 */
@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class ExternalAccelerationGameTests {
    private ExternalAccelerationGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void computationNexusCapsRepeatedExternalTicks(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        var controller = new QuantumComputationNexusControllerBE(pos,
                MultiblockBlocks.QUANTUM_COMPUTATION_NEXUS_CONTROLLER.get().defaultBlockState());
        assertCap(helper, controller, pos, ignored -> controller.serverTick());
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void stellarNexusCapsRepeatedExternalTicks(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(6, 2, 2));
        var controller = new StellarNexusControllerBE(ModBlockEntities.STELLAR_NEXUS_CONTROLLER_BE.get(), pos,
                MultiblockBlocks.STELLAR_NEXUS_CONTROLLER.get().defaultBlockState());
        assertCap(helper, controller, pos, ignored -> controller.serverTick());
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void dimensionalMatterAssemblerCapsRepeatedExternalTicks(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        var assembler = new DimensionalMatterAssemblerBlockEntity(ModBlockEntities.DIMENSIONAL_MATTER_ASSEMBLER_BE.get(), pos,
                ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER_BLOCK.get().defaultBlockState());
        assertPrivateLimiterCap(helper, assembler, assembler::serverTick);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void quantumInterfaceCapsRepeatedExternalTicks(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 2));
        var quantumInterface = new QuantumInterfaceBlockEntity(ModBlockEntities.QUANTUM_INTERFACE_BE.get(), pos,
                MultiblockBlocks.QUANTUM_INTERFACE.get().defaultBlockState());
        assertPrivateLimiterCap(helper, quantumInterface, quantumInterface::serverTick);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void energyCellsCapRepeatedExternalTicks(GameTestHelper helper) {
        BlockPos ufoPos = helper.absolutePos(new BlockPos(6, 2, 2));
        var ufoCell = new UfoEnergyCellBlockEntity(ModBlockEntities.UFO_ENERGY_CELL_BE.get(), ufoPos,
                ModBlocks.UFO_ENERGY_CELL.get().defaultBlockState());
        assertPrivateLimiterCap(helper, ufoCell, ufoCell::serverTick);

        BlockPos quantumPos = helper.absolutePos(new BlockPos(8, 2, 2));
        var quantumCell = new QuantumEnergyCellBlockEntity(ModBlockEntities.QUANTUM_ENERGY_CELL_BE.get(), quantumPos,
                ModBlocks.QUANTUM_ENERGY_CELL.get().defaultBlockState());
        assertPrivateLimiterCap(helper, quantumCell, quantumCell::serverTick);
        helper.succeed();
    }

    private static void assertCap(GameTestHelper helper, BlockEntity controller, BlockPos pos,
                                  Consumer<Void> tick) {
        ServerLevel level = helper.getLevel();
        controller.setLevel(level);
        int cap = UFOConfig.maxExternalAccelerationTicks();
        long before = MachinePerformanceRegistry.INSTANCE
                .snapshot(level.dimension().location().toString(), pos.asLong())
                .map(MachinePerformanceRegistry.Snapshot::tickCount)
                .orElse(0L);

        for (int call = 0; call < cap + 5; call++) {
            tick.accept(null);
        }

        long after = MachinePerformanceRegistry.INSTANCE
                .snapshot(level.dimension().location().toString(), pos.asLong())
                .orElseThrow(() -> new AssertionError("ticker did not record " + controller.getClass().getSimpleName()))
                .tickCount();
        helper.assertTrue(after - before == cap,
                controller.getClass().getSimpleName() + " accepted " + (after - before)
                        + " updates in one game tick; configured cap is " + cap);
        helper.succeed();
    }

    /**
     * Some infrastructure tickers have no processing metric. Inspecting their limiter after real
     * ticker calls proves they consume the common budget before returning for an inactive network
     * or an absent adjacent capability.
     */
    private static void assertPrivateLimiterCap(GameTestHelper helper, BlockEntity blockEntity, Runnable tick) {
        ServerLevel level = helper.getLevel();
        blockEntity.setLevel(level);
        int cap = UFOConfig.maxExternalAccelerationTicks();
        for (int call = 0; call < cap + 5; call++) {
            tick.run();
        }
        try {
            Field field = blockEntity.getClass().getDeclaredField("tickAccelerationLimiter");
            field.setAccessible(true);
            TickAccelerationLimiter limiter = (TickAccelerationLimiter) field.get(blockEntity);
            helper.assertTrue(!limiter.tryAcquire(level.getGameTime(), cap),
                    blockEntity.getClass().getSimpleName() + " accepted more than " + cap + " updates in one game tick");
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Could not inspect ticker limiter for " + blockEntity.getClass().getSimpleName(), exception);
        }
    }
}
