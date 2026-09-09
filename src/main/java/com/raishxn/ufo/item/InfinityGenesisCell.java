package com.raishxn.ufo.item;

import appeng.api.stacks.GenericStack;
import appeng.api.config.FuzzyMode;
import appeng.api.ids.AEComponents;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.core.AEConfig;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.storage.StorageCellTooltipComponent;
import appeng.util.ConfigInventory;
import com.raishxn.ufo.datagen.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class InfinityGenesisCell extends AEBaseItem implements ICellWorkbenchItem {

    public InfinityGenesisCell() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack stack) {
        return CellConfig.create(stack);
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack stack) {
        return UpgradeInventories.forItem(stack, 2);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack stack) {
        return stack.getOrDefault(AEComponents.STORAGE_CELL_FUZZY_MODE, FuzzyMode.IGNORE_ALL);
    }

    @Override
    public void setFuzzyMode(ItemStack stack, FuzzyMode fuzzyMode) {
        stack.set(AEComponents.STORAGE_CELL_FUZZY_MODE, fuzzyMode);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context,
                                @NotNull List<Component> lines, @NotNull TooltipFlag advancedTooltips) {
        List<GenericStack> keys = stack.getOrDefault(ModDataComponents.INFINITY_GENESIS_CELL_KEYS.get(), List.of());
        lines.add(Component.translatable("item.ufo.infinity_genesis_cell.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE));
        lines.add(Component.translatable("item.ufo.infinity_genesis_cell.types", keys.size()).withStyle(ChatFormatting.GRAY));
        if (!keys.isEmpty()) {
            lines.add(Component.translatable("item.ufo.infinity_genesis_cell.learned").withStyle(ChatFormatting.DARK_AQUA));
            int shown = 0;
            for (var key : keys) {
                lines.add(Component.literal(" - ").append(key.what().getDisplayName()));
                if (++shown >= 8) {
                    if (keys.size() > shown) {
                        lines.add(Component.translatable("item.ufo.infinity_genesis_cell.more", keys.size() - shown)
                                .withStyle(ChatFormatting.DARK_GRAY));
                    }
                    break;
                }
            }
        }
    }

    @NotNull
    @Override
    public Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        List<GenericStack> keys = stack.getOrDefault(ModDataComponents.INFINITY_GENESIS_CELL_KEYS.get(), List.of());
        var content = keys.stream()
                .limit(5)
                .map(entry -> new GenericStack(entry.what(), InfinityCell.getAsIntMax(entry.what())))
                .toList();
        List<ItemStack> upgrades = Collections.emptyList();
        if (AEConfig.instance().isTooltipShowCellUpgrades()) {
            List<ItemStack> installedUpgrades = new ArrayList<>();
            getUpgrades(stack).forEach(installedUpgrades::add);
            upgrades = installedUpgrades;
        }
        return Optional.of(new StorageCellTooltipComponent(upgrades, content, keys.size() > 5, true));
    }
}
