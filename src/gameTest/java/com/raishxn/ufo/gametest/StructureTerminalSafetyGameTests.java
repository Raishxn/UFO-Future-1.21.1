package com.raishxn.ufo.gametest;

import com.raishxn.ufo.api.multiblock.MultiblockAutoBuildService;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinitions;
import com.raishxn.ufo.api.multiblock.StructureTerminalOps;
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
import net.minecraft.world.level.block.Blocks;
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
        helper.assertFalse(StructureTerminalSettings.getBuildMode(stack),
                "Build mode must default to off so the terminal scans by default");
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

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void demolitionPlansOnlyMatchingStructureBlocks(GameTestHelper helper) {
        placeController(helper);
        BlockEntity controller = helper.getLevel().getBlockEntity(helper.absolutePos(CONTROLLER));
        helper.assertTrue(controller != null, "Controller block entity missing");
        var definition = MultiblockControllerDefinitions.getDefinition(controller).orElseThrow();
        var pattern = definition.pattern();
        BlockPos field = pattern.getExpectedPositions(controller.getBlockPos(), Direction.NORTH, 'F').getFirst();
        BlockPos foreign = pattern.getExpectedPositions(controller.getBlockPos(), Direction.NORTH, 'C').getFirst();
        helper.getLevel().setBlock(field, definition.defaultCreativeStates().get('F'), 3);
        helper.getLevel().setBlock(foreign, Blocks.STONE.defaultBlockState(), 3);
        helper.assertTrue(pattern.matchesSlot('F', helper.getLevel().getBlockState(field), helper.getLevel(), field),
                "Field fixture must match its pattern slot");
        helper.assertTrue(pattern.getSymbols().stream()
                        .filter(definition.defaultCreativeStates()::containsKey)
                        .flatMap(symbol -> pattern.getExpectedPositions(controller.getBlockPos(), Direction.NORTH, symbol).stream())
                        .allMatch(helper.getLevel()::isLoaded),
                "All structural positions must be loaded");

        var scan = StructureTerminalOps.scanDemolition(definition, helper.getLevel(),
                controller.getBlockPos(), Direction.NORTH);
        helper.assertTrue(scan.available(), "Dismantle scan requires loaded structure chunks");
        helper.assertTrue(scan.targets().stream().anyMatch(target -> target.position().equals(field)),
                "Dismantle must select a matching structural block");
        helper.assertFalse(scan.targets().stream().anyMatch(target -> target.position().equals(foreign)),
                "Dismantle must leave unrelated blocks untouched");
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
