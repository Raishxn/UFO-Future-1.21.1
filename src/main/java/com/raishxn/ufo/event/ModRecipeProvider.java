package com.raishxn.ufo.event;

// Imports de Minecraft e NeoForge
import appeng.core.definitions.AEBlocks;
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
                .unlockedBy("has_item", has(ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get()))
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
                .unlockedBy("has_item", has(ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TESSERACT_COMPONENT_MATRIX.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ACA")
                .define('A', AEItems.ENDER_DUST)
                .define('B', ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get())
                .define('C', ModItems.HYPER_DENSE_COMPONENT_MATRIX.get())
                .define('D', AEBlocks.QUARTZ_VIBRANT_GLASS)
                .unlockedBy("has_item", has(ModItems.HYPER_DENSE_COMPONENT_MATRIX.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ACA")
                .define('A', AEItems.SINGULARITY)
                .define('B', ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get())
                .define('C', ModItems.TESSERACT_COMPONENT_MATRIX.get())
                .define('D', AEBlocks.QUARTZ_VIBRANT_GLASS)
                .unlockedBy("has_item", has(ModItems.TESSERACT_COMPONENT_MATRIX.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("ACA")
                .define('A', AEItems.SINGULARITY)
                .define('B', ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get())
                .define('C', ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get())
                .define('D', AEBlocks.QUARTZ_VIBRANT_GLASS)
                .unlockedBy("has_item", has(ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get()))
                .save(c);

        // --- RECEITAS DE BLOCOS E ITENS ---
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.WHITE_DWARF_FRAGMENT_INGOT.get(), 9)
                .requires(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK.get())
                .unlockedBy("has_item", has(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK.get()))
                .save(c);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.WHITE_DWARF_FRAGMENT_INGOT.get())
                .unlockedBy("has_item", has(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get()))
                .save(c);

        // --- RECEITAS DE INFINITY CELLS ---
        createInfinityCellRecipe(c, ModItems.INFINITY_COBBLED_DEEPSLATE_CELL.get(), Items.COBBLED_DEEPSLATE);
        createInfinityCellRecipe(c, ModItems.INFINITY_END_STONE_CELL.get(), Items.END_STONE);
        createInfinityCellRecipe(c, ModItems.INFINITY_NETHERRACK_CELL.get(), Items.NETHERRACK);
        createInfinityCellRecipe(c, ModItems.INFINITY_SAND_CELL.get(), Items.SAND);
        createInfinityCellRecipe(c, ModItems.INFINITY_LAVA_CELL.get(), Items.LAVA_BUCKET);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.INFINITY_SKY_STONE_CELL.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', ModItems.COSMIC_STRING_COMPONENT_MATRIX.get())
                .define('B', Items.NETHER_STAR)
                .define('C', AEBlocks.SKY_STONE_BLOCK)
                .unlockedBy("has_item", has(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get()))
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
                .unlockedBy("has_item", has(ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get()))
                .save(c, UfoMod.id("phase_shift_component_matrix_megacells_compat"));
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

        CircuitCutterRecipeBuilder
                .cut(ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get(), 9)
                .input(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK.get())
                .save(c, UfoMod.id("cutter/printed_dimensional_processor_eae_compat"));
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