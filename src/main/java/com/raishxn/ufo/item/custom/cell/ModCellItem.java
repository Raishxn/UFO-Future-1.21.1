package com.raishxn.ufo.item.custom.cell;

import appeng.api.stacks.AEKeyType;
import appeng.items.storage.BasicStorageCell;
import appeng.items.storage.StorageTier;
import com.raishxn.ufo.item.ModCellItems;
import net.minecraft.world.item.Item;

/**
 * Classe base para as novas células de armazenamento customizadas.
 */
public class ModCellItem extends BasicStorageCell {

    public ModCellItem(StorageTier tier, int maxTypes, AEKeyType keyType) {
        super(
                new Item.Properties().stacksTo(1),
                tier.idleDrain(),
                calculateKibiBytes(tier), // Corrigido para retornar int
                calculateBytesPerType(tier),
                maxTypes,
                keyType
        );
    }

    private static int calculateKibiBytes(StorageTier tier) {
        // Para a tier infinita, usamos o maior valor de int possível.
        if (tier == ModCellItems.TIER_INFINITY) {
            return Integer.MAX_VALUE;
        }
        // Para as outras tiers, o valor cabe em um int, então fazemos a conversão.
        return tier.bytes() / 1024;
    }

    private static int calculateBytesPerType(StorageTier tier) {
        if (tier == ModCellItems.TIER_INFINITY) {
            // Um valor alto, similar a outras células de grande capacidade
            return 262144;
        }
        // O cálculo padrão do AE2.
        return tier.bytes() / 128;
    }
}