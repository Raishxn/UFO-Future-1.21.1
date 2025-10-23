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

        // Receita padrão para o Phase Shift Component, usada quando o MegaCells NÃO está instalado.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ACA")
                .define('A', Items.NETHER_STAR)
                .define('B', ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get())
                .define('C', AEItems.CELL_COMPONENT_256K) // Usa o item padrão do AE2
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

        // --- RECEITAS DE BLOCOS E ITENS ---
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

        // --- RECEITAS DE CRAFTING STORAGE ---
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1B).get())
                .requires(ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get()) // Ingrediente 1
                .requires(ModBlocks.GRAVITON_PLATED_CASING.get()) // Ingrediente 2
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("1b_mega_crafting_storage_from_casing"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_50B).get())
                .requires(ModItems.HYPER_DENSE_COMPONENT_MATRIX.get()) // Ingrediente 1
                .requires(ModBlocks.GRAVITON_PLATED_CASING.get()) // Ingrediente 2
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("50b_mega_crafting_storage_from_casing"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1T).get())
                .requires(ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get()) // Ingrediente 1
                .requires(ModBlocks.GRAVITON_PLATED_CASING.get()) // Ingrediente 2
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("1t_mega_crafting_storage_from_casing"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_250T).get())
                .requires(ModItems.TESSERACT_COMPONENT_MATRIX.get()) // Ingrediente 1
                .requires(ModBlocks.GRAVITON_PLATED_CASING.get()) // Ingrediente 2
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("250t_mega_crafting_storage_from_casing"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1QD).get())
                .requires(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get()) // Ingrediente 1
                .requires(ModBlocks.GRAVITON_PLATED_CASING.get()) // Ingrediente 2
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("1qd_mega_crafting_storage_from_casing"));

        //receita coprocessor



        // ========================================================================================
        // --- RECEITAS DE INFINITY CELLS ---
        // ========================================================================================
        createInfinityCellRecipe(c, ModItems.INFINITY_COBBLED_DEEPSLATE_CELL.get(), Items.COBBLED_DEEPSLATE);
        createInfinityCellRecipe(c, ModItems.INFINITY_END_STONE_CELL.get(), Items.END_STONE);
        createInfinityCellRecipe(c, ModItems.INFINITY_NETHERRACK_CELL.get(), Items.NETHERRACK);
        createInfinityCellRecipe(c, ModItems.INFINITY_SAND_CELL.get(), Items.SAND);
        createInfinityCellRecipe(c, ModItems.INFINITY_LAVA_CELL.get(), Items.LAVA_BUCKET);
        createInfinityCellRecipe(c, ModItems.INFINITY_ANTIMATTER_PELLET_CELL.get(), MekanismItems.ANTIMATTER_PELLET);

        // --- Novas Receitas (Mekanism) ---
        createInfinityCellRecipe(c, ModItems.INFINITY_PLUTONIUM_PELLET_CELL.get(), MekanismItems.PLUTONIUM_PELLET.get());
        createInfinityCellRecipe(c, ModItems.INFINITY_POLONIUM_PELLET_CELL.get(), MekanismItems.POLONIUM_PELLET.get());
        createInfinityCellRecipe(c, ModItems.INFINITY_HDPE_PELLET_CELL.get(), MekanismItems.HDPE_PELLET.get());

        // --- Novas Receitas (Minecraft Vanilla) ---
        createInfinityCellRecipe(c, ModItems.INFINITY_OBSIDIAN_CELL.get(), Items.OBSIDIAN);
        createInfinityCellRecipe(c, ModItems.INFINITY_GRAVEL_CELL.get(), Items.GRAVEL);
        createInfinityCellRecipe(c, ModItems.INFINITY_OAK_LOG_CELL.get(), Items.OAK_LOG);
        createInfinityCellRecipe(c, ModItems.INFINITY_GLASS_CELL.get(), Items.GLASS);
        createInfinityCellRecipe(c, ModItems.INFINITY_AMETHYST_SHARD_CELL.get(), Items.AMETHYST_SHARD);

        // --- Novas Receitas (Corantes) ---
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
        // --- CHAMADAS DE COMPATIBILIDADE ---
        // ========================================================================================
        if (GlodUtil.checkMod("megacells")) {
            megaCellsRecipes(c);
        }
        if (GlodUtil.checkMod("extendedae")) {
            extendedAERecipes(c);
        }
    }

    /**
     * Contém as receitas de compatibilidade para o MegaCells.
     * Este método só é chamado se o mod "megacells" estiver carregado.
     */
    private void megaCellsRecipes(RecipeOutput c) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ACA")
                .define('A', Items.NETHER_STAR)
                .define('B', ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get())
                .define('C', MEGAItems.CELL_COMPONENT_256M) // Referência direta ao item do MegaCells
                .define('D', AEBlocks.QUARTZ_VIBRANT_GLASS)
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("phase_shift_component_matrix_megacells_compat"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.GRAVITON_PLATED_CASING.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ABA")
                .define('A', MEGABlocks.MEGA_CRAFTING_UNIT)
                .define('B', MEGAItems.ACCUMULATION_PROCESSOR.get())
                .define('C',  AEParts.SMART_DENSE_CABLE.item(AEColor.TRANSPARENT)) // Referência direta ao item do MegaCells
                .define('D', ModItems.DIMENSIONAL_PROCESSOR)
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("graviton_plated_casing_megacells_compat"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_50M).get())
                .requires(AEItems.LOGIC_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.ENGINEERING_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.CALCULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get()) // Ingrediente 1
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(ModBlocks.QUANTUM_LATTICE_FRAME.get()) // Ingrediente 2// Ingrediente 2
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("50m_co_processor_compat_mega"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_150M).get())
                .requires(AEItems.LOGIC_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.ENGINEERING_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.CALCULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get()) // Ingrediente 1
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_50M).get()) // Ingrediente 2// Ingrediente 2
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("150m_co_processor_compat_mega"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_300M).get())
                .requires(AEItems.LOGIC_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.ENGINEERING_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.CALCULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get()) // Ingrediente 1
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_150M).get()) // Ingrediente 2// Ingrediente 2
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("300m_co_processor_compat_mega"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_750M).get())
                .requires(AEItems.LOGIC_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.ENGINEERING_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.CALCULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get()) // Ingrediente 1
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_300M).get()) // Ingrediente 2// Ingrediente 2
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("750m_co_processor_compat_mega"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_2B).get())
                .requires(AEItems.LOGIC_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.ENGINEERING_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.CALCULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get()) // Ingrediente 1
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_750M).get()) // Ingrediente 2// Ingrediente 2
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("2b_co_processor_compat_mega"));
    }

    /**
     * Contém as receitas de compatibilidade para o Extended AE.
     * Este método só é chamado se o mod "extendedae" estiver carregado.
     */
    private void extendedAERecipes(RecipeOutput c) {
        // Receita alternativa para o 'White Dwarf Fragment Ingot'
        CrystalAssemblerRecipeBuilder
                .assemble(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get(), 4)
                .input(AEItems.SINGULARITY, 4)
                .input(Tags.Items.INGOTS_NETHERITE, 4)
                .input(EAESingletons.ENTRO_INGOT, 4) // Referência direta ao item do EAE
                .fluid(Fluids.LAVA, 10000)
                .save(c, UfoMod.id("assembler/white_dwarf_fragment_ingot_eae_compat"));

        CrystalAssemblerRecipeBuilder
                .assemble(ModItems.DIMENSIONAL_PROCESSOR.get(), 2)
                .input(ModItems.PRINTED_DIMENSIONAL_PROCESSOR, 4)
                .input(EAESingletons.CONCURRENT_PROCESSOR_PRINT, 4)
                .input(EAESingletons.ENTRO_DUST, 4) // Referência direta ao item do EAE
                .fluid(Fluids.LAVA, 2000)
                .save(c, UfoMod.id("assembler/dimensional_processor_eae_compat"));

        CrystalAssemblerRecipeBuilder
                .assemble(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get(), 1)
                .input(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK, 4)
                .input(MEGAItems.ACCUMULATION_PROCESSOR_PRESS, 2)
                .input(MEGABlocks.SKY_STEEL_BLOCK, 4)
                .input(EAESingletons.CONCURRENT_PROCESSOR_PRESS, 2) // Referência direta ao item do EAE
                .input(EAESingletons.ENTRO_BLOCK, 4) // Referência direta ao item do EAE
                .input(Items.NETHER_STAR, 19) // Referência direta ao item do EAE
                .fluid(Fluids.LAVA, 10000)
                .save(c, UfoMod.id("assembler/dimensional_processor_press_eae_compat"));

        CircuitCutterRecipeBuilder
                .cut(ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get(), 9)
                .input(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK.get())
                .save(c, UfoMod.id("cutter/printed_dimensional_processor_eae_compat"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_50M).get())
                .requires(AEItems.LOGIC_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.ENGINEERING_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.CALCULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get()) // Ingrediente 1
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(EAESingletons.CONCURRENT_PROCESSOR) // Ingrediente 1
                .requires(ModBlocks.QUANTUM_LATTICE_FRAME.get()) // Ingrediente 2// Ingrediente 2
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("50m_co_processor_compat_extended"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_150M).get())
                .requires(AEItems.LOGIC_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.ENGINEERING_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.CALCULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get()) // Ingrediente 1
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(EAESingletons.CONCURRENT_PROCESSOR) // Ingrediente 1
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_50M).get()) // Ingrediente 2// Ingrediente 2
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("150m_co_processor_compat_extended"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_300M).get())
                .requires(AEItems.LOGIC_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.ENGINEERING_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.CALCULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get()) // Ingrediente 1
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(EAESingletons.CONCURRENT_PROCESSOR) // Ingrediente 1
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_150M).get()) // Ingrediente 2// Ingrediente 2
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("300m_co_processor_compat_extended"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_750M).get())
                .requires(AEItems.LOGIC_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.ENGINEERING_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.CALCULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get()) // Ingrediente 1
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(EAESingletons.CONCURRENT_PROCESSOR) // Ingrediente 1
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_300M).get()) // Ingrediente 2// Ingrediente 2
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("750m_co_processor_compat_extended"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_2B).get())
                .requires(AEItems.LOGIC_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.ENGINEERING_PROCESSOR.get()) // Ingrediente 1
                .requires(AEItems.CALCULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(ModItems.DIMENSIONAL_PROCESSOR.get()) // Ingrediente 1
                .requires(MEGAItems.ACCUMULATION_PROCESSOR.get()) // Ingrediente 1
                .requires(EAESingletons.CONCURRENT_PROCESSOR) // Ingrediente 1
                .requires(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_750M).get()) // Ingrediente 2// Ingrediente 2
                .unlockedBy("has_item", has(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get()))
                .save(c, UfoMod.id("2b_co_processor_compat_extended"));
    }

    /**
     * Método helper para as receitas padrão das Infinity Cells.
     */
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