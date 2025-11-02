package com.raishxn.ufo.item.custom.cell;

import appeng.api.stacks.AEKeyType;
import appeng.items.storage.StorageTier;
import com.raishxn.ufo.util.ColorHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AnimatedAEBigIntegerCellItem extends AEBigIntegerCellItem {

    private final String baseNameKey;
    private final String tierNameKey;
    private final ChatFormatting[] baseNameColors; // <-- ADICIONADO
    private final ChatFormatting[] tierColors;

    public AnimatedAEBigIntegerCellItem(Item.Properties props, double idleDrain, AEKeyType keyType, StorageTier tier, String baseNameKey, String tierNameKey, ChatFormatting[] baseNameColors, ChatFormatting... tierColors) {
        super(props, idleDrain, keyType, tier);
        this.baseNameKey = baseNameKey;
        this.tierNameKey = tierNameKey;
        this.baseNameColors = baseNameColors; // <-- ADICIONADO
        this.tierColors = tierColors;
    }

    @Override
    public Component getName(ItemStack stack) {
        Component baseName = ColorHelper.getAnimatedColoredText(Component.translatable(this.baseNameKey).getString(), this.baseNameColors); // <-- CORRIGIDO
        Component tierName = ColorHelper.getAnimatedColoredText(Component.translatable(this.tierNameKey).getString(), this.tierColors);

        return baseName.copy().append(" - ").append(tierName);
    }
}