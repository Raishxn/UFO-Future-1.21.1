package com.raishxn.ufo.compat.jei;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinitions;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.client.preview.StructurePreviewModel;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/** Native JEI structure page; deliberately independent from LDLib/Glodium. */
public final class MultiblockInfoCategory implements IRecipeCategory<StructurePreviewModel> {
    public static final RecipeType<StructurePreviewModel> RECIPE_TYPE =
            RecipeType.create(UfoMod.MOD_ID, "multiblock_info", StructurePreviewModel.class);
    public static final int WIDTH = 300;
    public static final int HEIGHT = 184;
    private final IDrawable icon;

    public MultiblockInfoCategory(IJeiHelpers helpers) {
        this.icon = helpers.getGuiHelper().createDrawableItemStack(
                MultiblockBlocks.STELLAR_NEXUS_CONTROLLER.get().asItem().getDefaultInstance());
    }

    public static void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(RECIPE_TYPE, MultiblockControllerDefinitions.getPreviewEntries().stream()
                .map(StructurePreviewModel::from).toList());
    }

    @Override public @NotNull RecipeType<StructurePreviewModel> getRecipeType() { return RECIPE_TYPE; }
    @Override public @NotNull Component getTitle() { return Component.literal("Multiblock Structures"); }
    @Override public int getWidth() { return WIDTH; }
    @Override public int getHeight() { return HEIGHT; }
    @Override public @NotNull IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, StructurePreviewModel recipe, IFocusGroup focuses) {
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStacks(recipe.focusStacks());
        builder.moveRecipeTransferButton(WIDTH + 4, HEIGHT - 14);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, StructurePreviewModel recipe, IFocusGroup focuses) {
        StructurePreviewWidget widget = new StructurePreviewWidget(recipe, WIDTH, HEIGHT);
        builder.addWidget(widget);
        builder.addInputHandler(widget);
    }

    @Override public ResourceLocation getRegistryName(StructurePreviewModel recipe) { return recipe.id(); }
}
