package com.raishxn.ufo.block.entity;

import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.block.entity.pattern.QmfPatternFactory;
import com.raishxn.ufo.block.entity.processing.MultiblockProcessingRecipe;
import com.raishxn.ufo.init.ModBlockEntities;
import com.raishxn.ufo.init.ModRecipes;
import com.raishxn.ufo.recipe.UniversalMultiblockMachineKind;
import com.raishxn.ufo.screen.QmfControllerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class QmfControllerBE extends AbstractParallelMultiblockControllerBE {

    private static MultiblockPattern PATTERN;

    public QmfControllerBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QMF_CONTROLLER.get(), pos, state);
    }

    @Override
    protected MultiblockPattern getControllerPattern() {
        if (PATTERN == null) {
            PATTERN = QmfPatternFactory.getPattern();
        }
        return PATTERN;
    }

    @Override
    protected String getControllerTranslationKey() {
        return "block.ufo.quantum_matter_fabricator_controller";
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new QmfControllerMenu(id, playerInventory, this);
    }

    @Override
    protected List<MultiblockProcessingRecipe> getAvailableRecipes() {
        if (this.level == null) {
            return List.of();
        }

        List<MultiblockProcessingRecipe> recipes = new ArrayList<>();
        for (RecipeHolder<?> holder : this.level.getRecipeManager().getAllRecipesFor(ModRecipes.QMF_TYPE.get())) {
            recipes.add(MultiblockProcessingRecipe.fromQmf(holder.id(), (com.raishxn.ufo.recipe.QMFRecipe) holder.value()));
        }
        for (RecipeHolder<?> holder : this.level.getRecipeManager().getAllRecipesFor(ModRecipes.DMA_RECIPE_TYPE.get())) {
            recipes.add(MultiblockProcessingRecipe.fromDma(holder.id(), (com.raishxn.ufo.recipe.DimensionalMatterAssemblerRecipe) holder.value()));
        }
        for (RecipeHolder<?> holder : this.level.getRecipeManager().getAllRecipesFor(ModRecipes.UNIVERSAL_MULTIBLOCK_TYPE.get())) {
            var recipe = (com.raishxn.ufo.recipe.UniversalMultiblockRecipe) holder.value();
            if (recipe.getMachine() == UniversalMultiblockMachineKind.QMF) {
                recipes.add(MultiblockProcessingRecipe.fromUniversal(holder.id(), recipe));
            }
        }
        return recipes;
    }
}
