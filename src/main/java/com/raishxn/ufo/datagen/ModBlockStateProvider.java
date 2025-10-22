package com.raishxn.ufo.datagen;

import appeng.block.crafting.AbstractCraftingUnitBlock;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, UfoMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (var tier : MegaCraftingStorageTier.values()) {
            var block = ModBlocks.CRAFTING_STORAGE_BLOCKS.get(tier);
            String registryName = block.getId().getPath(); // Ex: "1b_mega_crafting_storage"

            // 1. Modelo para o estado normal (unformed)
            // Gera o arquivo: assets/ufo/models/block/1b_mega_crafting_storage.json
            // Este modelo usa a textura: assets/ufo/textures/block/1b_mega_crafting_storage.png
            ModelFile unformedModel = models().cubeAll(registryName, modLoc("block/" + registryName));

            // 2. Modelo VAZIO para o estado conectado (formed)
            // Gera o arquivo: assets/ufo/models/block/1b_mega_crafting_storage_formed.json
            // O conteúdo deste modelo é {}, pois ele é renderizado via código.
            ModelFile formedModel = models().getBuilder(registryName + "_formed");

            // 3. Gera o Blockstate que alterna entre os dois modelos
            // Gera o arquivo: assets/ufo/blockstates/1b_mega_crafting_storage.json
            getVariantBuilder(block.get())
                    .partialState().with(AbstractCraftingUnitBlock.FORMED, false)
                    .setModels(new ConfiguredModel(unformedModel))
                    .partialState().with(AbstractCraftingUnitBlock.FORMED, true)
                    .setModels(new ConfiguredModel(formedModel));

            // 4. Gera o modelo do item para o inventário
            // Gera o arquivo: assets/ufo/models/item/1b_mega_crafting_storage.json
            simpleBlockItem(block.get(), unformedModel);
        }
    }
}