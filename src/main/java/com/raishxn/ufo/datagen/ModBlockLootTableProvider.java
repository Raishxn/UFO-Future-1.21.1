package com.raishxn.ufo.datagen;

import com.raishxn.ufo.block.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModBlockLootTableProvider extends BlockLootSubProvider {

    protected ModBlockLootTableProvider(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        // --- Blocos normais estáticos ---
        this.dropSelf(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK.get());
        this.dropSelf(ModBlocks.NEUTRON_STAR_FRAGMENT_BLOCK.get());
        this.dropSelf(ModBlocks.PULSAR_FRAGMENT_BLOCK.get());
        this.dropSelf(ModBlocks.GRAVITON_PLATED_CASING.get());
        this.dropSelf(ModBlocks.QUANTUM_LATTICE_FRAME.get());
        this.dropSelf(ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER_BLOCK.get());

        ModBlocks.CO_PROCESSOR_BLOCKS.values().forEach(blockRegistry -> {
            this.dropSelf(blockRegistry.get());
        });
        ModBlocks.CRAFTING_STORAGE_BLOCKS.values().forEach(blockRegistry -> {
            this.dropSelf(blockRegistry.get());
        });
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Stream.concat(
                ModBlocks.BLOCKS.getEntries().stream().map(Holder::value),
                Stream.concat(
                        ModBlocks.CO_PROCESSOR_BLOCKS.values().stream().map(Holder::value),
                        ModBlocks.CRAFTING_STORAGE_BLOCKS.values().stream().map(Holder::value)
                )
        ).collect(Collectors.toSet());
    }
}