package com.raishxn.ufo.item;

import appeng.api.config.Actionable;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
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

/** AE2 Wireless Access Point link used by the Structure Scanner's material source. */
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
        GlobalPos linked = linkedPosition(scanner);
        if (linked == null || !linked.dimension().equals(player.level().dimension())) return false;
        if (!player.level().isLoaded(linked.pos())) return false;
        if (!(player.level().getBlockEntity(linked.pos()) instanceof IWirelessAccessPoint accessPoint)
                || !accessPoint.isActive() || accessPoint.getGrid() == null) return false;
        AEItemKey key = AEItemKey.of(new ItemStack(item));
        return key != null && accessPoint.getGrid().getStorageService().getInventory().insert(
                key, 1L, Actionable.MODULATE, IActionSource.ofPlayer(player)) == 1L;
    }

    private static long access(ItemStack scanner, ServerPlayer player, Item item, long amount, Actionable mode) {
        GlobalPos linked = linkedPosition(scanner);
        if (linked == null || !linked.dimension().equals(player.level().dimension())) return 0L;
        if (!player.level().isLoaded(linked.pos())) return 0L;
        if (!(player.level().getBlockEntity(linked.pos()) instanceof IWirelessAccessPoint accessPoint)
                || !accessPoint.isActive() || accessPoint.getGrid() == null) return 0L;
        double range = accessPoint.getRange();
        if (linked.pos().distSqr(player.blockPosition()) > range * range) return 0L;
        AEItemKey key = AEItemKey.of(new ItemStack(item));
        if (key == null) return 0L;
        return accessPoint.getGrid().getStorageService().getInventory().extract(
                key, amount, mode, IActionSource.ofPlayer(player));
    }
}
