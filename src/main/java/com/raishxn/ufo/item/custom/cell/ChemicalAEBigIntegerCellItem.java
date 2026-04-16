package com.raishxn.ufo.item.custom.cell;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.items.storage.StorageTier;
import com.raishxn.ufo.compat.mekanism.UfoMekanismStorageCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ChemicalAEBigIntegerCellItem extends AnimatedAEBigIntegerCellItem {
    public ChemicalAEBigIntegerCellItem(Item.Properties props, double idleDrain, StorageTier tier, String baseNameKey, String tierNameKey, ChatFormatting[] baseNameColors, ChatFormatting... tierColors) {
        super(props, idleDrain, chemicalKeyType(), tier, baseNameKey, tierNameKey, baseNameColors, tierColors);
    }

    private static AEKeyType chemicalKeyType() {
        return UfoMekanismStorageCompat.getChemicalKeyType();
    }

    @Override
    public AEKeyType getKeyType() {
        return chemicalKeyType();
    }

    @Override
    public boolean isBlackListed(ItemStack stack, AEKey requestedAddition) {
        return UfoMekanismStorageCompat.isChemicalBlacklisted(stack, requestedAddition);
    }
}
