package com.raishxn.ufo.gametest;

import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.MassiveOutputHatchBE;
import com.raishxn.ufo.block.entity.QuantumPatternHatchBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@Mod("ufo_tests")
@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class AeCableFaceGameTests {
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void patternBufferAcceptsCableOnFrontAndEverySide(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, MultiblockBlocks.QUANTUM_PATTERN_BUFFER.get());
        var buffer = (QuantumPatternHatchBE) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        assertEveryFace(helper, buffer.getGridConnectableSides(null), "Quantum Pattern Buffer");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void massiveFluidHatchAcceptsCableOnEverySide(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get());
        var hatch = (MassiveOutputHatchBE) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        assertEveryFace(helper, hatch.getGridConnectableSides(null), "ME Massive Fluid Hatch");
        helper.succeed();
    }

    private static void assertEveryFace(GameTestHelper helper, java.util.Set<Direction> sides, String name) {
        for (Direction direction : Direction.values()) {
            helper.assertTrue(sides.contains(direction), name + " rejects cable face " + direction);
        }
    }
}
