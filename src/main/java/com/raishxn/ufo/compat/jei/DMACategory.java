package com.raishxn.ufo.compat.jei;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.recipe.DimensionalMatterAssemblerRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class DMACategory implements IRecipeCategory<DimensionalMatterAssemblerRecipe> {

    public static final RecipeType<DimensionalMatterAssemblerRecipe> RECIPE_TYPE =
            RecipeType.create(UfoMod.MOD_ID, "dimensional_assembly", DimensionalMatterAssemblerRecipe.class);

    private static final ResourceLocation TEXTURE =
            UfoMod.id("textures/gui/dimensional_matter_assembler.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable progressArrow;
    private final IDrawable energyMeter;

    public DMACategory(IGuiHelper helper) {

        // Corta a aba de upgrades (que começa em x=175)
        this.background = helper.createDrawable(TEXTURE, 0, 0, 170, 95);
        this.icon = helper.createDrawableItemStack(new ItemStack(ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER.get()));

        // Barra de Progresso
        // Posição UV: (234, 0), Tamanho: (21, 12)
        this.progressArrow = helper.createAnimatedDrawable(
                helper.createDrawable(TEXTURE, 234, 0, 21, 12),
                200, IDrawableAnimated.StartDirection.LEFT, false);

        // Barra de Energia
        // Posição UV: (225, 0), Tamanho: (6, 18)
        this.energyMeter = helper.createAnimatedDrawable(
                helper.createDrawable(TEXTURE, 225, 0, 6, 18),
                400, IDrawableAnimated.StartDirection.BOTTOM, false);
    }

    @Override
    public @NotNull RecipeType<DimensionalMatterAssemblerRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.ufo.dimensional_matter_assembler");
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DimensionalMatterAssemblerRecipe recipe, @NotNull IFocusGroup focuses) {

        // --- Inputs ---

        // Slots de Item Input (3x3)
        // Posição inicial: (48, 22)
        int slotIndex = 0;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (slotIndex < recipe.getItemInputs().size()) {
                    builder.addSlot(RecipeIngredientRole.INPUT, 48 + x * 18, 22 + y * 18)
                            .addIngredients(JEIUtil.stackOf(recipe.getItemInputs().get(slotIndex)));
                    slotIndex++;
                }
            }
        }

        // Tanque de Coolant
        // Posição: (9, 21), Tamanho: (11, 53)
        builder.addSlot(RecipeIngredientRole.INPUT, 9, 21)
                .addIngredients(NeoForgeTypes.FLUID_STACK, JEIUtil.stackOf(recipe.getCoolantInput()))
                .setFluidRenderer(16000, true, 11, 53)
                .setSlotName("coolant_input");

        // Tanque de Fluid Input
        // Posição: (28, 21), Tamanho: (11, 53)
        builder.addSlot(RecipeIngredientRole.INPUT, 28, 21)
                .addIngredients(NeoForgeTypes.FLUID_STACK, JEIUtil.stackOf(recipe.getFluidInput()))
                .setFluidRenderer(16000, true, 11, 53)
                .setSlotName("fluid_input");


        // --- Outputs ---

        int itemOutputSlot = 0;
        int fluidOutputSlot = 0;

        for (int i = 0; i < recipe.getOutputs().size(); i++) {
            var gStack = recipe.getOutputs().get(i);
            var chance = recipe.getChance(i);

            // Output de Item
            if (gStack.what() instanceof AEItemKey itemKey) {
                ItemStack itemStack = itemKey.toStack((int) gStack.amount());
                IRecipeSlotBuilder slotBuilder = null;

                if (itemOutputSlot == 0) {
                    // Posição Output 1: (133, 22)
                    slotBuilder = builder.addSlot(RecipeIngredientRole.OUTPUT, 133, 22)
                            .addItemStack(itemStack);
                } else if (itemOutputSlot == 1) {
                    // Posição Output 2: (133, 50)
                    slotBuilder = builder.addSlot(RecipeIngredientRole.OUTPUT, 133, 50)
                            .addItemStack(itemStack);
                }

                if (slotBuilder != null && chance < 1.0f) {
                    slotBuilder.addRichTooltipCallback((slotView, tooltipElements) -> {
                        tooltipElements.add(Component.literal("Chance: " + (int)(chance * 100) + "%")
                                .withStyle(ChatFormatting.GRAY));
                    });
                }
                itemOutputSlot++;
            }
            // Output de Fluido
            else if (gStack.what() instanceof AEFluidKey fluidKey) {
                FluidStack fluidStack = fluidKey.toStack((int) gStack.amount());
                IRecipeSlotBuilder slotBuilder = null;

                if (fluidOutputSlot == 0) {
                    // Posição Output Fluido 1: (119, 76)
                    slotBuilder = builder.addSlot(RecipeIngredientRole.OUTPUT, 119, 76)
                            .addIngredient(NeoForgeTypes.FLUID_STACK, fluidStack)
                            .setFluidRenderer(16000, false, 14, 16);
                } else if (fluidOutputSlot == 1) {
                    // Posição Output Fluido 2: (148, 76)
                    slotBuilder = builder.addSlot(RecipeIngredientRole.OUTPUT, 148, 76)
                            .addIngredient(NeoForgeTypes.FLUID_STACK, fluidStack)
                            .setFluidRenderer(16000, false, 14, 16);
                }

                if (slotBuilder != null && chance < 1.0f) {
                    slotBuilder.addRichTooltipCallback((slotView, tooltipElements) -> {
                        tooltipElements.add(Component.literal("Chance: " + (int)(chance * 100) + "%")
                                .withStyle(ChatFormatting.GRAY));
                    });
                }
                fluidOutputSlot++;
            }
        }
    }

    @Override
    public void draw(@NotNull DimensionalMatterAssemblerRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {

        // Posição da Barra de Progresso: (105, 42)
        progressArrow.draw(guiGraphics, 105, 42);

        // Posição da Barra de Energia: (155, 34)
        energyMeter.draw(guiGraphics, 155, 34);


        // =================================================================================
        // --- INÍCIO DA CORREÇÃO ---
        // =================================================================================

        var font = Minecraft.getInstance().font;
        String energyText = String.format("%,d FE", recipe.getEnergy());
        int textWidth = font.width(energyText);

        // Posição Y = 85 (na linha da barra de temperatura)
        // Posição X = Centralizado entre fim da temp (39) e início do tanque (119)
        // Ponto central = (119 + 39) / 2 = 79
        int textX = 79 - (textWidth / 2); // 79 é o ponto central

        // Cor = 0xFFFFFF (Branco)
        guiGraphics.drawString(font, energyText, textX, 85, 0xFFFFFF, false);

        // =================================================================================
        // --- FIM DA CORREÇÃO ---
        // =================================================================================
    }
}