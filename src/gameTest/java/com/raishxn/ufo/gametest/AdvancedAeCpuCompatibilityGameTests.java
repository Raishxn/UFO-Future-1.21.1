package com.raishxn.ufo.gametest;

import appeng.api.config.Actionable;
import appeng.api.config.CpuSelectionMode;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.CraftingJobStatus;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.crafting.CraftingPlan;
import appeng.crafting.execution.CraftingSubmitResult;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.service.CraftingService;
import com.google.common.collect.ImmutableSet;
import com.raishxn.ufocore.api.crafting.SharedCraftingCpuPool;
import com.raishxn.ufocore.api.crafting.SharedCraftingCpuPoolProvider;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.jetbrains.annotations.Nullable;

/**
 * RaishxCore's shared CPU pool hooks AE2's crafting service with cancellable {@code @Inject}s on
 * {@code getCpus}, {@code insertIntoCpus} and {@code getRequestedAmount}. A cancellable callback
 * aborts the method as soon as it sets a value, so it silently skipped AdvancedAE's own callbacks
 * for the Quantum Computer, regardless of the planner configuration. This test keeps one shared
 * pool and one real Quantum Computer on the same grid and proves both stay visible, both report
 * their waiting item and both receive crafted items.
 */
@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class AdvancedAeCpuCompatibilityGameTests {
    private static final BlockPos CORE = new BlockPos(2, 1, 2);
    private static final BlockPos POWER = new BlockPos(2, 2, 2);
    private static final BlockPos POOL_NODE = new BlockPos(1, 1, 2);
    private static final BlockPos REQUESTER_NODE = new BlockPos(3, 1, 2);

    private AdvancedAeCpuCompatibilityGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void sharedPoolDoesNotHideTheQuantumComputer(GameTestHelper helper) {
        if (!ModList.get().isLoaded("advanced_ae")) {
            helper.succeed();
            return;
        }
        Block quantumCore = BuiltInRegistries.BLOCK.get(ResourceLocation.parse("advanced_ae:quantum_core"));
        helper.assertTrue(quantumCore != null && quantumCore != Blocks.AIR,
                "AdvancedAE is loaded but advanced_ae:quantum_core is not registered");
        helper.setBlock(CORE, quantumCore);
        helper.setBlock(POWER, AEBlocks.CREATIVE_ENERGY_CELL.block());

        TestPool pool = new TestPool();
        IManagedGridNode poolNode = GridHelper.createManagedNode(new TestPoolOwner(pool), (owner, node) -> {})
                .setInWorldNode(false);
        poolNode.create(helper.getLevel(), helper.absolutePos(POOL_NODE));
        TestRequester requester = new TestRequester();
        IManagedGridNode requesterNode = GridHelper.createManagedNode(requester, (owner, node) -> {})
                .setInWorldNode(false);
        requesterNode.create(helper.getLevel(), helper.absolutePos(REQUESTER_NODE));
        requester.attach(requesterNode);

        // Wait for the Quantum Computer to form and join a grid before bridging the two extra nodes.
        await(helper, 60, () -> coreFormed(helper) && coreNode(helper) != null && coreNode(helper).getGrid() != null,
                () -> "the Quantum Computer did not form: " + coreDiagnostics(helper), () -> {
                    IGridNode coreNode = coreNode(helper);
                    var poolConnection = GridHelper.createConnection(poolNode.getNode(), coreNode);
                    var requesterConnection = GridHelper.createConnection(requesterNode.getNode(), coreNode);
                    // The pool registers on the merged grid; the Quantum Computer is registered by
                    // its own add-on when the controller change is processed.
                    await(helper, 40, () -> hasPoolCpu(coreNode(helper), pool),
                            () -> "the shared pool never registered: " + coreDiagnostics(helper),
                            () -> helper.runAfterDelay(5, () -> {
                                assertSharedBridgeComposes(helper, coreNode(helper).getGrid(), pool, requester);
                                poolConnection.destroy();
                                requesterConnection.destroy();
                                poolNode.destroy();
                                requesterNode.destroy();
                                helper.succeed();
                            }));
                });
    }

    private static void assertSharedBridgeComposes(GameTestHelper helper, IGrid grid, TestPool pool,
                                                   TestRequester requester) {
        CraftingService service = (CraftingService) grid.getCraftingService();

        ICraftingCPU quantum = null;
        for (ICraftingCPU cpu : service.getCpus()) {
            if (cpu != pool && !(cpu instanceof CraftingCPUCluster)) {
                quantum = cpu;
                break;
            }
        }
        helper.assertTrue(quantum != null, "the Quantum Computer vanished from getCpus(): "
                + cpuDiagnostics(grid) + " core=" + coreDiagnostics(helper));
        helper.assertTrue(service.hasCpu(pool), "hasCpu() lost the shared pool");

        // A plan that is immediately waiting for its output makes insertion and requested amounts
        // observable without any pattern provider: both CPUs must report one waiting item.
        AEItemKey output = AEItemKey.of(Items.GLASS);
        ICraftingPlan plan = new CraftingPlan(new GenericStack(output, 1), 8, false, false,
                new KeyCounter(), waitingFor(output, 1), new KeyCounter(), Map.of());
        helper.assertTrue(service.submitJob(plan, requester, quantum, false, IActionSource.empty()).successful(),
                "the Quantum Computer rejected a runnable plan");
        helper.assertTrue(service.submitJob(plan, requester, pool, false, IActionSource.empty()).successful(),
                "the shared pool rejected a runnable plan");
        helper.assertTrue(service.getRequestedAmount(output) == 2,
                "getRequestedAmount() lost one of the two CPUs, got " + service.getRequestedAmount(output));
        helper.assertTrue(service.insertIntoCpus(output, 2, Actionable.MODULATE) == 2,
                "insertIntoCpus() lost one of the two CPUs");
        helper.assertTrue(requester.inserted == 1, "the Quantum Computer job never received its output");
    }

    private static KeyCounter waitingFor(AEItemKey key, long amount) {
        KeyCounter counter = new KeyCounter();
        counter.add(key, amount);
        return counter;
    }

    private static @Nullable IGridNode coreNode(GameTestHelper helper) {
        if (!(helper.getLevel().getBlockEntity(helper.absolutePos(CORE)) instanceof AENetworkedBlockEntity core)) {
            return null;
        }
        return core.getMainNode().getNode();
    }

    /** Reads AdvancedAE's {@code formed} state generically, without compiling against the add-on. */
    private static boolean coreFormed(GameTestHelper helper) {
        IGridNode node = coreNode(helper);
        if (node == null || node.getGrid() == null) return false;
        var state = helper.getLevel().getBlockState(helper.absolutePos(CORE));
        for (var property : state.getProperties()) {
            if (property.getName().equals("formed")) {
                return Boolean.TRUE.equals(state.getValue(property));
            }
        }
        return false;
    }

    private static String coreDiagnostics(GameTestHelper helper) {
        var state = helper.getLevel().getBlockState(helper.absolutePos(CORE));
        return state.getBlock() + state.getValues().toString();
    }

    private static String cpuDiagnostics(IGrid grid) {
        var names = new java.util.ArrayList<String>();
        for (ICraftingCPU cpu : grid.getCraftingService().getCpus()) names.add(cpu.getClass().getSimpleName());
        return names.toString();
    }

    private static boolean hasPoolCpu(@Nullable IGridNode coreNode, TestPool pool) {
        return coreNode != null && coreNode.getGrid() != null
                && coreNode.getGrid().getCraftingService().getCpus().contains(pool);
    }

    private static void await(GameTestHelper helper, int remainingTicks, BooleanSupplier ready,
                              java.util.function.Supplier<String> failure, Runnable action) {
        if (ready.getAsBoolean()) {
            action.run();
            return;
        }
        if (remainingTicks <= 0) {
            helper.fail("the fixture never reached the expected state: " + failure.get());
            return;
        }
        helper.runAfterDelay(1, () -> await(helper, remainingTicks - 1, ready, failure, action));
    }

    /** Minimal shared pool: exists on the grid, accepts one plan and waits for one item. */
    private static final class TestPool implements SharedCraftingCpuPool {
        private long waiting;

        @Override public boolean isActive() { return true; }
        @Override public List<ICraftingCPU> getActiveCpus() { return List.of(this); }
        @Override public long tickCraftingLogic(IEnergyService energyService, ICraftingService craftingService) { return 0L; }
        @Override public void addWaitingKeys(Set<AEKey> waitingKeys) { if (waiting > 0) waitingKeys.add(AEItemKey.of(Items.GLASS)); }
        @Override public long insert(AEKey what, long amount, Actionable mode) {
            if (waiting <= 0 || !what.equals(AEItemKey.of(Items.GLASS))) return 0L;
            long inserted = Math.min(amount, waiting);
            if (mode == Actionable.MODULATE) waiting -= inserted;
            return inserted;
        }
        @Override public long getRequestedAmount(AEKey what) {
            return what.equals(AEItemKey.of(Items.GLASS)) ? waiting : 0L;
        }
        @Override public ICraftingSubmitResult submitJob(IGrid grid, ICraftingPlan plan, IActionSource source,
                                                         @Nullable ICraftingRequester requester) {
            waiting = plan.emittedItems().get(AEItemKey.of(Items.GLASS));
            return CraftingSubmitResult.successful(null);
        }
        @Override public boolean isBusy() { return waiting > 0; }
        @Override public @Nullable CraftingJobStatus getJobStatus() { return null; }
        @Override public void cancelJob() { waiting = 0; }
        @Override public long getAvailableStorage() { return 1_000_000L; }
        @Override public int getCoProcessors() { return 1; }
        @Override public Component getName() { return Component.literal("Compatibility test pool"); }
        @Override public CpuSelectionMode getSelectionMode() { return CpuSelectionMode.ANY; }
    }

    private static final class TestPoolOwner implements SharedCraftingCpuPoolProvider {
        private final SharedCraftingCpuPool pool;

        private TestPoolOwner(SharedCraftingCpuPool pool) {
            this.pool = pool;
        }

        @Override public SharedCraftingCpuPool getSharedCraftingCpuPool() { return pool; }
    }

    /** Requester that records what the CPUs deliver and keeps its crafting link alive on the grid. */
    private static final class TestRequester implements ICraftingRequester {
        private IManagedGridNode node;
        private long inserted;

        private void attach(IManagedGridNode node) {
            this.node = node;
        }

        @Override public ImmutableSet<ICraftingLink> getRequestedJobs() { return ImmutableSet.of(); }
        @Override public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
            inserted += amount;
            return amount;
        }
        @Override public void jobStateChange(ICraftingLink link) { }
        @Override public IGridNode getActionableNode() { return node.getNode(); }
    }
}
