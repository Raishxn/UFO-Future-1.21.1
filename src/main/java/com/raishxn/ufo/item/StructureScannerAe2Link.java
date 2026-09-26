package com.raishxn.ufo.item;

import appeng.api.config.Actionable;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import com.raishxn.ufo.api.multiblock.StructureTerminalOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/** AE2 material source link used by the Structure Scanner/terminal and auto-build sessions. */
public final class StructureScannerAe2Link {
    private static final String ACCESS_POINT = "UfoStructureAccessPoint";

    private StructureScannerAe2Link() {
    }

    public static boolean isAccessPoint(@Nullable BlockEntity blockEntity) {
        return blockEntity instanceof IWirelessAccessPoint;
    }

    public static void link(ItemStack scanner, Level level, BlockPos pos) {
        GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
        GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, globalPos).result().ifPresent(encoded ->
                CustomData.update(DataComponents.CUSTOM_DATA, scanner, tag -> tag.put(ACCESS_POINT, encoded)));
    }

    public static @Nullable GlobalPos linkedPosition(ItemStack scanner) {
        GlobalPos terminalBound = StructureTerminalSettings.getBoundPos(scanner);
        if (terminalBound != null) {
            return terminalBound;
        }
        CompoundTag tag = scanner.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains(ACCESS_POINT)) return null;
        return GlobalPos.CODEC.parse(NbtOps.INSTANCE, tag.get(ACCESS_POINT)).result().orElse(null);
    }

    public static boolean isLinked(ItemStack scanner) {
        return linkedPosition(scanner) != null;
    }

    public static long available(ItemStack scanner, ServerPlayer player, Item item) {
        return access(scanner, player, item, Long.MAX_VALUE, Actionable.SIMULATE);
    }

    public static boolean extractOne(ItemStack scanner, ServerPlayer player, Item item) {
        return access(scanner, player, item, 1L, Actionable.MODULATE) == 1L;
    }

    public static boolean insertOne(ItemStack scanner, ServerPlayer player, Item item) {
        MEStorage storage = storage(scanner, player);
        if (storage == null) return false;
        AEItemKey key = AEItemKey.of(new ItemStack(item));
        return key != null && storage.insert(key, 1L, Actionable.MODULATE, IActionSource.ofPlayer(player)) == 1L;
    }

    private static long access(ItemStack scanner, ServerPlayer player, Item item, long amount, Actionable mode) {
        MEStorage storage = storage(scanner, player);
        if (storage == null) return 0L;
        AEItemKey key = AEItemKey.of(new ItemStack(item));
        if (key == null) return 0L;
        return storage.extract(key, amount, mode, IActionSource.ofPlayer(player));
    }

    private static @Nullable MEStorage storage(ItemStack scanner, ServerPlayer player) {
        GlobalPos linked = linkedPosition(scanner);
        Level level = player.level();
        if (linked == null || !linked.dimension().equals(level.dimension()) || !level.isLoaded(linked.pos())) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(linked.pos());
        if (blockEntity instanceof IWirelessAccessPoint accessPoint) {
            if (!accessPoint.isActive() || accessPoint.getGrid() == null) return null;
            double range = accessPoint.getRange();
            if (linked.pos().distSqr(player.blockPosition()) > range * range) return null;
            return accessPoint.getGrid().getStorageService().getInventory();
        }
        return StructureTerminalOps.findMeStorage(level, linked);
    }
}
