package com.raishxn.ufo.compat.jei;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.init.ModRecipes;
import com.raishxn.ufo.recipe.DimensionalMatterAssemblerRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@JeiPlugin
public class UfoJeiPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return UfoMod.id("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new DMACategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager rm = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();

        // Adiciona as receitas do DMA
        List<DimensionalMatterAssemblerRecipe> dmaRecipes = rm.getAllRecipesFor(ModRecipes.DMA_TYPE.get())
                .stream().map(RecipeHolder::value).toList();

        registration.addRecipes(DMACategory.RECIPE_TYPE, dmaRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // Define o bloco DMA como o catalisador
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER.get()),
                DMACategory.RECIPE_TYPE);
    }
}