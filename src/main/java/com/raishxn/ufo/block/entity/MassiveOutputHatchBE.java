package com.raishxn.ufo.block.entity;

import com.raishxn.ufo.api.ae.IMassiveInjector;
import com.raishxn.ufo.api.multiblock.IMultiblockPart;
import com.raishxn.ufo.api.multiblock.MultiblockCasingStyle;
import com.raishxn.ufocore.api.port.EnergyInputPort;
import com.raishxn.ufocore.api.port.CoolantTankState;
import com.raishxn.ufocore.api.port.FluidInputPort;
import com.raishxn.ufocore.api.port.ItemPort;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.compat.mekanism.MekanismChemicalStorage;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.orientation.BlockOrientation;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

/**
 * Block Entity for the ME Massive Output Hatch.
 * <p>
 * This is a hybrid entity: it extends AE2's {@link AENetworkedBlockEntity}
 * to gain a real grid connection (cables, channels, power), while also
 * implementing our internal API interfaces:
 * <ul>
 *   <li>{@link IMassiveInjector} — bulk item/fluid injection into the ME network</li>
 *   <li>{@link IMultiblockPart} — multiblock structure participation</li>
 * </ul>
 * <p>
 * <b>How injection works:</b>
 * <ol>
 *   <li>The Stellar Nexus Controller finishes a simulation cycle</li>
 *   <li>It locates all Output Hatches among its parts</li>
 *   <li>Calls {@link #injectIntoNetwork(AEKey, long, Level)} on each hatch</li>
 *   <li>The hatch uses {@code grid.getStorageService().getInventory().insert(...)} to push items directly into ME storage</li>
 * </ol>
 */
