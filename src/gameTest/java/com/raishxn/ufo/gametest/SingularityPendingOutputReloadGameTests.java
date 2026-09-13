package com.raishxn.ufo.gametest;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.definitions.AEBlocks;
import com.mojang.logging.LogUtils;
import com.raishxn.ufo.block.InfinityFabricationSingularityControllerBlock;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.InfinityFabricationSingularityControllerBE;
import com.raishxn.ufo.block.entity.QuantumGridLinkBE;
import com.raishxn.ufo.block.entity.pattern.InfinityFabricationSingularityPatternFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.raishxn.ufo.gametest.EndgameStructureLifecycleGameTests.awaitCondition;
import static com.raishxn.ufo.gametest.EndgameStructureLifecycleGameTests.releaseAndAwaitPhysicalUnload;
import static com.raishxn.ufo.gametest.EndgameStructureLifecycleGameTests.footprintChunks;
import static com.raishxn.ufo.gametest.EndgameStructureLifecycleGameTests.setChunksForced;

/** Real chunk serialization and physical ME reconnection, with persisted output ledgers as input. */
@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class SingularityPendingOutputReloadGameTests {
    private static final AEItemKey OUTPUT = AEItemKey.of(Items.DIAMOND);
    private static final long OUTPUT_AMOUNT = 4_000_000_000L;
    private static final int WAIT_TICKS = 600;

    @GameTest(template = "event_driven_structure", timeoutTicks = 2200)
    public static void fullNetworkPhysicalReloadThenDrainIsExact(GameTestHelper helper) {
        exercise(helper, new BlockPos(2068, 5, 20), 0L, false);
    }

    @GameTest(template = "event_driven_structure", timeoutTicks = 2200)
    public static void partialNetworkPhysicalReloadThenDrainIsExact(GameTestHelper helper) {
        exercise(helper, new BlockPos(2580, 5, 20), 17L, false);
    }

    @GameTest(template = "event_driven_structure", timeoutTicks = 2200)
    public static void fullNetworkPhysicalReloadThenBreakRecoversOnce(GameTestHelper helper) {
        exercise(helper, new BlockPos(3092, 5, 20), 0L, true);
    }

    private static void exercise(GameTestHelper helper, BlockPos relativePos, long initialRoom, boolean breakAfterReload) {
        var level = helper.getLevel();
        var definition = InfinityFabricationSingularityPatternFactory.getDefinition();
        BlockPos controllerPos = helper.absolutePos(relativePos);
        var chunks = new LinkedHashSet<>(footprintChunks(controllerPos, definition, Direction.EAST));
        setChunksForced(level, chunks, true);
        level.setBlockAndUpdate(controllerPos, MultiblockBlocks.INFINITY_FABRICATION_SINGULARITY_CONTROLLER.get()
                .defaultBlockState().setValue(InfinityFabricationSingularityControllerBlock.FACING, Direction.NORTH));
        definition.pattern().assembleAsCreative(level, controllerPos, Direction.EAST, definition.defaultCreativeStates());
        var controller = (InfinityFabricationSingularityControllerBE) level.getBlockEntity(controllerPos);
        controller.scanStructure(level);
        BlockPos linkPos = definition.pattern().getExpectedPositions(controllerPos, Direction.EAST, 'L').getFirst();
        var originalLink = (QuantumGridLinkBE) level.getBlockEntity(linkPos);
        BlockPos powerPos = linkPos.relative(originalLink.getBlockState().getValue(DirectionalBlock.FACING));
        helper.assertTrue(level.getBlockState(powerPos).isAir(), "physical ME power fixture overlaps the shell");
        chunks.add(new ChunkPos(powerPos));
        setChunksForced(level, chunks, true);
        level.setBlockAndUpdate(powerPos, AEBlocks.CREATIVE_ENERGY_CELL.block().defaultBlockState());
        var storage = new OutputStorage(catalyst(), initialRoom);

        var fixture = new Fixture(controller, originalLink, linkPos, powerPos, chunks, storage, initialRoom, breakAfterReload);
        // Adjacent blocks must reconnect themselves after reload; no synthetic GridHelper connection.
        awaitCondition(helper, WAIT_TICKS, () -> networkReady(helper, linkPos, powerPos),
                "physical ME network did not become ready", () -> preparePendingOutputs(helper, fixture));
    }

    private static void preparePendingOutputs(GameTestHelper helper, Fixture fixture) {
        mountStorage(fixture.originalLink, fixture.storage);
        seed(helper, fixture.originalLink, fixture.storage.catalyst);
        fixture.originalLink.getGrid().getTickManager().alertDevice(fixture.originalLink.getMainNode().getNode());
        awaitCondition(helper, WAIT_TICKS, () -> fixture.storage.inserted.get(OUTPUT) == fixture.initialRoom,
                "automatic grid ticking did not insert the initial partial output", () ->
                helper.runAfterDelay(40, () -> unloadWithPendingOutputs(helper, fixture)));
    }

    private static void unloadWithPendingOutputs(GameTestHelper helper, Fixture fixture) {
        assertPending(helper, fixture.originalLink, OUTPUT_AMOUNT - fixture.initialRoom, fixture.storage.catalyst);
        helper.assertTrue(packages(helper, fixture.linkPos).isEmpty(), "blocked outputs were ejected before unload");
        releaseAndAwaitPhysicalUnload(helper, fixture.chunks, fixture.controller.getBlockPos(), fixture.controller, WAIT_TICKS, () ->
                awaitCondition(helper, WAIT_TICKS, fixture.originalLink::isRemoved,
                        "Grid Link did not physically unload", () -> reloadWithPendingOutputs(helper, fixture)));
    }

    private static void reloadWithPendingOutputs(GameTestHelper helper, Fixture fixture) {
        var level = helper.getLevel();
        setChunksForced(level, fixture.chunks, true);
        fixture.chunks.forEach(chunk -> level.getChunk(chunk.x, chunk.z));
        awaitCondition(helper, WAIT_TICKS, () -> networkReady(helper, fixture.linkPos, fixture.powerPos)
                        && fixture.chunks.stream().allMatch(chunk -> level.areEntitiesLoaded(chunk.toLong())),
                "physical ME network did not reconnect after reload", () -> {
            var reloadedLink = (QuantumGridLinkBE) level.getBlockEntity(fixture.linkPos);
            helper.assertTrue(reloadedLink != fixture.originalLink, "reload reused the original Grid Link BE");
            mountStorage(reloadedLink, fixture.storage);
            helper.runAfterDelay(40, () -> verifyRestoredOutputs(helper, fixture, reloadedLink));
        });
    }

    private static void verifyRestoredOutputs(GameTestHelper helper, Fixture fixture, QuantumGridLinkBE link) {
        assertPending(helper, link, OUTPUT_AMOUNT - fixture.initialRoom, fixture.storage.catalyst);
        helper.assertTrue(fixture.storage.inserted.get(OUTPUT) == fixture.initialRoom,
                "reload replayed the already accepted output");
        helper.assertTrue(packages(helper, fixture.linkPos).isEmpty(), "unload/reload ejected pending outputs");
        if (fixture.breakAfterReload) {
            helper.getLevel().destroyBlock(fixture.linkPos, true);
            assertRecovered(helper, fixture.linkPos, OUTPUT, OUTPUT_AMOUNT - fixture.initialRoom);
            assertRecovered(helper, fixture.linkPos, fixture.storage.catalyst, 1L);
            link.unlinkForRemoval();
            assertRecovered(helper, fixture.linkPos, OUTPUT, OUTPUT_AMOUNT - fixture.initialRoom);
            assertRecovered(helper, fixture.linkPos, fixture.storage.catalyst, 1L);
            helper.assertTrue(link.getPendingCraftingRouteCount() == 0,
                    "physical removal retained a recoverable output ledger");
            // Move packages clear of the replacement block, whose collision otherwise pushes them out of the probe.
            Direction outward = link.getBlockState().getValue(DirectionalBlock.FACING);
            BlockPos packagePos = fixture.linkPos.relative(outward, 3);
            helper.assertTrue(helper.getLevel().getBlockState(packagePos).isAir(), "package observation position is obstructed");
            helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(fixture.linkPos).inflate(4.0D))
                    .stream().filter(entity -> GenericStack.unwrapItemStack(entity.getItem()) != null).forEach(entity -> {
                        entity.setNoGravity(true);
                        entity.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                        entity.setPos(packagePos.getX() + 0.5D, packagePos.getY() + 0.5D, packagePos.getZ() + 0.5D);
                    });
            helper.getLevel().setBlockAndUpdate(fixture.linkPos, MultiblockBlocks.QUANTUM_GRID_LINK.get().defaultBlockState());
            awaitCondition(helper, WAIT_TICKS, () -> networkReady(helper, fixture.linkPos, fixture.powerPos),
                    "replacement Grid Link did not reconnect", () -> helper.runAfterDelay(40, () -> {
                var replacement = (QuantumGridLinkBE) helper.getLevel().getBlockEntity(fixture.linkPos);
                helper.assertTrue(replacement.getPendingCraftingRouteCount() == 0,
                        "replacement Grid Link inherited a recovered route");
                helper.assertTrue(fixture.storage.inserted.get(OUTPUT) == fixture.initialRoom
                                && fixture.storage.inserted.get(fixture.storage.catalyst) == 0L,
                        "physical removal or replacement inserted blocked results into the full network");
                assertRecovered(helper, fixture.linkPos, OUTPUT, OUTPUT_AMOUNT - fixture.initialRoom);
                assertRecovered(helper, fixture.linkPos, fixture.storage.catalyst, 1L);
                helper.assertTrue(packages(helper, fixture.linkPos).size() == 2,
                        "replacement link created extra recovery packages");
                finish(helper, fixture);
            }));
        } else {
            fixture.storage.acceptAll = true;
            awaitCondition(helper, WAIT_TICKS, () -> link.getPendingCraftingRouteCount() == 0,
                    "restored route did not drain through automatic grid ticking", () ->
                    helper.runAfterDelay(40, () -> {
                helper.assertTrue(fixture.storage.inserted.get(OUTPUT) == OUTPUT_AMOUNT,
                        "drained output was lost or duplicated");
                helper.assertTrue(fixture.storage.inserted.get(fixture.storage.catalyst) == 1L,
                        "catalyst components or amount changed across reload");
                helper.assertTrue(packages(helper, fixture.linkPos).isEmpty(), "accepted results were also ejected");
                finish(helper, fixture);
            }));
        }
    }

    private static boolean networkReady(GameTestHelper helper, BlockPos linkPos, BlockPos powerPos) {
        var level = helper.getLevel();
        if (!(level.getBlockEntity(linkPos) instanceof QuantumGridLinkBE link) || !link.isNetworkReady()) return false;
        if (!(level.getBlockEntity(powerPos) instanceof AENetworkedBlockEntity power)) return false;
        return power.getMainNode().getNode() != null && link.getGridLinkNode().getGrid() == power.getMainNode().getNode().getGrid();
    }

    private static void mountStorage(QuantumGridLinkBE link, OutputStorage storage) {
        link.getGrid().getStorageService().addGlobalStorageProvider(mounts -> mounts.mount(storage, 0));
    }

    private static void seed(GameTestHelper helper, QuantumGridLinkBE link, AEItemKey catalyst) {
        CompoundTag saved = new CompoundTag();
        link.saveAdditional(saved, helper.getLevel().registryAccess());
        ListTag outputs = new ListTag();
        outputs.add(GenericStack.writeTag(helper.getLevel().registryAccess(), new GenericStack(OUTPUT, OUTPUT_AMOUNT)));
        outputs.add(GenericStack.writeTag(helper.getLevel().registryAccess(), new GenericStack(catalyst, 1L)));
        CompoundTag route = new CompoundTag();
        route.put("outputs", outputs);
        ListTag routes = new ListTag();
        routes.add(route);
        saved.put("pendingCraftingRoutes", routes);
        link.loadTag(saved, helper.getLevel().registryAccess());
        link.setChanged();
    }

    private static void assertPending(GameTestHelper helper, QuantumGridLinkBE link, long outputAmount, AEItemKey catalyst) {
        CompoundTag saved = new CompoundTag();
        link.saveAdditional(saved, helper.getLevel().registryAccess());
        var routes = saved.getList("pendingCraftingRoutes", net.minecraft.nbt.Tag.TAG_COMPOUND);
        helper.assertTrue(routes.size() == 1, "pending route count changed across chunk serialization");
        var outputs = routes.getCompound(0).getList("outputs", net.minecraft.nbt.Tag.TAG_COMPOUND);
        Map<AEKey, Long> actual = new java.util.HashMap<>();
        for (var tag : outputs) {
            var stack = GenericStack.readTag(helper.getLevel().registryAccess(), (CompoundTag) tag);
            helper.assertTrue(stack != null && actual.put(stack.what(), stack.amount()) == null,
                    "invalid or repeated pending output key");
        }
        helper.assertTrue(actual.equals(Map.of(OUTPUT, outputAmount, catalyst, 1L)),
                "pending amounts or catalyst components changed across physical reload: " + actual);
    }

    private static List<GenericStack> packages(GameTestHelper helper, BlockPos linkPos) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(linkPos).inflate(4.0D)).stream()
                .map(entity -> GenericStack.unwrapItemStack(entity.getItem())).filter(Objects::nonNull).toList();
    }

    private static void assertRecovered(GameTestHelper helper, BlockPos linkPos, AEKey key, long expected) {
        long actual = packages(helper, linkPos).stream().filter(stack -> stack.what().equals(key))
                .mapToLong(GenericStack::amount).sum();
        helper.assertTrue(actual == expected, "physical removal recovered an incorrect amount: " + actual);
    }

    private static AEItemKey catalyst() {
        ItemStack stack = new ItemStack(Items.NETHER_STAR);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Persisted reusable catalyst"));
        return AEItemKey.of(stack);
    }

    private static void finish(GameTestHelper helper, Fixture fixture) {
        LogUtils.getLogger().info("Pending output physical reload passed: initialRoom={}, brokenAfterReload={}",
                fixture.initialRoom, fixture.breakAfterReload);
        setChunksForced(helper.getLevel(), fixture.chunks, false);
        helper.succeed();
    }

    private record Fixture(InfinityFabricationSingularityControllerBE controller, QuantumGridLinkBE originalLink,
                           BlockPos linkPos, BlockPos powerPos, Set<ChunkPos> chunks, OutputStorage storage,
                           long initialRoom, boolean breakAfterReload) { }

    private static final class OutputStorage implements MEStorage {
        private final AEItemKey catalyst;
        private long room;
        private boolean acceptAll;
        private final KeyCounter inserted = new KeyCounter();

        private OutputStorage(AEItemKey catalyst, long room) {
            this.catalyst = catalyst;
            this.room = room;
        }

        @Override public Component getDescription() { return Component.literal("Persistent output test storage"); }

        @Override public long insert(AEKey key, long amount, Actionable mode, IActionSource source) {
            long accepted = acceptAll && (key.equals(OUTPUT) || key.equals(catalyst))
                    ? amount : key.equals(OUTPUT) ? Math.min(room, amount) : 0L;
            if (mode == Actionable.MODULATE && accepted > 0L) {
                if (!acceptAll) room -= accepted;
                inserted.add(key, accepted);
            }
            return accepted;
        }
    }
}
