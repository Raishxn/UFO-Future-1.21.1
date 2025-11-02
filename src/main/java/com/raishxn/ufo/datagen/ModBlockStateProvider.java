package com.raishxn.ufo.datagen;

import appeng.block.crafting.AbstractCraftingUnitBlock;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.block.MultiblockBlocks; // Importa a nova classe
import com.raishxn.ufo.core.MegaCoProcessorTier;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
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
        // --- Blocos Simples Originais ---
        simpleBlockWithItem(ModBlocks.QUANTUM_LATTICE_FRAME);
        simpleBlockWithItem(ModBlocks.GRAVITON_PLATED_CASING);
        simpleBlockWithItem(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK);
        simpleBlockWithItem(ModBlocks.PULSAR_FRAGMENT_BLOCK);
        simpleBlockWithItem(ModBlocks.NEUTRON_STAR_FRAGMENT_BLOCK);

        // --- Registro dos Novos Blocos Multiblock ---
        // Blocos que são um cubo simples
        multiblockCube(MultiblockBlocks.ENTROPY_ASSEMBLER_CORE_CASING);
        multiblockCube(MultiblockBlocks.ENTROPY_SINGULARITY_CASING);
        multiblockCube(MultiblockBlocks.ENTROPY_COMPUTER_CONDENSATION_MATRIX);

        // Blocos que são componentes direcionais com overlay
        multiblockComponentBlock(MultiblockBlocks.ENTROPY_CONTAINMENT_CHAMBER_COMPONENTS);
        multiblockComponentBlock(MultiblockBlocks.ENTROPY_COOLANT_MATRIX_COMPONENTS);
        multiblockComponentBlock(MultiblockBlocks.ENTROPY_CATALYST_BANK_COMPONENTS);


        // Bloco controlador com estados
        controllerBlock(MultiblockBlocks.ENTROPY_SINGULARITY_ARRAY_CONTROLLER);


        // --- Blocos com Variantes (Crafting Units) ---
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

    private void simpleBlockWithItem(DeferredBlock<Block> blockHolder) {
        String registryName = blockHolder.getId().getPath();
        ModelFile model = models().cubeAll(registryName, modLoc("block/" + registryName));
        simpleBlock(blockHolder.get(), model);
        simpleBlockItem(blockHolder.get(), model);
    }

    // --- MÉTODOS AUXILIARES PARA OS NOVOS BLOCOS ---

    /**
     * Registra um bloco de multiblock que é um cubo simples.
     */
    private void multiblockCube(DeferredBlock<Block> block) {
        String name = block.getId().getPath();
        ResourceLocation texture = modLoc("block/multiblock/" + name);
        simpleBlock(block.get(), models().cubeAll(name, texture));
        simpleBlockItem(block.get(), models().getExistingFile(modLoc("block/" + name)));
    }

    /**
     * Registra um bloco de componente direcional com uma textura base e um overlay frontal.
     */
    private void multiblockComponentBlock(DeferredBlock<Block> block) {
        String name = block.getId().getPath();
        ResourceLocation baseTexture = modLoc("block/multiblock/entropy_assembler_core_casing_base");
        ResourceLocation overlayTexture = modLoc("block/multiblock/" + name);

        ModelFile modelFile = models().withExistingParent(name, "block/block")
                .renderType("cutout") // <<-- CORREÇÃO: renderType aplicado ao ModelBuilder
                .texture("particle", baseTexture)
                .texture("base", baseTexture)
                .texture("overlay", overlayTexture)
                .element()
                .from(0, 0, 0).to(16, 16, 16)
                .allFaces((direction, faceBuilder) -> faceBuilder.texture("#base").cullface(direction))
                .end()
                .element()
                .from(0, 0, 0).to(16, 16, 16)
                .face(Direction.NORTH).texture("#overlay").cullface(Direction.NORTH).end()
                .end();

        getVariantBuilder(block.get()).forAllStates(state -> {
            Direction dir = state.getValue(DirectionalBlock.FACING);
            return ConfiguredModel.builder()
                    .modelFile(modelFile)
                    .rotationX(dir == Direction.DOWN ? 90 : dir == Direction.UP ? -90 : 0)
                    .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360)
                    .build();
        });

        simpleBlockItem(block.get(), modelFile);
    }

    private void controllerBlock(DeferredBlock<Block> block) {
        ResourceLocation baseTexture = modLoc("block/multiblock/entropy_assembler_core_casing_base");
        ResourceLocation overlayInactive = modLoc("block/general1/overlay_front");
        ResourceLocation overlayActive = modLoc("block/general1/overlay_front_active");

        var inactiveModel = models().withExistingParent(block.getId().getPath(), "block/block")
                .renderType("cutout") // <<-- CORREÇÃO: renderType aplicado ao ModelBuilder
                .texture("particle", baseTexture)
                .texture("base", baseTexture)
                .texture("overlay", overlayInactive)
                .element().from(0, 0, 0).to(16, 16, 16).allFaces((dir, face) -> face.texture("#base").cullface(dir)).end()
                .element().from(0, 0, 0).to(16, 16, 16).face(Direction.NORTH).texture("#overlay").cullface(Direction.NORTH).end();

        var activeModel = models().withExistingParent(block.getId().getPath() + "_active", "block/block")
                .renderType("cutout") // <<-- CORREÇÃO: renderType aplicado ao ModelBuilder
                .texture("particle", baseTexture)
                .texture("base", baseTexture)
                .texture("overlay", overlayActive)
                .element().from(0, 0, 0).to(16, 16, 16).allFaces((dir, face) -> face.texture("#base").cullface(dir)).end()
                .element().from(0, 0, 0).to(16, 16, 16).face(Direction.NORTH).texture("#overlay").cullface(Direction.NORTH).end();

        getVariantBuilder(block.get()).forAllStates(state -> {
            Direction dir = state.getValue(DirectionalBlock.FACING);
            boolean isActive = state.getValue(MultiblockBlocks.ControllerBlock.ACTIVE);
            return ConfiguredModel.builder()
                    .modelFile((isActive ? activeModel : inactiveModel).end())
                    .rotationX(dir == Direction.DOWN ? 90 : dir == Direction.UP ? -90 : 0)
                    .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360)
                    .build();
        });

        simpleBlockItem(block.get(), inactiveModel.end());
    }
}