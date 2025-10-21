package com.raishxn.ufo.core;

import appeng.block.crafting.ICraftingUnitType;
import com.raishxn.ufo.block.ModBlocks;
import net.minecraft.world.item.Item;

/**
 * Define os novos tiers de Crafting Storage de alta capacidade.
 */
public enum MegaCraftingStorageTier implements ICraftingUnitType {
    // --- VALORES CORRIGIDOS PARA BASE BINÁRIA (1024) ---
    // Mantendo a estrutura: (registryId, displayName, bytes)
    // Agora, o valor de 'bytes' é calculado usando potências de 1024.

    // 1 GiB (Gibibyte) = 1024 * 1024 * 1024 bytes. Será exibido como "1G" ou "1024M".
    STORAGE_1B("1b", "1B", 1024L * 1024L * 1024L),

    // 50 GiB
    STORAGE_50B("50b", "50B", 50L * 1024L * 1024L * 1024L),

    // 1 TiB (Tebibyte) = 1024 GiB
    STORAGE_1T("1t", "1T", 1024L * 1024L * 1024L * 1024L),

    // 250 TiB
    STORAGE_250T("250t", "250T", 250L * 1024L * 1024L * 1024L * 1024L),

    // 1 PiB (Pebibyte) = 1024 TiB
    STORAGE_1QD("1qd", "1QD", 1024L * 1024L * 1024L * 1024L * 1024L);

    private final String registryId;
    private final String displayName;
    private final long bytes; // O nome da variável foi mantido

    MegaCraftingStorageTier(String registryId, String displayName, long bytes) {
        this.registryId = registryId;
        this.displayName = displayName;
        this.bytes = bytes;
    }

    public String getRegistryId() {
        return registryId;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Este getter não é usado pela interface, mas é bom manter para consistência.
    public long getBytes() {
        return bytes;
    }

    // --- MÉTODOS DA INTERFACE ICraftingUnitType ---

    @Override
    public long getStorageBytes() {
        // Agora, este método simplesmente retorna o valor de bytes já calculado corretamente.
        return this.bytes;
    }

    @Override
    public int getAcceleratorThreads() {
        return 0;
    }

    @Override
    public Item getItemFromType() {
        return ModBlocks.CRAFTING_STORAGE_BLOCKS.get(this).get().asItem();
    }
}