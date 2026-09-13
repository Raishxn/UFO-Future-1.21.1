package com.raishxn.ufo.gametest;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.StorageCells;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.AbstractParallelMultiblockControllerBE;
import com.raishxn.ufo.block.entity.StellarNexusControllerBE;
import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.item.custom.cell.AEBigIntegerCellData;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

/** Literal 2.1 schemas with provenance; this is not a full legacy-world upgrade. */
@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class LegacySaveMigrationGameTests {
    @GameTest(template = "empty")
    public static void legacyParallelControllersKeepBuffersAndAddSafeDefaults(GameTestHelper helper) {
        var states = Map.of(
                "qmf_controller", MultiblockBlocks.QUANTUM_MATTER_FABRICATOR_CONTROLLER.get().defaultBlockState(),
                "quantum_slicer_controller", MultiblockBlocks.QUANTUM_SLICER_CONTROLLER.get().defaultBlockState(),
                "quantum_processor_assembler_controller", MultiblockBlocks.QUANTUM_PROCESSOR_ASSEMBLER_CONTROLLER.get().defaultBlockState(),
                "quantum_cryoforge_controller", MultiblockBlocks.QUANTUM_CRYOFORGE_CONTROLLER.get().defaultBlockState());
        states.forEach((id, state) -> {
            CompoundTag legacy = fixture("parallel");
            legacy.putString("id", "ufo:" + id);
            BlockEntity migrated = load(helper, state, legacy);
            helper.assertTrue(migrated instanceof AbstractParallelMultiblockControllerBE, "legacy type did not resolve: " + id);
            var saved = migrated.saveWithFullMetadata(helper.getLevel().registryAccess());
            assertFields(helper, legacy, saved, "storedEnergy", "maxStoredEnergy", "temperature", "safeMode");
            var oldProcess = legacy.getList("processStates", 10).getCompound(0);
            var process = saved.getList("processStates", 10).getCompound(0);
            assertFields(helper, oldProcess, process, "recipeId", "energyBuffer", "itemBuffers", "fluidBuffers", "chemicalBuffers", "progress", "patternPushed");
            helper.assertTrue(!process.getBoolean("outputsPrepared") && !process.getBoolean("paused"), "legacy process got fabricated work/pause state");
            helper.assertTrue(process.getList("bufferedInputs", 10).isEmpty() && process.getList("pendingOutputs", 10).isEmpty(), "legacy buffers got fabricated transactional entries");
            helper.assertTrue(process.getInt("outputPolicyVersion") == 0 && process.getLong("batchNumerator") == 1 && process.getLong("batchDenominator") == 1,
                    "legacy process lost unversioned/full-batch defaults");
            var roundTrip = load(helper, state, saved).saveWithFullMetadata(helper.getLevel().registryAccess());
            assertFields(helper, saved, roundTrip, "storedEnergy", "processStates");
        });
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyStellarKeepsEnergyAndOlderFuelAliases(GameTestHelper helper) {
        BlockState state = MultiblockBlocks.STELLAR_NEXUS_CONTROLLER.get().defaultBlockState();
        CompoundTag legacy = fixture("stellar");
        var migrated = load(helper, state, legacy);
        helper.assertTrue(migrated instanceof StellarNexusControllerBE, "legacy Stellar type did not resolve");
        var saved = migrated.saveWithFullMetadata(helper.getLevel().registryAccess());
        assertFields(helper, legacy, saved, "energyBuffer", "energyCapacity", "activeRecipeId", "progress", "heatLevel", "safeMode", "simulationLocked");
        helper.assertTrue(!saved.getBoolean("outputsPrepared") && saved.getList("pendingOutputs", 10).isEmpty(), "legacy Stellar fabricated pending outputs");
        // Supported fuel aliases are an explicit reader contract; modern fields take precedence.
        legacy.putLong("fuelBuffer", legacy.getLong("energyBuffer"));
        legacy.putLong("fuelCapacity", legacy.getLong("energyCapacity"));
        legacy.remove("energyBuffer"); legacy.remove("energyCapacity");
        var fuel = load(helper, state, legacy).saveWithFullMetadata(helper.getLevel().registryAccess());
        assertFields(helper, saved, fuel, "energyBuffer", "energyCapacity");
        legacy.putLong("energyBuffer", 101L); legacy.putLong("energyCapacity", 202L);
        var modern = load(helper, state, legacy).saveWithFullMetadata(helper.getLevel().registryAccess());
        helper.assertTrue(modern.getLong("energyBuffer") == 101L && modern.getLong("energyCapacity") == 202L, "fuel alias overrode modern energy fields");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyBeacoUuidLoadsSavedDataFromDiskWithoutNarrowing(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        CompoundTag legacy = fixture("cell");
        ItemStack cell = ItemStack.parseOptional(registries, legacy.getCompound("stack"));
        helper.assertTrue(!cell.isEmpty(), "legacy beaco stack identifier did not resolve");
        var uuid = cell.get(ModDataComponents.CELL_UUID.get());
        helper.assertTrue(uuid != null, "legacy cell_uuid component was lost");
        var server = helper.getLevel().getServer();
        var file = server.getWorldPath(LevelResource.ROOT).resolve("data/ae_universal_cell_data/" + uuid + ".dat");
        try {
            Files.createDirectories(file.getParent());
            var root = new CompoundTag(); root.put("data", legacy.getCompound("data")); root.putInt("DataVersion", 3955);
            NbtIo.writeCompressed(root, file);
        } catch (IOException failure) { throw new IllegalStateException("could not prepare isolated legacy SavedData", failure); }
        var data = AEBigIntegerCellData.getCellDataByUUID(uuid);
        var diamond = AEItemKey.of(Items.DIAMOND);
        BigInteger original = BigInteger.ONE.shiftLeft(80);
        helper.assertTrue(data != null && original.equals(data.getOriginalStorage().get(diamond)), "legacy disk quantity or UUID lookup changed");
        var inventory = StorageCells.getCellInventory(cell, null);
        helper.assertTrue(inventory != null, "legacy cell did not expose an AE2 inventory");
        var available = new KeyCounter(); inventory.getAvailableStacks(available);
        helper.assertTrue(available.get(diamond) == Long.MAX_VALUE, "AE2 long view did not saturate the BigInteger quantity");
        helper.assertTrue(inventory.extract(diamond, 37L, Actionable.MODULATE, IActionSource.empty()) == 37L, "legacy extraction failed");
        BigInteger remaining = original.subtract(BigInteger.valueOf(37L));
        helper.assertTrue(remaining.equals(data.getOriginalStorage().get(diamond)), "legacy extraction narrowed or lost the balance");
        var rewritten = data.save(new CompoundTag(), registries);
        helper.assertTrue(remaining.equals(AEBigIntegerCellData.load(rewritten, registries).getOriginalStorage().get(diamond)), "3.0 rewrite changed legacy signed bytes");
        var stackRoundTrip = ItemStack.parseOptional(registries, (CompoundTag) cell.save(registries));
        helper.assertTrue(uuid.equals(stackRoundTrip.get(ModDataComponents.CELL_UUID.get())), "rewrite replaced legacy cell UUID");
        helper.succeed();
    }

    private static BlockEntity load(GameTestHelper helper, BlockState state, CompoundTag tag) {
        var entity = BlockEntity.loadStatic(BlockPos.ZERO, state, tag, helper.getLevel().registryAccess());
        helper.assertTrue(entity != null, "legacy block entity identifier did not resolve: " + tag.getString("id"));
        return entity;
    }

    private static void assertFields(GameTestHelper helper, CompoundTag expected, CompoundTag actual, String... names) {
        for (String name : names) helper.assertTrue(java.util.Objects.equals(expected.get(name), actual.get(name)), "migration changed field " + name);
    }

    private static CompoundTag fixture(String name) {
        String resource = "/migration/v2.1-fix7/" + name + ".snbt";
        try (var input = LegacySaveMigrationGameTests.class.getResourceAsStream(resource)) {
            if (input == null) throw new IllegalStateException("missing legacy fixture " + resource);
            return TagParser.parseTag(new String(input.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException | com.mojang.brigadier.exceptions.CommandSyntaxException failure) {
            throw new IllegalStateException("invalid legacy fixture " + resource, failure);
        }
    }
}
