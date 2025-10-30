// Local: src/main/java/com/raishxn/ufo/item/custom/cell/InfinityModCellItem.java
package com.raishxn.ufo.item.custom.cell;

import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import appeng.items.storage.StorageCellTooltipComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InfinityModCellItem extends Item implements IInfinityCell {

    private final double idleDrain;

    public InfinityModCellItem(Properties pProperties, double idleDrain) {
        super(pProperties);
        this.idleDrain = idleDrain;
    }

    @Override
    public double getIdleDrain() {
        return this.idleDrain;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> lines, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);

        BigInteger usedBytes = IInfinityCell.getUsedBytes(stack);
        int usedTypes = IInfinityCell.getUsedTypes(stack);

        lines.add(Component.translatable("item.ufo.bytes_used", usedBytes.toString()).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("item.ufo.types_used", usedTypes, "∞").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        List<GenericStack> content = new ArrayList<>(IInfinityCell.getTooltipShowStacks(stack));
        boolean hasMore = content.size() > 5;
        if (hasMore) {
            content = content.subList(0, 5);
        }
        return Optional.of(new StorageCellTooltipComponent(List.of(), content, hasMore, true));
    }

    public static final ICellHandler HANDLER = new Handler();

    private static class Handler implements ICellHandler {
        @Override
        public boolean isCell(ItemStack is) {
            return is.getItem() instanceof InfinityModCellItem;
        }

        @Nullable
        @Override
        public StorageCell getCellInventory(ItemStack is, @Nullable ISaveProvider host) {
            // A verificação mais segura é se o host é um BlockEntity.
            // IChestOrDrive é implementado por BlockEntities como o ME Drive.
            if (host instanceof BlockEntity be) {
                Level level = be.getLevel(); // BlockEntity SEMPRE tem getLevel()

                if (level != null && !level.isClientSide()) {
                    var customData = is.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
                    var tag = customData.copyTag();
                    if (!tag.hasUUID("uuid")) {
                        tag.putUUID("uuid", UUID.randomUUID());
                        is.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
                    }
                    UUID uuid = tag.getUUID("uuid");

                    InfinityCellManager manager = InfinityCellManager.get(level);
                    var inventoryMap = manager.getOrCreateInventory(uuid);
                    return new InfinityModCellInventory(is, host, inventoryMap, manager);
                }
            }
            return null;
        }
    }
}