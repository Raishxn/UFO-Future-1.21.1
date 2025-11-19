package com.raishxn.ufo.event;

import appeng.core.definitions.AEParts;
import com.raishxn.ufo.core.MegaCoProcessorTier;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.minecraft.core.registries.BuiltInRegistries;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismFluids;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.datagen.DMARecipeBuilder;
import com.raishxn.ufo.fluid.ModFluids;
import com.raishxn.ufo.item.ModCellItems;
import com.raishxn.ufo.item.ModItems;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.world.level.material.Fluid;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput c) {
        // 1. Vanilla Crafting (Conversões básicas de metal/blocos)
        buildBasicConversions(c);

        // 2. Processadores e Circuitos (DMA)
        buildProcessorsDMA(c);

        // 3. Matrizes e Componentes de Armazenamento (DMA) - REFATORADO (Balanceamento 2x + Matéria)
        buildComponentsDMA(c);

        // 4. Matérias, Fluidos e Itens Intermediários (DMA) - REFATORADO (Dark Matter rebalanceada)
        buildMaterialsAndFluidsDMA(c);

        // 5. Catalisadores (DMA)
        buildCatalystsDMA(c);

        // 6. Armaduras (UFO e Thermal) (DMA)
        buildArmorsDMA(c);

        // 0. Geração de Ingots
        buildIngotGenerators(c);

        // 7. Células Infinitas (DMA Hardcore) - OTIMIZADO (Loops + Condicionais)
        buildInfinityCellsDMA(c);

        // 8. Housings de Células (Crafting Table)
        buildHousingRecipes(c);

        // 9. Máquinas e Blocos Estruturais (Crafting Table)
        buildMachineAndStorageRecipes(c);
    }

    // ========================================================================================
    // --- 1. CONVERSÕES BÁSICAS (CRAFTING TABLE) ---
    // ========================================================================================
    private void buildBasicConversions(RecipeOutput c) {
        // Obsidian Matrix (Vanilla)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.OBSIDIAN_MATRIX.get())
                .pattern("OOO")
                .pattern("OEO")
                .pattern("OOO")
                .define('O', Items.OBSIDIAN)
                .define('E', Items.ENDER_EYE)
                .unlockedBy("has_obsidian", has(Items.OBSIDIAN))
                .save(c, UfoMod.id("obsidian_matrix_vanilla"));

        createMetalRecipes(c, ModItems.WHITE_DWARF_FRAGMENT_INGOT.get(), ModItems.WHITE_DWARF_FRAGMENT_NUGGET.get(), ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK.get());
        createMetalRecipes(c, ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get(), ModItems.NEUTRON_STAR_FRAGMENT_NUGGET.get(), ModBlocks.NEUTRON_STAR_FRAGMENT_BLOCK.get());
        createMetalRecipes(c, ModItems.PULSAR_FRAGMENT_INGOT.get(), ModItems.PULSAR_FRAGMENT_NUGGET.get(), ModBlocks.PULSAR_FRAGMENT_BLOCK.get());
    }

    private void createMetalRecipes(RecipeOutput c, ItemLike ingot, ItemLike nugget, ItemLike block) {
        // Block <-> Ingot
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, block)
                .pattern("AAA").pattern("AAA").pattern("AAA")
                .define('A', ingot).unlockedBy("has_ingot", has(ingot)).save(c);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ingot, 9)
                .requires(block).unlockedBy("has_block", has(block)).save(c, UfoMod.id(BuiltInRegistries.ITEM.getKey(ingot.asItem()).getPath() + "_from_block"));

        // Ingot <-> Nugget
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ingot)
                .pattern("AAA").pattern("AAA").pattern("AAA")
                .define('A', nugget).unlockedBy("has_nugget", has(nugget)).save(c, UfoMod.id(BuiltInRegistries.ITEM.getKey(ingot.asItem()).getPath() + "_from_nugget"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, nugget, 9)
                .requires(ingot).unlockedBy("has_ingot", has(ingot)).save(c);
    }

    // ========================================================================================
    // --- 2. PROCESSADORES (DMA) ---
    // ========================================================================================
    private void buildProcessorsDMA(RecipeOutput c) {
        // Mantido igual - Base sólida
        DMARecipeBuilder.create("dma/dimensional_processor_press")
                .output(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get())
                .inputItem(Items.IRON_BLOCK, 2)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 500)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 250)
                .energy(100_000).time(200).save(c);

        DMARecipeBuilder.create("dma/printed_dimensional_processor")
                .output(ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get())
                .inputItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get())
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 100)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 50)
                .energy(50_000).time(100).save(c);

        DMARecipeBuilder.create("dma/dimensional_processor")
                .output(ModItems.DIMENSIONAL_PROCESSOR.get())
                .inputItem(ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get())
                .inputItem(AEItems.SILICON_PRINT)
                .inputItem(AEItems.FLUIX_DUST)
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 200)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 100)
                .energy(80_000).time(150).save(c);
    }

    // ========================================================================================
    // --- 3. COMPONENTES E MATRIZES (DMA) - REBALANCED ---
    // ========================================================================================
    private void buildComponentsDMA(RecipeOutput c) {
        // Lógica: 2x Tier Anterior + 4x Matéria Específica (Progressão Astrofísica)
        // Reduz o crafting bloat mas força o jogador a ter a infraestrutura de matéria.

        // Tier 1: Phase Shift (Entrada)
        DMARecipeBuilder.create("dma/component/phase_shift")
                .output(ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get())
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), 2)
                .inputItem(AEItems.CELL_COMPONENT_256K)
                .inputItem(AEBlocks.QUARTZ_VIBRANT_GLASS, 4)
                .inputItem(Items.NETHER_STAR) // Catalisador
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 1000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 500)
                .energy(500_000).time(400).save(c);

        // Tier 2: Hyper Dense (Requer White Dwarf Matter)
        createTieredComponent(c, ModItems.HYPER_DENSE_COMPONENT_MATRIX.get(),
                ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get(), 2,
                ModItems.WHITE_DWARF_MATTER.get(), 4,
                ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID, 1000,
                2_000_000, 800);

        // Tier 3: Tesseract (Requer Neutron Star Matter)
        createTieredComponent(c, ModItems.TESSERACT_COMPONENT_MATRIX.get(),
                ModItems.HYPER_DENSE_COMPONENT_MATRIX.get(), 2,
                ModItems.NEUTRON_STAR_MATTER.get(), 4,
                ModFluids.SOURCE_TEMPORAL_FLUID, 2000,
                8_000_000, 1600);

        // Tier 4: Event Horizon (Requer Pulsar Matter)
        createTieredComponent(c, ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get(),
                ModItems.TESSERACT_COMPONENT_MATRIX.get(), 2,
                ModItems.PULSAR_MATTER.get(), 4,
                ModFluids.SOURCE_SPATIAL_FLUID, 4000,
                32_000_000, 3200);

        // Tier 5: Cosmic String (GOD TIER - Requer Dark Matter)
        // Salto de dificuldade: Dark Matter é muito custosa.
        createTieredComponent(c, ModItems.COSMIC_STRING_COMPONENT_MATRIX.get(),
                ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get(), 2,
                ModItems.DARK_MATTER.get(), 2,
                ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID, 8000,
                100_000_000, 6000);
    }

    // ========================================================================================
    // --- 4. MATÉRIAS, FLUIDOS E INTERMEDIÁRIOS (DMA) ---
    // ========================================================================================
    private void buildMaterialsAndFluidsDMA(RecipeOutput c) {
        // --- Metais Sintéticos ---
        // White Dwarf
        DMARecipeBuilder.create("dma/white_dwarf_dust")
                .output(ModItems.WHITE_DWARF_FRAGMENT_DUST.get())
                .inputItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 20)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 20)
                .energy(10_000).time(60).save(c);

        // Neutron Star
        DMARecipeBuilder.create("dma/neutron_star_dust")
                .output(ModItems.NEUTRON_STAR_FRAGMENT_DUST.get())
                .inputItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 20)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 20)
                .energy(15_000).time(80).save(c);

        // Pulsar
        DMARecipeBuilder.create("dma/pulsar_dust")
                .output(ModItems.PULSAR_FRAGMENT_DUST.get())
                .inputItem(ModItems.PULSAR_FRAGMENT_INGOT.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 20)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 20)
                .energy(20_000).time(100).save(c);

        // Thermal Dusts (Blizz/Cryotheum)
        DMARecipeBuilder.create("dma/dust_blizz")
                .output(ModItems.DUST_BLIZZ.get(), 4)
                .inputItem(Items.SNOWBALL, 4).inputItem(Items.REDSTONE)
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 50).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 25)
                .energy(20_000).time(80).save(c);

        DMARecipeBuilder.create("dma/dust_cryotheum")
                .output(ModItems.DUST_CRYOTHEUM.get(), 2)
                .inputItem(ModItems.DUST_BLIZZ.get(), 2).inputItem(Items.REDSTONE, 2).inputItem(Items.SNOWBALL)
                .inputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 100).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 50)
                .energy(40_000).time(100).save(c);

        // Rods
        buildRodRecipe(c, ModItems.WHITE_DWARF_FRAGMENT_ROD, ModItems.WHITE_DWARF_FRAGMENT_INGOT, 10_000, 60);
        buildRodRecipe(c, ModItems.NEUTRON_STAR_FRAGMENT_ROD, ModItems.NEUTRON_STAR_FRAGMENT_INGOT, 20_000, 80);

        // --- Itens High-End ---
        // Nuclear Star
        DMARecipeBuilder.create("dma/nuclear_star")
                .output(ModItems.NUCLEAR_STAR.get())
                .inputItem(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK.get())
                .inputItem(ModBlocks.NEUTRON_STAR_FRAGMENT_BLOCK.get())
                .inputItem(ModBlocks.PULSAR_FRAGMENT_BLOCK.get())
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 5000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 5000)
                .energy(25_000_000).time(3000).save(c);

        // Unstable White Hole
        DMARecipeBuilder.create("dma/unstable_white_hole_matter")
                .output(ModItems.UNSTABLE_WHITE_HOLE_MATTER.get())
                .inputItem(ModItems.WHITE_DWARF_MATTER.get(), 4)
                .inputItem(ModItems.QUANTUM_ANOMALY.get())
                .inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 2000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 8000)
                .energy(50_000_000).time(4000).save(c);

        // Safe Containment & Capsule
        DMARecipeBuilder.create("dma/safe_containment_matter")
                .output(ModItems.SAFE_CONTAINMENT_MATTER.get())
                .inputItem(ModItems.OBSIDIAN_MATRIX.get(), 4).inputItem(Items.NETHERITE_INGOT)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 500).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 200)
                .energy(100_000).time(200).save(c);

        DMARecipeBuilder.create("dma/aether_containment_capsule")
                .output(ModItems.AETHER_CONTAINMENT_CAPSULE.get())
                .inputItem(ModItems.SAFE_CONTAINMENT_MATTER.get(), 2).inputItem(ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get())
                .inputFluid(ModFluids.SOURCE_SPATIAL_FLUID.get(), 1000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 500)
                .energy(500_000).time(400).save(c);

        // Scrap & Scrap Box
        DMARecipeBuilder.create("dma/scrap_from_matrix")
                .output(ModItems.SCRAP.get(), 4).output(AEItems.MATTER_BALL.get(), 1, 0.10f)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get())
                .inputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 10).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 10)
                .energy(100_000).time(200).save(c);

        DMARecipeBuilder.create("dma/scrap_box")
                .output(ModItems.SCRAP_BOX.get()).output(ModItems.SCAR.get(), 1, 0.03f)
                .inputItem(ModItems.SCRAP.get(), 9)
                .inputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 100).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 100)
                .energy(10_000_000).time(2000).save(c);

        // Fluid Recipes (UU, Temporal, etc.)
        buildFluidRecipes(c);

        // Matters e Esferas (Progresso Principal)
        buildMatterProgression(c);

        // Dark Matter - THE ENDGAME
        DMARecipeBuilder.create("dma/dark_matter")
                .output(ModItems.DARK_MATTER.get())
                // Rebalanceado: Menos itens (16 vs 64), mas MUITO mais tempo e energia.
                // Isso reduz o grind de itens (TPS lag) e aumenta o requisito de infraestrutura (Power/Time).
                .inputItem(ModItems.WHITE_DWARF_MATTER.get(), 16)
                .inputItem(ModItems.NEUTRON_STAR_MATTER.get(), 16)
                .inputItem(ModItems.PULSAR_MATTER.get(), 16)
                .inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 10000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 10000)
                .energy(200_000_000)
                .time(20_000) // ~16.5 minutos de craft por item se tiver apenas 1 DMA.
                .save(c);

        // Staff e Anomalia
        buildStaffAndAnomaly(c);
    }

    // --- Helpers para buildMaterialsAndFluidsDMA ---
    private void buildRodRecipe(RecipeOutput c, Supplier<net.minecraft.world.item.Item> rod, Supplier<net.minecraft.world.item.Item> ingot, int energy, int time) {
        DMARecipeBuilder.create("dma/" + BuiltInRegistries.ITEM.getKey(rod.get()).getPath())
                .output(rod.get(), 2)
                .inputItem(ingot.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 50)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 20)
                .energy(energy).time(time).save(c);
    }

    private void buildFluidRecipes(RecipeOutput c) {
        // Amplificadores e Fluidos Básicos
        DMARecipeBuilder.create("dma/uu_amplifier")
                .outputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 9, 1.0f)
                .inputItem(ModItems.SCRAP_BOX.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 10).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 100)
                .energy(200_000).time(600).save(c);

        DMARecipeBuilder.create("dma/uu_matter")
                .outputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 10, 1.0f)
                .inputItem(AEItems.MATTER_BALL.get(), 64)
                .inputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 1000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 200)
                .energy(500_000).time(1200).save(c);

        // Fluidos Avançados
        createFluidRecipe(c, ModFluids.SOURCE_TEMPORAL_FLUID, ModItems.QUANTUM_ANOMALY, ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID, 2_000_000);
        createFluidRecipe(c, ModFluids.SOURCE_SPATIAL_FLUID, ModItems.NUCLEAR_STAR, ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID, 6_000_000);

        // Cryotheum e Plasmas
        DMARecipeBuilder.create("dma/gelid_cryotheum")
                .outputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 10000, 1.0f)
                .inputItem(ModItems.DUST_CRYOTHEUM.get(), 4)
                .inputFluid(MekanismFluids.HYDROGEN.get(), 2000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 100)
                .energy(500_000).time(200).save(c);

        DMARecipeBuilder.create("dma/primordial_matter_liquid")
                .outputFluid(ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID.get(), 5000, 1.0f)
                .inputItem(ModItems.QUANTUM_ANOMALY.get())
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 8000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 2000)
                .energy(3_000_000).time(700).save(c);

        DMARecipeBuilder.create("dma/liquid_starlight")
                .outputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 10000, 1.0f)
                .inputItem(ModItems.QUANTUM_ANOMALY.get(), 2).inputItem(ModItems.PULSAR_FRAGMENT_DUST.get(), 8)
                .inputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 100).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(4_000_000).time(1000).save(c);

        DMARecipeBuilder.create("dma/raw_star_matter_plasma")
                .outputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 8000, 1.0f)
                .inputItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get(), 4).inputItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get(), 2).inputItem(ModItems.PULSAR_FRAGMENT_DUST.get(), 2)
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 1000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 2000)
                .energy(2_500_000).time(600).save(c);

        DMARecipeBuilder.create("dma/transcending_matter_fluid")
                .outputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 10000, 1.0f)
                .inputItem(ModItems.UNSTABLE_WHITE_HOLE_MATTER.get())
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 10000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 5000)
                .energy(12_000_000).time(3000).save(c);
    }

    private void createFluidRecipe(RecipeOutput c, Supplier<? extends Fluid> output, Supplier<net.minecraft.world.item.Item> inputItem, Supplier<? extends Fluid> inputFluid, int energy) {
        DMARecipeBuilder.create("dma/" + BuiltInRegistries.FLUID.getKey(output.get()).getPath())
                .outputFluid(output.get(), 5000, 1.0f) // Padrão 5B
                .inputItem(inputItem.get())
                .inputFluid(inputFluid.get(), 2000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 500)
                .energy(energy).time(400).save(c);
    }

    private void buildMatterProgression(RecipeOutput c) {
        // Esferas
        DMARecipeBuilder.create("dma/neutronium_sphere")
                .output(ModItems.NEUTRONIUM_SPHERE.get())
                .inputItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get(), 9)
                .inputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 500).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 500)
                .energy(1_000_000).time(600).save(c);

        DMARecipeBuilder.create("dma/enriched_neutronium_sphere")
                .output(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get())
                .inputItem(ModItems.NEUTRONIUM_SPHERE.get())
                .inputItem(ModItems.QUANTUM_ANOMALY.get(), 2)
                .inputFluid(ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID.get(), 12000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(8_000_000).time(3000).save(c);

        DMARecipeBuilder.create("dma/proto_matter")
                .output(ModItems.PROTO_MATTER.get())
                .inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get())
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 1000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(2_000_000).time(800).save(c);

        // Tier Matters
        DMARecipeBuilder.create("dma/corporeal_matter")
                .output(ModItems.CORPOREAL_MATTER.get()).output(ModItems.SCAR.get(), 1, 0.08f)
                .inputItem(ModItems.PROTO_MATTER.get()).inputItem(Items.IRON_BLOCK, 64)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 1000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(5_000_000).time(1200).save(c);

        DMARecipeBuilder.create("dma/uu_matter_crystal")
                .output(ModItems.UU_MATTER_CRYSTAL.get())
                .inputItem(Items.AMETHYST_SHARD)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 10000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 2000)
                .energy(20_000_000).time(4000).save(c);

        createMatterTier(c, ModItems.WHITE_DWARF_MATTER, ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK, ModFluids.SOURCE_WHITE_DWARF_FRAGMENT_FLUID, 7_000_000, 1500);
        createMatterTier(c, ModItems.NEUTRON_STAR_MATTER, ModBlocks.NEUTRON_STAR_FRAGMENT_BLOCK, ModFluids.SOURCE_NEUTRON_STAR_FRAGMENT_FLUID, 9_500_000, 1850);
        createMatterTier(c, ModItems.PULSAR_MATTER, ModBlocks.PULSAR_FRAGMENT_BLOCK, ModFluids.SOURCE_PULSAR_FRAGMENT_FLUID, 12_000_000, 2200);
    }

    private void createMatterTier(RecipeOutput c, Supplier<net.minecraft.world.item.Item> output, Supplier<net.minecraft.world.level.block.Block> block, Supplier<? extends Fluid> fluid, int energy, int time) {
        DMARecipeBuilder.create("dma/" + BuiltInRegistries.ITEM.getKey(output.get()).getPath())
                .output(output.get()).output(ModItems.SCAR.get(), 1, 0.10f)
                .inputItem(ModItems.CORPOREAL_MATTER.get())
                .inputItem(ModItems.UU_MATTER_CRYSTAL.get())
                .inputItem(block.get())
                .inputFluid(fluid.get(), 1000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(energy).time(time).save(c);
    }

    private void buildStaffAndAnomaly(RecipeOutput c) {
        DMARecipeBuilder.create("dma/ufo_staff")
                .output(ModItems.UFO_STAFF.get())
                .inputItem(ModItems.CHARGED_ENRICHED_NEUTRONIUM_SPHERE.get())
                .inputItem(ModItems.QUANTUM_ANOMALY.get(), 2)
                .inputItem(ModItems.PROTO_MATTER.get(), 2)
                .inputItem(MekanismItems.ATOMIC_ALLOY.get(), 4)
                .inputItem(AEItems.MATTER_BALL.get(), 16)
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 2000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 2000)
                .energy(12_000_000).time(2400).save(c);

        DMARecipeBuilder.create("dma/charged_enriched_neutronium_sphere")
                .output(ModItems.CHARGED_ENRICHED_NEUTRONIUM_SPHERE.get())
                .inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get())
                .inputItem(ModItems.NUCLEAR_STAR.get())
                .inputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 4000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 4000)
                .energy(10_000_000).time(4000).save(c);

        DMARecipeBuilder.create("dma/quantum_anomaly")
                .output(ModItems.QUANTUM_ANOMALY.get())
                .inputItem(ModItems.PULSAR_FRAGMENT_DUST.get(), 12)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 8000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(6_000_000).time(1200).save(c);
    }

    // ========================================================================================
    // --- 5. CATALISADORES (DMA) - PROGRESSIVOS ---
    // ========================================================================================
    private void buildCatalystsDMA(RecipeOutput c) {
        // --- Matterflow (Produção/Velocidade) ---
        // Tier 1: Base
        createProgressiveCatalyst(c, ModItems.MATTERFLOW_CATALYST_T1.get(),
                ModItems.OBSIDIAN_MATRIX.get(), // Base
                ModItems.PROTO_MATTER.get(),
                ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID, 1000, 100_000, 200);

        // Tier 2: Requer Matterflow T1
        createProgressiveCatalyst(c, ModItems.MATTERFLOW_CATALYST_T2.get(),
                ModItems.MATTERFLOW_CATALYST_T1.get(), // Upgrade
                ModItems.CORPOREAL_MATTER.get(),
                ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID, 2000, 500_000, 400);

        // Tier 3: Requer Matterflow T2
        createProgressiveCatalyst(c, ModItems.MATTERFLOW_CATALYST_T3.get(),
                ModItems.MATTERFLOW_CATALYST_T2.get(), // Upgrade
                ModItems.NEUTRON_STAR_MATTER.get(),
                ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID, 4000, 2_500_000, 800);


        // --- Chrono (Tempo/Ticks) ---
        // Tier 1: Base
        createProgressiveCatalyst(c, ModItems.CHRONO_CATALYST_T1.get(),
                ModItems.OBSIDIAN_MATRIX.get(),
                ModItems.PROTO_MATTER.get(),
                ModFluids.SOURCE_TEMPORAL_FLUID, 1000, 150_000, 200);

        // Tier 2: Requer Chrono T1
        createProgressiveCatalyst(c, ModItems.CHRONO_CATALYST_T2.get(),
                ModItems.CHRONO_CATALYST_T1.get(),
                ModItems.CORPOREAL_MATTER.get(),
                ModFluids.SOURCE_TEMPORAL_FLUID, 2000, 600_000, 500); // Mais Fluido Temporal

        // Tier 3: Requer Chrono T2
        createProgressiveCatalyst(c, ModItems.CHRONO_CATALYST_T3.get(),
                ModItems.CHRONO_CATALYST_T2.get(),
                ModItems.PULSAR_MATTER.get(), // Tema de rotação/pulsos
                ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID, 4000, 3_000_000, 1000);


        // --- Overflux (Energia/Eficiência) ---
        // Tier 1: Base
        createProgressiveCatalyst(c, ModItems.OVERFLUX_CATALYST_T1.get(),
                ModItems.OBSIDIAN_MATRIX.get(),
                ModItems.PROTO_MATTER.get(),
                ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID, 1000, 150_000, 200);

        // Tier 2: Requer Overflux T1
        createProgressiveCatalyst(c, ModItems.OVERFLUX_CATALYST_T2.get(),
                ModItems.OVERFLUX_CATALYST_T1.get(),
                ModItems.CORPOREAL_MATTER.get(),
                ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID, 2000, 600_000, 400);

        // Tier 3: Requer Overflux T2
        createProgressiveCatalyst(c, ModItems.OVERFLUX_CATALYST_T3.get(),
                ModItems.OVERFLUX_CATALYST_T2.get(),
                ModItems.WHITE_DWARF_MATTER.get(), // Tema de densidade energética
                ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID, 4000, 3_000_000, 800);


        // --- Quantum (Espaço/Wireless) ---
        // Tier 1: Base
        createProgressiveCatalyst(c, ModItems.QUANTUM_CATALYST_T1.get(),
                ModItems.OBSIDIAN_MATRIX.get(),
                ModItems.PROTO_MATTER.get(),
                ModFluids.SOURCE_SPATIAL_FLUID, 1000, 200_000, 200);

        // Tier 2: Requer Quantum T1
        createProgressiveCatalyst(c, ModItems.QUANTUM_CATALYST_T2.get(),
                ModItems.QUANTUM_CATALYST_T1.get(),
                ModItems.CORPOREAL_MATTER.get(),
                ModFluids.SOURCE_SPATIAL_FLUID, 2000, 800_000, 500);

        // Tier 3: Requer Quantum T2
        createProgressiveCatalyst(c, ModItems.QUANTUM_CATALYST_T3.get(),
                ModItems.QUANTUM_CATALYST_T2.get(),
                ModItems.DARK_MATTER.get(), // Tema de gravidade extrema
                ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID, 5000, 6_000_000, 1200);


        // --- Dimensional Catalyst (GOD TIER) ---
        // Requer todos os T3 + Componente Máximo
        DMARecipeBuilder.create("dma/dimensional_catalyst")
                .output(ModItems.DIMENSIONAL_CATALYST.get())
                .inputItem(ModItems.MATTERFLOW_CATALYST_T3.get())
                .inputItem(ModItems.CHRONO_CATALYST_T3.get())
                .inputItem(ModItems.OVERFLUX_CATALYST_T3.get())
                .inputItem(ModItems.QUANTUM_CATALYST_T3.get())
                .inputItem(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get()) // Componente de Armazenamento Final
                .inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 10000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 10000)
                .energy(500_000_000) // 500M FE
                .time(10_000)        // Demorado
                .save(c);
    }

    // Helper Específico para Catalisadores Progressivos
    private void createProgressiveCatalyst(RecipeOutput c,
                                           net.minecraft.world.item.Item output,
                                           net.minecraft.world.item.Item inputBaseOrPrev, // T1 usa Matrix, T2/T3 usam Catalyst anterior
                                           net.minecraft.world.item.Item coreMatter,      // Matéria do Tier
                                           Supplier<? extends Fluid> fluid, int fluidAmount,
                                           int energy, int time) {
        DMARecipeBuilder.create("dma/" + BuiltInRegistries.ITEM.getKey(output).getPath())
                .output(output)
                .inputItem(inputBaseOrPrev)       // Item anterior ou base
                .inputItem(coreMatter, 2)         // 2x Matéria para dar custo
                .inputItem(AEBlocks.QUARTZ_VIBRANT_GLASS, 2) // Estabilizador Estético
                .inputFluid(fluid.get(), fluidAmount)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), fluidAmount / 2) // Resfriamento proporcional
                .energy(energy)
                .time(time)
                .save(c);
    }

    // ========================================================================================
    // --- 6. ARMADURAS (DMA) ---
    // ========================================================================================
    private void buildArmorsDMA(RecipeOutput c) {
        DMARecipeBuilder.create("dma/thermal_resistor_plating")
                .output(ModItems.THERMAL_RESISTOR_PLATING.get())
                .inputItem(Items.NETHERITE_INGOT)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get())
                .inputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 500)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 500)
                .energy(50_000).time(100).save(c);

        createArmorDMA(c, ModItems.THERMAL_RESISTOR_MASK, ModItems.THERMAL_RESISTOR_PLATING, 5, 1_000_000);
        createArmorDMA(c, ModItems.THERMAL_RESISTOR_CHEST, ModItems.THERMAL_RESISTOR_PLATING, 8, 1_500_000);
        createArmorDMA(c, ModItems.THERMAL_RESISTOR_PANTS, ModItems.THERMAL_RESISTOR_PLATING, 7, 1_200_000);
        createArmorDMA(c, ModItems.THERMAL_RESISTOR_BOOTS, ModItems.THERMAL_RESISTOR_PLATING, 4, 800_000);

        createUfoArmorDMA(c, ModItems.UFO_HELMET, ModItems.THERMAL_RESISTOR_MASK, ModItems.ENRICHED_NEUTRONIUM_SPHERE);
        createUfoArmorDMA(c, ModItems.UFO_CHESTPLATE, ModItems.THERMAL_RESISTOR_CHEST, ModItems.ENRICHED_NEUTRONIUM_SPHERE);
        createUfoArmorDMA(c, ModItems.UFO_LEGGINGS, ModItems.THERMAL_RESISTOR_PANTS, ModItems.ENRICHED_NEUTRONIUM_SPHERE);
        createUfoArmorDMA(c, ModItems.UFO_BOOTS, ModItems.THERMAL_RESISTOR_BOOTS, ModItems.ENRICHED_NEUTRONIUM_SPHERE);
    }

    private void createArmorDMA(RecipeOutput c, Supplier<net.minecraft.world.item.Item> output, Supplier<net.minecraft.world.item.Item> material, int amount, int energy) {
        DMARecipeBuilder.create("dma/" + BuiltInRegistries.ITEM.getKey(output.get()).getPath())
                .output(output.get())
                .inputItem(material.get(), amount)
                .inputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(energy).time(500).save(c);
    }

    private void createUfoArmorDMA(RecipeOutput c, Supplier<net.minecraft.world.item.Item> output, Supplier<net.minecraft.world.item.Item> baseArmor, Supplier<net.minecraft.world.item.Item> rareItem) {
        DMARecipeBuilder.create("dma/" + BuiltInRegistries.ITEM.getKey(output.get()).getPath())
                .output(output.get())
                .inputItem(baseArmor.get())
                .inputItem(rareItem.get(), 2)
                .inputItem(ModItems.QUANTUM_ANOMALY.get())
                .inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 2000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 5000)
                .energy(50_000_000).time(2000).save(c);
    }

    // ========================================================================================
    // --- 7. CÉLULAS INFINITAS (DMA) - OTIMIZADO ---
    // ========================================================================================
    private void buildInfinityCellsDMA(RecipeOutput c) {
        // Vanilla Map - Simplifica o código e facilita adicionar novos blocos
        Map<ItemLike, ItemLike> vanillaMap = new HashMap<>();
        vanillaMap.put(ModItems.INFINITY_WATER_CELL.get(), Items.WATER_BUCKET);
        vanillaMap.put(ModItems.INFINITY_COBBLESTONE_CELL.get(), Items.COBBLESTONE);
        vanillaMap.put(ModItems.INFINITY_COBBLED_DEEPSLATE_CELL.get(), Items.COBBLED_DEEPSLATE);
        vanillaMap.put(ModItems.INFINITY_END_STONE_CELL.get(), Items.END_STONE);
        vanillaMap.put(ModItems.INFINITY_NETHERRACK_CELL.get(), Items.NETHERRACK);
        vanillaMap.put(ModItems.INFINITY_SAND_CELL.get(), Items.SAND);
        vanillaMap.put(ModItems.INFINITY_LAVA_CELL.get(), Items.LAVA_BUCKET);
        vanillaMap.put(ModItems.INFINITY_OBSIDIAN_CELL.get(), Items.OBSIDIAN);
        vanillaMap.put(ModItems.INFINITY_GRAVEL_CELL.get(), Items.GRAVEL);
        vanillaMap.put(ModItems.INFINITY_OAK_LOG_CELL.get(), Items.OAK_LOG);
        vanillaMap.put(ModItems.INFINITY_GLASS_CELL.get(), Items.GLASS);
        vanillaMap.put(ModItems.INFINITY_AMETHYST_SHARD_CELL.get(), Items.AMETHYST_SHARD);

        // Dyes
        vanillaMap.put(ModItems.INFINITY_WHITE_DYE_CELL.get(), Items.WHITE_DYE);
        vanillaMap.put(ModItems.INFINITY_ORANGE_DYE_CELL.get(), Items.ORANGE_DYE);
        vanillaMap.put(ModItems.INFINITY_MAGENTA_DYE_CELL.get(), Items.MAGENTA_DYE);
        vanillaMap.put(ModItems.INFINITY_LIGHT_BLUE_DYE_CELL.get(), Items.LIGHT_BLUE_DYE);
        vanillaMap.put(ModItems.INFINITY_YELLOW_DYE_CELL.get(), Items.YELLOW_DYE);
        vanillaMap.put(ModItems.INFINITY_LIME_DYE_CELL.get(), Items.LIME_DYE);
        vanillaMap.put(ModItems.INFINITY_PINK_DYE_CELL.get(), Items.PINK_DYE);
        vanillaMap.put(ModItems.INFINITY_GRAY_DYE_CELL.get(), Items.GRAY_DYE);
        vanillaMap.put(ModItems.INFINITY_LIGHT_GRAY_DYE_CELL.get(), Items.LIGHT_GRAY_DYE);
        vanillaMap.put(ModItems.INFINITY_CYAN_DYE_CELL.get(), Items.CYAN_DYE);
        vanillaMap.put(ModItems.INFINITY_PURPLE_DYE_CELL.get(), Items.PURPLE_DYE);
        vanillaMap.put(ModItems.INFINITY_BLUE_DYE_CELL.get(), Items.BLUE_DYE);
        vanillaMap.put(ModItems.INFINITY_BROWN_DYE_CELL.get(), Items.BROWN_DYE);
        vanillaMap.put(ModItems.INFINITY_GREEN_DYE_CELL.get(), Items.GREEN_DYE);
        vanillaMap.put(ModItems.INFINITY_RED_DYE_CELL.get(), Items.RED_DYE);
        vanillaMap.put(ModItems.INFINITY_BLACK_DYE_CELL.get(), Items.BLACK_DYE);

        // Gera todas as receitas Vanilla (Sem condição de mod)
        vanillaMap.forEach((cell, target) -> createInfinityCellRecipe(c, cell, target, null));

        // Mekanism (Com Condição "mekanism")
        // ATENÇÃO: Isso requer que o Mekanism esteja no workspace para compilar.
        createInfinityCellRecipe(c, ModItems.INFINITY_ANTIMATTER_PELLET_CELL.get(), MekanismItems.ANTIMATTER_PELLET.get(), "mekanism");
        createInfinityCellRecipe(c, ModItems.INFINITY_PLUTONIUM_PELLET_CELL.get(), MekanismItems.PLUTONIUM_PELLET.get(), "mekanism");
        createInfinityCellRecipe(c, ModItems.INFINITY_POLONIUM_PELLET_CELL.get(), MekanismItems.POLONIUM_PELLET.get(), "mekanism");
        createInfinityCellRecipe(c, ModItems.INFINITY_HDPE_PELLET_CELL.get(), MekanismItems.HDPE_PELLET.get(), "mekanism");

        // AE2 (Com Condição "ae2")
        createInfinityCellRecipe(c, ModItems.INFINITY_SKY_STONE_CELL.get(), AEBlocks.SKY_STONE_BLOCK, "ae2");
    }

    // ========================================================================================
    // --- 8. HOUSINGS (CRAFTING TABLE) ---
    // ========================================================================================
    private void buildHousingRecipes(RecipeOutput c) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get())
                .pattern("GRG").pattern("R R").pattern("III")
                .define('G', AEBlocks.QUARTZ_VIBRANT_GLASS).define('R', ModItems.WHITE_DWARF_FRAGMENT_ROD.get())
                .define('I', ModItems.WHITE_DWARF_FRAGMENT_INGOT.get())
                .unlockedBy("has_item", has(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get())).save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get())
                .pattern("GRG").pattern("R R").pattern("III")
                .define('G', AEBlocks.QUARTZ_VIBRANT_GLASS).define('R', ModItems.NEUTRON_STAR_FRAGMENT_ROD.get())
                .define('I', ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get())
                .unlockedBy("has_item", has(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get())).save(c);

        buildCellAssembly(c, ModCellItems.ITEM_CELL_40M.get(), ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get(), ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get());
        buildCellAssembly(c, ModCellItems.ITEM_CELL_100M.get(), ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get(), ModItems.HYPER_DENSE_COMPONENT_MATRIX.get());
        buildCellAssembly(c, ModCellItems.ITEM_CELL_250M.get(), ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get(), ModItems.TESSERACT_COMPONENT_MATRIX.get());
        buildCellAssembly(c, ModCellItems.ITEM_CELL_750M.get(), ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get(), ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get());
        buildCellAssembly(c, ModCellItems.ITEM_CELL_SINGULARITY.get(), ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get(), ModItems.COSMIC_STRING_COMPONENT_MATRIX.get());

        buildCellAssembly(c, ModCellItems.FLUID_CELL_40M.get(), ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get(), ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get());
        buildCellAssembly(c, ModCellItems.FLUID_CELL_100M.get(), ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get(), ModItems.HYPER_DENSE_COMPONENT_MATRIX.get());
        buildCellAssembly(c, ModCellItems.FLUID_CELL_250M.get(), ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get(), ModItems.TESSERACT_COMPONENT_MATRIX.get());
        buildCellAssembly(c, ModCellItems.FLUID_CELL_750M.get(), ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get(), ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get());
        buildCellAssembly(c, ModCellItems.FLUID_CELL_SINGULARITY.get(), ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get(), ModItems.COSMIC_STRING_COMPONENT_MATRIX.get());
    }

    private void buildCellAssembly(RecipeOutput c, ItemLike cell, ItemLike housing, ItemLike component) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, cell)
                .requires(housing).requires(component)
                .unlockedBy("has_housing", has(housing)).save(c);
    }

    // ========================================================================================
    // --- 9. MÁQUINAS E ESTRUTURAS (CRAFTING TABLE) ---
    // ========================================================================================
    private void buildMachineAndStorageRecipes(RecipeOutput c) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.GRAVITON_PLATED_CASING.get())
                .pattern("IDI").pattern("OMO").pattern("IDI")
                .define('I', Items.IRON_BLOCK).define('D', ModItems.DIMENSIONAL_PROCESSOR.get())
                .define('M', ModItems.OBSIDIAN_MATRIX.get()).define('O', Items.OBSIDIAN)
                .unlockedBy("has_processor", has(ModItems.DIMENSIONAL_PROCESSOR.get())).save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.QUANTUM_LATTICE_FRAME.get())
                .pattern("GDG").pattern("DQD").pattern("GDG")
                .define('G', AEBlocks.QUARTZ_VIBRANT_GLASS).define('D', ModItems.DIMENSIONAL_PROCESSOR.get())
                .define('Q', AEParts.QUARTZ_FIBER)
                .unlockedBy("has_processor", has(ModItems.DIMENSIONAL_PROCESSOR.get())).save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER.get())
                .pattern("OPO").pattern("CMC").pattern("OEO")
                .define('O', ModItems.OBSIDIAN_MATRIX.get()).define('P', ModItems.DIMENSIONAL_PROCESSOR.get())
                .define('C', ModBlocks.GRAVITON_PLATED_CASING.get()).define('M', AEBlocks.CONTROLLER)
                .define('E', Items.ENDER_EYE)
                .unlockedBy("has_controller", has(AEBlocks.CONTROLLER)).save(c);

        // Storage Blocks
        buildStorageBlock(c, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1B).get(), ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get(), "storage_1b");
        buildStorageBlock(c, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_50B).get(), ModItems.HYPER_DENSE_COMPONENT_MATRIX.get(), "storage_50b");
        buildStorageBlock(c, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1T).get(), ModItems.TESSERACT_COMPONENT_MATRIX.get(), "storage_1t");
        buildStorageBlock(c, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_250T).get(), ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get(), "storage_250t");
        buildStorageBlock(c, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1QD).get(), ModItems.COSMIC_STRING_COMPONENT_MATRIX.get(), "storage_1qd");

        // Co-Processors
        buildCoProcessor(c, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_50M).get(), ModBlocks.QUANTUM_LATTICE_FRAME.get(), "coprocessor_50m", true);
        buildCoProcessor(c, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_150M).get(), ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_50M).get(), "coprocessor_150m", false);
        buildCoProcessor(c, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_300M).get(), ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_150M).get(), "coprocessor_300m", false);
        buildCoProcessor(c, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_750M).get(), ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_300M).get(), "coprocessor_750m", false);
        buildCoProcessor(c, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_2B).get(), ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_750M).get(), "coprocessor_2b", false);
    }

    private void buildStorageBlock(RecipeOutput c, ItemLike output, ItemLike component, String name) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, output)
                .requires(ModBlocks.GRAVITON_PLATED_CASING.get()).requires(component)
                .unlockedBy("has_component", has(component)).save(c, UfoMod.id(name));
    }

    private void buildCoProcessor(RecipeOutput c, ItemLike output, ItemLike previous, String name, boolean isFirst) {
        if (isFirst) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output)
                    .pattern("PEP").pattern("EFE").pattern("PEP")
                    .define('F', previous).define('P', ModItems.DIMENSIONAL_PROCESSOR.get()).define('E', AEItems.ENGINEERING_PROCESSOR)
                    .unlockedBy("has_frame", has(previous)).save(c, UfoMod.id(name));
        } else {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, output)
                    .requires(previous).requires(ModItems.DIMENSIONAL_PROCESSOR.get()).requires(AEItems.ENGINEERING_PROCESSOR)
                    .unlockedBy("has_prev", has(previous)).save(c, UfoMod.id(name));
        }
    }

    // ========================================================================================
    // --- 0. GERAÇÃO DE INGOTS (DMA) ---
    // ========================================================================================
    private void buildIngotGenerators(RecipeOutput c) {
        DMARecipeBuilder.create("dma/ingot/white_dwarf_fragment")
                .output(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get())
                .inputItem(Items.NETHERITE_INGOT, 2)
                .inputItem(Items.BLUE_ICE, 4)
                .inputItem(AEBlocks.SKY_STONE_BLOCK, 4)
                .inputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 250)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 250)
                .energy(100_000).time(200).save(c);

        DMARecipeBuilder.create("dma/ingot/neutron_star_fragment")
                .output(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get())
                .inputItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get(), 4)
                .inputItem(Items.NETHER_STAR)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get())
                .inputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 500).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(500_000).time(400).save(c);

        DMARecipeBuilder.create("dma/ingot/pulsar_fragment")
                .output(ModItems.PULSAR_FRAGMENT_INGOT.get())
                .inputItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get(), 2)
                .inputItem(Items.LODESTONE, 2).inputItem(Items.LIGHTNING_ROD, 4)
                .inputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 2000)
                .energy(1_000_000).time(600).save(c);
    }

    // ========================================================================================
    // --- HELPER METHODS (NOVA LÓGICA) ---
    // ========================================================================================

    // Helper para criar Componentes de Armazenamento (2x Prev + 4x Matéria)
    private void createTieredComponent(RecipeOutput c, ItemLike output, ItemLike prevTier, int prevCount,
                                       ItemLike rareItem, int rareCount,
                                       Supplier<? extends Fluid> fluid, int fluidAmount,
                                       int energy, int time) {
        DMARecipeBuilder.create("dma/component/" + BuiltInRegistries.ITEM.getKey(output.asItem()).getPath())
                .output(output)
                .inputItem(prevTier, prevCount)
                .inputItem(rareItem, rareCount)
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), 1)
                .inputFluid(fluid.get(), fluidAmount)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), fluidAmount / 2)
                .energy(energy).time(time).save(c);
    }

    // Helper para Células Infinitas com suporte a ModLoadedCondition
    private void createInfinityCellRecipe(RecipeOutput c, ItemLike cellItem, ItemLike targetItem, String modIdCondition) {
        var builder = DMARecipeBuilder.create("dma/infinity_cell/" + BuiltInRegistries.ITEM.getKey(cellItem.asItem()).getPath())
                .output(cellItem)
                .inputItem(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())
                .inputItem(ModItems.QUANTUM_ANOMALY.get())
                .inputItem(targetItem, 64)
                .inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 2500)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 5000)
                .energy(250_000_000)
                .time(10_000);

        if (modIdCondition != null && !modIdCondition.isEmpty()) {
            // Salva com a condição de que o mod exista
            builder.save(c.withConditions(new ModLoadedCondition(modIdCondition)));
        } else {
            builder.save(c);
        }
    }
}