public class MassiveOutputHatchBE extends AENetworkedBlockEntity
        implements IMassiveInjector, IMultiblockPart, IGridTickable, MekanismChemicalStorage,
        EnergyInputPort, FluidInputPort<AEFluidKey>, ItemPort<AEItemKey> {
    private static final long CHEMICAL_CAPACITY = 16_000_000L;
    public static final int COOLANT_CAPACITY = 16_000_000;

    /** Controller position for multiblock link (null if standalone). */
    @Nullable
    private BlockPos controllerPos = null;

    /** Statistics: total items injected since last reset. */
    private long totalInjected = 0;

    /** Statistics: last injection amount (for GUI/tooltip display). */
    private long lastInjectionAmount = 0;
    @Nullable
    private net.minecraft.resources.ResourceLocation storedChemicalId = null;
    private long storedChemicalAmount = 0L;
    private final CoolantTankState<Fluid> coolantTank = new CoolantTankState<>(
            COOLANT_CAPACITY, MassiveOutputHatchBE::isSupportedCoolant);
    private final IFluidHandler externalCoolantHandler = new ExternalCoolantFillHandler();

    public MassiveOutputHatchBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        // The block model's facing is the physical AE2 connector. Keeping the
        // other five faces closed prevents neighboring multiblock hatches from
        // silently joining the same grid through the structure shell.
        this.getMainNode()
                .setExposedOnSides(exposedSides(state))
                .setFlags()                          // No special flags
                .setIdlePowerUsage(0)                // No passive drain
                .addService(IGridTickable.class, this); // Register for tick callbacks
    }

    // ═══════════════════════════════════════════════════════════
    //  IMassiveInjector — Bulk AE2 Network Injection
    // ═══════════════════════════════════════════════════════════

    @Override
    public long injectIntoNetwork(AEKey what, long amount, Level level) {
        if (!supportsItemOutput() || !isNetworkReady() || what == null || amount <= 0) {
            return 0;
        }

        // Access the ME grid's unified storage
        var gridNode = this.getMainNode().getNode();
        if (gridNode == null || gridNode.getGrid() == null) {
            return 0;
        }

        var grid = gridNode.getGrid();
        var storageService = grid.getStorageService();
        var inventory = storageService.getInventory();

        // Use IActionSource.ofMachine to identify ourselves as the source
        var source = IActionSource.ofMachine(this);

        // Inject directly — AE2's insert() natively handles long quantities
        long inserted = inventory.insert(what, amount, Actionable.MODULATE, source);

        // Track statistics
        if (inserted > 0) {
            this.totalInjected += inserted;
            this.lastInjectionAmount = inserted;
            this.setChanged();
        }

        return inserted;
    }

    // ═══════════════════════════════════════════════════════════
    //  ItemPort — explicit massive item input/output roles
    // ═══════════════════════════════════════════════════════════

    public boolean supportsItemInput() {
        return this.getBlockState().is(MultiblockBlocks.ME_MASSIVE_INPUT_HATCH.get());
    }

    public boolean supportsItemOutput() {
        return this.getBlockState().is(MultiblockBlocks.ME_MASSIVE_OUTPUT_HATCH.get());
    }

    @Override
    public long extractItem(AEItemKey item, long maxAmount, boolean simulate) {
        if (item == null || maxAmount <= 0L || !supportsItemInput() || !isNetworkReady()) {
            return 0L;
        }

        IGridNode node = this.getMainNode().getNode();
        if (node == null || node.getGrid() == null) {
            return 0L;
        }

        long extracted = node.getGrid().getStorageService().getInventory().extract(
                item,
                maxAmount,
                simulate ? Actionable.SIMULATE : Actionable.MODULATE,
                IActionSource.ofMachine(this));
        return Math.max(0L, Math.min(maxAmount, extracted));
    }

    @Override
    public long insertItem(AEItemKey item, long maxAmount, boolean simulate) {
        if (item == null || maxAmount <= 0L || (!supportsItemInput() && !supportsItemOutput()) || !isNetworkReady()) {
            return 0L;
        }

        IGridNode node = this.getMainNode().getNode();
        if (node == null || node.getGrid() == null) {
            return 0L;
        }

        long inserted = node.getGrid().getStorageService().getInventory().insert(
                item,
                maxAmount,
                simulate ? Actionable.SIMULATE : Actionable.MODULATE,
                IActionSource.ofMachine(this));
        return Math.max(0L, Math.min(maxAmount, inserted));
    }

    @Override
    public boolean isNetworkReady() {
        var node = this.getMainNode().getNode();
        return node != null && node.isActive() && node.isPowered();
    }

    // ═══════════════════════════════════════════════════════════
    //  EnergyInputPort — explicit AE Energy Input Hatch role
    // ═══════════════════════════════════════════════════════════

    public boolean supportsEnergyInput() {
        return this.getBlockState().is(MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get());
    }

    @Override
    public long extract(long maxAmount, boolean simulate) {
        if (maxAmount <= 0L || !supportsEnergyInput() || !isNetworkReady()) {
            return 0L;
        }

        IGridNode node = this.getMainNode().getNode();
        if (node == null || node.getGrid() == null) {
            return 0L;
        }

        IEnergyService energy = node.getGrid().getEnergyService();
        double extracted = energy.extractAEPower(
                maxAmount,
                simulate ? Actionable.SIMULATE : Actionable.MODULATE,
                PowerMultiplier.CONFIG);
        if (!Double.isFinite(extracted) || extracted <= 0.0D) {
            return 0L;
        }
        return Math.min(maxAmount, (long) extracted);
    }

    // ═══════════════════════════════════════════════════════════
    //  FluidInputPort — explicit ME Massive Fluid Hatch role
    // ═══════════════════════════════════════════════════════════

    public boolean supportsFluidInput() {
        return this.getBlockState().is(MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get());
    }

    public static boolean isSupportedCoolant(Fluid fluid) {
        return canonicalCoolant(fluid) != Fluids.EMPTY;
    }

    private static Fluid canonicalCoolant(Fluid fluid) {
        if (fluid == com.raishxn.ufo.fluid.ModFluids.SOURCE_GELID_CRYOTHEUM.get()
                || fluid == com.raishxn.ufo.fluid.ModFluids.FLOWING_GELID_CRYOTHEUM.get()) {
            return com.raishxn.ufo.fluid.ModFluids.SOURCE_GELID_CRYOTHEUM.get();
        }
        if (fluid == com.raishxn.ufo.fluid.ModFluids.SOURCE_STABLE_COOLANT.get()
                || fluid == com.raishxn.ufo.fluid.ModFluids.FLOWING_STABLE_COOLANT.get()) {
            return com.raishxn.ufo.fluid.ModFluids.SOURCE_STABLE_COOLANT.get();
        }
        if (fluid == com.raishxn.ufo.fluid.ModFluids.SOURCE_TEMPORAL_FLUID.get()
                || fluid == com.raishxn.ufo.fluid.ModFluids.FLOWING_TEMPORAL_FLUID.get()) {
            return com.raishxn.ufo.fluid.ModFluids.SOURCE_TEMPORAL_FLUID.get();
        }
        return Fluids.EMPTY;
    }

    @Nullable
    public IFluidHandler getExternalCoolantHandler(@Nullable Direction side) {
        return supportsFluidInput() ? this.externalCoolantHandler : null;
    }

    public FluidStack getStoredCoolant() {
        Fluid coolant = this.coolantTank.coolant();
        return coolant == null ? FluidStack.EMPTY : new FluidStack(coolant, this.coolantTank.amount());
    }

    public int getCoolantSpace() {
        return Math.max(0, COOLANT_CAPACITY - this.coolantTank.amount());
    }

    /**
     * Moves coolant from this hatch's ME grid into its visible local tank.
     * Simulation precedes mutation so the grid is never charged for fluid that
     * the single-fluid tank cannot accept.
     */
    public long bufferCoolantFromGrid(AEFluidKey fluid, long maxAmount) {
        if (fluid == null || maxAmount <= 0L || !supportsFluidInput() || !isNetworkReady()) {
            return 0L;
        }

        Fluid canonical = canonicalCoolant(fluid.getFluid());
        if (canonical == Fluids.EMPTY) {
            return 0L;
        }
        int requested = (int) Math.min(Math.min(maxAmount, Integer.MAX_VALUE), getCoolantSpace());
        int tankAcceptance = this.coolantTank.fill(canonical, requested, true);
        if (tankAcceptance <= 0) {
            return 0L;
        }

        IGridNode node = this.getMainNode().getNode();
        if (node == null || node.getGrid() == null) {
            return 0L;
        }
        var inventory = node.getGrid().getStorageService().getInventory();
        var source = IActionSource.ofMachine(this);
        AEFluidKey canonicalKey = AEFluidKey.of(canonical);
        long available = inventory.extract(canonicalKey, tankAcceptance, Actionable.SIMULATE, source);
        long planned = Math.min(tankAcceptance, Math.max(0L, available));
        if (planned <= 0L) {
            return 0L;
        }

        long extracted = inventory.extract(canonicalKey, planned, Actionable.MODULATE, source);
        int filled = this.coolantTank.fill(canonical, (int) Math.min(extracted, Integer.MAX_VALUE), false);
        if (filled > 0) {
            coolantContentsChanged();
        }
        if (filled < extracted) {
            inventory.insert(canonicalKey, extracted - filled, Actionable.MODULATE, source);
        }
        return filled;
    }

    /** Extracts exclusively from the visible local tank, never directly from ME storage. */
    public long extractBufferedCoolant(AEFluidKey fluid, long maxAmount, boolean simulate) {
        if (fluid == null || maxAmount <= 0L || !supportsFluidInput()) {
            return 0L;
        }
        Fluid canonical = canonicalCoolant(fluid.getFluid());
        if (canonical == Fluids.EMPTY) {
            return 0L;
        }
        long extracted = this.coolantTank.extract(canonical, maxAmount, simulate);
        if (!simulate && extracted > 0L) {
            coolantContentsChanged();
        }
        return extracted;
    }

    @Override
    public long extract(AEFluidKey fluid, long maxAmount, boolean simulate) {
        if (fluid == null || maxAmount <= 0L || !supportsFluidInput()) {
            return 0L;
        }

        Fluid canonical = canonicalCoolant(fluid.getFluid());
        if (canonical == Fluids.EMPTY) {
            return 0L;
        }
        long fromTank = extractBufferedCoolant(fluid, maxAmount, simulate);
        long remaining = maxAmount - fromTank;
        if (remaining <= 0L || !isNetworkReady()) {
            return fromTank;
        }

        IGridNode node = this.getMainNode().getNode();
        if (node == null || node.getGrid() == null) {
            return fromTank;
        }

        long extracted = node.getGrid().getStorageService().getInventory().extract(
                AEFluidKey.of(canonical),
                remaining,
                simulate ? Actionable.SIMULATE : Actionable.MODULATE,
                IActionSource.ofMachine(this));
        return fromTank + Math.max(0L, Math.min(remaining, extracted));
    }

    private void coolantContentsChanged() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  IMultiblockPart — Structure Participation
    // ═══════════════════════════════════════════════════════════

    @Override
    public void linkToController(BlockPos controllerPos) {
        this.controllerPos = controllerPos;
        updateCasingStyle(casingStyleFor(controllerPos));
        refreshGridConnection();
        this.setChanged();
    }

    @Override
    public void unlinkFromController() {
        this.controllerPos = null;
        updateCasingStyle(MultiblockCasingStyle.DEFAULT);
        refreshGridConnection();
        this.setChanged();
    }

    @Nullable
    @Override
    public BlockPos getControllerPos() {
        return this.controllerPos;
    }

    /**
     * @return {@code true} if this hatch is currently linked to a multiblock controller.
     */
    public boolean isLinked() {
        return this.controllerPos != null;
    }

    private MultiblockCasingStyle casingStyleFor(BlockPos controllerPos) {
        if (this.level != null && this.level.getBlockEntity(controllerPos) instanceof StellarNexusControllerBE) {
            return MultiblockCasingStyle.ENTROPY;
        }
        return MultiblockCasingStyle.QUANTUM;
    }

    private void updateCasingStyle(MultiblockCasingStyle style) {
        if (this.level == null || this.level.isClientSide()
                || !this.getBlockState().hasProperty(com.raishxn.ufo.block.MassiveOutputHatchBlock.CASING_STYLE)
                || this.getBlockState().getValue(com.raishxn.ufo.block.MassiveOutputHatchBlock.CASING_STYLE) == style) {
            return;
        }
        this.level.setBlock(this.worldPosition,
                this.getBlockState().setValue(com.raishxn.ufo.block.MassiveOutputHatchBlock.CASING_STYLE, style),
                net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
    }

    // ═══════════════════════════════════════════════════════════
    //  IGridTickable — AE2 Tick Callbacks
    // ═══════════════════════════════════════════════════════════

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        // We don't actively do work — injection is called by the controller.
        // Sleep until woken.
        return new TickingRequest(20, 20, true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        // Nothing to do autonomously; the controller drives injection.
        return TickRateModulation.SLEEP;
    }

    // ═══════════════════════════════════════════════════════════
    //  AE2 Configuration
    // ═══════════════════════════════════════════════════════════

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.DENSE_SMART;
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return exposedSides(this.getBlockState());
    }

    static Set<Direction> exposedSides(BlockState state) {
        Direction facing = state.hasProperty(DirectionalBlock.FACING)
                ? state.getValue(DirectionalBlock.FACING)
                : Direction.NORTH;
        return EnumSet.of(facing);
    }

    public void refreshGridConnection() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        this.getMainNode().setExposedOnSides(exposedSides(this.getBlockState()));
        this.onGridConnectableSidesChanged();
    }

    // ═══════════════════════════════════════════════════════════
    //  Statistics API (for future GUI / tooltips)
    // ═══════════════════════════════════════════════════════════

    public long getTotalInjected() {
        return this.totalInjected;
    }

    public long getLastInjectionAmount() {
        return this.lastInjectionAmount;
    }

    public void resetStatistics() {
        this.totalInjected = 0;
        this.lastInjectionAmount = 0;
        this.setChanged();
    }

    @Override
    public boolean supportsChemicalIO() {
        return this.getBlockState().is(MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get());
    }

    @Override
    public long getChemicalCapacity() {
        return CHEMICAL_CAPACITY;
    }

    @Override
    public @Nullable net.minecraft.resources.ResourceLocation getStoredChemicalId() {
        return this.storedChemicalId;
    }

    @Override
    public long getStoredChemicalAmount() {
        return this.storedChemicalAmount;
    }

    @Override
    public void setStoredChemical(@Nullable net.minecraft.resources.ResourceLocation chemicalId, long amount) {
        long clamped = Math.max(0L, Math.min(CHEMICAL_CAPACITY, amount));
        this.storedChemicalId = clamped > 0L ? chemicalId : null;
        this.storedChemicalAmount = clamped;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public long extractChemical(net.minecraft.resources.ResourceLocation chemicalId, long maxAmount, boolean simulate) {
        if (!supportsChemicalIO() || chemicalId == null || maxAmount <= 0L
                || !chemicalId.equals(this.storedChemicalId)) {
            return 0L;
        }

        long extracted = Math.min(maxAmount, this.storedChemicalAmount);
        if (!simulate && extracted > 0L) {
            setStoredChemical(this.storedChemicalId, this.storedChemicalAmount - extracted);
        }
        return extracted;
    }

    @Override
    public long insertChemical(net.minecraft.resources.ResourceLocation chemicalId, long maxAmount, boolean simulate) {
        if (!supportsChemicalIO() || chemicalId == null || maxAmount <= 0L
                || (this.storedChemicalId != null && !chemicalId.equals(this.storedChemicalId))) {
            return 0L;
        }

        long inserted = Math.min(maxAmount, Math.max(0L, CHEMICAL_CAPACITY - this.storedChemicalAmount));
        if (!simulate && inserted > 0L) {
            setStoredChemical(chemicalId, this.storedChemicalAmount + inserted);
        }
        return inserted;
    }

    // ═══════════════════════════════════════════════════════════
    //  NBT Persistence (AE2 pattern: saveAdditional + loadTag)
    // ═══════════════════════════════════════════════════════════

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.controllerPos != null) {
            tag.put("controllerPos", NbtUtils.writeBlockPos(this.controllerPos));
        }
        tag.putLong("totalInjected", this.totalInjected);
        tag.putLong("lastInjection", this.lastInjectionAmount);
        if (this.storedChemicalId != null && this.storedChemicalAmount > 0L) {
            tag.putString("storedChemicalId", this.storedChemicalId.toString());
            tag.putLong("storedChemicalAmount", this.storedChemicalAmount);
        }
        Fluid coolant = this.coolantTank.coolant();
        if (coolant != null && this.coolantTank.amount() > 0) {
            tag.putString("coolantFluid", BuiltInRegistries.FLUID.getKey(coolant).toString());
            tag.putInt("coolantAmount", this.coolantTank.amount());
        }
    }

    @Override
    public void loadTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadTag(tag, registries);
        if (tag.contains("controllerPos")) {
            NbtUtils.readBlockPos(tag.getCompound("controllerPos"), "").ifPresent(pos -> this.controllerPos = pos);
        } else {
            this.controllerPos = null;
        }
        this.totalInjected = tag.getLong("totalInjected");
        this.lastInjectionAmount = tag.getLong("lastInjection");
        this.storedChemicalId = tag.contains("storedChemicalId") ? net.minecraft.resources.ResourceLocation.parse(tag.getString("storedChemicalId")) : null;
        this.storedChemicalAmount = this.storedChemicalId == null ? 0L : tag.getLong("storedChemicalAmount");
        Fluid coolant = Fluids.EMPTY;
        if (tag.contains("coolantFluid")) {
            net.minecraft.resources.ResourceLocation coolantId = net.minecraft.resources.ResourceLocation.tryParse(tag.getString("coolantFluid"));
            if (coolantId != null) {
                coolant = canonicalCoolant(BuiltInRegistries.FLUID.get(coolantId));
            }
        }
        this.coolantTank.restore(coolant == Fluids.EMPTY ? null : coolant, tag.getInt("coolantAmount"));
    }

    private final class ExternalCoolantFillHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return tank == 0 ? getStoredCoolant() : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? COOLANT_CAPACITY : 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return tank == 0 && isSupportedCoolant(stack.getFluid());
        }

        @Override
        public int fill(@NotNull FluidStack resource, FluidAction action) {
            Fluid canonical = canonicalCoolant(resource.getFluid());
            if (canonical == Fluids.EMPTY) {
                return 0;
            }
            int accepted = MassiveOutputHatchBE.this.coolantTank.fill(
                    canonical, resource.getAmount(), action.simulate());
            if (action.execute() && accepted > 0) {
                coolantContentsChanged();
            }
            return accepted;
        }

        @Override
        public @NotNull FluidStack drain(@NotNull FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}
