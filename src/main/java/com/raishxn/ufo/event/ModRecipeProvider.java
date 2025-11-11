package com.raishxn.ufo.event;

// Imports de Minecraft e NeoForge
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
import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipeBuilder;
import com.glodblock.github.extendedae.common.EAESingletons;
import com.glodblock.github.extendedae.recipe.CircuitCutterRecipeBuilder;
import com.glodblock.github.extendedae.recipe.CrystalAssemblerRecipeBuilder;
import com.glodblock.github.glodium.util.GlodUtil;
import gripe._90.megacells.definition.MEGABlocks;
import gripe._90.megacells.definition.MEGAItems;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;

// Imports do seu Mod
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.core.MegaCoProcessorTier;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import com.raishxn.ufo.datagen.DMARecipeBuilder; // <-- NOSSO BUILDER CUSTOMIZADO
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
        // --- 1. Receitas de Crafting Vanilla (do PDF e do seu arquivo) ---
        buildVanillaRecipes(c);

        // --- 2. Receitas do Applied Energistics 2 (do PDF e do seu arquivo) ---
        buildAERecipes(c);

        // --- 3. Receitas da sua Máquina Custom (DMA) (TODAS AS RECEITAS DO PDF MOVIDAS) ---
        buildDMARecipes(c);

        // --- 4. Receitas de Células e Componentes (do seu arquivo original) ---
        buildComponentAndCellRecipes(c);

        // --- 5. Receitas de Compatibilidade (do seu arquivo original) ---
        if (GlodUtil.checkMod("megacells")) {
            megaCellsRecipes(c);
        }
        if (GlodUtil.checkMod("extendedae")) {
            extendedAERecipes(c);
        }
    }

    // ========================================================================================
    // --- 1. VANILLA CRAFTING (CRAFTING TABLE) ---
    // ========================================================================================
    private void buildVanillaRecipes(RecipeOutput c) {
        // OBSIDIAN MATRIX
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.OBSIDIAN_MATRIX.get())
                .pattern("OOO")
                .pattern("OEO")
                .pattern("OOO")
                .define('O', Items.OBSIDIAN)
                .define('E', Items.ENDER_EYE)
                .unlockedBy("has_item", has(Items.OBSIDIAN))
                .save(c, UfoMod.id("obsidian_matrix_vanilla"));

        // --- (Receitas do seu arquivo original) ---
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
    }

    // ========================================================================================
    // --- 2. RECEITAS DO APPLIED ENERGISTICS 2 ---
    // ========================================================================================
    private void buildAERecipes(RecipeOutput c) {
        // --- Inscriber (do seu arquivo original) ---
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

        // --- Molecular Assembler (Receitas do PDF) ---

        // PROTO MATTER

    }

    // ========================================================================================
    // --- 3. RECEITAS DO DIMENSIONAL MATTER ASSEMBLER (DMA) ---
    // (Receitas do PDF movidas para o DMA por incompatibilidade com Mekanism)
    // ========================================================================================
    private void buildDMARecipes(RecipeOutput c) {

        // --- SEÇÃO 1: ITENS SÓLIDOS ---

        // SCRAP (Movido do Crusher para suportar 10% de chance de bônus)
        // O PDF cita "obsidian_matrix OR scar", então criamos duas receitas.
        DMARecipeBuilder.create("dma/scrap_from_matrix")
                .output(ModItems.SCRAP.get(), 4) // Média de 1-8
                .output(AEItems.MATTER_BALL.get(), 1, 0.10f) // 10% chance matterball
                .inputItem(ModItems.OBSIDIAN_MATRIX.get())
                .inputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 10) // Placeholder
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 10) // Placeholder
                .energy(100_000)
                .time(200)
                .save(c);

        DMARecipeBuilder.create("dma/scrap_from_scar")
                .output(ModItems.SCRAP.get(), 4)
                .output(AEItems.MATTER_BALL.get(), 1, 0.10f)
                .inputItem(ModItems.SCAR.get())
                .inputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 10)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 10)
                .energy(100_000)
                .time(200)
                .save(c);

        // SCRAP BOX (Movido do Combiner, que não aceita 9 inputs nem chances)
        DMARecipeBuilder.create("dma/scrap_box")
                .output(ModItems.SCRAP_BOX.get())
                .output(ModItems.SCAR.get(), 1, 0.03f) // 3% scar
                .inputItem(ModItems.SCRAP.get(), 9)
                .inputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 100)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 100)
                .energy(10_000_000)
                .time(2000)
                .save(c);

        // UU AMPLIFIER (Movido do Rotary, que não faz Item -> Fluid)
        DMARecipeBuilder.create("dma/uu_amplifier")
                .outputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 9, 1.0f)
                .inputItem(ModItems.SCRAP_BOX.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 10) // Placeholder
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 100)
                .energy(200_000)
                .time(600)
                .save(c);

        // UU MATTER (Movido do Chemical Mixer, que não aceita Fluid + Item)
        DMARecipeBuilder.create("dma/uu_matter")
                .outputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 10, 1.0f)
                .inputItem(AEItems.MATTER_BALL.get(), 64)
                .inputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 1000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 200)
                .energy(500_000)
                .time(1200)
                .save(c);

        // NEUTRONIUM SPHERE (Movido do Combiner, que não aceita 9 inputs)
        DMARecipeBuilder.create("dma/neutronium_sphere")
                .output(ModItems.NEUTRONIUM_SPHERE.get())
                .inputItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get(), 9)
                .inputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 500)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 500)
                .energy(1_000_000)
                .time(600)
                .save(c);

        // ENRICHED NEUTRONIUM SPHERE (Movido do PRC, que não aceita 2 Items)
        DMARecipeBuilder.create("dma/enriched_neutronium_sphere")
                .output(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get())
                .inputItem(ModItems.NEUTRONIUM_SPHERE.get())
                .inputItem(ModItems.QUANTUM_ANOMALY.get(), 2)
                .inputFluid(ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID.get(), 12000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(8_000_000)
                .time(3000)
                .save(c);


        DMARecipeBuilder.create("dma/proto_matter")
                .output(ModItems.PROTO_MATTER.get())
                .inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get())
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 1000)
                // Adição necessária para o DMA:
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(2_000_000)
                .time(800)
                .save(c);

        // UFO STAFF
        DMARecipeBuilder.create("dma/ufo_staff")
                .output(ModItems.UFO_STAFF.get())
                .inputItem(ModItems.CHARGED_ENRICHED_NEUTRONIUM_SPHERE.get())
                .inputItem(ModItems.QUANTUM_ANOMALY.get(), 2)
                .inputItem(ModItems.PROTO_MATTER.get(), 2)
                .inputItem(MekanismItems.ATOMIC_ALLOY.get(), 4)
                .inputItem(AEItems.MATTER_BALL.get(), 16)
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 2000)
                // Adição necessária para o DMA:
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 2000)
                .energy(12_000_000)
                .time(2400)
                .save(c);

        // CHARGED ENRICHED NEUTRONIUM SPHERE (Movido do Chemical Infuser, que não aceita Itens)
        DMARecipeBuilder.create("dma/charged_enriched_neutronium_sphere")
                .output(ModItems.CHARGED_ENRICHED_NEUTRONIUM_SPHERE.get())
                .inputItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE.get())
                .inputItem(ModItems.NUCLEAR_STAR.get())
                .inputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 4000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 4000) // Fluido de input é o próprio coolant
                .energy(10_000_000)
                .time(4000)
                .save(c);

        // QUANTUM ANOMALY (Movido do Rotary, que não aceita 2 Items + 1 Fluid)
        DMARecipeBuilder.create("dma/quantum_anomaly")
                .output(ModItems.QUANTUM_ANOMALY.get())
                .inputItem(ModItems.PULSAR_FRAGMENT_DUST.get(), 12)
                .inputItem(ModItems.OBSIDIAN_MATRIX.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 8000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(6_000_000)
                .time(1200)
                .save(c);

        // --- SEÇÃO 2: FLUIDOS E COOLANTS (Movidos para o DMA) ---

        // TEMPORAL FLUID (Movido do Rotary)
        DMARecipeBuilder.create("dma/temporal_fluid")
                .outputFluid(ModFluids.SOURCE_TEMPORAL_FLUID.get(), 5000, 1.0f)
                .inputItem(ModItems.QUANTUM_ANOMALY.get())
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 2000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 500)
                .energy(2_000_000)
                .time(400)
                .save(c);

        // SPATIAL FLUID (Movido do Chemical Infuser)
        DMARecipeBuilder.create("dma/spatial_fluid")
                .outputFluid(ModFluids.SOURCE_SPATIAL_FLUID.get(), 10000, 1.0f)
                .inputItem(ModItems.NUCLEAR_STAR.get())
                .inputFluid(ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID.get(), 5000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(6_000_000)
                .time(1200)
                .save(c);

        // GELID CRYOTHEUM (Movido do Chemical Crystallizer)
        DMARecipeBuilder.create("dma/gelid_cryotheum")
                .outputFluid(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 10000, 1.0f)
                .inputItem(ModItems.DUST_CRYOTHEUM.get(), 4)
                .inputFluid(MekanismFluids.HYDROGEN.get(), 2000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 100) // Requer um pouco de si mesmo
                .energy(500_000)
                .time(200)
                .save(c);

        // PRIMORDIAL MATTER LIQUID (Movido do Rotary)
        DMARecipeBuilder.create("dma/primordial_matter_liquid")
                .outputFluid(ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID.get(), 5000, 1.0f)
                .inputItem(ModItems.QUANTUM_ANOMALY.get())
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 8000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 2000)
                .energy(3_000_000)
                .time(700)
                .save(c);

        // LIQUID STARLIGHT (Movido do Rotary)
        DMARecipeBuilder.create("dma/liquid_starlight")
                .outputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 10000, 1.0f)
                .inputItem(ModItems.QUANTUM_ANOMALY.get(), 2)
                .inputItem(ModItems.PULSAR_FRAGMENT_DUST.get(), 8)
                .inputFluid(ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), 100) // Placeholder
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(4_000_000)
                .time(1000)
                .save(c);

        // RAW STAR MATTER PLASMA (Movido do Injection Chamber)
        DMARecipeBuilder.create("dma/raw_star_matter_plasma")
                .outputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 8000, 1.0f)
                .inputItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get(), 4)
                .inputItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get(), 2)
                .inputItem(ModItems.PULSAR_FRAGMENT_DUST.get(), 2)
                .inputFluid(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 1000) // Placeholder
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 2000)
                .energy(2_500_000)
                .time(600)
                .save(c);

        // TRANSCENDING MATTER FLUID (Movido do PRC)
        DMARecipeBuilder.create("dma/transcending_matter_fluid")
                .outputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 10000, 1.0f)
                .inputItem(ModItems.UNSTABLE_WHITE_HOLE_MATTER.get())
                .inputFluid(ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), 10000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 5000)
                .energy(12_000_000)
                .time(3000)
                .save(c);

        // --- SEÇÃO 3: RECEITAS DMA (Implementação Direta) ---

        // CORPOREAL MATTER
        DMARecipeBuilder.create("dma/corporeal_matter")
                .output(ModItems.CORPOREAL_MATTER.get())
                .output(ModItems.SCAR.get(), 1, 0.08f) // 8% chance SCAR
                .inputItem(ModItems.PROTO_MATTER.get())
                .inputItem(Items.IRON_BLOCK, 64)
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 1000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000) // Coolant Padrão
                .energy(5_000_000)
                .time(1200)
                .save(c);

        DMARecipeBuilder.create("dma/uu_matter_crystal")
                .output(ModItems.UU_MATTER_CRYSTAL.get())
                .inputFluid(ModFluids.SOURCE_UU_MATTER_FLUID.get(), 10000) // 10 baldes
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 2000)
                .energy(20_000_000)
                .time(4000)
                .save(c);

        // --- FINAL MATTERS (PATTERN) ---
        // Padrão: corporeal_matter, 1000 mB uu_matter, 1000 mB star fluid, 1 ingot_block
        // Assumindo UU_MATTER como ITEM (ModItems.UU_MATTER) para resolver conflito de 2 fluidos.

        // WHITE DWARF MATTER
        DMARecipeBuilder.create("dma/white_dwarf_matter")
                .output(ModItems.WHITE_DWARF_MATTER.get())
                .output(ModItems.SCAR.get(), 1, 0.10f)
                .inputItem(ModItems.CORPOREAL_MATTER.get())
                .inputItem(ModItems.UU_MATTER_CRYSTAL.get()) // Assumindo item uu_matter
                .inputItem(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK.get())
                .inputFluid(ModFluids.SOURCE_WHITE_DWARF_FRAGMENT_FLUID.get(), 1000) // "star fluid"
                .inputCoolant(ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), 1000)
                .energy(7_000_000)
                .time(1500)
                .save(c);

        // NEUTRON STAR MATTER
        DMARecipeBuilder.create("dma/neutron_star_matter")
                .output(ModItems.NEUTRON_STAR_MATTER.get())
                .output(ModItems.SCAR.get(), 1, 0.10f)
                .inputItem(ModItems.CORPOREAL_MATTER.get())
                .inputItem(ModItems.UU_MATTER_CRYSTAL.get())
                .inputItem(ModBlocks.NEUTRON_STAR_FRAGMENT_BLOCK.get())
                .inputFluid(ModFluids.SOURCE_NEUTRON_STAR_FRAGMENT_FLUID.get(), 1000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(9_500_000)
                .time(1850)
                .save(c);

        // PULSAR MATTER
        DMARecipeBuilder.create("dma/pulsar_matter")
                .output(ModItems.PULSAR_MATTER.get())
                .output(ModItems.SCAR.get(), 1, 0.10f)
                .inputItem(ModItems.CORPOREAL_MATTER.get())
                .inputItem(ModItems.UU_MATTER_CRYSTAL.get())
                .inputItem(ModBlocks.PULSAR_FRAGMENT_BLOCK.get())
                .inputFluid(ModFluids.SOURCE_PULSAR_FRAGMENT_FLUID.get(), 1000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1500)
                .energy(12_000_000)
                .time(2200)
                .save(c);

        // DARK MATTER
        DMARecipeBuilder.create("dma/dark_matter")
                .output(ModItems.DARK_MATTER.get())
                .inputItem(ModItems.WHITE_DWARF_MATTER.get(), 64)
                .inputItem(ModItems.NEUTRON_STAR_MATTER.get(), 64)
                .inputItem(ModItems.PULSAR_MATTER.get(), 64)
                .inputFluid(ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), 10000) // Fluido avançado
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 10000)
                .energy(200_000_000)
                .time(20_000)
                .save(c);

        // --- SEÇÃO 4: RECEITAS AVANÇADAS (DMA) ---

        // UFO ARMOR (Helm, Chest, Leggings, Boots)
        // O PDF não lista os ingredientes da armadura,
        // apenas a máquina, energia e tempo. Você precisará adicionar os
        // ingredientes (inputItem/inputFluid) quando os definir.

        // Exemplo de como seria (PLACEHOLDER):
        /*
        DMARecipeBuilder.create("dma/ufo_helmet")
                .output(ModItems.UFO_HELMET.get())
                // .inputItem(...) // ADICIONAR INGREDIENTES
                .inputFluid(ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID.get(), 1000)
                .inputCoolant(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), 1000)
                .energy(6_000_000)
                .time(1400)
                .save(c);
        */
    }

    // ========================================================================================
    // --- 4. RECEITAS DE COMPONENTES E CÉLULAS (Seu Código Original) ---
    // (Mantido do seu arquivo original)
    // ========================================================================================
    private void buildComponentAndCellRecipes(RecipeOutput c) {
        // --- Componentes Principais (Seu código original) ---
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

        // --- Blocos de Crafting Storage (Seu código original) ---
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


        // --- Células Infinitas (Seu código original) ---
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

        // --- Células de Armazenamento (Seu código original) ---
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
    }


    // ========================================================================================
    // --- 5. MÉTODOS DE COMPATIBILIDADE (Seu Código Original) ---
    // ========================================================================================
    private void megaCellsRecipes(RecipeOutput c) {
        // (Seu código original de compatibilidade do MegaCells)
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
    }

    private void extendedAERecipes(RecipeOutput c) {
        // (Seu código original de compatibilidade do ExtendedAE)
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
    }

    // ========================================================================================
    // --- HELPERS (Seu Código Original) ---
    // ========================================================================================
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