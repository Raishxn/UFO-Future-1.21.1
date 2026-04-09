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
import net.minecraft.resources.ResourceLocation;
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
import com.raishxn.ufo.recipe.UniversalMultiblockMachineKind;

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
        this.buildStellarNexusRecipes(c);
        this.buildToolAndUtilityRecipes(c);
        this.buildUniversalMultiblockRecipes(c);
        this.buildStellarSimulationRecipes(c);
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
                .inputItem(Items.IRON_BLOCK, 4)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get(), 2)
                .inputItem(AEItems.ENGINEERING_PROCESSOR, 4)
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 1000)
                .energy(500000).time(600)
                .save(c);

        DMARecipeBuilder.create("dma/printed_dimensional_processor")
                .output(ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get())
                .inputItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get(), 2)
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get())
                .inputItem(AEBlocks.QUARTZ_VIBRANT_GLASS, 4)
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 250)
                .energy(200000).time(240)
                .save(c);

        DMARecipeBuilder.create("dma/dimensional_processor")
                .output(ModItems.DIMENSIONAL_PROCESSOR.get(), 2)
                .inputItem(ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get(), 2)
                .inputItem(AEItems.SILICON_PRINT, 2)
                .inputItem(AEItems.ENGINEERING_PROCESSOR)
                .inputItem(AEItems.FLUIX_DUST, 16)
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 500)
                .energy(750000).time(500)
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
                .energy(1200000).time(600)
                .save(c);

        this.createTieredComponent(c, ModItems.HYPER_DENSE_COMPONENT_MATRIX.get(), ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get(), 12, ModItems.WHITE_DWARF_MATTER.get(), 8, 4, ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID, 2000, 4000000, 1200);
        this.createTieredComponent(c, ModItems.TESSERACT_COMPONENT_MATRIX.get(), ModItems.HYPER_DENSE_COMPONENT_MATRIX.get(), 16, ModItems.NEUTRON_STAR_MATTER.get(), 8, 8, ModFluids.SOURCE_SPATIAL_FLUID, 4000, 12000000, 2400);
        this.createTieredComponent(c, ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get(), ModItems.TESSERACT_COMPONENT_MATRIX.get(), 16, ModItems.PULSAR_MATTER.get(), 12, 16, ModFluids.SOURCE_SPATIAL_FLUID, 8000, 48000000, 4800);
        this.createTieredComponent(c, ModItems.COSMIC_STRING_COMPONENT_MATRIX.get(), ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get(), 24, ModItems.DARK_MATTER.get(), 16, 32, ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID, 16000, 180000000, 9600);
    }

    private void buildMaterialsAndFluidsDMA(RecipeOutput c) {
        DMARecipeBuilder.create("dma/white_dwarf_dust").output(ModItems.WHITE_DWARF_FRAGMENT_DUST.get()).inputItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get()).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 20).energy(10000).time(60).save(c);
        DMARecipeBuilder.create("dma/white_dwarf_fluid").outputFluid(ModFluids.SOURCE_WHITE_DWARF_FRAGMENT_FLUID.get(), 1000, 1.0F).inputItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get(), 4).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 200).energy(40000).time(120).save(c);
        DMARecipeBuilder.create("dma/neutron_star_dust").output(ModItems.NEUTRON_STAR_FRAGMENT_DUST.get()).inputItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get()).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 20).energy(15000).time(80).save(c);
        DMARecipeBuilder.create("dma/neutron_star_fluid").outputFluid(ModFluids.SOURCE_NEUTRON_STAR_FRAGMENT_FLUID.get(), 1000, 1.0F).inputItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get(), 4).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 200).energy(60000).time(150).save(c);
        DMARecipeBuilder.create("dma/pulsar_dust").output(ModItems.PULSAR_FRAGMENT_DUST.get()).inputItem(ModItems.PULSAR_FRAGMENT_INGOT.get()).inputItem(AEBlocks.TINY_TNT).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 20).energy(20000).time(100).save(c);
        DMARecipeBuilder.create("dma/pulsar_fluid").outputFluid(ModFluids.SOURCE_PULSAR_FRAGMENT_FLUID.get(), 1000, 1.0F).inputItem(ModItems.PULSAR_FRAGMENT_INGOT.get(), 4).inputItem(Items.LIGHTNING_ROD, 4).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 200).energy(80000).time(180).save(c);
        DMARecipeBuilder.create("dma/dust_blizz").output(ModItems.DUST_BLIZZ.get(), 4).inputItem(Items.SNOWBALL, 4).inputItem(Items.REDSTONE).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 50).energy(20000).time(80).save(c);
        DMARecipeBuilder.create("dma/dust_cryotheum").output(ModItems.DUST_CRYOTHEUM.get(), 2).inputItem(ModItems.DUST_BLIZZ.get(), 2).inputItem(Items.REDSTONE, 2).inputItem(Items.SNOWBALL).inputFluid(Fluids.WATER, 100).energy(40000).time(100).save(c);
        
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
        
        DMARecipeBuilder.create("dma/dark_matter")
                .output(ModItems.DARK_MATTER.get())
                .inputItem(ModItems.WHITE_DWARF_MATTER.get(), 128)
                .inputItem(ModItems.NEUTRON_STAR_MATTER.get(), 128)
                .inputItem(ModItems.PULSAR_MATTER.get(), 128)
                .inputItem(ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get(), 4)
                .inputItem(ModItems.CHARGED_ENRICHED_NEUTRONIUM_SPHERE.get(), 2)
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), 64)
                .inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 20000)
                .energy(450000000)
                .time(36000)
                .save(c);
        
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
        DMARecipeBuilder.create("dma/stable_coolant").outputFluid(ModFluids.SOURCE_STABLE_COOLANT.get(), 10000, 1.0F).inputFluid(Fluids.WATER, 10000).inputItem(Items.BLUE_ICE, 16).inputItem(ModItems.OBSIDIAN_MATRIX.get()).energy(3000000).time(600).save(c);
        DMARecipeBuilder.create("dma/primordial_matter_liquid").outputFluid(ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID.get(), 5000, 1.0F).inputItem(ModItems.QUANTUM_ANOMALY.get()).inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 8000).energy(3000000).time(700).save(c);
        DMARecipeBuilder.create("dma/liquid_starlight").outputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 10000, 1.0F).inputItem(ModItems.OBSIDIAN_MATRIX.get(), 4).inputItem(Items.NETHER_STAR, 16).inputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 100).energy(4000000).time(1000).save(c);
        DMARecipeBuilder.create("dma/raw_star_matter_plasma")
                .outputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 8000, 1.0F)
                .inputItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get(), 8)
                .inputItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get(), 4)
                .inputItem(ModItems.PULSAR_FRAGMENT_DUST.get(), 8)
                .inputItem(ModItems.CORPOREAL_MATTER.get(), 2)
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), 16)
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 4000)
                .energy(12000000)
                .time(1800)
                .save(c);
        DMARecipeBuilder.create("dma/transcending_matter_fluid").outputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 10000, 1.0F).inputItem(ModItems.UNSTABLE_WHITE_HOLE_MATTER.get()).inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 10000).energy(12000000).time(3000).save(c);
    }

    private void createFluidRecipe(RecipeOutput c, Supplier<? extends Fluid> output, Supplier<Item> inputItem, Supplier<? extends Fluid> inputFluid, int energy) {
        String name = BuiltInRegistries.FLUID.getKey(output.get()).getPath();
        DMARecipeBuilder.create("dma/" + name).outputFluid(output.get(), 5000, 1.0F).inputItem(inputItem.get()).inputFluid(inputFluid.get(), 2000).energy(energy).time(400).save(c);
    }

    private void buildMatterProgression(RecipeOutput c) {
        DMARecipeBuilder.create("dma/neutronium_sphere").output(ModItems.NEUTRONIUM_SPHERE.get()).inputItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get(), 9).inputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 500).energy(1000000).time(600).save(c);
        DMARecipeBuilder.create("dma/enriched_neutronium_sphere")
                .output(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get())
                .inputItem(ModItems.NEUTRONIUM_SPHERE.get(), 2)
                .inputItem(ModItems.QUANTUM_ANOMALY.get(), 8)
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), 8)
                .inputFluid(ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID.get(), 24000)
                .energy(24000000)
                .time(7200)
                .save(c);
        DMARecipeBuilder.create("dma/proto_matter")
                .output(ModItems.PROTO_MATTER.get())
                .inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get(), 2)
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), 4)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 4000)
                .energy(10000000)
                .time(2400)
                .save(c);
        DMARecipeBuilder.create("dma/corporeal_matter")
                .output(ModItems.CORPOREAL_MATTER.get())
                .output(ModItems.SCAR.get(), 1, 0.08F)
                .inputItem(ModItems.PROTO_MATTER.get(), 2)
                .inputItem(Items.IRON_BLOCK, 64)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get(), 16)
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), 8)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 4000)
                .energy(20000000)
                .time(3600)
                .save(c);
        DMARecipeBuilder.create("dma/uu_matter_crystal").output(ModItems.UU_MATTER_CRYSTAL.get()).inputItem(Items.AMETHYST_SHARD).inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 2500).energy(20000000).time(400).save(c);
        this.createMatterTier(c, ModItems.WHITE_DWARF_MATTER, ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK, ModFluids.SOURCE_WHITE_DWARF_FRAGMENT_FLUID, 4, 7000000, 1500);
        this.createMatterTier(c, ModItems.NEUTRON_STAR_MATTER, ModBlocks.NEUTRON_STAR_FRAGMENT_BLOCK, ModFluids.SOURCE_NEUTRON_STAR_FRAGMENT_FLUID, 8, 15000000, 2600);
        this.createMatterTier(c, ModItems.PULSAR_MATTER, ModBlocks.PULSAR_FRAGMENT_BLOCK, ModFluids.SOURCE_PULSAR_FRAGMENT_FLUID, 16, 30000000, 4200);
    }

    private void createMatterTier(RecipeOutput c, Supplier<Item> output, Supplier<Block> block, Supplier<? extends Fluid> fluid, int processorCount, int energy, int time) {
        String name = BuiltInRegistries.ITEM.getKey(output.get()).getPath();
        DMARecipeBuilder.create("dma/" + name).output(output.get()).output(ModItems.SCAR.get(), 1, 0.1F).inputItem(ModItems.CORPOREAL_MATTER.get()).inputItem(ModItems.UU_MATTER_CRYSTAL.get()).inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), processorCount).inputItem(block.get()).inputFluid(fluid.get(), 2000).energy(energy).time(time).save(c);
    }

    private void buildStaffAndAnomaly(RecipeOutput c) {
        DMARecipeBuilder.create("dma/ufo_staff")
                .output(ModTools.UFO_STAFF.get())
                .inputItem(ModItems.CHARGED_ENRICHED_NEUTRONIUM_SPHERE.get(), 4)
                .inputItem(ModItems.QUANTUM_ANOMALY.get(), 16)
                .inputItem(ModItems.PROTO_MATTER.get(), 16)
                .inputItem(ModItems.DARK_MATTER.get(), 2)
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), 32)
                .inputItem(Items.NETHERITE_INGOT, 16)
                .inputItem(AEItems.MATTER_BALL.get(), 256)
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 12000)
                .energy(100000000)
                .time(12000)
                .save(c);
        DMARecipeBuilder.create("dma/charged_enriched_neutronium_sphere")
                .output(ModItems.CHARGED_ENRICHED_NEUTRONIUM_SPHERE.get())
                .inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get(), 4)
                .inputItem(ModItems.NUCLEAR_STAR.get(), 2)
                .inputItem(ModItems.PULSAR_MATTER.get(), 8)
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), 16)
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 16000)
                .energy(80000000)
                .time(12000)
                .save(c);
        DMARecipeBuilder.create("dma/quantum_anomaly")
                .output(ModItems.QUANTUM_ANOMALY.get())
                .inputItem(ModItems.PULSAR_FRAGMENT_DUST.get(), 64)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get(), 16)
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), 8)
                .inputFluid(ModFluids.SOURCE_PULSAR_FRAGMENT_FLUID.get(), 24000)
                .energy(30000000)
                .time(4800)
                .save(c);
    }

    private void buildCatalystsDMA(RecipeOutput c) {
        this.createProgressiveCatalyst(c, ModItems.MATTERFLOW_CATALYST_T1.get(), ModItems.OBSIDIAN_MATRIX.get(), 1, ModItems.PROTO_MATTER.get(), 2, 0, ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID, 1000, 100000, 200);
        this.createProgressiveCatalyst(c, ModItems.MATTERFLOW_CATALYST_T2.get(), ModItems.MATTERFLOW_CATALYST_T1.get(), 2, ModItems.CORPOREAL_MATTER.get(), 2, 2, ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID, 2000, 500000, 400);
        this.createProgressiveCatalyst(c, ModItems.MATTERFLOW_CATALYST_T3.get(), ModItems.MATTERFLOW_CATALYST_T2.get(), 4, ModItems.NEUTRON_STAR_MATTER.get(), 4, 12, ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID, 8000, 12000000, 3200);
        this.createProgressiveCatalyst(c, ModItems.CHRONO_CATALYST_T1.get(), ModItems.OBSIDIAN_MATRIX.get(), 1, ModItems.PROTO_MATTER.get(), 2, 0, ModFluids.SOURCE_SPATIAL_FLUID, 1000, 150000, 200);
        this.createProgressiveCatalyst(c, ModItems.CHRONO_CATALYST_T2.get(), ModItems.CHRONO_CATALYST_T1.get(), 2, ModItems.CORPOREAL_MATTER.get(), 2, 2, ModFluids.SOURCE_SPATIAL_FLUID, 2000, 600000, 500);
        this.createProgressiveCatalyst(c, ModItems.CHRONO_CATALYST_T3.get(), ModItems.CHRONO_CATALYST_T2.get(), 4, ModItems.PULSAR_MATTER.get(), 4, 12, ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID, 10000, 15000000, 3600);
        this.createProgressiveCatalyst(c, ModItems.OVERFLUX_CATALYST_T1.get(), ModItems.OBSIDIAN_MATRIX.get(), 1, ModItems.PROTO_MATTER.get(), 2, 0, ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID, 1000, 150000, 200);
        this.createProgressiveCatalyst(c, ModItems.OVERFLUX_CATALYST_T2.get(), ModItems.OVERFLUX_CATALYST_T1.get(), 2, ModItems.CORPOREAL_MATTER.get(), 2, 2, ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID, 2000, 600000, 400);
        this.createProgressiveCatalyst(c, ModItems.OVERFLUX_CATALYST_T3.get(), ModItems.OVERFLUX_CATALYST_T2.get(), 4, ModItems.WHITE_DWARF_MATTER.get(), 4, 16, ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID, 12000, 18000000, 4200);
        this.createProgressiveCatalyst(c, ModItems.QUANTUM_CATALYST_T1.get(), ModItems.OBSIDIAN_MATRIX.get(), 1, ModItems.PROTO_MATTER.get(), 2, 0, ModFluids.SOURCE_SPATIAL_FLUID, 1000, 200000, 200);
        this.createProgressiveCatalyst(c, ModItems.QUANTUM_CATALYST_T2.get(), ModItems.QUANTUM_CATALYST_T1.get(), 2, ModItems.CORPOREAL_MATTER.get(), 2, 2, ModFluids.SOURCE_SPATIAL_FLUID, 2000, 800000, 500);
        this.createProgressiveCatalyst(c, ModItems.QUANTUM_CATALYST_T3.get(), ModItems.QUANTUM_CATALYST_T2.get(), 4, ModItems.DARK_MATTER.get(), 2, 20, ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID, 16000, 28000000, 5200);
        DMARecipeBuilder.create("dma/dimensional_catalyst").output(ModItems.DIMENSIONAL_CATALYST.get()).inputItem(ModItems.MATTERFLOW_CATALYST_T3.get()).inputItem(ModItems.CHRONO_CATALYST_T3.get()).inputItem(ModItems.OVERFLUX_CATALYST_T3.get()).inputItem(ModItems.QUANTUM_CATALYST_T3.get()).inputItem(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get()).inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 10000).energy(500000000).time(10000).save(c);
    }

    private void createProgressiveCatalyst(RecipeOutput c, Item output, Item inputBaseOrPrev, int previousCount, Item coreMatter, int matterCount, int processorCount, Supplier<? extends Fluid> fluid, int fluidAmount, int energy, int time) {
        DMARecipeBuilder builder = DMARecipeBuilder.create("dma/" + BuiltInRegistries.ITEM.getKey(output).getPath())
                .output(output)
                .inputItem(inputBaseOrPrev, previousCount)
                .inputItem(coreMatter, matterCount)
                .inputItem(AEBlocks.QUARTZ_VIBRANT_GLASS, 2)
                .inputFluid(fluid.get(), fluidAmount)
                .energy(energy)
                .time(time);
        if (processorCount > 0) {
            builder.inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), processorCount);
        }
        builder.save(c);
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
        DMARecipeBuilder.create("dma/" + name)
                .output(output.get())
                .inputItem(baseArmor.get())
                .inputItem(rareItem.get(), 8)
                .inputItem(ModItems.QUANTUM_ANOMALY.get(), 4)
                .inputItem(ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get())
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), 8)
                .inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 8000)
                .energy(120000000)
                .time(6000)
                .save(c);
    }

    private void buildInfinityCellsDMA(RecipeOutput c) {
        Map<ItemLike, ItemLike> commonCells = new HashMap<>();
        commonCells.put(ModCells.INFINITY_WATER_CELL.get(), Items.WATER_BUCKET);
        commonCells.put(ModCells.INFINITY_COBBLESTONE_CELL.get(), Items.COBBLESTONE);
        commonCells.put(ModCells.INFINITY_COBBLED_DEEPSLATE_CELL.get(), Items.COBBLED_DEEPSLATE);
        commonCells.put(ModCells.INFINITY_NETHERRACK_CELL.get(), Items.NETHERRACK);
        commonCells.put(ModCells.INFINITY_SAND_CELL.get(), Items.SAND);
        commonCells.put(ModCells.INFINITY_GRAVEL_CELL.get(), Items.GRAVEL);
        commonCells.put(ModCells.INFINITY_OAK_LOG_CELL.get(), Items.OAK_LOG);
        commonCells.put(ModCells.INFINITY_GLASS_CELL.get(), Items.GLASS);
        commonCells.put(ModCells.INFINITY_WHITE_DYE_CELL.get(), Items.WHITE_DYE);
        commonCells.put(ModCells.INFINITY_ORANGE_DYE_CELL.get(), Items.ORANGE_DYE);
        commonCells.put(ModCells.INFINITY_MAGENTA_DYE_CELL.get(), Items.MAGENTA_DYE);
        commonCells.put(ModCells.INFINITY_LIGHT_BLUE_DYE_CELL.get(), Items.LIGHT_BLUE_DYE);
        commonCells.put(ModCells.INFINITY_YELLOW_DYE_CELL.get(), Items.YELLOW_DYE);
        commonCells.put(ModCells.INFINITY_LIME_DYE_CELL.get(), Items.LIME_DYE);
        commonCells.put(ModCells.INFINITY_PINK_DYE_CELL.get(), Items.PINK_DYE);
        commonCells.put(ModCells.INFINITY_GRAY_DYE_CELL.get(), Items.GRAY_DYE);
        commonCells.put(ModCells.INFINITY_LIGHT_GRAY_DYE_CELL.get(), Items.LIGHT_GRAY_DYE);
        commonCells.put(ModCells.INFINITY_CYAN_DYE_CELL.get(), Items.CYAN_DYE);
        commonCells.put(ModCells.INFINITY_PURPLE_DYE_CELL.get(), Items.PURPLE_DYE);
        commonCells.put(ModCells.INFINITY_BLUE_DYE_CELL.get(), Items.BLUE_DYE);
        commonCells.put(ModCells.INFINITY_BROWN_DYE_CELL.get(), Items.BROWN_DYE);
        commonCells.put(ModCells.INFINITY_GREEN_DYE_CELL.get(), Items.GREEN_DYE);
        commonCells.put(ModCells.INFINITY_RED_DYE_CELL.get(), Items.RED_DYE);
        commonCells.put(ModCells.INFINITY_BLACK_DYE_CELL.get(), Items.BLACK_DYE);
        commonCells.forEach((cell, target) -> this.createInfinityCellRecipe(c, cell, target, 1, 2500, 250000000, 10000, null));

        Map<ItemLike, ItemLike> advancedCells = new HashMap<>();
        advancedCells.put(ModCells.INFINITY_END_STONE_CELL.get(), Items.END_STONE);
        advancedCells.put(ModCells.INFINITY_LAVA_CELL.get(), Items.LAVA_BUCKET);
        advancedCells.put(ModCells.INFINITY_OBSIDIAN_CELL.get(), Items.OBSIDIAN);
        advancedCells.put(ModCells.INFINITY_AMETHYST_SHARD_CELL.get(), Items.AMETHYST_SHARD);
        advancedCells.forEach((cell, target) -> this.createInfinityCellRecipe(c, cell, target, 2, 5000, 450000000, 16000, null));

        this.createInfinityCellRecipe(c, ModCells.INFINITY_SKY_STONE_CELL.get(), AEBlocks.SKY_STONE_BLOCK, 2, 6000, 500000000, 18000, "ae2");
        this.createInfinityCellRecipe(c, ModCells.INFINITY_ANTIMATTER_PELLET_CELL.get(), BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:pellet_antimatter")), 4, 16000, 1500000000, 36000, "mekanism");
        this.createInfinityCellRecipe(c, ModCells.INFINITY_PLUTONIUM_PELLET_CELL.get(), BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:pellet_plutonium")), 3, 12000, 950000000, 28000, "mekanism");
        this.createInfinityCellRecipe(c, ModCells.INFINITY_POLONIUM_PELLET_CELL.get(), BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:pellet_polonium")), 3, 12000, 950000000, 28000, "mekanism");
        this.createInfinityCellRecipe(c, ModCells.INFINITY_HDPE_PELLET_CELL.get(), BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:hdpe_pellet")), 2, 8000, 650000000, 22000, "mekanism");
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

        this.buildStorageBlock(c, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1B).get(),
                AEItems.CELL_COMPONENT_256K.get(), ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get(),
                ModBlocks.GRAVITON_PLATED_CASING.get(), "storage_1b");
        this.buildStorageUpgrade(c, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_50B).get(),
                ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1B).get(),
                ModItems.HYPER_DENSE_COMPONENT_MATRIX.get(), ModItems.CORPOREAL_MATTER.get(), "storage_50b");
        this.buildStorageUpgrade(c, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1T).get(),
                ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_50B).get(),
                ModItems.TESSERACT_COMPONENT_MATRIX.get(), ModItems.WHITE_DWARF_MATTER.get(), "storage_1t");
        this.buildStorageUpgrade(c, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_250T).get(),
                ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1T).get(),
                ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get(), ModItems.NEUTRON_STAR_MATTER.get(), "storage_250t");
        this.buildStorageUpgrade(c, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1QD).get(),
                ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_250T).get(),
                ModItems.COSMIC_STRING_COMPONENT_MATRIX.get(), ModItems.DARK_MATTER.get(), "storage_1qd");

        this.buildCoProcessor(c, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_50M).get(),
                ModBlocks.QUANTUM_LATTICE_FRAME.get(), ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get(),
                ModItems.WHITE_DWARF_FRAGMENT_INGOT.get(), "coprocessor_50m", true);
        this.buildCoProcessor(c, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_150M).get(),
                ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_50M).get(),
                ModItems.HYPER_DENSE_COMPONENT_MATRIX.get(), ModItems.WHITE_DWARF_MATTER.get(), "coprocessor_150m", false);
        this.buildCoProcessor(c, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_300M).get(),
                ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_150M).get(),
                ModItems.TESSERACT_COMPONENT_MATRIX.get(), ModItems.NEUTRON_STAR_MATTER.get(), "coprocessor_300m", false);
        this.buildCoProcessor(c, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_750M).get(),
                ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_300M).get(),
                ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get(), ModItems.PULSAR_MATTER.get(), "coprocessor_750m", false);
        this.buildCoProcessor(c, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_2B).get(),
                ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_750M).get(),
                ModItems.COSMIC_STRING_COMPONENT_MATRIX.get(), ModItems.DARK_MATTER.get(), "coprocessor_2b", false);
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
                .define('D', ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_250T).get())
                .define('Q', MultiblockBlocks.QUANTUM_HYPER_MECHANICAL_CASING.get())
                .define('E', ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_750M).get())
                .unlockedBy("has_quantum_anomaly", has(ModItems.QUANTUM_ANOMALY.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MultiblockBlocks.QUANTUM_SLICER_CONTROLLER.get())
                .pattern("PGP")
                .pattern("QDQ")
                .pattern("FEF")
                .define('P', ModItems.HYPER_DENSE_COMPONENT_MATRIX.get())
                .define('G', ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_50M).get())
                .define('Q', MultiblockBlocks.QUANTUM_HYPER_MECHANICAL_CASING.get())
                .define('D', ModItems.DIMENSIONAL_PROCESSOR.get())
                .define('F', ModBlocks.QUANTUM_LATTICE_FRAME.get())
                .define('E', ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER_BLOCK.get())
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
                .define('H', ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_150M).get())
                .define('E', ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_50B).get())
                .unlockedBy("has_tesseract_component", has(ModItems.TESSERACT_COMPONENT_MATRIX.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MultiblockBlocks.QUANTUM_PATTERN_HATCH.get())
                .pattern("QCQ")
                .pattern("PHP")
                .pattern("QSQ")
                .define('Q', MultiblockBlocks.QUANTUM_HYPER_MECHANICAL_CASING.get())
                .define('C', ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())
                .define('P', ModItems.DIMENSIONAL_PROCESSOR.get())
                .define('H', MultiblockBlocks.QUANTUM_PROCESSOR_ASSEMBLER_CONTROLLER.get())
                .define('S', MultiblockBlocks.QUANTUM_SLICER_CONTROLLER.get())
                .unlockedBy("has_quantum_processor_assembler", has(MultiblockBlocks.QUANTUM_PROCESSOR_ASSEMBLER_CONTROLLER.get()))
                .save(c);
    }

    private void buildUniversalMultiblockRecipes(RecipeOutput c) {
        UniversalMultiblockRecipeBuilder.create("universal/quantum_processor_assembler/dimensional_processor", UniversalMultiblockMachineKind.QUANTUM_PROCESSOR_ASSEMBLER)
                .inputItem(ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get(), 64)
                .inputItem(AEItems.SILICON_PRINT, 64)
                .inputItem(AEItems.FLUIX_DUST, 128)
                .outputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), 64)
                .energy(12000000)
                .time(1200)
                .requiredTier(1)
                .save(c);

        UniversalMultiblockRecipeBuilder.create("universal/qmf/stable_coolant_polish", UniversalMultiblockMachineKind.QMF)
                .inputItem(Items.BLUE_ICE, 8)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get(), 1)
                .inputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 4000)
                .outputFluid(ModFluids.SOURCE_STABLE_COOLANT.get(), 4000)
                .energy(1200000)
                .time(300)
                .requiredTier(1)
                .save(c);
    }

    private void buildStorageBlock(RecipeOutput c, ItemLike output, ItemLike cellComponent, ItemLike matrix, ItemLike casing, String name) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output)
                .pattern("CMC")
                .pattern("GDG")
                .pattern("CMC")
                .define('C', cellComponent)
                .define('M', matrix)
                .define('G', casing)
                .define('D', ModItems.DIMENSIONAL_PROCESSOR.get())
                .unlockedBy("has_component", has(matrix))
                .save(c, UfoMod.id(name));
    }

    private void buildStorageUpgrade(RecipeOutput c, ItemLike output, ItemLike previous, ItemLike matrix, ItemLike prestige, String name) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output)
                .pattern("PMP")
                .pattern("DGD")
                .pattern("PXP")
                .define('P', previous)
                .define('M', matrix)
                .define('D', ModItems.DIMENSIONAL_PROCESSOR.get())
                .define('G', ModBlocks.GRAVITON_PLATED_CASING.get())
                .define('X', prestige)
                .unlockedBy("has_prev", has(previous))
                .save(c, UfoMod.id(name));
    }

    private void buildCoProcessor(RecipeOutput c, ItemLike output, ItemLike previous, ItemLike matrix, ItemLike prestige, String name, boolean isFirst) {
        if (isFirst) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output)
                    .pattern("PEP")
                    .pattern("FMF")
                    .pattern("PEP")
                    .define('F', previous)
                    .define('P', ModItems.DIMENSIONAL_PROCESSOR.get())
                    .define('E', AEItems.ENGINEERING_PROCESSOR)
                    .define('M', matrix)
                    .unlockedBy("has_frame", has(previous))
                    .save(c, UfoMod.id(name));
        } else {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output)
                    .pattern("PMP")
                    .pattern("DED")
                    .pattern("PXP")
                    .define('P', previous)
                    .define('M', matrix)
                    .define('D', ModItems.DIMENSIONAL_PROCESSOR.get())
                    .define('E', AEItems.ENGINEERING_PROCESSOR)
                    .define('X', prestige)
                    .unlockedBy("has_prev", has(previous))
                    .save(c, UfoMod.id(name));
        }
    }

    private void buildIngotGenerators(RecipeOutput c) {
        DMARecipeBuilder.create("dma/ingot/white_dwarf_fragment").output(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get()).inputItem(Items.NETHERITE_INGOT, 2).inputItem(Items.BLUE_ICE, 4).inputItem(AEBlocks.SKY_STONE_BLOCK, 4).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 250).energy(100000).time(200).save(c);
        DMARecipeBuilder.create("dma/ingot/neutron_star_fragment").output(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get()).inputItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get(), 4).inputItem(Items.NETHER_STAR).inputItem(ModItems.OBSIDIAN_MATRIX.get()).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 500).energy(500000).time(400).save(c);
        DMARecipeBuilder.create("dma/ingot/pulsar_fragment").output(ModItems.PULSAR_FRAGMENT_INGOT.get()).inputItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get(), 2).inputItem(Items.LODESTONE, 2).inputItem(Items.LIGHTNING_ROD, 4).inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 1000).energy(1000000).time(600).save(c);
    }

    private void createTieredComponent(RecipeOutput c, ItemLike output, ItemLike prevTier, int prevCount, ItemLike rareItem, int rareCount, int processorCount, Supplier<? extends Fluid> fluid, int fluidAmount, int energy, int time) {
        String name = BuiltInRegistries.ITEM.getKey(output.asItem()).getPath();
        DMARecipeBuilder.create("dma/component/" + name).output(output).inputItem(prevTier, prevCount).inputItem(rareItem, rareCount).inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), processorCount).inputFluid(fluid.get(), fluidAmount).energy(energy).time(time).save(c);
    }

    private void createInfinityCellRecipe(RecipeOutput c, ItemLike cellItem, ItemLike targetItem, int matrixCount, int fluidAmount, int energy, int time, String modIdCondition) {
        String name = BuiltInRegistries.ITEM.getKey(cellItem.asItem()).getPath();
        DMARecipeBuilder builder = DMARecipeBuilder.create("dma/infinity_cell/" + name)
                .output(cellItem)
                .inputItem(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get(), matrixCount)
                .inputItem(ModItems.QUANTUM_ANOMALY.get())
                .inputItem(targetItem, 64)
                .inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), fluidAmount)
                .energy(energy)
                .time(time);
        if (modIdCondition != null && !modIdCondition.isEmpty()) {
            builder.save(c.withConditions(new ModLoadedCondition(modIdCondition)));
        } else {
            builder.save(c);
        }
    }

    private void buildMassiveStellarSimulations(RecipeOutput c) {
        StellarSimulationRecipeBuilder.create("stellar_nexus/massive_iron_synthesis")
                .simulationName("Massive Iron Synthesis")
                .output(Items.IRON_INGOT, 15000000)
                .output(Items.REDSTONE, 8000000)
                .output(Items.QUARTZ, 6000000)
                .output(Items.COBBLESTONE, 30000000)
                .output(Items.OBSIDIAN, 4000000)
                .outputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 2000000)
                .outputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 1500000)
                .outputFluid(ModFluids.SOURCE_STABLE_COOLANT.get(), 750000)
                .outputFluid(ModFluids.SOURCE_WHITE_DWARF_FRAGMENT_FLUID.get(), 500000)
                .inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get(), 160)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get(), 1024)
                .inputItem(AEItems.MATTER_BALL.get(), 32768)
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 8000000)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 2000000)
                .fuel(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 150000)
                .coolant(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 100000)
                .fieldLevel(3)
                .energy(3000000000L)
                .time(72000)
                .save(c);

        StellarSimulationRecipeBuilder.create("stellar_nexus/massive_copper_synthesis")
                .simulationName("Massive Copper Synthesis")
                .output(Items.COPPER_INGOT, 15000000)
                .output(Items.REDSTONE, 5000000)
                .output(Items.AMETHYST_SHARD, 3500000)
                .output(Items.GLOWSTONE_DUST, 4500000)
                .output(Items.QUARTZ, 5000000)
                .outputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 1750000)
                .outputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 1250000)
                .outputFluid(ModFluids.SOURCE_STABLE_COOLANT.get(), 600000)
                .outputFluid(ModFluids.SOURCE_PULSAR_FRAGMENT_FLUID.get(), 450000)
                .inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get(), 160)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get(), 768)
                .inputItem(AEItems.MATTER_BALL.get(), 24576)
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 7800000)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 1600000)
                .fuel(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 140000)
                .coolant(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 100000)
                .fieldLevel(3)
                .energy(3200000000L)
                .time(70000)
                .save(c);

        StellarSimulationRecipeBuilder.create("stellar_nexus/massive_gold_synthesis")
                .simulationName("Massive Gold Synthesis")
                .output(Items.GOLD_INGOT, 15000000)
                .output(Items.DIAMOND, 1500000)
                .output(Items.EMERALD, 1500000)
                .output(Items.LAPIS_LAZULI, 9000000)
                .output(Items.GLOWSTONE_DUST, 7000000)
                .outputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 2500000)
                .outputFluid(ModFluids.SOURCE_SPATIAL_FLUID.get(), 900000)
                .outputFluid(ModFluids.SOURCE_STABLE_COOLANT.get(), 900000)
                .outputFluid(ModFluids.SOURCE_NEUTRON_STAR_FRAGMENT_FLUID.get(), 600000)
                .inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get(), 220)
                .inputItem(ModItems.QUANTUM_ANOMALY.get(), 64)
                .inputItem(AEItems.MATTER_BALL.get(), 32768)
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 9500000)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 2500000)
                .fuel(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 220000)
                .coolant(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 150000)
                .fieldLevel(3)
                .energy(4200000000L)
                .time(84000)
                .save(c);

        StellarSimulationRecipeBuilder.create("stellar_nexus/massive_netherite_synthesis")
                .simulationName("Massive Netherite Synthesis")
                .output(Items.NETHERITE_INGOT, 5000000)
                .output(Items.NETHERITE_SCRAP, 12000000)
                .output(Items.ANCIENT_DEBRIS, 3000000)
                .output(Items.GOLD_INGOT, 9000000)
                .output(Items.QUARTZ, 10000000)
                .outputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 3000000)
                .outputFluid(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 1200000)
                .outputFluid(ModFluids.SOURCE_SPATIAL_FLUID.get(), 1000000)
                .outputFluid(ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID.get(), 800000)
                .inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get(), 320)
                .inputItem(ModItems.QUANTUM_ANOMALY.get(), 128)
                .inputItem(ModItems.DARK_MATTER.get(), 64)
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 14000000)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 4000000)
                .inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 1500000)
                .fuel(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 320000)
                .coolant(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 250000)
                .fieldLevel(3)
                .energy(6500000000L)
                .time(120000)
                .save(c);
    }

    private void buildStellarNexusRecipes(RecipeOutput c) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MultiblockBlocks.ENTROPY_SINGULARITY_CASING.get(), 4)
                .pattern("WOW")
                .pattern("NGN")
                .pattern("WOW")
                .define('W', ModItems.WHITE_DWARF_FRAGMENT_INGOT.get())
                .define('O', ModItems.OBSIDIAN_MATRIX.get())
                .define('N', ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get())
                .define('G', ModBlocks.GRAVITON_PLATED_CASING.get())
                .unlockedBy("has_graviton_casing", has(ModBlocks.GRAVITON_PLATED_CASING.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MultiblockBlocks.ENTROPY_ASSEMBLER_CORE_CASING.get(), 2)
                .pattern("EPE")
                .pattern("DLD")
                .pattern("EPE")
                .define('E', MultiblockBlocks.ENTROPY_SINGULARITY_CASING.get())
                .define('P', ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get())
                .define('D', ModItems.DIMENSIONAL_PROCESSOR.get())
                .define('L', ModBlocks.QUANTUM_LATTICE_FRAME.get())
                .unlockedBy("has_entropy_casing", has(MultiblockBlocks.ENTROPY_SINGULARITY_CASING.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MultiblockBlocks.ENTROPY_COMPUTER_CONDENSATION_MATRIX.get(), 2)
                .pattern("HTH")
                .pattern("AQA")
                .pattern("HTH")
                .define('H', ModItems.HYPER_DENSE_COMPONENT_MATRIX.get())
                .define('T', ModItems.TESSERACT_COMPONENT_MATRIX.get())
                .define('A', ModItems.QUANTUM_ANOMALY.get())
                .define('Q', MultiblockBlocks.ENTROPY_ASSEMBLER_CORE_CASING.get())
                .unlockedBy("has_entropy_core", has(MultiblockBlocks.ENTROPY_ASSEMBLER_CORE_CASING.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MultiblockBlocks.QUANTUM_ENTROPY_CASING.get(), 2)
                .pattern("ECE")
                .pattern("NQN")
                .pattern("ECE")
                .define('E', ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get())
                .define('C', ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())
                .define('N', ModItems.NEUTRON_STAR_MATTER.get())
                .define('Q', MultiblockBlocks.ENTROPY_COMPUTER_CONDENSATION_MATRIX.get())
                .unlockedBy("has_condensation_matrix", has(MultiblockBlocks.ENTROPY_COMPUTER_CONDENSATION_MATRIX.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MultiblockBlocks.ME_MASSIVE_INPUT_HATCH.get())
                .pattern("SES")
                .pattern("LCL")
                .pattern("SDS")
                .define('S', MultiblockBlocks.ENTROPY_SINGULARITY_CASING.get())
                .define('E', ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get())
                .define('L', ModItems.DIMENSIONAL_PROCESSOR.get())
                .define('C', ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get())
                .define('D', ModItems.WHITE_DWARF_MATTER.get())
                .unlockedBy("has_entropy_casing", has(MultiblockBlocks.ENTROPY_SINGULARITY_CASING.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MultiblockBlocks.ME_MASSIVE_OUTPUT_HATCH.get())
                .pattern("SES")
                .pattern("LCL")
                .pattern("SDS")
                .define('S', MultiblockBlocks.ENTROPY_SINGULARITY_CASING.get())
                .define('E', ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get())
                .define('L', ModItems.DIMENSIONAL_PROCESSOR.get())
                .define('C', ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())
                .define('D', ModItems.PULSAR_MATTER.get())
                .unlockedBy("has_entropy_casing", has(MultiblockBlocks.ENTROPY_SINGULARITY_CASING.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get())
                .pattern("SES")
                .pattern("LCL")
                .pattern("SDS")
                .define('S', MultiblockBlocks.ENTROPY_SINGULARITY_CASING.get())
                .define('E', ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get())
                .define('L', ModItems.DIMENSIONAL_PROCESSOR.get())
                .define('C', ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get())
                .define('D', ModItems.NEUTRON_STAR_MATTER.get())
                .unlockedBy("has_entropy_casing", has(MultiblockBlocks.ENTROPY_SINGULARITY_CASING.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get())
                .pattern("SES")
                .pattern("LCL")
                .pattern("SDS")
                .define('S', MultiblockBlocks.ENTROPY_SINGULARITY_CASING.get())
                .define('E', ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get())
                .define('L', ModItems.DIMENSIONAL_PROCESSOR.get())
                .define('C', AEBlocks.CONTROLLER)
                .define('D', ModItems.UU_MATTER_CRYSTAL.get())
                .unlockedBy("has_entropy_casing", has(MultiblockBlocks.ENTROPY_SINGULARITY_CASING.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get())
                .pattern("PAP")
                .pattern("ECE")
                .pattern("PQP")
                .define('P', ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get())
                .define('A', MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get())
                .define('E', MultiblockBlocks.ENTROPY_SINGULARITY_CASING.get())
                .define('C', MultiblockBlocks.ENTROPY_ASSEMBLER_CORE_CASING.get())
                .define('Q', ModItems.NEUTRONIUM_SPHERE.get())
                .unlockedBy("has_energy_hatch", has(MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get())
                .pattern("HTH")
                .pattern("FGF")
                .pattern("ICI")
                .define('H', ModItems.HYPER_DENSE_COMPONENT_MATRIX.get())
                .define('T', ModItems.TESSERACT_COMPONENT_MATRIX.get())
                .define('F', MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get())
                .define('G', MultiblockBlocks.ME_MASSIVE_INPUT_HATCH.get())
                .define('I', ModItems.ENRICHED_NEUTRONIUM_SPHERE.get())
                .define('C', ModItems.WHITE_DWARF_MATTER.get())
                .unlockedBy("has_field_generator_t1", has(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get())
                .pattern("ECE")
                .pattern("FGF")
                .pattern("DAD")
                .define('E', ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get())
                .define('C', ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())
                .define('F', MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get())
                .define('G', MultiblockBlocks.ME_MASSIVE_OUTPUT_HATCH.get())
                .define('D', ModItems.DARK_MATTER.get())
                .define('A', MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get())
                .unlockedBy("has_field_generator_t2", has(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MultiblockBlocks.STELLAR_NEXUS_CONTROLLER.get())
                .pattern("CFC")
                .pattern("ATA")
                .pattern("CHC")
                .define('C', MultiblockBlocks.ENTROPY_COMPUTER_CONDENSATION_MATRIX.get())
                .define('F', MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get())
                .define('A', ModItems.QUANTUM_ANOMALY.get())
                .define('T', MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get())
                .define('H', MultiblockBlocks.ME_MASSIVE_OUTPUT_HATCH.get())
                .unlockedBy("has_field_generator_t3", has(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get()))
                .save(c);
    }

    private void buildToolAndUtilityRecipes(RecipeOutput c) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BISMUTH.get(), 2)
                .requires(Items.AMETHYST_SHARD)
                .requires(Items.COPPER_INGOT)
                .requires(ModItems.WHITE_DWARF_FRAGMENT_DUST.get())
                .unlockedBy("has_amethyst", has(Items.AMETHYST_SHARD))
                .save(c, UfoMod.id("bismuth"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.STRUCTURE_SCANNER.get())
                .pattern(" Q ")
                .pattern("SDE")
                .pattern(" O ")
                .define('Q', ModItems.QUANTUM_ANOMALY.get())
                .define('S', AEItems.SINGULARITY)
                .define('D', ModItems.DIMENSIONAL_PROCESSOR.get())
                .define('E', AEBlocks.CONTROLLER)
                .define('O', ModItems.OBSIDIAN_MATRIX.get())
                .unlockedBy("has_dimensional_processor", has(ModItems.DIMENSIONAL_PROCESSOR.get()))
                .save(c);

        this.buildToolRecipe(c, ModTools.UFO_SWORD.get(), "ufo_sword", ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get(), ModItems.WHITE_DWARF_FRAGMENT_INGOT.get());
        this.buildToolRecipe(c, ModTools.UFO_PICKAXE.get(), "ufo_pickaxe", ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get(), ModItems.WHITE_DWARF_FRAGMENT_INGOT.get());
        this.buildToolRecipe(c, ModTools.UFO_AXE.get(), "ufo_axe", ModItems.HYPER_DENSE_COMPONENT_MATRIX.get(), ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get());
        this.buildToolRecipe(c, ModTools.UFO_SHOVEL.get(), "ufo_shovel", ModItems.HYPER_DENSE_COMPONENT_MATRIX.get(), ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get());
        this.buildToolRecipe(c, ModTools.UFO_HOE.get(), "ufo_hoe", ModItems.TESSERACT_COMPONENT_MATRIX.get(), ModItems.PULSAR_FRAGMENT_INGOT.get());
        this.buildToolRecipe(c, ModTools.UFO_BOW.get(), "ufo_bow", ModItems.TESSERACT_COMPONENT_MATRIX.get(), ModItems.PULSAR_FRAGMENT_INGOT.get());
        this.buildToolRecipe(c, ModTools.UFO_FISHING_ROD.get(), "ufo_fishing_rod", ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get(), ModItems.WHITE_DWARF_MATTER.get());
        this.buildToolRecipe(c, ModTools.UFO_HAMMER.get(), "ufo_hammer", ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get(), ModItems.NEUTRON_STAR_MATTER.get());
        this.buildToolRecipe(c, ModTools.UFO_GREATSWORD.get(), "ufo_greatsword", ModItems.COSMIC_STRING_COMPONENT_MATRIX.get(), ModItems.PULSAR_MATTER.get());
    }

    private void buildToolRecipe(RecipeOutput c, ItemLike output, String name, ItemLike matrix, ItemLike prestige) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, output)
                .pattern(" M ")
                .pattern("SPS")
                .pattern(" D ")
                .define('M', matrix)
                .define('S', ModTools.UFO_STAFF.get())
                .define('P', prestige)
                .define('D', ModItems.DIMENSIONAL_PROCESSOR.get())
                .unlockedBy("has_staff", has(ModTools.UFO_STAFF.get()))
                .save(c, UfoMod.id(name));
    }

    private void buildStellarSimulationRecipes(RecipeOutput c) {
        StellarSimulationRecipeBuilder.create("stellar_simulation/massive_iron_synthesis")
                .simulationName("Massive Iron Synthesis")
                .output(Items.IRON_INGOT, 15000000)
                .output(Items.REDSTONE, 8000000)
                .output(Items.QUARTZ, 6000000)
                .output(Items.COBBLESTONE, 30000000)
                .output(Items.OBSIDIAN, 4000000)
                .outputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 2000000)
                .outputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 1500000)
                .outputFluid(ModFluids.SOURCE_STABLE_COOLANT.get(), 750000)
                .outputFluid(ModFluids.SOURCE_WHITE_DWARF_FRAGMENT_FLUID.get(), 500000)
                .inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get(), 160)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get(), 1024)
                .inputItem(AEItems.MATTER_BALL.get(), 32768)
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 8000000)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 2000000)
                .fuel(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 150000)
                .coolant(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 100000)
                .fieldLevel(3)
                .energy(3000000000L)
                .time(72000)
                .save(c);

        StellarSimulationRecipeBuilder.create("stellar_simulation/massive_copper_synthesis")
                .simulationName("Massive Copper Synthesis")
                .output(Items.COPPER_INGOT, 15000000)
                .output(Items.REDSTONE, 5000000)
                .output(Items.AMETHYST_SHARD, 3500000)
                .output(Items.GLOWSTONE_DUST, 4500000)
                .output(Items.QUARTZ, 5000000)
                .outputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 1750000)
                .outputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 1250000)
                .outputFluid(ModFluids.SOURCE_STABLE_COOLANT.get(), 600000)
                .outputFluid(ModFluids.SOURCE_PULSAR_FRAGMENT_FLUID.get(), 450000)
                .inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get(), 160)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get(), 768)
                .inputItem(AEItems.MATTER_BALL.get(), 24576)
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 7800000)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 1600000)
                .fuel(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 140000)
                .coolant(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 100000)
                .fieldLevel(3)
                .energy(3200000000L)
                .time(70000)
                .save(c);

        StellarSimulationRecipeBuilder.create("stellar_simulation/massive_gold_synthesis")
                .simulationName("Massive Gold Synthesis")
                .output(Items.GOLD_INGOT, 15000000)
                .output(Items.DIAMOND, 1500000)
                .output(Items.EMERALD, 1500000)
                .output(Items.LAPIS_LAZULI, 9000000)
                .output(Items.GLOWSTONE_DUST, 7000000)
                .outputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 2500000)
                .outputFluid(ModFluids.SOURCE_SPATIAL_FLUID.get(), 900000)
                .outputFluid(ModFluids.SOURCE_STABLE_COOLANT.get(), 900000)
                .outputFluid(ModFluids.SOURCE_NEUTRON_STAR_FRAGMENT_FLUID.get(), 600000)
                .inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get(), 220)
                .inputItem(ModItems.QUANTUM_ANOMALY.get(), 64)
                .inputItem(AEItems.MATTER_BALL.get(), 32768)
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 9500000)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 2500000)
                .fuel(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 220000)
                .coolant(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 150000)
                .fieldLevel(3)
                .energy(4200000000L)
                .time(84000)
                .save(c);

        StellarSimulationRecipeBuilder.create("stellar_simulation/massive_netherite_synthesis")
                .simulationName("Massive Netherite Synthesis")
                .output(Items.NETHERITE_INGOT, 5000000)
                .output(Items.NETHERITE_SCRAP, 12000000)
                .output(Items.ANCIENT_DEBRIS, 3000000)
                .output(Items.GOLD_INGOT, 9000000)
                .output(Items.QUARTZ, 10000000)
                .outputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 3000000)
                .outputFluid(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 1200000)
                .outputFluid(ModFluids.SOURCE_SPATIAL_FLUID.get(), 1000000)
                .outputFluid(ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID.get(), 800000)
                .inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get(), 320)
                .inputItem(ModItems.QUANTUM_ANOMALY.get(), 128)
                .inputItem(ModItems.DARK_MATTER.get(), 64)
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 14000000)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 4000000)
                .inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 1500000)
                .fuel(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 320000)
                .coolant(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 250000)
                .fieldLevel(3)
                .energy(6500000000L)
                .time(120000)
                .save(c);

        StellarSimulationRecipeBuilder.create("stellar_simulation/stellar_synthesis")
                .simulationName("Stellar Synthesis")
                .output(ModItems.PULSAR_FRAGMENT_DUST.get(), 12000000)
                .output(ModItems.WHITE_DWARF_MATTER.get(), 7000000)
                .output(ModItems.NEUTRON_STAR_MATTER.get(), 5000000)
                .output(ModItems.PULSAR_MATTER.get(), 3500000)
                .output(ModItems.QUANTUM_ANOMALY.get(), 500000)
                .outputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 1000000)
                .outputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 600000)
                .outputFluid(ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID.get(), 300000)
                .outputFluid(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 250000)
                .inputItem(ModItems.CHARGED_ENRICHED_NEUTRONIUM_SPHERE.get(), 64)
                .inputItem(ModItems.AETHER_CONTAINMENT_CAPSULE.get(), 32)
                .inputItem(ModItems.DARK_MATTER.get(), 16)
                .inputItem(ModItems.QUANTUM_ANOMALY.get(), 64)
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 2500000)
                .inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 1000000)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 500000)
                .fuel(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 180000)
                .coolant(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 220000)
                .fieldLevel(3)
                .energy(2500000000L)
                .time(72000)
                .save(c);

        StellarSimulationRecipeBuilder.create("stellar_simulation/advancedae_quantum")
                .simulationName("Quantum Alloy Infusion")
                .output(BuiltInRegistries.ITEM.get(ResourceLocation.parse("advanced_ae:quantum_alloy")), 192)
                .output(BuiltInRegistries.ITEM.get(ResourceLocation.parse("advanced_ae:printed_quantum_processor")), 96)
                .output(BuiltInRegistries.ITEM.get(ResourceLocation.parse("advanced_ae:quantum_processor")), 48)
                .output(AEItems.SINGULARITY, 16)
                .output(ModItems.DIMENSIONAL_PROCESSOR.get(), 32)
                .outputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 75000)
                .outputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 50000)
                .outputFluid(ModFluids.SOURCE_SPATIAL_FLUID.get(), 25000)
                .outputFluid(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 25000)
                .inputItem(BuiltInRegistries.ITEM.get(ResourceLocation.parse("advanced_ae:quantum_infused_dust")), 256)
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), 64)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get(), 32)
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 250000)
                .inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 100000)
                .fuel(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 150000)
                .coolant(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 250000)
                .fieldLevel(3)
                .energy(150000000L)
                .time(12000)
                .save(c.withConditions(new ModLoadedCondition("advanced_ae")));

        StellarSimulationRecipeBuilder.create("stellar_simulation/ae2_singularity")
                .simulationName("Quantum Singularity Collapse")
                .output(AEItems.SINGULARITY, 128)
                .output(AEItems.MATTER_BALL.get(), 2048)
                .output(AEItems.SKY_DUST, 4096)
                .output(AEItems.CERTUS_QUARTZ_CRYSTAL, 4096)
                .output(AEItems.FLUIX_CRYSTAL, 2048)
                .outputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 50000)
                .outputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 75000)
                .outputFluid(ModFluids.SOURCE_SPATIAL_FLUID.get(), 20000)
                .outputFluid(ModFluids.SOURCE_STABLE_COOLANT.get(), 25000)
                .inputItem(AEItems.MATTER_BALL.get(), 4096)
                .inputItem(ModItems.QUANTUM_ANOMALY.get(), 8)
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 100000)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 50000)
                .fuel(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 50000)
                .coolant(ModFluids.SOURCE_STABLE_COOLANT.get(), 100000)
                .coolingLevel(2)
                .fieldLevel(2)
                .energy(50000000L)
                .time(12000)
                .save(c.withConditions(new ModLoadedCondition("ae2")));

        StellarSimulationRecipeBuilder.create("stellar_simulation/extendedae_entro")
                .simulationName("Entro Crystal Synthesis")
                .output(BuiltInRegistries.ITEM.get(ResourceLocation.parse("extendedae:entro_crystal")), 96)
                .output(BuiltInRegistries.ITEM.get(ResourceLocation.parse("extendedae:concurrent_processor_print")), 128)
                .output(BuiltInRegistries.ITEM.get(ResourceLocation.parse("extendedae:concurrent_processor")), 64)
                .output(AEItems.SINGULARITY, 24)
                .output(ModItems.QUANTUM_ANOMALY.get(), 16)
                .outputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 100000)
                .outputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 75000)
                .outputFluid(ModFluids.SOURCE_SPATIAL_FLUID.get(), 50000)
                .outputFluid(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 50000)
                .inputItem(BuiltInRegistries.ITEM.get(ResourceLocation.parse("extendedae:entro_dust")), 128)
                .inputItem(ModItems.QUANTUM_ANOMALY.get(), 32)
                .inputItem(ModItems.DIMENSIONAL_PROCESSOR.get(), 64)
                .inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 150000)
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 250000)
                .fuel(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 250000)
                .coolant(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 300000)
                .fieldLevel(3)
                .energy(220000000L)
                .time(18000)
                .save(c.withConditions(new ModLoadedCondition("extendedae")));

        StellarSimulationRecipeBuilder.create("stellar_simulation/megacells_skysteel")
                .simulationName("Sky Steel Forging")
                .output(BuiltInRegistries.ITEM.get(ResourceLocation.parse("megacells:sky_steel_block")), 64)
                .output(BuiltInRegistries.ITEM.get(ResourceLocation.parse("megacells:printed_accumulation_processor")), 32)
                .output(BuiltInRegistries.ITEM.get(ResourceLocation.parse("megacells:accumulation_processor")), 16)
                .output(AEItems.SKY_DUST, 256)
                .output(Items.IRON_INGOT, 128)
                .outputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 16000)
                .outputFluid(ModFluids.SOURCE_WHITE_DWARF_FRAGMENT_FLUID.get(), 8000)
                .outputFluid(ModFluids.SOURCE_STABLE_COOLANT.get(), 4000)
                .outputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 4000)
                .inputItem(AEBlocks.SKY_STONE_BLOCK, 64)
                .inputItem(Items.IRON_BLOCK, 64)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get(), 16)
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 20000)
                .fuel(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 20000)
                .coolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 40000)
                .coolingLevel(1)
                .fieldLevel(1)
                .energy(15000000L)
                .time(4000)
                .save(c.withConditions(new ModLoadedCondition("megacells")));

        StellarSimulationRecipeBuilder.create("stellar_simulation/mekanism_ethylene")
                .simulationName("Ethylene Enrichment")
                .output(BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:hdpe_sheet")), 512)
                .output(BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:hdpe_pellet")), 2048)
                .output(BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:substrate")), 1024)
                .output(ModItems.DUST_BLIZZ.get(), 128)
                .output(ModItems.DUST_CRYOTHEUM.get(), 64)
                .outputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 20000)
                .outputFluid(ModFluids.SOURCE_STABLE_COOLANT.get(), 12000)
                .outputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 12000)
                .outputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 8000)
                .inputItem(BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:substrate")), 256)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get(), 16)
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 10000)
                .fuel("mekanism:ethene", 40000)
                .coolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 50000)
                .coolingLevel(1)
                .fieldLevel(1)
                .energy(6000000L)
                .time(8000)
                .save(c.withConditions(new ModLoadedCondition("mekanism")));

        StellarSimulationRecipeBuilder.create("stellar_simulation/mekanism_fission")
                .simulationName("Fission Reactor Process")
                .output(BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:pellet_antimatter")), 64)
                .output(BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:pellet_polonium")), 192)
                .output(BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:pellet_plutonium")), 128)
                .output(BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:hdpe_sheet")), 64)
                .output(BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:substrate")), 128)
                .outputFluid(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 50000)
                .outputFluid(ModFluids.SOURCE_STABLE_COOLANT.get(), 40000)
                .outputFluid(ModFluids.SOURCE_SPATIAL_FLUID.get(), 30000)
                .outputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 25000)
                .inputItem(BuiltInRegistries.ITEM.get(ResourceLocation.parse("mekanism:pellet_plutonium")), 64)
                .inputItem(ModItems.QUANTUM_ANOMALY.get(), 32)
                .inputFluid(BuiltInRegistries.FLUID.get(ResourceLocation.parse("mekanism:sulfuric_acid")), 20000)
                .fuel("mekanism:sulfuric_acid", 80000)
                .coolant(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 250000)
                .fieldLevel(3)
                .energy(150000000L)
                .time(24000)
                .save(c.withConditions(new ModLoadedCondition("mekanism")));
    }
}
