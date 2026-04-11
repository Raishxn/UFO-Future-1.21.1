package com.raishxn.ufo.compat.jei;

import com.lowdragmc.lowdraglib.jei.ModularUIRecipeCategory;
import com.raishxn.ufo.UfoMod;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiblockInfoCategory extends ModularUIRecipeCategory<MultiblockInfoWrapper> {

    public static final RecipeType<MultiblockInfoWrapper> RECIPE_TYPE =
            RecipeType.create(UfoMod.MOD_ID, "multiblock_info", MultiblockInfoWrapper.class);

    private final IDrawable background;
    private final IDrawable icon;

    public MultiblockInfoCategory(IJeiHelpers helpers) {
        super();
        this.background = helpers.getGuiHelper().createBlankDrawable(184, 184);
        this.icon = helpers.getGuiHelper().createDrawableItemStack(
                com.raishxn.ufo.block.MultiblockBlocks.STELLAR_NEXUS_CONTROLLER.get().asItem().getDefaultInstance());
    }

    public static void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(RECIPE_TYPE,
                com.raishxn.ufo.api.multiblock.MultiblockControllerDefinitions.getPreviewEntries().stream()
                        .map(MultiblockInfoWrapper::new)
                        .toList());
    }

    @Override
    public @Nullable ResourceLocation getRegistryName(@NotNull MultiblockInfoWrapper recipe) {
        return recipe.entry().id();
    }

    @Override
    public @NotNull RecipeType<MultiblockInfoWrapper> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.literal("Multiblock Info");
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }
}
