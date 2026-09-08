package com.raishxn.ufo.block.entity;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.MEStorage;
import appeng.blockentity.misc.InterfaceBlockEntity;
import appeng.helpers.InterfaceLogic;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import appeng.parts.automation.StackWorldBehaviors;
import com.raishxn.ufo.init.ModMenus;
import com.raishxn.ufo.wireless.QuantumWirelessHost;
import com.raishxn.ufo.wireless.QuantumWirelessLinks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

/** First Quantum Interface transport slice: 36 stock slots and bounded, fair remote I/O. */
public class QuantumInterfaceBlockEntity extends InterfaceBlockEntity implements QuantumWirelessHost {
    private final QuantumWirelessLinks links = new QuantumWirelessLinks();
    private boolean autoExport;
    private boolean autoImport;
    private boolean fast;
    private GenericStack pendingReturn;
    private int localSide;
    private int stockCursor;
    private long unlimitedSlots;

    public long unlimitedSlots() { return unlimitedSlots; }
    public void toggleUnlimited(int slot) {
        if (slot < 0 || slot >= 36 || getInterfaceLogic().getConfig().getStack(slot) == null) return;
        unlimitedSlots ^= 1L << slot;
        setChanged();
    }

    public QuantumInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected InterfaceLogic createLogic() {
        var logic = new InterfaceLogic(getMainNode(), this, getBlockState().getBlock().asItem(), 36);
        for (var type : appeng.api.stacks.AEKeyTypes.getAll()) {
            long capacity = 1024L * type.getAmountPerByte();
            logic.getConfig().setCapacity(type, capacity);
            logic.getStorage().setCapacity(type, capacity);
        }
        return logic;
    }

    @Override public QuantumWirelessLinks wirelessLinks() { return links; }
    public boolean autoExport() { return autoExport; }
    public boolean autoImport() { return autoImport; }
    public boolean fast() { return fast; }
    public void toggleExport() { autoExport = !autoExport; setChanged(); }
    public void toggleImport() { autoImport = !autoImport; setChanged(); }
    public void toggleFast() { fast = !fast; setChanged(); }

    public void serverTick() {
        if (level == null || level.isClientSide() || !getMainNode().isActive()) return;
        if (level.getGameTime() % (fast ? 1 : 5) != 0) return;
        var node = getMainNode().getNode();
        if (node == null) return;
        var grid = node.getGrid();
        var network = grid.getStorageService().getInventory();
        var source = IActionSource.ofMachine(this);
        if (pendingReturn != null) {
            long inserted = network.insert(pendingReturn.what(), pendingReturn.amount(), Actionable.MODULATE, source);
            pendingReturn = inserted >= pendingReturn.amount() ? null
                    : new GenericStack(pendingReturn.what(), pendingReturn.amount() - inserted);
            if (inserted > 0) setChanged();
            if (pendingReturn != null) return;
        }
        if (!autoExport && !autoImport) return;
        int count = links.enabled() ? links.size() : 6;
        for (int i = 0; i < Math.min(count, 4); i++) {
            BlockPos pos;
            Direction face;
            if (links.enabled()) {
                var target = links.next();
                if (target == null || links.resolve(this, target) == null) continue;
                pos = target.pos(); face = target.face();
            } else {
                var direction = Direction.values()[localSide++ % 6];
                localSide %= 6;
                pos = worldPosition.relative(direction); face = direction.getOpposite();
                if (!level.hasChunkAt(pos)) continue;
            }
            // Charge a bounded operation cost, separate from energy used by the destination.
            if (grid.getEnergyService().extractAEPower(1, Actionable.MODULATE, PowerMultiplier.CONFIG) < 1) return;
            for (var entry : StackWorldBehaviors.createExternalStorageStrategies((net.minecraft.server.level.ServerLevel) level, pos, face).entrySet()) {
                var remote = entry.getValue().createWrapper(false, this::setChanged);
                if (remote == null) continue;
                if (autoExport) exportTo(remote, network, source, entry.getKey());
                if (pendingReturn != null) break;
                if (autoImport) importFrom(remote, network, source, entry.getKey());
                if (pendingReturn != null) break;
            }
            if (pendingReturn != null) return;
        }
    }

