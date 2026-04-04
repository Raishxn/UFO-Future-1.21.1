package com.raishxn.ufo.datagen;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, UfoMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // --- INÍCIO DA CORREÇÃO ---
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.QUANTUM_LATTICE_FRAME.get())         // <<-- ADICIONE .get() AQUI
                .add(ModBlocks.GRAVITON_PLATED_CASING.get())       // <<-- E AQUI
                .add(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK.get());  // <<-- E AQUI


        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocks.QUANTUM_LATTICE_FRAME.get())         // <<-- ADICIONE .get() AQUI
                .add(ModBlocks.GRAVITON_PLATED_CASING.get())       // <<-- E AQUI
                .add(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK.get());  // <<-- E AQUI
        // --- FIM DA CORREÇÃO ---


        tag(BlockTags.NEEDS_DIAMOND_TOOL);


        tag(ModTags.Blocks.NEEDS_UFO_TOOL)
                .addTag(BlockTags.NEEDS_IRON_TOOL)
                .addTag(BlockTags.NEEDS_DIAMOND_TOOL);

        tag(ModTags.Blocks.INCORRECT_FOR_UFO_TOOL)
                .addTag(BlockTags.INCORRECT_FOR_IRON_TOOL)
                .addTag(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)
                .remove(ModTags.Blocks.NEEDS_UFO_TOOL);
    }
}