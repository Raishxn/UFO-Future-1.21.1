package com.raishxn.ufo.datagen;

import appeng.api.ids.AEItemIds;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.items.AEBaseItem;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.core.MegaCoProcessorTier;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import com.raishxn.ufo.fluid.ModFluids;
import com.raishxn.ufo.item.ModCellItems;
import com.raishxn.ufo.item.ModItems;
import com.raishxn.ufo.item.ModArmor;
import com.raishxn.ufo.item.ModTools;
import com.raishxn.ufo.item.ModCells;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import com.raishxn.ufo.datagen.StellarSimulationRecipeBuilder;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput c) {
        this.buildBasicConversions(c);
        this.buildProcessorsDMA(c);
        this.buildComponentsDMA(c);
        this.buildMaterialsAndFluidsDMA(c);
        this.buildCatalystsDMA(c);
        this.buildArmorsDMA(c);
        this.buildIngotGenerators(c);
        this.buildInfinityCellsDMA(c);
        this.buildHousingRecipes(c);
        this.buildMachineAndStorageRecipes(c);
        this.buildQuantumMultiblockRecipes(c);
        this.buildMassiveStellarSimulations(c);
    }

    private void buildBasicConversions(RecipeOutput c) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.OBSIDIAN_MATRIX.get())
                .pattern("OOO")
                .pattern("OEO")
                .pattern("OOO")
                .define('O', Items.OBSIDIAN)
                .define('E', Items.ENDER_EYE)
                .unlockedBy("has_obsidian", has(Items.OBSIDIAN))
                .save(c, UfoMod.id("obsidian_matrix_vanilla"));

        this.createMetalRecipes(c, ModItems.WHITE_DWARF_FRAGMENT_INGOT.get(), ModItems.WHITE_DWARF_FRAGMENT_NUGGET.get(), ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK.get());
        this.createMetalRecipes(c, ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get(), ModItems.NEUTRON_STAR_FRAGMENT_NUGGET.get(), ModBlocks.NEUTRON_STAR_FRAGMENT_BLOCK.get());
        this.createMetalRecipes(c, ModItems.PULSAR_FRAGMENT_INGOT.get(), ModItems.PULSAR_FRAGMENT_NUGGET.get(), ModBlocks.PULSAR_FRAGMENT_BLOCK.get());
    }

    private void createMetalRecipes(RecipeOutput c, ItemLike ingot, ItemLike nugget, ItemLike block) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, block)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ingot)
                .unlockedBy("has_ingot", has(ingot))
                .save(c);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ingot, 9)
                .requires(block)
                .unlockedBy("has_block", has(block))
                .save(c, UfoMod.id(BuiltInRegistries.ITEM.getKey(ingot.asItem()).getPath() + "_from_block"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ingot)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', nugget)
                .unlockedBy("has_nugget", has(nugget))
                .save(c, UfoMod.id(BuiltInRegistries.ITEM.getKey(ingot.asItem()).getPath() + "_from_nugget"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, nugget, 9)
                .requires(ingot)
                .unlockedBy("has_ingot", has(ingot))
                .save(c);
    }

    private void buildProcessorsDMA(RecipeOutput c) {
        // Substituído Hydrogen por Water para remover Mekanism
        DMARecipeBuilder.create("dma/cryotheum_dust_bootstrap")
                .output(ModItems.DUST_CRYOTHEUM.get(), 2)
                .inputFluid(Fluids.WATER, 1000)
                .inputItem(ModItems.DUST_BLIZZ.get(), 2)
                .inputItem(Items.SNOWBALL)
                .energy(50000).time(80)
                .save(c);

        DMARecipeBuilder.create("dma/dust_blizz_bootstrap")
                .output(ModItems.DUST_BLIZZ.get(), 4)
                .inputFluid(Fluids.WATER, 1000)
                .inputItem(Items.SNOWBALL, 8)
                .inputItem(Items.PACKED_ICE, 1)
                .energy(30000).time(60)
                .save(c);

        DMARecipeBuilder.create("dma/gelid_cryotheum_bootstrap")
                .outputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 2000, 1.0F)
                .inputItem(ModItems.DUST_BLIZZ.get(), 4)
                .inputFluid(Fluids.WATER, 1000)
                .energy(200000).time(180)
                .save(c);

        DMARecipeBuilder.create("dma/dimensional_processor_press")
                .output(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get())
                .inputItem(Items.IRON_BLOCK, 2)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 500)
                .energy(100000).time(200)
                .save(c);

        DMARecipeBuilder.create("dma/printed_dimensional_processor")
                .output(ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get())
                .inputItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get())
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 100)
                .energy(50000).time(100)
                .save(c);

        DMARecipeBuilder.create("dma/dimensional_processor")
                .output(ModItems.DIMENSIONAL_PROCESSOR.get())
                .inputItem(ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get())
                .inputItem(AEItems.SILICON_PRINT)
                .inputItem(AEItems.FLUIX_DUST)
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 200)
                .energy(80000).time(150)
                .save(c);
    }

    private void buildComponentsDMA(RecipeOutput c) {
        DMARecipeBuilder.create("dma/component/phase_shift")
                .output(ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get())
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), 8)
                .inputItem(AEItems.CELL_COMPONENT_256K.get(),8)
                .inputItem(AEBlocks.QUARTZ_VIBRANT_GLASS, 8)
                .inputItem(Items.NETHER_STAR,8)
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 1000)
                .energy(500000).time(400)
                .save(c);

        this.createTieredComponent(c, ModItems.HYPER_DENSE_COMPONENT_MATRIX.get(), ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get(), 8, ModItems.WHITE_DWARF_MATTER.get(), 8, ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID, 1000, 2000000, 800);
        this.createTieredComponent(c, ModItems.TESSERACT_COMPONENT_MATRIX.get(), ModItems.HYPER_DENSE_COMPONENT_MATRIX.get(), 8, ModItems.NEUTRON_STAR_MATTER.get(), 8, ModFluids.SOURCE_SPATIAL_FLUID, 2000, 8000000, 1600);
        this.createTieredComponent(c, ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get(), ModItems.TESSERACT_COMPONENT_MATRIX.get(), 8, ModItems.PULSAR_MATTER.get(), 8, ModFluids.SOURCE_SPATIAL_FLUID, 4000, 32000000, 3200);
        this.createTieredComponent(c, ModItems.COSMIC_STRING_COMPONENT_MATRIX.get(), ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get(), 8, ModItems.DARK_MATTER.get(), 8, ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID, 8000, 100000000, 6000);
    }

    private void buildMaterialsAndFluidsDMA(RecipeOutput c) {
        DMARecipeBuilder.create("dma/white_dwarf_dust").output(ModItems.WHITE_DWARF_FRAGMENT_DUST.get()).inputItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get()).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 20).energy(10000).time(60).save(c);
        DMARecipeBuilder.create("dma/white_dwarf_fluid").outputFluid(ModFluids.SOURCE_WHITE_DWARF_FRAGMENT_FLUID.get(), 1000, 1.0F).inputItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get(), 4).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 200).energy(40000).time(120).save(c);
        DMARecipeBuilder.create("dma/neutron_star_dust").output(ModItems.NEUTRON_STAR_FRAGMENT_DUST.get()).inputItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get()).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 20).energy(15000).time(80).save(c);
        DMARecipeBuilder.create("dma/neutron_star_fluid").outputFluid(ModFluids.SOURCE_NEUTRON_STAR_FRAGMENT_FLUID.get(), 1000, 1.0F).inputItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get(), 4).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 200).energy(60000).time(150).save(c);
        DMARecipeBuilder.create("dma/pulsar_dust").output(ModItems.PULSAR_FRAGMENT_DUST.get()).inputItem(ModItems.PULSAR_FRAGMENT_INGOT.get()).inputItem(AEBlocks.TINY_TNT).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 20).energy(20000).time(100).save(c);
        DMARecipeBuilder.create("dma/pulsar_fluid").outputFluid(ModFluids.SOURCE_PULSAR_FRAGMENT_FLUID.get(), 1000, 1.0F).inputItem(ModItems.PULSAR_FRAGMENT_INGOT.get(), 4).inputItem(Items.LIGHTNING_ROD, 4).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 200).energy(80000).time(180).save(c);
        DMARecipeBuilder.create("dma/dust_blizz").output(ModItems.DUST_BLIZZ.get(), 4).inputItem(Items.SNOWBALL, 4).inputItem(Items.REDSTONE).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 50).energy(20000).time(80).save(c);
        DMARecipeBuilder.create("dma/dust_cryotheum").output(ModItems.DUST_CRYOTHEUM.get(), 2).inputItem(ModItems.DUST_BLIZZ.get(), 2).inputItem(Items.REDSTONE, 2).inputItem(Items.SNOWBALL).inputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 100).energy(40000).time(100).save(c);
        
        this.buildRodRecipe(c, ModItems.WHITE_DWARF_FRAGMENT_ROD, ModItems.WHITE_DWARF_FRAGMENT_INGOT, 10000, 60);
        this.buildRodRecipe(c, ModItems.NEUTRON_STAR_FRAGMENT_ROD, ModItems.NEUTRON_STAR_FRAGMENT_INGOT, 20000, 80);
        
        DMARecipeBuilder.create("dma/nuclear_star").output(ModItems.NUCLEAR_STAR.get()).inputItem(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK.get()).inputItem(ModBlocks.NEUTRON_STAR_FRAGMENT_BLOCK.get()).inputItem(ModBlocks.PULSAR_FRAGMENT_BLOCK.get()).inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 5000).energy(25000000).time(3000).save(c);
        DMARecipeBuilder.create("dma/unstable_white_hole_matter").output(ModItems.UNSTABLE_WHITE_HOLE_MATTER.get()).inputItem(ModItems.WHITE_DWARF_MATTER.get(), 4).inputItem(ModItems.QUANTUM_ANOMALY.get()).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 2000).energy(50000000).time(4000).save(c);
        DMARecipeBuilder.create("dma/safe_containment_matter").output(ModItems.SAFE_CONTAINMENT_MATTER.get()).inputItem(ModItems.OBSIDIAN_MATRIX.get(), 4).inputItem(Items.NETHERITE_INGOT).inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 500).energy(100000).time(200).save(c);
        DMARecipeBuilder.create("dma/aether_containment_capsule").output(ModItems.AETHER_CONTAINMENT_CAPSULE.get()).inputItem(ModItems.SAFE_CONTAINMENT_MATTER.get(), 2).inputItem(ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get()).inputFluid(ModFluids.SOURCE_SPATIAL_FLUID.get(), 1000).energy(500000).time(400).save(c);
        DMARecipeBuilder.create("dma/scrap_from_matrix").output(ModItems.SCRAP.get(), 4).output(AEItems.MATTER_BALL.get(), 1, 0.1F).inputItem(ModItems.OBSIDIAN_MATRIX.get()).inputFluid(Fluids.WATER, 1000).energy(100000).time(200).save(c);
        DMARecipeBuilder.create("dma/scrap_box").output(ModItems.SCRAP_BOX.get()).output(ModItems.SCAR.get(), 1, 1F).inputItem(ModItems.SCRAP.get(), 576).inputFluid(Fluids.WATER, 1000).energy(10000000).time(2000).save(c);
        
        this.buildFluidRecipes(c);
        this.buildMatterProgression(c);
        
        DMARecipeBuilder.create("dma/dark_matter").output(ModItems.DARK_MATTER.get()).inputItem(ModItems.WHITE_DWARF_MATTER.get(), 128).inputItem(ModItems.NEUTRON_STAR_MATTER.get(), 128).inputItem(ModItems.PULSAR_MATTER.get(), 128).inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 10000).energy(200000000).time(20000).save(c);
        
        this.buildStaffAndAnomaly(c);
    }

    private void buildRodRecipe(RecipeOutput c, Supplier<Item> rod, Supplier<Item> ingot, int energy, int time) {
        String name = BuiltInRegistries.ITEM.getKey(rod.get()).getPath();
        DMARecipeBuilder.create("dma/" + name)
                .output(rod.get(), 2)
                .inputItem(ingot.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 50)
                .energy(energy).time(time).save(c);
    }

    private void buildFluidRecipes(RecipeOutput c) {
        DMARecipeBuilder.create("dma/uu_amplifier").outputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 250, 1.0F).inputItem(ModItems.SCRAP_BOX.get()).inputFluid(Fluids.WATER, 1000).energy(20000).time(300).save(c);
        DMARecipeBuilder.create("dma/uu_matter").outputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 250, 1.0F).inputItem(AEItems.MATTER_BALL.get(), 64).inputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 1000).energy(500000).time(300).save(c);
        this.createFluidRecipe(c, ModFluids.SOURCE_TEMPORAL_FLUID, ModItems.QUANTUM_ANOMALY, ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID, 2000000);
        this.createFluidRecipe(c, ModFluids.SOURCE_SPATIAL_FLUID, ModItems.NUCLEAR_STAR, ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID, 6000000);
        DMARecipeBuilder.create("dma/gelid_cryotheum").outputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 10000, 1.0F).inputItem(ModItems.DUST_CRYOTHEUM.get(), 4).inputFluid(Fluids.WATER, 2000).energy(500000).time(200).save(c);
        DMARecipeBuilder.create("dma/stable_coolant").outputFluid(ModFluids.SOURCE_STABLE_COOLANT.get(), 10000, 1.0F).inputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 10000).inputItem(Items.BLUE_ICE, 16).inputItem(ModItems.OBSIDIAN_MATRIX.get()).energy(3000000).time(600).save(c);
        DMARecipeBuilder.create("dma/primordial_matter_liquid").outputFluid(ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID.get(), 5000, 1.0F).inputItem(ModItems.QUANTUM_ANOMALY.get()).inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 8000).energy(3000000).time(700).save(c);
        DMARecipeBuilder.create("dma/liquid_starlight").outputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 10000, 1.0F).inputItem(ModItems.OBSIDIAN_MATRIX.get(), 4).inputItem(Items.NETHER_STAR, 16).inputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 100).energy(4000000).time(1000).save(c);
        DMARecipeBuilder.create("dma/raw_star_matter_plasma").outputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 8000, 1.0F).inputItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get(), 4).inputItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get(), 2).inputItem(ModItems.PULSAR_FRAGMENT_DUST.get(), 2).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 1000).energy(2500000).time(600).save(c);
        DMARecipeBuilder.create("dma/transcending_matter_fluid").outputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 10000, 1.0F).inputItem(ModItems.UNSTABLE_WHITE_HOLE_MATTER.get()).inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 10000).energy(12000000).time(3000).save(c);
    }

    private void createFluidRecipe(RecipeOutput c, Supplier<? extends Fluid> output, Supplier<Item> inputItem, Supplier<? extends Fluid> inputFluid, int energy) {
        String name = BuiltInRegistries.FLUID.getKey(output.get()).getPath();
        DMARecipeBuilder.create("dma/" + name).outputFluid(output.get(), 5000, 1.0F).inputItem(inputItem.get()).inputFluid(inputFluid.get(), 2000).energy(energy).time(400).save(c);
    }

    private void buildMatterProgression(RecipeOutput c) {
        DMARecipeBuilder.create("dma/neutronium_sphere").output(ModItems.NEUTRONIUM_SPHERE.get()).inputItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get(), 9).inputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 500).energy(1000000).time(600).save(c);
        DMARecipeBuilder.create("dma/enriched_neutronium_sphere").output(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get()).inputItem(ModItems.NEUTRONIUM_SPHERE.get()).inputItem(ModItems.QUANTUM_ANOMALY.get(), 2).inputFluid(ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID.get(), 12000).energy(8000000).time(3000).save(c);
        DMARecipeBuilder.create("dma/proto_matter").output(ModItems.PROTO_MATTER.get()).inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get()).inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 1000).energy(2000000).time(800).save(c);
        DMARecipeBuilder.create("dma/corporeal_matter").output(ModItems.CORPOREAL_MATTER.get()).output(ModItems.SCAR.get(), 1, 0.08F).inputItem(ModItems.PROTO_MATTER.get()).inputItem(Items.IRON_BLOCK, 64).inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 1000).energy(5000000).time(1200).save(c);
        DMARecipeBuilder.create("dma/uu_matter_crystal").output(ModItems.UU_MATTER_CRYSTAL.get()).inputItem(Items.AMETHYST_SHARD).inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 2500).energy(20000000).time(400).save(c);
        this.createMatterTier(c, ModItems.WHITE_DWARF_MATTER, ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK, ModFluids.SOURCE_WHITE_DWARF_FRAGMENT_FLUID, 7000000, 1500);
        this.createMatterTier(c, ModItems.NEUTRON_STAR_MATTER, ModBlocks.NEUTRON_STAR_FRAGMENT_BLOCK, ModFluids.SOURCE_NEUTRON_STAR_FRAGMENT_FLUID, 9500000, 1850);
        this.createMatterTier(c, ModItems.PULSAR_MATTER, ModBlocks.PULSAR_FRAGMENT_BLOCK, ModFluids.SOURCE_PULSAR_FRAGMENT_FLUID, 12000000, 2200);
    }

    private void createMatterTier(RecipeOutput c, Supplier<Item> output, Supplier<Block> block, Supplier<? extends Fluid> fluid, int energy, int time) {
        String name = BuiltInRegistries.ITEM.getKey(output.get()).getPath();
        DMARecipeBuilder.create("dma/" + name).output(output.get()).output(ModItems.SCAR.get(), 1, 0.1F).inputItem(ModItems.CORPOREAL_MATTER.get()).inputItem(ModItems.UU_MATTER_CRYSTAL.get()).inputItem(block.get()).inputFluid(fluid.get(), 1000).energy(energy).time(time).save(c);
    }

    private void buildStaffAndAnomaly(RecipeOutput c) {
        DMARecipeBuilder.create("dma/ufo_staff").output(ModTools.UFO_STAFF.get()).inputItem(ModItems.CHARGED_ENRICHED_NEUTRONIUM_SPHERE.get()).inputItem(ModItems.QUANTUM_ANOMALY.get(), 2).inputItem(ModItems.PROTO_MATTER.get(), 2).inputItem(Items.NETHERITE_INGOT, 4).inputItem(AEItems.MATTER_BALL.get(), 16).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 2000).energy(12000000).time(2400).save(c);
        DMARecipeBuilder.create("dma/charged_enriched_neutronium_sphere").output(ModItems.CHARGED_ENRICHED_NEUTRONIUM_SPHERE.get()).inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get()).inputItem(ModItems.NUCLEAR_STAR.get()).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 4000).energy(10000000).time(4000).save(c);
        DMARecipeBuilder.create("dma/quantum_anomaly").output(ModItems.QUANTUM_ANOMALY.get()).inputItem(ModItems.PULSAR_FRAGMENT_DUST.get(), 12).inputItem(ModItems.OBSIDIAN_MATRIX.get()).inputFluid(ModFluids.SOURCE_PULSAR_FRAGMENT_FLUID.get(), 8000).energy(6000000).time(1200).save(c);
    }

    private void buildCatalystsDMA(RecipeOutput c) {
        this.createProgressiveCatalyst(c, ModItems.MATTERFLOW_CATALYST_T1.get(), ModItems.OBSIDIAN_MATRIX.get(), ModItems.PROTO_MATTER.get(), ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID, 1000, 100000, 200);
        this.createProgressiveCatalyst(c, ModItems.MATTERFLOW_CATALYST_T2.get(), ModItems.MATTERFLOW_CATALYST_T1.get(), ModItems.CORPOREAL_MATTER.get(), ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID, 2000, 500000, 400);
        this.createProgressiveCatalyst(c, ModItems.MATTERFLOW_CATALYST_T3.get(), ModItems.MATTERFLOW_CATALYST_T2.get(), ModItems.NEUTRON_STAR_MATTER.get(), ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID, 4000, 2500000, 800);
        this.createProgressiveCatalyst(c, ModItems.CHRONO_CATALYST_T1.get(), ModItems.OBSIDIAN_MATRIX.get(), ModItems.PROTO_MATTER.get(), ModFluids.SOURCE_SPATIAL_FLUID, 1000, 150000, 200);
        this.createProgressiveCatalyst(c, ModItems.CHRONO_CATALYST_T2.get(), ModItems.CHRONO_CATALYST_T1.get(), ModItems.CORPOREAL_MATTER.get(), ModFluids.SOURCE_SPATIAL_FLUID, 2000, 600000, 500);
        this.createProgressiveCatalyst(c, ModItems.CHRONO_CATALYST_T3.get(), ModItems.CHRONO_CATALYST_T2.get(), ModItems.PULSAR_MATTER.get(), ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID, 4000, 3000000, 1000);
        this.createProgressiveCatalyst(c, ModItems.OVERFLUX_CATALYST_T1.get(), ModItems.OBSIDIAN_MATRIX.get(), ModItems.PROTO_MATTER.get(), ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID, 1000, 150000, 200);
        this.createProgressiveCatalyst(c, ModItems.OVERFLUX_CATALYST_T2.get(), ModItems.OVERFLUX_CATALYST_T1.get(), ModItems.CORPOREAL_MATTER.get(), ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID, 2000, 600000, 400);
        this.createProgressiveCatalyst(c, ModItems.OVERFLUX_CATALYST_T3.get(), ModItems.OVERFLUX_CATALYST_T2.get(), ModItems.WHITE_DWARF_MATTER.get(), ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID, 4000, 3000000, 800);
        this.createProgressiveCatalyst(c, ModItems.QUANTUM_CATALYST_T1.get(), ModItems.OBSIDIAN_MATRIX.get(), ModItems.PROTO_MATTER.get(), ModFluids.SOURCE_SPATIAL_FLUID, 1000, 200000, 200);
        this.createProgressiveCatalyst(c, ModItems.QUANTUM_CATALYST_T2.get(), ModItems.QUANTUM_CATALYST_T1.get(), ModItems.CORPOREAL_MATTER.get(), ModFluids.SOURCE_SPATIAL_FLUID, 2000, 800000, 500);
        this.createProgressiveCatalyst(c, ModItems.QUANTUM_CATALYST_T3.get(), ModItems.QUANTUM_CATALYST_T2.get(), ModItems.DARK_MATTER.get(), ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID, 5000, 6000000, 1200);
        DMARecipeBuilder.create("dma/dimensional_catalyst").output(ModItems.DIMENSIONAL_CATALYST.get()).inputItem(ModItems.MATTERFLOW_CATALYST_T3.get()).inputItem(ModItems.CHRONO_CATALYST_T3.get()).inputItem(ModItems.OVERFLUX_CATALYST_T3.get()).inputItem(ModItems.QUANTUM_CATALYST_T3.get()).inputItem(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get()).inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 10000).energy(500000000).time(10000).save(c);
    }

    private void createProgressiveCatalyst(RecipeOutput c, Item output, Item inputBaseOrPrev, Item coreMatter, Supplier<? extends Fluid> fluid, int fluidAmount, int energy, int time) {
        DMARecipeBuilder.create("dma/" + BuiltInRegistries.ITEM.getKey(output).getPath()).output(output).inputItem(inputBaseOrPrev).inputItem(coreMatter, 2).inputItem(AEBlocks.QUARTZ_VIBRANT_GLASS, 2).inputFluid(fluid.get(), fluidAmount).energy(energy).time(time).save(c);
    }

    private void buildArmorsDMA(RecipeOutput c) {
        DMARecipeBuilder.create("dma/thermal_resistor_plating").output(ModArmor.THERMAL_RESISTOR_PLATING.get()).inputItem(Items.NETHERITE_INGOT).inputItem(ModItems.OBSIDIAN_MATRIX.get()).inputItem(Items.BLUE_ICE, 2).inputFluid(Fluids.WATER, 500).energy(50000).time(100).save(c);
        this.createArmorDMA(c, ModArmor.THERMAL_RESISTOR_MASK, Items.NETHERITE_HELMET, ModArmor.THERMAL_RESISTOR_PLATING, 5, 1000000);
        this.createArmorDMA(c, ModArmor.THERMAL_RESISTOR_CHEST, Items.NETHERITE_CHESTPLATE, ModArmor.THERMAL_RESISTOR_PLATING, 8, 1500000);
        this.createArmorDMA(c, ModArmor.THERMAL_RESISTOR_PANTS, Items.NETHERITE_LEGGINGS, ModArmor.THERMAL_RESISTOR_PLATING, 7, 1200000);
        this.createArmorDMA(c, ModArmor.THERMAL_RESISTOR_BOOTS, Items.NETHERITE_BOOTS, ModArmor.THERMAL_RESISTOR_PLATING, 4, 800000);
        this.createUfoArmorDMA(c, ModArmor.UFO_HELMET, ModArmor.THERMAL_RESISTOR_MASK, ModItems.ENRICHED_NEUTRONIUM_SPHERE);
        this.createUfoArmorDMA(c, ModArmor.UFO_CHESTPLATE, ModArmor.THERMAL_RESISTOR_CHEST, ModItems.ENRICHED_NEUTRONIUM_SPHERE);
        this.createUfoArmorDMA(c, ModArmor.UFO_LEGGINGS, ModArmor.THERMAL_RESISTOR_PANTS, ModItems.ENRICHED_NEUTRONIUM_SPHERE);
        this.createUfoArmorDMA(c, ModArmor.UFO_BOOTS, ModArmor.THERMAL_RESISTOR_BOOTS, ModItems.ENRICHED_NEUTRONIUM_SPHERE);
    }

    private void createArmorDMA(RecipeOutput c, Supplier<Item> output, Item baseArmor, Supplier<Item> material, int amount, int energy) {
        String name = BuiltInRegistries.ITEM.getKey(output.get()).getPath();
        DMARecipeBuilder.create("dma/" + name)
                .output(output.get())
                .inputItem(baseArmor)
                .inputItem(material.get(), amount)
                .inputItem(Items.BLUE_ICE, 4)
                .inputFluid(Fluids.WATER, 1000)
                .energy(energy)
                .time(500)
                .save(c);
    }

    private void createUfoArmorDMA(RecipeOutput c, Supplier<Item> output, Supplier<Item> baseArmor, Supplier<Item> rareItem) {
        String name = BuiltInRegistries.ITEM.getKey(output.get()).getPath();
        DMARecipeBuilder.create("dma/" + name).output(output.get()).inputItem(baseArmor.get()).inputItem(rareItem.get(), 2).inputItem(ModItems.QUANTUM_ANOMALY.get()).inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 2000).energy(50000000).time(2000).save(c);
    }

    private void buildInfinityCellsDMA(RecipeOutput c) {
        Map<ItemLike, ItemLike> vanillaMap = new HashMap<>();
        vanillaMap.put(ModCells.INFINITY_WATER_CELL.get(), Items.WATER_BUCKET);
        vanillaMap.put(ModCells.INFINITY_COBBLESTONE_CELL.get(), Items.COBBLESTONE);
        vanillaMap.put(ModCells.INFINITY_COBBLED_DEEPSLATE_CELL.get(), Items.COBBLED_DEEPSLATE);
        vanillaMap.put(ModCells.INFINITY_END_STONE_CELL.get(), Items.END_STONE);
        vanillaMap.put(ModCells.INFINITY_NETHERRACK_CELL.get(), Items.NETHERRACK);
        vanillaMap.put(ModCells.INFINITY_SAND_CELL.get(), Items.SAND);
        vanillaMap.put(ModCells.INFINITY_LAVA_CELL.get(), Items.LAVA_BUCKET);
        vanillaMap.put(ModCells.INFINITY_OBSIDIAN_CELL.get(), Items.OBSIDIAN);
        vanillaMap.put(ModCells.INFINITY_GRAVEL_CELL.get(), Items.GRAVEL);
        vanillaMap.put(ModCells.INFINITY_OAK_LOG_CELL.get(), Items.OAK_LOG);
        vanillaMap.put(ModCells.INFINITY_GLASS_CELL.get(), Items.GLASS);
        vanillaMap.put(ModCells.INFINITY_AMETHYST_SHARD_CELL.get(), Items.AMETHYST_SHARD);
        vanillaMap.put(ModCells.INFINITY_WHITE_DYE_CELL.get(), Items.WHITE_DYE);
        vanillaMap.put(ModCells.INFINITY_ORANGE_DYE_CELL.get(), Items.ORANGE_DYE);
        vanillaMap.put(ModCells.INFINITY_MAGENTA_DYE_CELL.get(), Items.MAGENTA_DYE);
        vanillaMap.put(ModCells.INFINITY_LIGHT_BLUE_DYE_CELL.get(), Items.LIGHT_BLUE_DYE);
        vanillaMap.put(ModCells.INFINITY_YELLOW_DYE_CELL.get(), Items.YELLOW_DYE);
        vanillaMap.put(ModCells.INFINITY_LIME_DYE_CELL.get(), Items.LIME_DYE);
        vanillaMap.put(ModCells.INFINITY_PINK_DYE_CELL.get(), Items.PINK_DYE);
        vanillaMap.put(ModCells.INFINITY_GRAY_DYE_CELL.get(), Items.GRAY_DYE);
        vanillaMap.put(ModCells.INFINITY_LIGHT_GRAY_DYE_CELL.get(), Items.LIGHT_GRAY_DYE);
        vanillaMap.put(ModCells.INFINITY_CYAN_DYE_CELL.get(), Items.CYAN_DYE);
        vanillaMap.put(ModCells.INFINITY_PURPLE_DYE_CELL.get(), Items.PURPLE_DYE);
        vanillaMap.put(ModCells.INFINITY_BLUE_DYE_CELL.get(), Items.BLUE_DYE);
        vanillaMap.put(ModCells.INFINITY_BROWN_DYE_CELL.get(), Items.BROWN_DYE);
        vanillaMap.put(ModCells.INFINITY_GREEN_DYE_CELL.get(), Items.GREEN_DYE);
        vanillaMap.put(ModCells.INFINITY_RED_DYE_CELL.get(), Items.RED_DYE);
        vanillaMap.put(ModCells.INFINITY_BLACK_DYE_CELL.get(), Items.BLACK_DYE);
        vanillaMap.forEach((cell, target) -> this.createInfinityCellRecipe(c, cell, target, null));
        
        this.createInfinityCellRecipe(c, ModCells.INFINITY_SKY_STONE_CELL.get(), AEBlocks.SKY_STONE_BLOCK, "ae2");
    }

    private void buildHousingRecipes(RecipeOutput c) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get())
                .pattern("GRG")
                .pattern("R R")
                .pattern("III")
                .define('G', AEBlocks.QUARTZ_VIBRANT_GLASS)
                .define('R', ModItems.WHITE_DWARF_FRAGMENT_ROD.get())
                .define('I', ModItems.WHITE_DWARF_FRAGMENT_INGOT.get())
                .unlockedBy("has_item", has(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get()))
                .save(c);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get())
                .pattern("GRG")
                .pattern("R R")
                .pattern("III")
                .define('G', AEBlocks.QUARTZ_VIBRANT_GLASS)
                .define('R', ModItems.NEUTRON_STAR_FRAGMENT_ROD.get())
                .define('I', ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get())
                .unlockedBy("has_item", has(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get()))
                .save(c);

        this.buildCellAssembly(c, ModCellItems.ITEM_CELL_40M.get(), ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get(), ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get());
        this.buildCellAssembly(c, ModCellItems.ITEM_CELL_100M.get(), ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get(), ModItems.HYPER_DENSE_COMPONENT_MATRIX.get());
        this.buildCellAssembly(c, ModCellItems.ITEM_CELL_250M.get(), ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get(), ModItems.TESSERACT_COMPONENT_MATRIX.get());
        this.buildCellAssembly(c, ModCellItems.ITEM_CELL_750M.get(), ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get(), ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get());
        this.buildCellAssembly(c, ModCellItems.ITEM_CELL_SINGULARITY.get(), ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get(), ModItems.COSMIC_STRING_COMPONENT_MATRIX.get());
        
        this.buildCellAssembly(c, ModCellItems.FLUID_CELL_40M.get(), ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get(), ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get());
        this.buildCellAssembly(c, ModCellItems.FLUID_CELL_100M.get(), ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get(), ModItems.HYPER_DENSE_COMPONENT_MATRIX.get());
        this.buildCellAssembly(c, ModCellItems.FLUID_CELL_250M.get(), ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get(), ModItems.TESSERACT_COMPONENT_MATRIX.get());
        this.buildCellAssembly(c, ModCellItems.FLUID_CELL_750M.get(), ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get(), ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get());
        this.buildCellAssembly(c, ModCellItems.FLUID_CELL_SINGULARITY.get(), ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get(), ModItems.COSMIC_STRING_COMPONENT_MATRIX.get());
    }

    private void buildCellAssembly(RecipeOutput c, ItemLike cell, ItemLike housing, ItemLike component) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, cell)
                .requires(housing)
                .requires(component)
                .unlockedBy("has_housing", has(housing))
                .save(c);
    }

    private void buildMachineAndStorageRecipes(RecipeOutput c) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.GRAVITON_PLATED_CASING.get())
                .pattern("IDI")
                .pattern("OMO")
                .pattern("IDI")
                .define('I', Items.IRON_BLOCK)
                .define('D', AEItems.ENGINEERING_PROCESSOR)
                .define('M', ModItems.OBSIDIAN_MATRIX.get())
                .define('O', Items.OBSIDIAN)
                .unlockedBy("has_processor", has(AEItems.ENGINEERING_PROCESSOR))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.QUANTUM_LATTICE_FRAME.get())
                .pattern("GDG")
                .pattern("DQD")
                .pattern("GDG")
                .define('G', AEBlocks.QUARTZ_VIBRANT_GLASS)
                .define('D', ModItems.DIMENSIONAL_PROCESSOR.get())
                .define('Q', AEParts.QUARTZ_FIBER)
                .unlockedBy("has_processor", has(ModItems.DIMENSIONAL_PROCESSOR.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER_BLOCK.get())
                .pattern("OPO")
                .pattern("CMC")
                .pattern("OEO")
                .define('O', ModItems.OBSIDIAN_MATRIX.get())
                .define('P', AEItems.ENGINEERING_PROCESSOR)
                .define('C', ModBlocks.GRAVITON_PLATED_CASING.get())
                .define('M', AEBlocks.CONTROLLER)
                .define('E', Items.ENDER_EYE)
                .unlockedBy("has_controller", has(AEBlocks.CONTROLLER))
                .save(c);

        this.buildStorageBlock(c, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1B).get(), ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get(), "storage_1b");
        this.buildStorageBlock(c, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_50B).get(), ModItems.HYPER_DENSE_COMPONENT_MATRIX.get(), "storage_50b");
        this.buildStorageBlock(c, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1T).get(), ModItems.TESSERACT_COMPONENT_MATRIX.get(), "storage_1t");
        this.buildStorageBlock(c, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_250T).get(), ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get(), "storage_250t");
        this.buildStorageBlock(c, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1QD).get(), ModItems.COSMIC_STRING_COMPONENT_MATRIX.get(), "storage_1qd");

        this.buildCoProcessor(c, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_50M).get(), ModBlocks.QUANTUM_LATTICE_FRAME.get(), "coprocessor_50m", true);
        this.buildCoProcessor(c, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_150M).get(), ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_50M).get(), "coprocessor_150m", false);
        this.buildCoProcessor(c, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_300M).get(), ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_150M).get(), "coprocessor_300m", false);
        this.buildCoProcessor(c, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_750M).get(), ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_300M).get(), "coprocessor_750m", false);
        this.buildCoProcessor(c, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_2B).get(), ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_750M).get(), "coprocessor_2b", false);
    }

    private void buildQuantumMultiblockRecipes(RecipeOutput c) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MultiblockBlocks.QUANTUM_HYPER_MECHANICAL_CASING.get(), 4)
                .pattern("NHN")
                .pattern("HCH")
                .pattern("NHN")
                .define('N', ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get())
                .define('H', ModItems.HYPER_DENSE_COMPONENT_MATRIX.get())
                .define('C', ModBlocks.GRAVITON_PLATED_CASING.get())
                .unlockedBy("has_hyper_dense_component", has(ModItems.HYPER_DENSE_COMPONENT_MATRIX.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MultiblockBlocks.QUANTUM_MATTER_FABRICATOR_CONTROLLER.get())
                .pattern("ACA")
                .pattern("DQD")
                .pattern("AEA")
                .define('A', ModItems.QUANTUM_ANOMALY.get())
                .define('C', ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())
                .define('D', ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER_BLOCK.get())
                .define('Q', MultiblockBlocks.QUANTUM_HYPER_MECHANICAL_CASING.get())
                .define('E', AEBlocks.CONTROLLER)
                .unlockedBy("has_quantum_anomaly", has(ModItems.QUANTUM_ANOMALY.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MultiblockBlocks.QUANTUM_SLICER_CONTROLLER.get())
                .pattern("PGP")
                .pattern("QDQ")
                .pattern("FEF")
                .define('P', ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get())
                .define('G', AEBlocks.QUARTZ_VIBRANT_GLASS)
                .define('Q', MultiblockBlocks.QUANTUM_HYPER_MECHANICAL_CASING.get())
                .define('D', ModItems.DIMENSIONAL_PROCESSOR.get())
                .define('F', ModBlocks.QUANTUM_LATTICE_FRAME.get())
                .define('E', AEItems.ENGINEERING_PROCESSOR)
                .unlockedBy("has_dimensional_processor", has(ModItems.DIMENSIONAL_PROCESSOR.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MultiblockBlocks.QUANTUM_PROCESSOR_ASSEMBLER_CONTROLLER.get())
                .pattern("TST")
                .pattern("QCQ")
                .pattern("HEH")
                .define('T', ModItems.TESSERACT_COMPONENT_MATRIX.get())
                .define('S', AEItems.SINGULARITY)
                .define('Q', MultiblockBlocks.QUANTUM_HYPER_MECHANICAL_CASING.get())
                .define('C', ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())
                .define('H', ModItems.HYPER_DENSE_COMPONENT_MATRIX.get())
                .define('E', AEBlocks.CONTROLLER)
                .unlockedBy("has_tesseract_component", has(ModItems.TESSERACT_COMPONENT_MATRIX.get()))
                .save(c);
    }

    private void buildStorageBlock(RecipeOutput c, ItemLike output, ItemLike component, String name) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, output)
                .requires(ModBlocks.GRAVITON_PLATED_CASING.get())
                .requires(component)
                .unlockedBy("has_component", has(component))
                .save(c, UfoMod.id(name));
    }

    private void buildCoProcessor(RecipeOutput c, ItemLike output, ItemLike previous, String name, boolean isFirst) {
        if (isFirst) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output)
                    .pattern("PEP")
                    .pattern("EFE")
                    .pattern("PEP")
                    .define('F', previous)
                    .define('P', ModItems.DIMENSIONAL_PROCESSOR.get())
                    .define('E', AEItems.ENGINEERING_PROCESSOR)
                    .unlockedBy("has_frame", has(previous))
                    .save(c, UfoMod.id(name));
        } else {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, output)
                    .requires(previous)
                    .requires(ModItems.DIMENSIONAL_PROCESSOR.get())
                    .requires(AEItems.ENGINEERING_PROCESSOR)
                    .unlockedBy("has_prev", has(previous))
                    .save(c, UfoMod.id(name));
        }
    }

    private void buildIngotGenerators(RecipeOutput c) {
        DMARecipeBuilder.create("dma/ingot/white_dwarf_fragment").output(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get()).inputItem(Items.NETHERITE_INGOT, 2).inputItem(Items.BLUE_ICE, 4).inputItem(AEBlocks.SKY_STONE_BLOCK, 4).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 250).energy(100000).time(200).save(c);
        DMARecipeBuilder.create("dma/ingot/neutron_star_fragment").output(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get()).inputItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get(), 4).inputItem(Items.NETHER_STAR).inputItem(ModItems.OBSIDIAN_MATRIX.get()).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 500).energy(500000).time(400).save(c);
        DMARecipeBuilder.create("dma/ingot/pulsar_fragment").output(ModItems.PULSAR_FRAGMENT_INGOT.get()).inputItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get(), 2).inputItem(Items.LODESTONE, 2).inputItem(Items.LIGHTNING_ROD, 4).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 1000).energy(1000000).time(600).save(c);
    }

    private void createTieredComponent(RecipeOutput c, ItemLike output, ItemLike prevTier, int prevCount, ItemLike rareItem, int rareCount, Supplier<? extends Fluid> fluid, int fluidAmount, int energy, int time) {
        String name = BuiltInRegistries.ITEM.getKey(output.asItem()).getPath();
        DMARecipeBuilder.create("dma/component/" + name).output(output).inputItem(prevTier, prevCount).inputItem(rareItem, rareCount).inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), 1).inputFluid(fluid.get(), fluidAmount).energy(energy).time(time).save(c);
    }

    private void createInfinityCellRecipe(RecipeOutput c, ItemLike cellItem, ItemLike targetItem, String modIdCondition) {
        String name = BuiltInRegistries.ITEM.getKey(cellItem.asItem()).getPath();
        DMARecipeBuilder builder = DMARecipeBuilder.create("dma/infinity_cell/" + name).output(cellItem).inputItem(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get()).inputItem(ModItems.QUANTUM_ANOMALY.get()).inputItem(targetItem, 64).inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 2500).energy(250000000).time(10000);
        if (modIdCondition != null && !modIdCondition.isEmpty()) {
            builder.save(c.withConditions(new ModLoadedCondition(modIdCondition)));
        } else {
            builder.save(c);
        }
    }

    private void buildMassiveStellarSimulations(RecipeOutput c) {
        StellarSimulationRecipeBuilder.create("stellar_nexus/massive_iron_synthesis")
                .output(Items.IRON_INGOT, 15000000)
                .inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get(), 100)
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 5000000)
                .fuel(ModFluids.SOURCE_STABLE_COOLANT.get(), 50000) // Using stable coolant as proxy for ethene if not present
                .coolant(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 100000)
                .fieldLevel(3)
                .energy(2000000000) // 2 Billion
                .time(54000) // 45 minutes * 20 ticks * 60 seconds
                .save(c);

        StellarSimulationRecipeBuilder.create("stellar_nexus/massive_copper_synthesis")
                .output(Items.COPPER_INGOT, 15000000)
                .inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get(), 100)
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 5000000)
                .fuel(ModFluids.SOURCE_STABLE_COOLANT.get(), 50000)
                .coolant(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 100000)
                .fieldLevel(3)
                .energy(2000000000)
                .time(54000)
                .save(c);

        StellarSimulationRecipeBuilder.create("stellar_nexus/massive_gold_synthesis")
                .output(Items.GOLD_INGOT, 15000000)
                .inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get(), 150)
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 6000000)
                .fuel(ModFluids.SOURCE_STABLE_COOLANT.get(), 60000)
                .coolant(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 150000)
                .fieldLevel(3)
                .energy(2500000000L) // 2.5 Billion
                .time(60000) // 50m
                .save(c);

        StellarSimulationRecipeBuilder.create("stellar_nexus/massive_netherite_synthesis")
                .output(Items.NETHERITE_INGOT, 5000000)
                .inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get(), 200)
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 10000000)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 1000000)
                .fuel(ModFluids.SOURCE_STABLE_COOLANT.get(), 100000)
                .coolant(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 250000)
                .fieldLevel(3)
                .energy(5000000000L) // 5 Billion
                .time(96000) // 1h 20m
                .save(c);
    }
}
