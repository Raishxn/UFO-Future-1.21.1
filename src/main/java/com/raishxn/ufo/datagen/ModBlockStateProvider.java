package com.raishxn.ufo.datagen;

import appeng.block.crafting.AbstractCraftingUnitBlock;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.api.multiblock.MultiblockCasingStyle;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.block.MultiblockBlocks; // Importa a nova classe
import com.raishxn.ufo.core.MegaCoProcessorTier;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
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
        casingBlockWithItem(ModBlocks.QUANTUM_LATTICE_FRAME);
        casingBlockWithItem(ModBlocks.GRAVITON_PLATED_CASING);
        blockWithFluidTexture(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK, "white_dwarf_fragment");
        blockWithFluidTexture(ModBlocks.PULSAR_FRAGMENT_BLOCK, "pulsar_fragment");
        blockWithFluidTexture(ModBlocks.NEUTRON_STAR_FRAGMENT_BLOCK, "neutron_star_fragment");

        // --- Registro dos Novos Blocos Multiblock ---
        // Blocos que são um cubo simples
        multiblockCube(MultiblockBlocks.ENTROPY_ASSEMBLER_CORE_CASING);
        connectedTextureCube(MultiblockBlocks.ENTROPY_SINGULARITY_CASING, "entropy_singularity_casing");
        craftingLikeCube(MultiblockBlocks.ENTROPY_COMPUTER_CONDENSATION_MATRIX, "entropy_computer_condensation_matrix");
        craftingLikeCube(MultiblockBlocks.ENTROPIC_CONVERGENCE_ENGINE, "entropy_computer_condensation_matrix");
        entropicMachineCube(MultiblockBlocks.ENTROPIC_CONVERGENCE_CASING, "entropic_convergence_casing");
        multiblockCubeWithTexture(MultiblockBlocks.QUANTUM_ENTROPY_CASING, "quantum_hyper_mechanical_casing");
        connectedTextureCube(MultiblockBlocks.QUANTUM_HYPER_MECHANICAL_CASING, "quantum_hyper_mechanical_casing");
        qmfControllerBlock(MultiblockBlocks.QUANTUM_MATTER_FABRICATOR_CONTROLLER);
        controllerWithBase(MultiblockBlocks.QUANTUM_SLICER_CONTROLLER, "quantum_hyper_mechanical_casing");
        controllerWithBase(MultiblockBlocks.QUANTUM_PROCESSOR_ASSEMBLER_CONTROLLER, "quantum_hyper_mechanical_casing");
        controllerWithBase(MultiblockBlocks.QUANTUM_CRYOFORGE_CONTROLLER, "quantum_hyper_mechanical_casing");
        endgameControllerWithOverlay(MultiblockBlocks.QUANTUM_COMPUTATION_NEXUS_CONTROLLER,
                "quantum_computation_nexus",
                com.raishxn.ufo.block.QuantumComputationNexusControllerBlock.POWERED, true);
        endgameControllerWithOverlay(MultiblockBlocks.QUANTUM_PATTERN_FABRICATION_MATRIX_CONTROLLER,
                "quantum_pattern_fabrication_matrix",
                com.raishxn.ufo.block.QuantumPatternFabricationMatrixControllerBlock.POWERED, false);
        endgameControllerWithOverlay(MultiblockBlocks.INFINITY_FABRICATION_SINGULARITY_CONTROLLER,
                "infinity_fabrication_singularity",
                com.raishxn.ufo.block.InfinityFabricationSingularityControllerBlock.POWERED, true);
        quantumPortWithOverlay(MultiblockBlocks.QUANTUM_GRID_LINK, "quantum_grid_link_overlay");

        // ═══════════════════ STELLAR NEXUS ═══════════════════
        stellarNexusControllerBlock(MultiblockBlocks.STELLAR_NEXUS_CONTROLLER);
        hatchWithOverlay(MultiblockBlocks.ME_MASSIVE_OUTPUT_HATCH, "me_massive_output_hatch_overlay");
        hatchWithOverlay(MultiblockBlocks.ME_MASSIVE_FLUID_HATCH, "me_massive_fluid_hatch_overlay");
        hatchWithOverlay(MultiblockBlocks.ME_MASSIVE_INPUT_HATCH, "me_massive_input_hatch_overlay");
        hatchWithOverlay(MultiblockBlocks.AE_ENERGY_INPUT_HATCH, "ae_energy_input_hatch_overlay");
        multiblockCube(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1);
        multiblockCube(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2);
        multiblockCube(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3);


        // --- Blocos com Variantes (Crafting Units) ---
        for (var tier : MegaCraftingStorageTier.values()) {
            var block = ModBlocks.CRAFTING_STORAGE_BLOCKS.get(tier);
            String registryName = block.getId().getPath();
            ModelFile unformedModel = models().cubeAll(registryName, modLoc("block/crafting/" + registryName));
            ModelFile formedModel = models().getBuilder(registryName + "_formed")
                    .customLoader(CraftingModelBuilder::new).tier(tier, false).end();

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
            ModelFile unformedModel = models().cubeAll(registryName, modLoc("block/crafting/" + registryName));
            ModelFile formedModel = models().getBuilder(registryName + "_formed")
                    .customLoader(CraftingModelBuilder::new).tier(tier, true).end();

            getVariantBuilder(block.get())
                    .partialState().with(AbstractCraftingUnitBlock.FORMED, false)
                    .setModels(new ConfiguredModel(unformedModel))
                    .partialState().with(AbstractCraftingUnitBlock.FORMED, true)
                    .setModels(new ConfiguredModel(formedModel));

            simpleBlockItem(block.get(), unformedModel);
        }
    }

    private void casingBlockWithItem(DeferredBlock<Block> blockHolder) {
        String registryName = blockHolder.getId().getPath();
        ModelFile model = models().cubeAll(registryName, modLoc("block/casings/" + registryName));
        simpleBlock(blockHolder.get(), model);
        simpleBlockItem(blockHolder.get(), model);
    }

    // --- MÉTODOS AUXILIARES PARA OS NOVOS BLOCOS ---

    /**
     * Registra um bloco de multiblock que é um cubo simples.
     */
    private void multiblockCube(DeferredBlock<? extends Block> block) {
        String name = block.getId().getPath();
        ResourceLocation texture = modLoc("block/multiblock/" + name);
        simpleBlock(block.get(), models().cubeAll(name, texture));
        simpleBlockItem(block.get(), models().getExistingFile(modLoc("block/" + name)));
    }

    private void multiblockCubeWithTexture(DeferredBlock<? extends Block> block, String textureName) {
        String name = block.getId().getPath();
        ResourceLocation texture = modLoc("block/multiblock/" + textureName);
        simpleBlock(block.get(), models().cubeAll(name, texture));
        simpleBlockItem(block.get(), models().getExistingFile(modLoc("block/" + name)));
    }

    private void connectedTextureCube(DeferredBlock<? extends Block> block, String textureName) {
        String name = block.getId().getPath();
        ResourceLocation baseTexture = modLoc("block/multiblock/" + textureName);
        ResourceLocation ctmTexture = modLoc("block/multiblock/" + textureName + "_ctm");
        ModelFile model = models().getBuilder(name)
                .renderType("solid")
                .texture("base", baseTexture)
                .texture("ctm", ctmTexture)
                .texture("particle", baseTexture)
                .customLoader(ConnectedTextureModelBuilder::new)
                .connection(modLoc("same_block"))
                .end();
        simpleBlock(block.get(), model);
        ModelFile inventoryModel = models().cubeAll(name + "_inventory", baseTexture);
        simpleBlockItem(block.get(), inventoryModel);
    }

    private void craftingLikeCube(DeferredBlock<? extends Block> block, String textureName) {
        String name = block.getId().getPath();
        ResourceLocation texture = modLoc("block/multiblock/" + textureName);
        // cutout declared in the model itself so ItemBlockRenderTypes.setRenderLayer stays unused.
        ModelFile model = models().cubeAll(name, texture).renderType("cutout");

        getVariantBuilder(block.get())
                .partialState().with(AbstractCraftingUnitBlock.FORMED, false)
                .setModels(new ConfiguredModel(model))
                .partialState().with(AbstractCraftingUnitBlock.FORMED, true)
                .setModels(new ConfiguredModel(model));

        simpleBlockItem(block.get(), model);
    }

    private void entropicMachineCube(DeferredBlock<? extends Block> block, String textureName) {
        String name = block.getId().getPath();
        ResourceLocation texture = modLoc("block/multiblock/" + textureName);
        ModelFile model = models().cubeAll(name, texture);

        getVariantBuilder(block.get()).forAllStates(state -> {
            boolean formed = getBooleanPropertyByName(state, "formed");
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .build();
        });

        simpleBlockItem(block.get(), model);
    }

    private boolean getBooleanPropertyByName(BlockState state, String propertyName) {
        for (var property : state.getProperties()) {
            if (property.getName().equals(propertyName) && property instanceof BooleanProperty booleanProperty) {
                return state.getValue(booleanProperty);
            }
        }
        return false;
    }

    /**
     * Directional multiblock cube — same texture on all faces, rotated by FACING.
     */
    private void directionalMultiblockCube(DeferredBlock<? extends Block> block) {
        String name = block.getId().getPath();
        ResourceLocation texture = modLoc("block/multiblock/" + name);
        ModelFile model = models().cubeAll(name, texture);

        getVariantBuilder(block.get()).forAllStates(state -> {
            Direction dir = state.getValue(DirectionalBlock.FACING);
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationX(dir == Direction.DOWN ? 90 : dir == Direction.UP ? -90 : 0)
                    .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360)
                    .build();
        });

        simpleBlockItem(block.get(), model);
    }

    private void blockWithFluidTexture(DeferredBlock<Block> block, String fluidTextureName) {
        String name = block.getId().getPath();
        ResourceLocation texture = modLoc("block/fluid/" + fluidTextureName);
        ModelFile model = models().cubeAll(name, texture);
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }
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

    private void endgameControllerWithOverlay(DeferredBlock<? extends Block> block, String overlayFolder,
                                               BooleanProperty poweredProperty, boolean hasInactiveGlow) {
        String name = block.getId().getPath();
        ResourceLocation base = modLoc("block/multiblock/quantum_hyper_mechanical_casing");
        ResourceLocation inactiveOverlay = modLoc("block/" + overlayFolder + "/overlay_front");
        ModelFile inactive = hasInactiveGlow
                ? endgameControllerModel(name, base, inactiveOverlay,
                        modLoc("block/" + overlayFolder + "/overlay_front_glow"))
                : endgameControllerModel(name, base, inactiveOverlay);
        ModelFile active = endgameControllerModel(name + "_active", base,
                modLoc("block/" + overlayFolder + "/overlay_front_active"),
                modLoc("block/" + overlayFolder + "/overlay_front_active_glow"));

        getVariantBuilder(block.get()).forAllStates(state -> {
            Direction direction = state.getValue(DirectionalBlock.FACING);
            boolean powered = state.getValue(poweredProperty);
            return ConfiguredModel.builder()
                    .modelFile(powered ? active : inactive)
                    .rotationX(direction == Direction.DOWN ? 90 : direction == Direction.UP ? -90 : 0)
                    .rotationY(direction.getAxis().isVertical() ? 0 : (((int) direction.toYRot()) + 180) % 360)
                    .build();
        });
        // Keep exactly one front in inventory too; duplicated overlays make the controller
        // appear to have several fronts when the item camera shows two horizontal faces.
        ModelFile inventory = hasInactiveGlow
                ? endgameControllerItemModel(name + "_inventory", base, inactiveOverlay,
                        modLoc("block/" + overlayFolder + "/overlay_front_glow"))
                : endgameControllerItemModel(name + "_inventory", base, inactiveOverlay);
        simpleBlockItem(block.get(), inventory);
    }

    private ModelFile endgameControllerModel(String name, ResourceLocation base, ResourceLocation overlay) {
        return models().withExistingParent(name, "block/block")
                .renderType("cutout")
                .texture("particle", base)
                .texture("base", base)
                .texture("overlay", overlay)
                .element().from(0, 0, 0).to(16, 16, 16)
                    .allFaces((direction, face) -> face.texture("#base").cullface(direction)).end()
                .element().from(0, 0, 0).to(16, 16, 16)
                    .face(Direction.NORTH).texture("#overlay").cullface(Direction.NORTH).end().end();
    }

    private ModelFile endgameControllerModel(String name, ResourceLocation base,
                                             ResourceLocation overlay, ResourceLocation glow) {
        return models().withExistingParent(name, "block/block")
                .renderType("cutout")
                .texture("particle", base)
                .texture("base", base)
                .texture("overlay", overlay)
                .texture("glow", glow)
                .element().from(0, 0, 0).to(16, 16, 16)
                    .allFaces((direction, face) -> face.texture("#base").cullface(direction)).end()
                .element().from(0, 0, 0).to(16, 16, 16)
                    .face(Direction.NORTH).texture("#overlay").cullface(Direction.NORTH).end().end()
                .element().from(0, 0, 0).to(16, 16, 16)
                    .face(Direction.NORTH).texture("#glow").cullface(Direction.NORTH).end().end();
    }

    private ModelFile endgameControllerItemModel(String name, ResourceLocation base,
                                                 ResourceLocation overlay, ResourceLocation glow) {
        return models().withExistingParent(name, "block/block")
                .renderType("cutout")
                .texture("particle", base)
                .texture("base", base)
                .texture("overlay", overlay)
                .texture("glow", glow)
                .element().from(0, 0, 0).to(16, 16, 16)
                    .allFaces((direction, face) -> face.texture("#base").cullface(direction)).end()
                // The inherited GUI camera (30, 225, 0) sees NORTH. Give the item one
                // visible front without duplicating the overlay on another side.
                .element().from(0, 0, -0.01F).to(16, 16, 0)
                    .face(Direction.NORTH).texture("#overlay").uvs(0, 0, 16, 16).end().end()
                .element().from(0, 0, -0.02F).to(16, 16, -0.01F)
                    .face(Direction.NORTH).texture("#glow").uvs(0, 0, 16, 16).end().end();
    }

    private ModelFile endgameControllerItemModel(String name, ResourceLocation base,
                                                 ResourceLocation overlay) {
        return models().withExistingParent(name, "block/block")
                .renderType("cutout")
                .texture("particle", base)
                .texture("base", base)
                .texture("overlay", overlay)
                .element().from(0, 0, 0).to(16, 16, 16)
                    .allFaces((direction, face) -> face.texture("#base").cullface(direction)).end()
                .element().from(0, 0, -0.01F).to(16, 16, 0)
                    .face(Direction.NORTH).texture("#overlay").uvs(0, 0, 16, 16).end().end();
    }

    private void controllerWithBase(DeferredBlock<? extends Block> block, String baseTextureName) {
        String name = block.getId().getPath();
        ResourceLocation baseTexture = modLoc("block/multiblock/" + baseTextureName);
        ResourceLocation overlayTexture = modLoc("block/multiblock/overlay_front");

        ModelFile model = models().withExistingParent(name, "block/block")
                .renderType("cutout")
                .texture("particle", baseTexture)
                .texture("base", baseTexture)
                .texture("overlay", overlayTexture)
                .element().from(0, 0, 0).to(16, 16, 16).allFaces((dir, face) -> face.texture("#base").cullface(dir)).end()
                .element().from(0, 0, 0).to(16, 16, 16)
                .face(Direction.NORTH).texture("#overlay").cullface(Direction.NORTH).end().end();

        getVariantBuilder(block.get()).forAllStates(state -> {
            Direction dir = state.getValue(DirectionalBlock.FACING);
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationX(dir == Direction.DOWN ? 90 : dir == Direction.UP ? -90 : 0)
                    .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360)
                    .build();
        });

        simpleBlockItem(block.get(), model);
    }

    private void qmfControllerBlock(DeferredBlock<? extends Block> block) {
        String name = block.getId().getPath();
        ResourceLocation baseTexture = modLoc("block/multiblock/quantum_hyper_mechanical_casing");
        ResourceLocation overlayInactive = modLoc("block/qmf/overlay_front");
        ResourceLocation overlayActive = modLoc("block/qmf/overlay_front_active");

        ModelFile inactiveModel = models().withExistingParent(name, "block/block")
                .renderType("cutout")
                .texture("particle", baseTexture)
                .texture("base", baseTexture)
                .texture("overlay", overlayInactive)
                .element().from(0, 0, 0).to(16, 16, 16).allFaces((dir, face) -> face.texture("#base").cullface(dir)).end()
                .element().from(0, 0, 0).to(16, 16, 16).face(Direction.NORTH).texture("#overlay").cullface(Direction.NORTH).end().end();

        ModelFile activeModel = models().withExistingParent(name + "_active", "block/block")
                .renderType("cutout")
                .texture("particle", baseTexture)
                .texture("base", baseTexture)
                .texture("overlay", overlayActive)
                .element().from(0, 0, 0).to(16, 16, 16).allFaces((dir, face) -> face.texture("#base").cullface(dir)).end()
                .element().from(0, 0, 0).to(16, 16, 16).face(Direction.NORTH).texture("#overlay").cullface(Direction.NORTH).end().end();

        getVariantBuilder(block.get()).forAllStates(state -> {
            Direction dir = state.getValue(DirectionalBlock.FACING);
            boolean active = state.getValue(com.raishxn.ufo.block.MultiblockBlocks.ControllerBlock.ACTIVE);
            return ConfiguredModel.builder()
                    .modelFile(active ? activeModel : inactiveModel)
                    .rotationX(dir == Direction.DOWN ? 90 : dir == Direction.UP ? -90 : 0)
                    .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360)
                    .build();
        });

        simpleBlockItem(block.get(), inactiveModel);
    }

    private void stellarNexusControllerBlock(DeferredBlock<? extends Block> block) {
        String name = block.getId().getPath();
        ResourceLocation baseTexture = modLoc("block/multiblock/entropy_singularity_casing");
        ResourceLocation overlayTexture = modLoc("block/multiblock/overlay_front");

        ModelFile normalModel = models().withExistingParent(name, "block/block")
                .renderType("cutout")
                .texture("particle", baseTexture)
                .texture("base", baseTexture)
                .texture("overlay", overlayTexture)
                .element().from(0, 0, 0).to(16, 16, 16).allFaces((dir, face) -> face.texture("#base").cullface(dir)).end()
                .element().from(0, 0, 0).to(16, 16, 16).face(Direction.NORTH).texture("#overlay").cullface(Direction.NORTH).end().end();

        ResourceLocation assembledBase = modLoc("block/multiblock/entropy_assembler_core_casing");
        ModelFile assembledModel = models().withExistingParent(name + "_assembled", "block/block")
                .renderType("cutout")
                .texture("particle", assembledBase)
                .texture("base", assembledBase)
                .texture("overlay", overlayTexture)
                .element().from(0, 0, 0).to(16, 16, 16).allFaces((dir, face) -> face.texture("#base").cullface(dir)).end()
                .element().from(0, 0, 0).to(16, 16, 16).face(Direction.NORTH).texture("#overlay").cullface(Direction.NORTH).end().end();

        getVariantBuilder(block.get()).forAllStates(state -> {
            Direction dir = state.getValue(DirectionalBlock.FACING);
            boolean assembled = state.getValue(com.raishxn.ufo.block.StellarNexusControllerBlock.ASSEMBLED);
            return ConfiguredModel.builder()
                    .modelFile(assembled ? assembledModel : normalModel)
                    .rotationX(dir == Direction.DOWN ? 90 : dir == Direction.UP ? -90 : 0)
                    .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360)
                    .build();
        });

        simpleBlockItem(block.get(), normalModel);
    }

    /**
     * Hatch block with entropy_singularity_casing as base + a per-hatch overlay on the front face.
     * Uses cutout render type to support animated overlay textures.
     */
    private void hatchWithOverlay(DeferredBlock<? extends Block> block, String overlayName) {
        String name = block.getId().getPath();
        ResourceLocation overlayTexture = modLoc("block/multiblock/" + overlayName);

        ModelFile defaultModel = hatchModel(name, modLoc("block/multiblock/entropy_singularity_casing"), overlayTexture);
        ModelFile quantumModel = hatchModel(name + "_quantum", modLoc("block/multiblock/quantum_hyper_mechanical_casing"), overlayTexture);
        ModelFile entropyModel = hatchModel(name + "_entropy", modLoc("block/multiblock/entropy_singularity_casing"), overlayTexture);

        getVariantBuilder(block.get()).forAllStates(state -> {
            Direction dir = state.getValue(DirectionalBlock.FACING);
            MultiblockCasingStyle style = state.getValue(com.raishxn.ufo.block.MassiveOutputHatchBlock.CASING_STYLE);
            ModelFile model = switch (style) {
                case QUANTUM -> quantumModel;
                case ENTROPY -> entropyModel;
                case DEFAULT -> defaultModel;
            };
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationX(dir == Direction.DOWN ? 90 : dir == Direction.UP ? -90 : 0)
                    .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360)
                    .build();
        });

        simpleBlockItem(block.get(), defaultModel);
    }

    private ModelFile hatchModel(String name, ResourceLocation baseTexture, ResourceLocation overlayTexture) {
        return models().withExistingParent(name, "block/block")
                .renderType("cutout")
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
    }

    private void quantumPortWithOverlay(DeferredBlock<? extends Block> block, String overlayName) {
        String name = block.getId().getPath();
        ResourceLocation overlay = modLoc("block/multiblock/" + overlayName);
        ResourceLocation quantumBase = modLoc("block/multiblock/quantum_hyper_mechanical_casing");
        ResourceLocation entropyBase = modLoc("block/multiblock/entropy_singularity_casing");
        ModelFile defaultModel = hatchModel(name, quantumBase, overlay);
        ModelFile quantumModel = hatchModel(name + "_quantum", quantumBase, overlay);
        ModelFile entropyModel = hatchModel(name + "_entropy", entropyBase, overlay);

        getVariantBuilder(block.get()).forAllStates(state -> {
            Direction direction = state.getValue(DirectionalBlock.FACING);
            MultiblockCasingStyle style = state.getValue(com.raishxn.ufo.block.QuantumGridLinkBlock.CASING_STYLE);
            ModelFile model = switch (style) {
                case QUANTUM -> quantumModel;
                case ENTROPY -> entropyModel;
                case DEFAULT -> defaultModel;
            };
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationX(direction == Direction.DOWN ? 90 : direction == Direction.UP ? -90 : 0)
                    .rotationY(direction.getAxis().isVertical() ? 0 : (((int) direction.toYRot()) + 180) % 360)
                    .build();
        });
        simpleBlockItem(block.get(), defaultModel);
    }
}
