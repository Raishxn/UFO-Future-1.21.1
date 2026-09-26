package com.raishxn.ufo.gametest;

import com.raishxn.ufo.api.multiblock.MultiblockAutoBuildService;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.QuantumPatternFabricationMatrixControllerBlock;
import com.raishxn.ufo.item.ModItems;
import com.raishxn.ufo.item.StructureScannerSettings;
import com.raishxn.ufo.item.StructureTerminalSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Server-safety contract for the Structure Terminal and its guarded auto-build flow. */
@Mod("ufo_tests")
@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class StructureTerminalSafetyGameTests {

    private static final BlockPos CONTROLLER = new BlockPos(1, 1, 1);
    private static final BlockPos PROBE = new BlockPos(0, 1, 0);

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void terminalModesDefaultToSafeValues(GameTestHelper helper) {
        ItemStack stack = new ItemStack(ModItems.STRUCTURE_SCANNER.get());
        helper.assertFalse(StructureTerminalSettings.getReplaceMode(stack),
                "Replace mode must default to off");
        helper.assertFalse(StructureTerminalSettings.getDismantleMode(stack),
                "Dismantle mode must default to off");
        helper.assertFalse(StructureTerminalSettings.getAeMode(stack),
                "AE mode must default to off");
        StructureTerminalSettings.setReplaceMode(stack, true);
        helper.assertTrue(StructureTerminalSettings.getReplaceMode(stack), "Replace mode must persist");
        StructureTerminalSettings.setReplaceMode(stack, false);
        helper.assertFalse(StructureTerminalSettings.getReplaceMode(stack), "Replace mode must be cleared");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void spectatorCannotStartAutoBuild(GameTestHelper helper) {
        placeController(helper);
        FakePlayer player = FakePlayerFactory.getMinecraft(helper.getLevel());
        player.setGameMode(GameType.SPECTATOR);
        BlockEntity be = helper.getLevel().getBlockEntity(helper.absolutePos(CONTROLLER));
        helper.assertTrue(be != null, "Controller block entity missing");

        startBuild(player, be);
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(helper.getBlockState(PROBE).isAir(),
                    "A spectator must not place blocks through auto-build");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void autoBuildNeverEditsBlocksInstantly(GameTestHelper helper) {
        placeController(helper);
        FakePlayer player = FakePlayerFactory.getMinecraft(helper.getLevel());
        player.setGameMode(GameType.SURVIVAL);
        BlockEntity be = helper.getLevel().getBlockEntity(helper.absolutePos(CONTROLLER));
        helper.assertTrue(be != null, "Controller block entity missing");

        startBuild(player, be);
        helper.assertTrue(helper.getBlockState(PROBE).isAir(),
                "Auto-build must not edit blocks in the same tick it starts");
        helper.succeed();
    }

    private static void startBuild(FakePlayer player, BlockEntity controller) {
        MultiblockAutoBuildService.start(player, controller,
                new StructureScannerSettings(StructureScannerSettings.Mode.BUILD, true, 1, false),
                new ItemStack(ModItems.STRUCTURE_SCANNER.get()));
    }

    private static void placeController(GameTestHelper helper) {
        var state = MultiblockBlocks.QUANTUM_PATTERN_FABRICATION_MATRIX_CONTROLLER.get()
                .defaultBlockState().setValue(QuantumPatternFabricationMatrixControllerBlock.FACING, Direction.NORTH);
        helper.setBlock(CONTROLLER, state);
    }
}
