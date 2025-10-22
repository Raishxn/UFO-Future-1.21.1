package com.raishxn.ufo.datagen;

import appeng.block.crafting.AbstractCraftingUnitBlock;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.core.MegaCoProcessorTier;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, UfoMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // --- 1. CHAMADAS AO MÉTODO HELPER CORRIGIDAS ---
        // Agora passamos o DeferredBlock inteiro, sem o .get()
        simpleBlockWithItem(ModBlocks.QUANTUM_LATTICE_FRAME);
        simpleBlockWithItem(ModBlocks.GRAVITON_PLATED_CASING);
        simpleBlockWithItem(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK);


        // --- 2. REGISTRO DOS BLOCOS COMPLEXOS (COM VARIANTES) ---
        // Esta parte já estava correta.
        for (var tier : MegaCraftingStorageTier.values()) {
            var block = ModBlocks.CRAFTING_STORAGE_BLOCKS.get(tier);
            String registryName = block.getId().getPath();
            ModelFile unformedModel = models().cubeAll(registryName, modLoc("block/" + registryName));
            ModelFile formedModel = models().getBuilder(registryName + "_formed");

            getVariantBuilder(block.get())
                    .partialState().with(AbstractCraftingUnitBlock.FORMED, false)
                    .setModels(new ConfiguredModel(unformedModel))
                    .partialState().with(AbstractCraftingUnitBlock.FORMED, true)
                    .setModels(new ConfiguredModel(formedModel));

            simpleBlockItem(block.get(), unformedModel);
        }

        for (var tier : MegaCoProcessorTier.values()) {
            var block = ModBlocks.CO_PROCESSOR_BLOCKS.get(tier);
            String registryName = block.getId().getPath();
            ModelFile unformedModel = models().cubeAll(registryName, modLoc("block/" + registryName));
            ModelFile formedModel = models().getBuilder(registryName + "_formed");

            getVariantBuilder(block.get())
                    .partialState().with(AbstractCraftingUnitBlock.FORMED, false)
                    .setModels(new ConfiguredModel(unformedModel))
                    .partialState().with(AbstractCraftingUnitBlock.FORMED, true)
                    .setModels(new ConfiguredModel(formedModel));

            simpleBlockItem(block.get(), unformedModel);
        }
    }

    // --- 3. MÉTODO HELPER CORRIGIDO ---
    // Agora ele aceita um DeferredBlock<Block> para que possamos usar .getId()
    private void simpleBlockWithItem(DeferredBlock<Block> blockHolder) {
        // Pegamos o nome do registro da forma correta e segura
        String registryName = blockHolder.getId().getPath();

        // Geramos o modelo do bloco
        ModelFile model = models().cubeAll(registryName, modLoc("block/" + registryName));

        // Geramos o estado do bloco (precisamos do Block aqui, então usamos .get())
        simpleBlock(blockHolder.get(), model);

        // Geramos o modelo do item (também precisa do Block, então usamos .get())
        simpleBlockItem(blockHolder.get(), model);
    }
}