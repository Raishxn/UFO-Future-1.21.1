package com.raishxn.ufo.event;

// Imports de Minecraft e NeoForge
import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import com.raishxn.ufo.core.MegaCoProcessorTier;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import gripe._90.megacells.definition.MEGABlocks;
import mekanism.common.registries.MekanismItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.NotCondition;

// Imports diretos de mods para compatibilidade (Estilo EAE)
import appeng.core.definitions.AEItems;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipeBuilder;
import com.glodblock.github.extendedae.common.EAESingletons;
import com.glodblock.github.extendedae.recipe.CircuitCutterRecipeBuilder;
import com.glodblock.github.extendedae.recipe.CrystalAssemblerRecipeBuilder;
import com.glodblock.github.glodium.util.GlodUtil;
import gripe._90.megacells.definition.MEGAItems;

// Imports do seu mod
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.item.ModCellItems;
import com.raishxn.ufo.item.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput c) {
        // ========================================================================================
        // --- RECEITAS DO INSCRIPTOR (AE2 PADRÃO) ---
        // ========================================================================================

        InscriberRecipeBuilder.inscribe(AEItems.SINGULARITY, ModItems.DIMENSIONAL_PROCESSOR_PRESS.get(), 1)
                .setTop(Ingredient.of(AEItems.ENGINEERING_PROCESSOR_PRESS))
                .setBottom(Ingredient.of(AEItems.CALCULATION_PROCESSOR_PRESS))
                .setMode(InscriberProcessType.PRESS)
                .save(c, UfoMod.id("inscriber/dimensional_processor_press"));

        InscriberRecipeBuilder.inscribe(ModItems.WHITE_DWARF_FRAGMENT_INGOT, ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get(), 1)
                .setTop(Ingredient.of(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .setMode(InscriberProcessType.INSCRIBE)
                .save(c, UfoMod.id("inscriber/printed_dimensional_processor"));

        InscriberRecipeBuilder.inscribe(AEItems.FLUIX_DUST, ModItems.DIMENSIONAL_PROCESSOR.get(), 1)
                .setTop(Ingredient.of(ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get()))
                .setBottom(Ingredient.of(AEItems.SILICON_PRINT))
                .setMode(InscriberProcessType.PRESS)
                .save(c, UfoMod.id("inscriber/dimensional_processor"));

        // ========================================================================================
        // --- RECEITAS PRINCIPAIS E DE COMPONENTES ---
        // ========================================================================================

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ACA")
                .define('A', Items.NETHER_STAR)
                .define('B', ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get())
                .define('C', AEItems.CELL_COMPONENT_256K)
                .define('D', AEBlocks.QUARTZ_VIBRANT_GLASS)
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c.withConditions(new NotCondition(new ModLoadedCondition("megacells"))),
                        UfoMod.id("phase_shift_component_matrix_default"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HYPER_DENSE_COMPONENT_MATRIX.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ACA")
                .define('A', AEItems.ENDER_DUST)
                .define('B', ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get())
                .define('C', ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get())
                .define('D', AEBlocks.QUARTZ_VIBRANT_GLASS)
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TESSERACT_COMPONENT_MATRIX.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ACA")
                .define('A', AEItems.ENDER_DUST)
                .define('B', ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get())
                .define('C', ModItems.HYPER_DENSE_COMPONENT_MATRIX.get())
                .define('D', AEBlocks.QUARTZ_VIBRANT_GLASS)
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ACA")
                .define('A', AEItems.SINGULARITY)
                .define('B', ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get())
                .define('C', ModItems.TESSERACT_COMPONENT_MATRIX.get())
                .define('D', AEBlocks.QUARTZ_VIBRANT_GLASS)
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ACA")
                .define('A', AEItems.SINGULARITY)
                .define('B', ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get())
                .define('C', ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get())
                .define('D', AEBlocks.QUARTZ_VIBRANT_GLASS)
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.WHITE_DWARF_FRAGMENT_INGOT.get(), 9)
                .requires(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK.get())
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.WHITE_DWARF_FRAGMENT_INGOT.get())
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1B).get())
                .requires(ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get())
                .requires(ModBlocks.GRAVITON_PLATED_CASING.get())
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("1b_mega_crafting_storage_from_casing"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_50B).get())
                .requires(ModItems.HYPER_DENSE_COMPONENT_MATRIX.get())
                .requires(ModBlocks.GRAVITON_PLATED_CASING.get())
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("50b_mega_crafting_storage_from_casing"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1T).get())
                .requires(ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get())
                .requires(ModBlocks.GRAVITON_PLATED_CASING.get())
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("1t_mega_crafting_storage_from_casing"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_250T).get())
                .requires(ModItems.TESSERACT_COMPONENT_MATRIX.get())
                .requires(ModBlocks.GRAVITON_PLATED_CASING.get())
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("250t_mega_crafting_storage_from_casing"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1QD).get())
                .requires(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())
                .requires(ModBlocks.GRAVITON_PLATED_CASING.get())
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("1qd_mega_crafting_storage_from_casing"));

        createInfinityCellRecipe(c, ModItems.INFINITY_COBBLED_DEEPSLATE_CELL.get(), Items.COBBLED_DEEPSLATE);
        createInfinityCellRecipe(c, ModItems.INFINITY_END_STONE_CELL.get(), Items.END_STONE);
        createInfinityCellRecipe(c, ModItems.INFINITY_NETHERRACK_CELL.get(), Items.NETHERRACK);
        createInfinityCellRecipe(c, ModItems.INFINITY_SAND_CELL.get(), Items.SAND);
        createInfinityCellRecipe(c, ModItems.INFINITY_LAVA_CELL.get(), Items.LAVA_BUCKET);
        createInfinityCellRecipe(c, ModItems.INFINITY_ANTIMATTER_PELLET_CELL.get(), MekanismItems.ANTIMATTER_PELLET);
        createInfinityCellRecipe(c, ModItems.INFINITY_PLUTONIUM_PELLET_CELL.get(), MekanismItems.PLUTONIUM_PELLET.get());
        createInfinityCellRecipe(c, ModItems.INFINITY_POLONIUM_PELLET_CELL.get(), MekanismItems.POLONIUM_PELLET.get());
        createInfinityCellRecipe(c, ModItems.INFINITY_HDPE_PELLET_CELL.get(), MekanismItems.HDPE_PELLET.get());
        createInfinityCellRecipe(c, ModItems.INFINITY_OBSIDIAN_CELL.get(), Items.OBSIDIAN);
        createInfinityCellRecipe(c, ModItems.INFINITY_GRAVEL_CELL.get(), Items.GRAVEL);
        createInfinityCellRecipe(c, ModItems.INFINITY_OAK_LOG_CELL.get(), Items.OAK_LOG);
        createInfinityCellRecipe(c, ModItems.INFINITY_GLASS_CELL.get(), Items.GLASS);
        createInfinityCellRecipe(c, ModItems.INFINITY_AMETHYST_SHARD_CELL.get(), Items.AMETHYST_SHARD);
        createInfinityCellRecipe(c, ModItems.INFINITY_WHITE_DYE_CELL.get(), Items.WHITE_DYE);
        createInfinityCellRecipe(c, ModItems.INFINITY_ORANGE_DYE_CELL.get(), Items.ORANGE_DYE);
        createInfinityCellRecipe(c, ModItems.INFINITY_MAGENTA_DYE_CELL.get(), Items.MAGENTA_DYE);
        createInfinityCellRecipe(c, ModItems.INFINITY_LIGHT_BLUE_DYE_CELL.get(), Items.LIGHT_BLUE_DYE);
        createInfinityCellRecipe(c, ModItems.INFINITY_YELLOW_DYE_CELL.get(), Items.YELLOW_DYE);
        createInfinityCellRecipe(c, ModItems.INFINITY_LIME_DYE_CELL.get(), Items.LIME_DYE);
        createInfinityCellRecipe(c, ModItems.INFINITY_PINK_DYE_CELL.get(), Items.PINK_DYE);
        createInfinityCellRecipe(c, ModItems.INFINITY_GRAY_DYE_CELL.get(), Items.GRAY_DYE);
        createInfinityCellRecipe(c, ModItems.INFINITY_LIGHT_GRAY_DYE_CELL.get(), Items.LIGHT_GRAY_DYE);
        createInfinityCellRecipe(c, ModItems.INFINITY_CYAN_DYE_CELL.get(), Items.CYAN_DYE);
        createInfinityCellRecipe(c, ModItems.INFINITY_PURPLE_DYE_CELL.get(), Items.PURPLE_DYE);
        createInfinityCellRecipe(c, ModItems.INFINITY_BLUE_DYE_CELL.get(), Items.BLUE_DYE);
        createInfinityCellRecipe(c, ModItems.INFINITY_BROWN_DYE_CELL.get(), Items.BROWN_DYE);
        createInfinityCellRecipe(c, ModItems.INFINITY_GREEN_DYE_CELL.get(), Items.GREEN_DYE);
        createInfinityCellRecipe(c, ModItems.INFINITY_RED_DYE_CELL.get(), Items.RED_DYE);
        createInfinityCellRecipe(c, ModItems.INFINITY_BLACK_DYE_CELL.get(), Items.BLACK_DYE);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.INFINITY_SKY_STONE_CELL.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())
                .define('B', Items.NETHER_STAR)
                .define('C', AEBlocks.SKY_STONE_BLOCK)
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c.withConditions(new ModLoadedCondition("ae2")), UfoMod.id("infinity_sky_stone_cell"));

        // ========================================================================================
        // --- RECEITAS DAS CÉLULAS DE ARMAZENAMENTO ---
        // ========================================================================================

        // --- Células de Item (White Dwarf) ---
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModCellItems.ITEM_CELL_40M.get())
                .requires(ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get())
                .requires(ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get())
                .unlockedBy("has_item", has(ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get()))
                .save(c);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModCellItems.ITEM_CELL_100M.get())
                .requires(ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get())
                .requires(ModItems.HYPER_DENSE_COMPONENT_MATRIX.get())
                .unlockedBy("has_item", has(ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get()))
                .save(c);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModCellItems.ITEM_CELL_250M.get())
                .requires(ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get())
                .requires(ModItems.TESSERACT_COMPONENT_MATRIX.get())
                .unlockedBy("has_item", has(ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get()))
                .save(c);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModCellItems.ITEM_CELL_750M.get())
                .requires(ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get())
                .requires(ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get())
                .unlockedBy("has_item", has(ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get()))
                .save(c);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModCellItems.ITEM_CELL_SINGULARITY.get())
                .requires(ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get())
                .requires(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())
                .unlockedBy("has_item", has(ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get()))
                .save(c);

        // --- Células de Fluido (Neutron Star) ---
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModCellItems.FLUID_CELL_40M.get())
                .requires(ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get())
                .requires(ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get())
                .unlockedBy("has_item", has(ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get()))
                .save(c);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModCellItems.FLUID_CELL_100M.get())
                .requires(ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get())
                .requires(ModItems.HYPER_DENSE_COMPONENT_MATRIX.get())
                .unlockedBy("has_item", has(ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get()))
                .save(c);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModCellItems.FLUID_CELL_250M.get())
                .requires(ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get())
                .requires(ModItems.TESSERACT_COMPONENT_MATRIX.get())
                .unlockedBy("has_item", has(ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get()))
                .save(c);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModCellItems.FLUID_CELL_750M.get())
                .requires(ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get())
                .requires(ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get())
                .unlockedBy("has_item", has(ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get()))
                .save(c);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModCellItems.FLUID_CELL_SINGULARITY.get())
                .requires(ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get())
                .requires(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())
                .unlockedBy("has_item", has(ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get()))
                .save(c);

        // ========================================================================================
        // --- CHAMADAS DE COMPATIBILIDADE ---
        // ========================================================================================
        if (GlodUtil.checkMod("megacells")) {
            megaCellsRecipes(c);
        }
        if (GlodUtil.checkMod("extendedae")) {
            extendedAERecipes(c);
        }
    }

    private void megaCellsRecipes(RecipeOutput c) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ACA")
                .define('A', Items.NETHER_STAR)
                .define('B', ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get())
                .define('C', MEGAItems.CELL_COMPONENT_256M)
                .define('D', AEBlocks.QUARTZ_VIBRANT_GLASS)
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("phase_shift_component_matrix_megacells_compat"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.GRAVITON_PLATED_CASING.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ABA")
                .define('A', MEGABlocks.MEGA_CRAFTING_UNIT)
                .define('B', MEGAItems.ACCUMULATION_PROCESSOR.get())
                .define('C', AEParts.SMART_DENSE_CABLE.item(AEColor.TRANSPARENT))
                .define('D', ModItems.DIMENSIONAL_PROCESSOR)
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("graviton_plated_casing_megacells_compat"));

        // --- INÍCIO DA CORREÇÃO ---
        // Cria um novo 'RecipeOutput' que só será usado se 'megacells' estiver carregado
        // E 'extendedae' NÃO estiver carregado.
        RecipeOutput megaOnlyOutput = c.withConditions(new ModLoadedCondition("megacells"))
                .withConditions(new NotCondition(new ModLoadedCondition("extendedae")));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_50M).get())
                .requires(AEItems.LOGIC_PROCESSOR.get())
                .requires(AEItems.ENGINEERING_PROCESSOR.get())
                .requires(AEItems.CALCULATION_PROCESSOR.get())
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get())
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get())
                .requires(ModBlocks.QUANTUM_LATTICE_FRAME.get())
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(megaOnlyOutput, UfoMod.id("50m_co_processor_compat_mega"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_150M).get())
                .requires(AEItems.LOGIC_PROCESSOR.get())
                .requires(AEItems.ENGINEERING_PROCESSOR.get())
                .requires(AEItems.CALCULATION_PROCESSOR.get())
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get())
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get())
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_50M).get())
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(megaOnlyOutput, UfoMod.id("150m_co_processor_compat_mega"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_300M).get())
                .requires(AEItems.LOGIC_PROCESSOR.get())
                .requires(AEItems.ENGINEERING_PROCESSOR.get())
                .requires(AEItems.CALCULATION_PROCESSOR.get())
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get())
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get())
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_150M).get())
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(megaOnlyOutput, UfoMod.id("300m_co_processor_compat_mega"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_750M).get())
                .requires(AEItems.LOGIC_PROCESSOR.get())
                .requires(AEItems.ENGINEERING_PROCESSOR.get())
                .requires(AEItems.CALCULATION_PROCESSOR.get())
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get())
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get())
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_300M).get())
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(megaOnlyOutput, UfoMod.id("750m_co_processor_compat_mega"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_2B).get())
                .requires(AEItems.LOGIC_PROCESSOR.get())
                .requires(AEItems.ENGINEERING_PROCESSOR.get())
                .requires(AEItems.CALCULATION_PROCESSOR.get())
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get())
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get())
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_750M).get())
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(megaOnlyOutput, UfoMod.id("2b_co_processor_compat_mega"));
        // --- FIM DA CORREÇÃO ---
    }

    private void extendedAERecipes(RecipeOutput c) {
        // ========================================================================================
        // --- RECEITAS DOS HOUSINGS (Compatibilidade com ExtendedAE) ---
        // ========================================================================================
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get())
                .pattern("ABA")
                .pattern("B B")
                .pattern("CCC")
                .define('A', EAESingletons.ASSEMBLER_MATRIX_GLASS)
                .define('B', EAESingletons.ENTRO_DUST)
                .define('C', ModItems.WHITE_DWARF_FRAGMENT_INGOT.get())
                .unlockedBy("has_item", has(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get()))
                .save(c, UfoMod.id("white_dwarf_item_cell_housing_eae_compat"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get())
                .pattern("ABA")
                .pattern("B B")
                .pattern("CCC")
                .define('A', EAESingletons.ASSEMBLER_MATRIX_GLASS)
                .define('B', EAESingletons.ENTRO_DUST)
                .define('C', ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get())
                .unlockedBy("has_item", has(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get()))
                .save(c, UfoMod.id("neutron_fluid_cell_housing_eae_compat"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModCellItems.PULSAR_CELL_HOUSING.get())
                .pattern("ABA")
                .pattern("B B")
                .pattern("CCC")
                .define('A', EAESingletons.ASSEMBLER_MATRIX_GLASS)
                .define('B', EAESingletons.ENTRO_DUST)
                .define('C', ModItems.PULSAR_FRAGMENT_INGOT.get())
                .unlockedBy("has_item", has(ModItems.PULSAR_FRAGMENT_INGOT.get()))
                .save(c, UfoMod.id("pulsar_cell_housing_eae_compat"));


        CrystalAssemblerRecipeBuilder
                .assemble(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get(), 4)
                .input(AEItems.SINGULARITY, 4)
                .input(Tags.Items.INGOTS_NETHERITE, 4)
                .input(EAESingletons.ENTRO_INGOT, 4)
                .fluid(Fluids.LAVA, 10000)
                .save(c, UfoMod.id("assembler/white_dwarf_fragment_ingot_eae_compat"));

        CrystalAssemblerRecipeBuilder
                .assemble(ModItems.DIMENSIONAL_PROCESSOR.get(), 2)
                .input(ModItems.PRINTED_DIMENSIONAL_PROCESSOR, 4)
                .input(EAESingletons.CONCURRENT_PROCESSOR_PRINT, 4)
                .input(EAESingletons.ENTRO_DUST, 4)
                .fluid(Fluids.LAVA, 2000)
                .save(c, UfoMod.id("assembler/dimensional_processor_eae_compat"));

        CrystalAssemblerRecipeBuilder
                .assemble(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get(), 1)
                .input(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK, 4)
                .input(MEGAItems.ACCUMULATION_PROCESSOR_PRESS, 2)
                .input(MEGABlocks.SKY_STEEL_BLOCK, 4)
                .input(EAESingletons.CONCURRENT_PROCESSOR_PRESS, 2)
                .input(EAESingletons.ENTRO_BLOCK, 4)
                .input(Items.NETHER_STAR, 19)
                .fluid(Fluids.LAVA, 10000)
                .save(c, UfoMod.id("assembler/dimensional_processor_press_eae_compat"));

        CircuitCutterRecipeBuilder
                .cut(ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get(), 9)
                .input(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK.get())
                .save(c, UfoMod.id("cutter/printed_dimensional_processor_eae_compat"));

        // --- INÍCIO DA CORREÇÃO ---
        // Cria um novo 'RecipeOutput' que só será usado se AMBOS 'extendedae' E 'megacells'
        // estiverem carregados.
        RecipeOutput extendedAndMegaOutput = c.withConditions(new ModLoadedCondition("extendedae"))
                .withConditions(new ModLoadedCondition("megacells"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_50M).get())
                .requires(AEItems.LOGIC_PROCESSOR.get())
                .requires(AEItems.ENGINEERING_PROCESSOR.get())
                .requires(AEItems.CALCULATION_PROCESSOR.get())
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get())
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get())
                .requires(EAESingletons.CONCURRENT_PROCESSOR)
                .requires(ModBlocks.QUANTUM_LATTICE_FRAME.get())
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(extendedAndMegaOutput, UfoMod.id("50m_co_processor_compat_extended"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_150M).get())
                .requires(AEItems.LOGIC_PROCESSOR.get())
                .requires(AEItems.ENGINEERING_PROCESSOR.get())
                .requires(AEItems.CALCULATION_PROCESSOR.get())
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get())
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get())
                .requires(EAESingletons.CONCURRENT_PROCESSOR)
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_50M).get())
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(extendedAndMegaOutput, UfoMod.id("150m_co_processor_compat_extended"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_300M).get())
                .requires(AEItems.LOGIC_PROCESSOR.get())
                .requires(AEItems.ENGINEERING_PROCESSOR.get())
                .requires(AEItems.CALCULATION_PROCESSOR.get())
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get())
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get())
                .requires(EAESingletons.CONCURRENT_PROCESSOR)
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_150M).get())
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(extendedAndMegaOutput, UfoMod.id("300m_co_processor_compat_extended"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_750M).get())
                .requires(AEItems.LOGIC_PROCESSOR.get())
                .requires(AEItems.ENGINEERING_PROCESSOR.get())
                .requires(AEItems.CALCULATION_PROCESSOR.get())
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get())
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get())
                .requires(EAESingletons.CONCURRENT_PROCESSOR)
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_300M).get())
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(extendedAndMegaOutput, UfoMod.id("750m_co_processor_compat_extended"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_2B).get())
                .requires(AEItems.LOGIC_PROCESSOR.get())
                .requires(AEItems.ENGINEERING_PROCESSOR.get())
                .requires(AEItems.CALCULATION_PROCESSOR.get())
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get())
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get())
                .requires(EAESingletons.CONCURRENT_PROCESSOR)
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_750M).get())
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(extendedAndMegaOutput, UfoMod.id("2b_co_processor_compat_extended"));
        // --- FIM DA CORREÇÃO ---
    }

    private void createInfinityCellRecipe(RecipeOutput recipeOutput, ItemLike cell, ItemLike coreItem) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, cell)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())
                .define('B', Items.NETHER_STAR)
                .define('C', coreItem)
                .unlockedBy("has_item", has(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get()))
                .save(recipeOutput);
    }
}