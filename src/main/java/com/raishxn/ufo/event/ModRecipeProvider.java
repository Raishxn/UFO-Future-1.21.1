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
import com.glodblock.github.glodium.util.GlodUtil;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismFluids;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.datagen.DMARecipeBuilder;
import com.raishxn.ufo.fluid.ModFluids;
import com.raishxn.ufo.item.ModCellItems;
import com.raishxn.ufo.item.ModItems;

import java.util.concurrent.CompletableFuture;

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

        // 3. Matrizes e Componentes de Armazenamento (DMA)
        buildComponentsDMA(c);

        // 4. Matérias, Fluidos e Itens Intermediários (DMA)
        buildMaterialsAndFluidsDMA(c);

        // 5. Catalisadores (DMA)
        buildCatalystsDMA(c);

        // 6. Armaduras (UFO e Thermal) (DMA)
        buildArmorsDMA(c);

        buildIngotGenerators(c);

        // 1. Vanilla Crafting
        buildBasicConversions(c);

        // 7. Células Infinitas (DMA Hardcore)
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


        // Helper para criar receitas de bloco/ingot/nugget
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
        // Dimensional Processor Press (Molde)
        DMARecipeBuilder.create("dma/dimensional_processor_press")
                .output(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get())
                .inputItem(Items.IRON_BLOCK, 2)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 500)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 250)
                .energy(100_000).time(200).save(c);

        // Printed Dimensional Processor
        DMARecipeBuilder.create("dma/printed_dimensional_processor")
                .output(ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get())
                .inputItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get())
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 100)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 50)
                .energy(50_000).time(100).save(c);

        // Dimensional Processor Final
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
    // --- 3. COMPONENTES E MATRIZES (DMA) ---
    // ========================================================================================
    private void buildComponentsDMA(RecipeOutput c) {
        // Tier 1: Phase Shift
        DMARecipeBuilder.create("dma/component/phase_shift")
                .output(ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get())
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), 2)
                .inputItem(AEItems.CELL_COMPONENT_256K)
                .inputItem(AEBlocks.QUARTZ_VIBRANT_GLASS, 4)
                .inputItem(Items.NETHER_STAR)
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 1000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 500)
                .energy(500_000).time(400).save(c);

        // Tier 2: Hyper Dense
        DMARecipeBuilder.create("dma/component/hyper_dense")
                .output(ModItems.HYPER_DENSE_COMPONENT_MATRIX.get())
                .inputItem(ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get())
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), 4)
                .inputItem(AEItems.ENDER_DUST, 8)
                .inputFluid(ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID.get(), 1000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(2_000_000).time(800).save(c);

        // Tier 3: Tesseract
        DMARecipeBuilder.create("dma/component/tesseract")
                .output(ModItems.TESSERACT_COMPONENT_MATRIX.get())
                .inputItem(ModItems.HYPER_DENSE_COMPONENT_MATRIX.get())
                .inputItem(ModItems.QUANTUM_ANOMALY.get())
                .inputFluid(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 2000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 2000)
                .energy(8_000_000).time(1600).save(c);

        // Tier 4: Event Horizon
        DMARecipeBuilder.create("dma/component/event_horizon")
                .output(ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get())
                .inputItem(ModItems.TESSERACT_COMPONENT_MATRIX.get())
                .inputItem(AEItems.SINGULARITY)
                .inputFluid(ModFluids.SOURCE_SPATIAL_FLUID.get(), 4000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 4000)
                .energy(32_000_000).time(3200).save(c);

        // Tier 5: Cosmic String (GOD TIER)
        DMARecipeBuilder.create("dma/component/cosmic_string")
                .output(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())
                .inputItem(ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get())
                .inputItem(ModItems.NUCLEAR_STAR.get())
                .inputItem(ModItems.UNSTABLE_WHITE_HOLE_MATTER.get())
                .inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 5000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 10000)
                .energy(100_000_000).time(6000).save(c);
    }

    // ========================================================================================
    // --- 4. MATÉRIAS, FLUIDOS E INTERMEDIÁRIOS (DMA) ---
    // ========================================================================================
    private void buildMaterialsAndFluidsDMA(RecipeOutput c) {

        // --- Metais Sintéticos (Dusts/Rods) ---

        // White Dwarf Dust
        DMARecipeBuilder.create("dma/white_dwarf_dust")
                .output(ModItems.WHITE_DWARF_FRAGMENT_DUST.get())
                .inputItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 20)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 20)
                .energy(10_000).time(60).save(c);

        // Neutron Star Dust
        DMARecipeBuilder.create("dma/neutron_star_dust")
                .output(ModItems.NEUTRON_STAR_FRAGMENT_DUST.get())
                .inputItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 20)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 20)
                .energy(15_000).time(80).save(c);

        // Pulsar Dust
        DMARecipeBuilder.create("dma/pulsar_dust")
                .output(ModItems.PULSAR_FRAGMENT_DUST.get())
                .inputItem(ModItems.PULSAR_FRAGMENT_INGOT.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 20)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 20)
                .energy(20_000).time(100).save(c);

        // Thermal Dusts
        DMARecipeBuilder.create("dma/dust_blizz")
                .output(ModItems.DUST_BLIZZ.get(), 4)
                .inputItem(Items.SNOWBALL, 4)
                .inputItem(Items.REDSTONE)
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 50)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 25)
                .energy(20_000).time(80).save(c);

        DMARecipeBuilder.create("dma/dust_cryotheum")
                .output(ModItems.DUST_CRYOTHEUM.get(), 2)
                .inputItem(ModItems.DUST_BLIZZ.get(), 2)
                .inputItem(Items.REDSTONE, 2)
                .inputItem(Items.SNOWBALL)
                .inputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 100)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 50)
                .energy(40_000).time(100).save(c);

        // Rods
        DMARecipeBuilder.create("dma/white_dwarf_rod")
                .output(ModItems.WHITE_DWARF_FRAGMENT_ROD.get(), 2)
                .inputItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 50)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 20)
                .energy(10_000).time(60).save(c);

        DMARecipeBuilder.create("dma/neutron_star_rod")
                .output(ModItems.NEUTRON_STAR_FRAGMENT_ROD.get(), 2)
                .inputItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 50)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 20)
                .energy(20_000).time(80).save(c);

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

        // Unstable White Hole Matter
        DMARecipeBuilder.create("dma/unstable_white_hole_matter")
                .output(ModItems.UNSTABLE_WHITE_HOLE_MATTER.get())
                .inputItem(ModItems.WHITE_DWARF_MATTER.get(), 4)
                .inputItem(ModItems.QUANTUM_ANOMALY.get())
                .inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 2000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 8000)
                .energy(50_000_000).time(4000).save(c);

        // Safe Containment Matter
        DMARecipeBuilder.create("dma/safe_containment_matter")
                .output(ModItems.SAFE_CONTAINMENT_MATTER.get())
                .inputItem(ModItems.OBSIDIAN_MATRIX.get(), 4)
                .inputItem(Items.NETHERITE_INGOT)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 500)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 200)
                .energy(100_000).time(200).save(c);

        // Aether Containment Capsule
        DMARecipeBuilder.create("dma/aether_containment_capsule")
                .output(ModItems.AETHER_CONTAINMENT_CAPSULE.get())
                .inputItem(ModItems.SAFE_CONTAINMENT_MATTER.get(), 2)
                .inputItem(ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get())
                .inputFluid(ModFluids.SOURCE_SPATIAL_FLUID.get(), 1000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 500)
                .energy(500_000).time(400).save(c);

        // Scrap
        DMARecipeBuilder.create("dma/scrap_from_matrix")
                .output(ModItems.SCRAP.get(), 4).output(AEItems.MATTER_BALL.get(), 1, 0.10f)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get())
                .inputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 10).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 10)
                .energy(100_000).time(200).save(c);

        // Scrap Box
        DMARecipeBuilder.create("dma/scrap_box")
                .output(ModItems.SCRAP_BOX.get()).output(ModItems.SCAR.get(), 1, 0.03f)
                .inputItem(ModItems.SCRAP.get(), 9)
                .inputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 100).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 100)
                .energy(10_000_000).time(2000).save(c);

        // Fluidos
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

        // Matters e Esferas
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

        // Staff
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

        // Fluidos Avançados
        DMARecipeBuilder.create("dma/temporal_fluid")
                .outputFluid(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 5000, 1.0f)
                .inputItem(ModItems.QUANTUM_ANOMALY.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 2000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 500)
                .energy(2_000_000).time(400).save(c);

        DMARecipeBuilder.create("dma/spatial_fluid")
                .outputFluid(ModFluids.SOURCE_SPATIAL_FLUID.get(), 10000, 1.0f)
                .inputItem(ModItems.NUCLEAR_STAR.get())
                .inputFluid(ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID.get(), 5000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(6_000_000).time(1200).save(c);

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

        // Matter Tiers
        DMARecipeBuilder.create("dma/corporeal_matter")
                .output(ModItems.CORPOREAL_MATTER.get()).output(ModItems.SCAR.get(), 1, 0.08f)
                .inputItem(ModItems.PROTO_MATTER.get()).inputItem(Items.IRON_BLOCK, 64)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 1000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(5_000_000).time(1200).save(c);

        DMARecipeBuilder.create("dma/uu_matter_crystal")
                .output(ModItems.UU_MATTER_CRYSTAL.get())
                .inputItem(Items.AMETHYST_SHARD) // CORREÇÃO: Adicionado item input obrigatório (Catalisador)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 10000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 2000)
                .energy(20_000_000).time(4000).save(c);

        DMARecipeBuilder.create("dma/white_dwarf_matter")
                .output(ModItems.WHITE_DWARF_MATTER.get()).output(ModItems.SCAR.get(), 1, 0.10f)
                .inputItem(ModItems.CORPOREAL_MATTER.get()).inputItem(ModItems.UU_MATTER_CRYSTAL.get()).inputItem(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK.get())
                .inputFluid(ModFluids.SOURCE_WHITE_DWARF_FRAGMENT_FLUID.get(), 1000).inputCoolant(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 1000)
                .energy(7_000_000).time(1500).save(c);

        DMARecipeBuilder.create("dma/neutron_star_matter")
                .output(ModItems.NEUTRON_STAR_MATTER.get()).output(ModItems.SCAR.get(), 1, 0.10f)
                .inputItem(ModItems.CORPOREAL_MATTER.get()).inputItem(ModItems.UU_MATTER_CRYSTAL.get()).inputItem(ModBlocks.NEUTRON_STAR_FRAGMENT_BLOCK.get())
                .inputFluid(ModFluids.SOURCE_NEUTRON_STAR_FRAGMENT_FLUID.get(), 1000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(9_500_000).time(1850).save(c);

        DMARecipeBuilder.create("dma/pulsar_matter")
                .output(ModItems.PULSAR_MATTER.get()).output(ModItems.SCAR.get(), 1, 0.10f)
                .inputItem(ModItems.CORPOREAL_MATTER.get()).inputItem(ModItems.UU_MATTER_CRYSTAL.get()).inputItem(ModBlocks.PULSAR_FRAGMENT_BLOCK.get())
                .inputFluid(ModFluids.SOURCE_PULSAR_FRAGMENT_FLUID.get(), 1000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1500)
                .energy(12_000_000).time(2200).save(c);

        DMARecipeBuilder.create("dma/dark_matter")
                .output(ModItems.DARK_MATTER.get())
                .inputItem(ModItems.WHITE_DWARF_MATTER.get(), 64).inputItem(ModItems.NEUTRON_STAR_MATTER.get(), 64).inputItem(ModItems.PULSAR_MATTER.get(), 64)
                .inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 10000).inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 10000)
                .energy(200_000_000).time(20_000).save(c);
    }

    // ========================================================================================
    // --- 5. CATALISADORES (DMA) ---
    // ========================================================================================
    private void buildCatalystsDMA(RecipeOutput c) {
        // Helper para Matterflow
        createCatalystRecipe(c, ModItems.MATTERFLOW_CATALYST_T1, ModItems.PROTO_MATTER, ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID, 100_000);
        createCatalystRecipe(c, ModItems.MATTERFLOW_CATALYST_T2, ModItems.CORPOREAL_MATTER, ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID, 500_000);
        createCatalystRecipe(c, ModItems.MATTERFLOW_CATALYST_T3, ModItems.NEUTRON_STAR_MATTER, ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID, 2_000_000);

        // Helper para Chrono
        createCatalystRecipe(c, ModItems.CHRONO_CATALYST_T1, ModItems.PROTO_MATTER, ModFluids.SOURCE_TEMPORAL_FLUID, 150_000);
        createCatalystRecipe(c, ModItems.CHRONO_CATALYST_T2, ModItems.CORPOREAL_MATTER, ModFluids.SOURCE_TEMPORAL_FLUID, 600_000);
        createCatalystRecipe(c, ModItems.CHRONO_CATALYST_T3, ModItems.PULSAR_MATTER, ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID, 2_500_000);

        // Helper para Overflux
        createCatalystRecipe(c, ModItems.OVERFLUX_CATALYST_T1, ModItems.PROTO_MATTER, ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID, 150_000);
        createCatalystRecipe(c, ModItems.OVERFLUX_CATALYST_T2, ModItems.CORPOREAL_MATTER, ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID, 600_000);
        createCatalystRecipe(c, ModItems.OVERFLUX_CATALYST_T3, ModItems.WHITE_DWARF_MATTER, ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID, 2_500_000);

        // Helper para Quantum
        createCatalystRecipe(c, ModItems.QUANTUM_CATALYST_T1, ModItems.PROTO_MATTER, ModFluids.SOURCE_SPATIAL_FLUID, 200_000);
        createCatalystRecipe(c, ModItems.QUANTUM_CATALYST_T2, ModItems.CORPOREAL_MATTER, ModFluids.SOURCE_SPATIAL_FLUID, 800_000);
        createCatalystRecipe(c, ModItems.QUANTUM_CATALYST_T3, ModItems.DARK_MATTER, ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID, 5_000_000);

        // Dimensional Catalyst
        DMARecipeBuilder.create("dma/dimensional_catalyst")
                .output(ModItems.DIMENSIONAL_CATALYST.get())
                .inputItem(ModItems.MATTERFLOW_CATALYST_T3.get())
                .inputItem(ModItems.CHRONO_CATALYST_T3.get())
                .inputItem(ModItems.OVERFLUX_CATALYST_T3.get())
                .inputItem(ModItems.QUANTUM_CATALYST_T3.get())
                .inputItem(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())
                .inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 10000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 5000)
                .energy(500_000_000).time(10000).save(c);
    }

    private void createCatalystRecipe(RecipeOutput c, java.util.function.Supplier<net.minecraft.world.item.Item> output, java.util.function.Supplier<net.minecraft.world.item.Item> coreItem, java.util.function.Supplier<? extends net.minecraft.world.level.material.Fluid> fluid, int energy) {
        DMARecipeBuilder.create("dma/" + BuiltInRegistries.ITEM.getKey(output.get()).getPath())
                .output(output.get())
                .inputItem(coreItem.get())
                .inputItem(ModItems.BISMUTH.get(), 4)
                .inputFluid(fluid.get(), 1000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 500)
                .energy(energy)
                .time(300)
                .save(c);
    }

    // ========================================================================================
    // --- 6. ARMADURAS (DMA) ---
    // ========================================================================================
    private void buildArmorsDMA(RecipeOutput c) {
        // Thermal Resistor Plating
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

        // UFO Armor Set
        createUfoArmorDMA(c, ModItems.UFO_HELMET, ModItems.THERMAL_RESISTOR_MASK, ModItems.ENRICHED_NEUTRONIUM_SPHERE);
        createUfoArmorDMA(c, ModItems.UFO_CHESTPLATE, ModItems.THERMAL_RESISTOR_CHEST, ModItems.ENRICHED_NEUTRONIUM_SPHERE);
        createUfoArmorDMA(c, ModItems.UFO_LEGGINGS, ModItems.THERMAL_RESISTOR_PANTS, ModItems.ENRICHED_NEUTRONIUM_SPHERE);
        createUfoArmorDMA(c, ModItems.UFO_BOOTS, ModItems.THERMAL_RESISTOR_BOOTS, ModItems.ENRICHED_NEUTRONIUM_SPHERE);
    }

    private void createArmorDMA(RecipeOutput c, java.util.function.Supplier<net.minecraft.world.item.Item> output, java.util.function.Supplier<net.minecraft.world.item.Item> material, int amount, int energy) {
        DMARecipeBuilder.create("dma/" + BuiltInRegistries.ITEM.getKey(output.get()).getPath())
                .output(output.get())
                .inputItem(material.get(), amount)
                .inputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000) // CORREÇÃO: Adicionado Fluid Input obrigatório
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(energy).time(500).save(c);
    }

    private void createUfoArmorDMA(RecipeOutput c, java.util.function.Supplier<net.minecraft.world.item.Item> output, java.util.function.Supplier<net.minecraft.world.item.Item> baseArmor, java.util.function.Supplier<net.minecraft.world.item.Item> rareItem) {
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
    // --- 7. CÉLULAS INFINITAS (DMA) ---
    // ========================================================================================
    private void buildInfinityCellsDMA(RecipeOutput c) {
        // Vanilla
        createInfinityDMA(c, ModItems.INFINITY_WATER_CELL.get(), Items.WATER_BUCKET);
        createInfinityDMA(c, ModItems.INFINITY_COBBLESTONE_CELL.get(), Items.COBBLESTONE);
        createInfinityDMA(c, ModItems.INFINITY_COBBLED_DEEPSLATE_CELL.get(), Items.COBBLED_DEEPSLATE);
        createInfinityDMA(c, ModItems.INFINITY_END_STONE_CELL.get(), Items.END_STONE);
        createInfinityDMA(c, ModItems.INFINITY_NETHERRACK_CELL.get(), Items.NETHERRACK);
        createInfinityDMA(c, ModItems.INFINITY_SAND_CELL.get(), Items.SAND);
        createInfinityDMA(c, ModItems.INFINITY_LAVA_CELL.get(), Items.LAVA_BUCKET);
        createInfinityDMA(c, ModItems.INFINITY_OBSIDIAN_CELL.get(), Items.OBSIDIAN);
        createInfinityDMA(c, ModItems.INFINITY_GRAVEL_CELL.get(), Items.GRAVEL);
        createInfinityDMA(c, ModItems.INFINITY_OAK_LOG_CELL.get(), Items.OAK_LOG);
        createInfinityDMA(c, ModItems.INFINITY_GLASS_CELL.get(), Items.GLASS);
        createInfinityDMA(c, ModItems.INFINITY_AMETHYST_SHARD_CELL.get(), Items.AMETHYST_SHARD);

        // Mekanism
        createInfinityDMA(c, ModItems.INFINITY_ANTIMATTER_PELLET_CELL.get(), MekanismItems.ANTIMATTER_PELLET.get());
        createInfinityDMA(c, ModItems.INFINITY_PLUTONIUM_PELLET_CELL.get(), MekanismItems.PLUTONIUM_PELLET.get());
        createInfinityDMA(c, ModItems.INFINITY_POLONIUM_PELLET_CELL.get(), MekanismItems.POLONIUM_PELLET.get());
        createInfinityDMA(c, ModItems.INFINITY_HDPE_PELLET_CELL.get(), MekanismItems.HDPE_PELLET.get());

        // AE2
        createInfinityDMA(c, ModItems.INFINITY_SKY_STONE_CELL.get(), AEBlocks.SKY_STONE_BLOCK);

        // Dyes
        createInfinityDMA(c, ModItems.INFINITY_WHITE_DYE_CELL.get(), Items.WHITE_DYE);
        createInfinityDMA(c, ModItems.INFINITY_ORANGE_DYE_CELL.get(), Items.ORANGE_DYE);
        createInfinityDMA(c, ModItems.INFINITY_MAGENTA_DYE_CELL.get(), Items.MAGENTA_DYE);
        createInfinityDMA(c, ModItems.INFINITY_LIGHT_BLUE_DYE_CELL.get(), Items.LIGHT_BLUE_DYE);
        createInfinityDMA(c, ModItems.INFINITY_YELLOW_DYE_CELL.get(), Items.YELLOW_DYE);
        createInfinityDMA(c, ModItems.INFINITY_LIME_DYE_CELL.get(), Items.LIME_DYE);
        createInfinityDMA(c, ModItems.INFINITY_PINK_DYE_CELL.get(), Items.PINK_DYE);
        createInfinityDMA(c, ModItems.INFINITY_GRAY_DYE_CELL.get(), Items.GRAY_DYE);
        createInfinityDMA(c, ModItems.INFINITY_LIGHT_GRAY_DYE_CELL.get(), Items.LIGHT_GRAY_DYE);
        createInfinityDMA(c, ModItems.INFINITY_CYAN_DYE_CELL.get(), Items.CYAN_DYE);
        createInfinityDMA(c, ModItems.INFINITY_PURPLE_DYE_CELL.get(), Items.PURPLE_DYE);
        createInfinityDMA(c, ModItems.INFINITY_BLUE_DYE_CELL.get(), Items.BLUE_DYE);
        createInfinityDMA(c, ModItems.INFINITY_BROWN_DYE_CELL.get(), Items.BROWN_DYE);
        createInfinityDMA(c, ModItems.INFINITY_GREEN_DYE_CELL.get(), Items.GREEN_DYE);
        createInfinityDMA(c, ModItems.INFINITY_RED_DYE_CELL.get(), Items.RED_DYE);
        createInfinityDMA(c, ModItems.INFINITY_BLACK_DYE_CELL.get(), Items.BLACK_DYE);
    }

    private void createInfinityDMA(RecipeOutput c, ItemLike resultCell, ItemLike targetItem) {
        DMARecipeBuilder.create("dma/infinity_cell/" + BuiltInRegistries.ITEM.getKey(resultCell.asItem()).getPath())
                .output(resultCell)
                .inputItem(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())
                .inputItem(ModItems.QUANTUM_ANOMALY.get())
                .inputItem(targetItem, 64)
                .inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 2000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 5000)
                .energy(250_000_000).time(10_000).save(c);
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

        // Cell Assembly
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
        // Graviton Casing
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.GRAVITON_PLATED_CASING.get())
                .pattern("IDI").pattern("OMO").pattern("IDI")
                .define('I', Items.IRON_BLOCK).define('D', ModItems.DIMENSIONAL_PROCESSOR.get())
                .define('M', ModItems.OBSIDIAN_MATRIX.get()).define('O', Items.OBSIDIAN)
                .unlockedBy("has_processor", has(ModItems.DIMENSIONAL_PROCESSOR.get())).save(c);

        // Quantum Lattice
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.QUANTUM_LATTICE_FRAME.get())
                .pattern("GDG").pattern("DQD").pattern("GDG")
                .define('G', AEBlocks.QUARTZ_VIBRANT_GLASS).define('D', ModItems.DIMENSIONAL_PROCESSOR.get())
                .define('Q', AEParts.QUARTZ_FIBER)
                .unlockedBy("has_processor", has(ModItems.DIMENSIONAL_PROCESSOR.get())).save(c);

        // DMA (MÁQUINA)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER.get())
                .pattern("OPO").pattern("CMC").pattern("OEO")
                .define('O', ModItems.OBSIDIAN_MATRIX.get()).define('P', ModItems.DIMENSIONAL_PROCESSOR.get())
                .define('C', ModBlocks.GRAVITON_PLATED_CASING.get()).define('M', AEBlocks.CONTROLLER)
                .define('E', Items.ENDER_EYE)
                .unlockedBy("has_controller", has(AEBlocks.CONTROLLER)).save(c);

        // Storage Blocks
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1B).get())
                .requires(ModBlocks.GRAVITON_PLATED_CASING.get()).requires(ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get())
                .unlockedBy("has_component", has(ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get())).save(c, UfoMod.id("storage_1b"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_50B).get())
                .requires(ModBlocks.GRAVITON_PLATED_CASING.get()).requires(ModItems.HYPER_DENSE_COMPONENT_MATRIX.get())
                .unlockedBy("has_component", has(ModItems.HYPER_DENSE_COMPONENT_MATRIX.get())).save(c, UfoMod.id("storage_50b"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1T).get())
                .requires(ModBlocks.GRAVITON_PLATED_CASING.get()).requires(ModItems.TESSERACT_COMPONENT_MATRIX.get())
                .unlockedBy("has_component", has(ModItems.TESSERACT_COMPONENT_MATRIX.get())).save(c, UfoMod.id("storage_1t"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_250T).get())
                .requires(ModBlocks.GRAVITON_PLATED_CASING.get()).requires(ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get())
                .unlockedBy("has_component", has(ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get())).save(c, UfoMod.id("storage_250t"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1QD).get())
                .requires(ModBlocks.GRAVITON_PLATED_CASING.get()).requires(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())
                .unlockedBy("has_component", has(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())).save(c, UfoMod.id("storage_1qd"));

        // Co-Processors
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_50M).get())
                .pattern("PEP").pattern("EFE").pattern("PEP")
                .define('F', ModBlocks.QUANTUM_LATTICE_FRAME.get()).define('P', ModItems.DIMENSIONAL_PROCESSOR.get()).define('E', AEItems.ENGINEERING_PROCESSOR)
                .unlockedBy("has_frame", has(ModBlocks.QUANTUM_LATTICE_FRAME.get())).save(c, UfoMod.id("coprocessor_50m"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_150M).get())
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_50M).get())
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get()).requires(AEItems.ENGINEERING_PROCESSOR)
                .unlockedBy("has_prev", has(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_50M).get())).save(c, UfoMod.id("coprocessor_150m"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_300M).get())
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_150M).get())
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get()).requires(AEItems.ENGINEERING_PROCESSOR)
                .unlockedBy("has_prev", has(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_150M).get())).save(c, UfoMod.id("coprocessor_300m"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_750M).get())
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_300M).get())
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get()).requires(AEItems.ENGINEERING_PROCESSOR)
                .unlockedBy("has_prev", has(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_300M).get())).save(c, UfoMod.id("coprocessor_750m"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_2B).get())
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_750M).get())
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get()).requires(AEItems.ENGINEERING_PROCESSOR)
                .unlockedBy("has_prev", has(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_750M).get())).save(c, UfoMod.id("coprocessor_2b"));
    }
    // ========================================================================================
    // --- 0. GERAÇÃO DE INGOTS (DMA) ---
    // ========================================================================================
    private void buildIngotGenerators(RecipeOutput c) {
        // 1. White Dwarf Fragment Ingot (A Estrela "Fria")
        // Lógica: Comprimir Netherite (densidade) com Gelo Azul (frio) e Sky Stone (origem espacial)
        DMARecipeBuilder.create("dma/ingot/white_dwarf_fragment")
                .output(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get())
                .inputItem(Items.NETHERITE_INGOT, 2)
                .inputItem(Items.BLUE_ICE, 4)
                .inputItem(AEBlocks.SKY_STONE_BLOCK, 4)
                .inputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 250) // Atua como "meio" frio
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 250) // Resfriamento
                .energy(100_000).time(200).save(c);

        // 2. Neutron Star Fragment Ingot (A Estrela de Nêutrons)
        // Lógica: Comprimir White Dwarfs ao extremo até colapsar, catalisado por uma Nether Star
        DMARecipeBuilder.create("dma/ingot/neutron_star_fragment")
                .output(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get())
                .inputItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get(), 4) // Progressão: 4 WD -> 1 NS
                .inputItem(Items.NETHER_STAR)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get())
                .inputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 500)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000) // Exige mais resfriamento
                .energy(500_000).time(400).save(c);

        // 3. Pulsar Fragment Ingot (A Estrela Magnética Rotativa)
        // Lógica: Energizar uma Neutron Star com magnetismo (Lodestone) e condutividade (Lightning Rod)
        DMARecipeBuilder.create("dma/ingot/pulsar_fragment")
                .output(ModItems.PULSAR_FRAGMENT_INGOT.get())
                .inputItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get(), 2) // Progressão: 2 NS -> 1 Pulsar
                .inputItem(Items.LODESTONE, 2) // Magnetismo forte
                .inputItem(Items.LIGHTNING_ROD, 4) // Energia/Pulsos
                .inputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 2000)
                .energy(1_000_000).time(600).save(c);
    }
}