    private void exportTo(MEStorage remote, MEStorage network, IActionSource source, appeng.api.stacks.AEKeyType type) {
        var storage = getInterfaceLogic().getStorage();
        for (int checked = 0; checked < storage.size(); checked++) {
            int slot = stockCursor++ % storage.size();
            stockCursor %= storage.size();
            var config = getInterfaceLogic().getConfig().getStack(slot);
            boolean unlimited = (unlimitedSlots & (1L << slot)) != 0 && config != null;
            var stack = unlimited ? config : storage.getStack(slot);
            if (stack == null || stack.amount() <= 0 || stack.what().getType() != type) continue;
            long amount = unlimited ? Math.max(1L, type.getAmountPerUnit()) * 64L : stack.amount();
            var result = com.raishxn.ufo.wireless.QuantumTransfer.move(amount,
                    n -> remote.insert(stack.what(), n, Actionable.SIMULATE, source),
                    n -> {
                        long buffered = storage.extract(slot, stack.what(), n, Actionable.MODULATE);
                        return unlimited && buffered < n ? buffered + network.extract(stack.what(), n - buffered,
                                Actionable.MODULATE, source) : buffered;
                    },
                    n -> remote.insert(stack.what(), n, Actionable.MODULATE, source));
            if (result.extracted() == 0) continue;
            if (result.retained() > 0) {
                pendingReturn = new GenericStack(stack.what(), result.retained());
            }
            setChanged();
            return;
        }
    }

    private boolean configured(AEKey key) {
        var config = getInterfaceLogic().getConfig();
        for (int i = 0; i < config.size(); i++) if (key.equals(config.getKey(i))) return true;
        return false;
    }

    private void importFrom(MEStorage remote, MEStorage network, IActionSource source, appeng.api.stacks.AEKeyType type) {
        var available = new KeyCounter();
        remote.getAvailableStacks(available);
        for (var entry : available) {
            AEKey key = entry.getKey();
            if (key.getType() != type || configured(key) || entry.getLongValue() <= 0) continue;
            long wanted = Math.min(entry.getLongValue(), Math.max(1L, key.getType().getAmountPerUnit()) * 64L);
            var result = com.raishxn.ufo.wireless.QuantumTransfer.move(wanted,
                    n -> network.insert(key, n, Actionable.SIMULATE, source),
                    n -> remote.extract(key, n, Actionable.MODULATE, source),
                    n -> network.insert(key, n, Actionable.MODULATE, source));
            if (result.retained() > 0) pendingReturn = new GenericStack(key, result.retained());
            if (result.extracted() > 0) { setChanged(); return; }
        }
    }

    @Override public ItemStack getMainMenuIcon() { return new ItemStack(getBlockState().getBlock()); }
    @Override public void openMenu(Player player, MenuHostLocator locator) { MenuOpener.open(ModMenus.QUANTUM_INTERFACE_MENU.get(), player, locator); }
    @Override public void returnToMainMenu(Player player, ISubMenu menu) { MenuOpener.returnTo(ModMenus.QUANTUM_INTERFACE_MENU.get(), player, menu.getLocator()); }

    @Override public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        links.save(tag);
        tag.putLong("quantumUnlimitedSlots", unlimitedSlots);
        tag.putBoolean("autoExport", autoExport); tag.putBoolean("autoImport", autoImport); tag.putBoolean("fastIo", fast);
        if (pendingReturn != null) tag.put("quantumPendingReturn", GenericStack.writeTag(registries, pendingReturn));
    }

    @Override public void loadTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTag(tag, registries);
        links.load(tag);
        unlimitedSlots = tag.getLong("quantumUnlimitedSlots") & ((1L << 36) - 1);
        autoExport = tag.getBoolean("autoExport"); autoImport = tag.getBoolean("autoImport"); fast = tag.getBoolean("fastIo");
        pendingReturn = tag.contains("quantumPendingReturn") ? GenericStack.readTag(registries, tag.getCompound("quantumPendingReturn")) : null;
    }

    @Override public void addAdditionalDrops(net.minecraft.world.level.Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        if (pendingReturn != null) drops.add(GenericStack.wrapInItemStack(pendingReturn));
    }

    @Override public void clearContent() { super.clearContent(); pendingReturn = null; }
}
