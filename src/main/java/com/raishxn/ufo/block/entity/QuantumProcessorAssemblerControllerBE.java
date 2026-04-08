package com.raishxn.ufo.block.entity;

import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.block.entity.pattern.QpaPatternFactory;
import com.raishxn.ufo.init.ModBlockEntities;
import com.raishxn.ufo.screen.QuantumProcessorAssemblerControllerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class QuantumProcessorAssemblerControllerBE extends AbstractParallelMultiblockControllerBE {

    private static MultiblockPattern PATTERN;

    public QuantumProcessorAssemblerControllerBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUANTUM_PROCESSOR_ASSEMBLER_CONTROLLER_BE.get(), pos, state);
    }

    @Override
    protected MultiblockPattern getControllerPattern() {
        if (PATTERN == null) {
            PATTERN = QpaPatternFactory.getPattern();
        }
        return PATTERN;
    }

    @Override
    protected String getControllerTranslationKey() {
        return "block.ufo.quantum_processor_assembler_controller";
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new QuantumProcessorAssemblerControllerMenu(id, playerInventory, this);
    }

    @Override
    protected java.util.List<com.raishxn.ufo.block.entity.processing.MultiblockProcessingRecipe> getAvailableRecipes() {
        if (this.level == null) {
            return java.util.List.of();
        }
        java.util.List<com.raishxn.ufo.block.entity.processing.MultiblockProcessingRecipe> recipes = new java.util.ArrayList<>();
        for (var holder : this.level.getRecipeManager().getAllRecipesFor(com.raishxn.ufo.init.ModRecipes.UNIVERSAL_MULTIBLOCK_TYPE.get())) {
            var recipe = holder.value();
            if (recipe.getMachine() == com.raishxn.ufo.recipe.UniversalMultiblockMachineKind.QUANTUM_PROCESSOR_ASSEMBLER) {
                recipes.add(com.raishxn.ufo.block.entity.processing.MultiblockProcessingRecipe.fromUniversal(holder.id(), recipe));
            }
        }
        return recipes;
    }
}
