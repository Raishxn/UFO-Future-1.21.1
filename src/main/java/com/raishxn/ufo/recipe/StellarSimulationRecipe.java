package com.raishxn.ufo.recipe;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.raishxn.ufo.init.ModRecipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.pedroksl.ae2addonlib.recipes.IngredientStack;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;

/**
 * A Stellar Simulation recipe — the "programs" that run inside the Stellar Nexus.
 * <p>
 * Each recipe represents a complete stellar simulation cycle (~20 minutes),
 * consuming massive amounts of energy, plasma, and optional item catalysts
 * to produce millions of items injected directly into the ME network.
 * <p>
 * <b>Design decision (Option C):</b> The Controller pulls inputs directly from
 * the connected ME grid. No input hatches or manual slot insertion required.
 * The player only needs the items/fluids in their ME storage and selects
 * the program via the Controller GUI.
 * <p>
 * Additional fields beyond the DMA pattern:
 * <ul>
 *   <li>{@code coolingLevel} — minimum cooling score required (0-3)</li>
 *   <li>{@code fieldTier} — minimum Stellar Field Generator tier (1-3)</li>
 * </ul>
 */
public class StellarSimulationRecipe implements Recipe<RecipeInput> {

    protected final List<IngredientStack.Item> itemInputs;
    protected final List<IngredientStack.Fluid> fluidInputs;
    protected final List<GenericStack> itemOutputs;
    protected final List<GenericStack> fluidOutputs;

    protected final int fuel;         // Total AE fuel cost for the simulation
    protected final int time;         // Total ticks for the simulation
    protected final int coolingLevel; // 0 = none, 1 = basic, 2 = advanced, 3 = extreme
    protected final int fieldTier;    // 1 = Mk.I, 2 = Mk.II, 3 = Mk.III

    public StellarSimulationRecipe(
            List<IngredientStack.Item> itemInputs,
            List<IngredientStack.Fluid> fluidInputs,
            List<GenericStack> itemOutputs,
            List<GenericStack> fluidOutputs,
            int fuel,
            int time,
            int coolingLevel,
            int fieldTier) {
        this.itemInputs = itemInputs;
        this.fluidInputs = fluidInputs;
        this.itemOutputs = itemOutputs;
        this.fluidOutputs = fluidOutputs;
        this.fuel = fuel;
        this.time = time > 0 ? time : 24000; // default 20 minutes
        this.coolingLevel = Math.clamp(coolingLevel, 0, 3);
        this.fieldTier = Math.clamp(fieldTier, 1, 3);
    }

    // ═══════════════════════════════════════════════════════════
    //  Recipe<RecipeInput> — Required overrides
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean matches(@NotNull RecipeInput recipeInput, @NotNull Level level) {
        // Matching is done manually by the Controller BE, not by the vanilla system
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull RecipeInput inv, HolderLookup.@NotNull Provider registries) {
        return getResultItem(registries).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return getResultItem();
    }

    public ItemStack getResultItem() {
        if (!this.itemOutputs.isEmpty() && this.itemOutputs.get(0).what() instanceof AEItemKey key) {
            // Cap at Integer.MAX_VALUE for ItemStack display — real injection uses long
            return key.toStack((int) Math.min(this.itemOutputs.get(0).amount(), Integer.MAX_VALUE));
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return StellarSimulationRecipeSerializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.STELLAR_SIMULATION_TYPE.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    // ═══════════════════════════════════════════════════════════
    //  Accessors
    // ═══════════════════════════════════════════════════════════

    public List<IngredientStack.Item> getItemInputs() {
        return itemInputs;
    }

    public List<IngredientStack.Fluid> getFluidInputs() {
        return fluidInputs;
    }

    public List<GenericStack> getItemOutputs() {
        return itemOutputs;
    }

    public List<GenericStack> getFluidOutputs() {
        return fluidOutputs;
    }

    /**
     * All valid inputs combined (items + fluids) for recipe matching in the Controller.
     */
    public List<IngredientStack<?, ?>> getValidInputs() {
        List<IngredientStack<?, ?>> validInputs = new ArrayList<>();

        for (var input : this.itemInputs) {
            if (!input.isEmpty()) {
                validInputs.add(input.sample());
            }
        }

        for (var input : this.fluidInputs) {
            if (!input.isEmpty()) {
                validInputs.add(input.sample());
            }
        }

        return validInputs;
    }

    /** Total AE fuel cost for the entire simulation. */
    public int getFuelCost() {
        return fuel;
    }

    /** Total ticks for the simulation cycle. */
    public int getTime() {
        return time;
    }

    /** Minimum cooling score required (0-3). */
    public int getCoolingLevel() {
        return coolingLevel;
    }

    /** Minimum Stellar Field Generator tier required (1-3). */
    public int getFieldTier() {
        return fieldTier;
    }

    /**
     * Total fuel cost (same as getFuelCost for display).
     */
    public long getTotalEnergy() {
        return fuel;
    }

    /**
     * Formatted time string for display (e.g., "20m 00s").
     */
    public String getFormattedTime() {
        int totalSeconds = time / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%dm %02ds", minutes, seconds);
    }
}
