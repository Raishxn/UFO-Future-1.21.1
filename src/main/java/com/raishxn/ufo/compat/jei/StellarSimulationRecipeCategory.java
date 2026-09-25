package com.raishxn.ufo.compat.jei;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.recipe.StellarSimulationRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

public class StellarSimulationRecipeCategory implements IRecipeCategory<StellarSimulationRecipe> {

    public static final RecipeType<StellarSimulationRecipe> RECIPE_TYPE =
            RecipeType.create(UfoMod.MOD_ID, "stellar_simulation", StellarSimulationRecipe.class);

    private static final int WIDTH = 256;
    private static final int HEIGHT = 256;
    // Slot coordinates point inside the frames in the supplied 256x256 atlas.
    private static final int[] ITEM_INPUT_X = {53, 76, 53, 76, 163, 186, 163, 186, 119};
    private static final int[] ITEM_INPUT_Y = {19, 19, 51, 51, 19, 19, 51, 51, 35};
    private static final int[] FLUID_INPUT_X = {96, 108, 149, 108};
    private static final int[] FLUID_INPUT_Y = {25, 58, 25, 17};
    private static final int[] FLUID_INPUT_WIDTH = {9, 38, 9, 38};
    private static final int[] FLUID_INPUT_HEIGHT = {34, 9, 34, 9};
    private static final int[] ITEM_OUTPUT_GRID_X = {21, 103, 185};
    private static final int[] FLUID_OUTPUT_X = {81, 164};
    private static final int OUTPUT_Y = 84;
    private static final int FLUID_OUTPUT_Y = 83;
    private static final ResourceLocation BACKGROUND =
            UfoMod.id("textures/guis/stellar_nexus_jei_v3_ae2_normal.png");

    private final IDrawable icon;
    private final IDrawable background;

    public StellarSimulationRecipeCategory(IJeiHelpers helpers) {
        var guiHelper = helpers.getGuiHelper();
        this.icon = guiHelper.createDrawableItemStack(
                MultiblockBlocks.STELLAR_NEXUS_CONTROLLER.get().asItem().getDefaultInstance());
        this.background = guiHelper.createDrawable(BACKGROUND, 0, 0, WIDTH, HEIGHT);
    }

    @Override
    public RecipeType<StellarSimulationRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.ufo.stellar_simulation");
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public ResourceLocation getRegistryName(StellarSimulationRecipe recipe) {
        return JeiRecipeIds.get(recipe);
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, StellarSimulationRecipe recipe, IFocusGroup focuses) {
        var itemInputs = recipe.getItemInputs();
        for (int i = 0; i < itemInputs.size() && i < ITEM_INPUT_X.length; i++) {
            if (!itemInputs.get(i).isEmpty()) {
                int finalI = i;

                var visualStacks = java.util.Arrays.stream(UfoJeiPlugin.stackOf(itemInputs.get(i)).getItems())
                        .map(stack -> {
                            var copy = stack.copy();
                            copy.setCount(1);
                            return copy;
                        }).toList();

                builder.addInputSlot(ITEM_INPUT_X[i], ITEM_INPUT_Y[i])
                        .addItemStacks(visualStacks)
                        .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                            long amount = itemInputs.get(finalI).getAmount();
                            tooltip.add(Component.literal("Amount Required: " + formatAmount(amount)));
                        });
            }
        }

        var fluidInputs = recipe.getFluidInputs();
        for (int i = 0; i < fluidInputs.size() && i < FLUID_INPUT_X.length; i++) {
            if (!fluidInputs.get(i).isEmpty()) {
                int finalI = i;

                var visualFluids = UfoJeiPlugin.stackOf(fluidInputs.get(i)).stream()
                        .map(stack -> new FluidStack(stack.getFluid(), 1000))
                        .toList();

                var slot = builder.addInputSlot(FLUID_INPUT_X[i], FLUID_INPUT_Y[i])
                        .setFluidRenderer(1_000, false, FLUID_INPUT_WIDTH[i], FLUID_INPUT_HEIGHT[i]);
                slot.addIngredients(NeoForgeTypes.FLUID_STACK, visualFluids)
                        .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                            long amount = fluidInputs.get(finalI).getAmount();
                            tooltip.add(Component.literal("Amount Required: " + formatAmount(amount) + " mB"));
                        });
            }
        }

        var itemOutputs = recipe.getItemOutputs();
        for (int i = 0; i < itemOutputs.size() && i < 81; i++) {
            if (itemOutputs.get(i).what() instanceof AEItemKey itemKey) {
                int grid = i / 27;
                int col = i % 3;
                int row = (i % 27) / 3;
                int finalI = i;
                builder.addOutputSlot(ITEM_OUTPUT_GRID_X[grid] + col * 17, OUTPUT_Y + row * 17)
                        .addItemStack(itemKey.toStack(1))
                        .addRichTooltipCallback((recipeSlotView, tooltip) ->
                                tooltip.add(Component.literal("Amount Produced: " + formatAmount(itemOutputs.get(finalI).amount()))));
            }
        }

        var fluidOutputs = recipe.getFluidOutputs();
        for (int i = 0; i < fluidOutputs.size() && i < 18; i++) {
            if (fluidOutputs.get(i).what() instanceof AEFluidKey fluidKey) {
                int col = i / 9;
                int row = i % 9;
                int finalI = i;
                var slot = builder.addOutputSlot(FLUID_OUTPUT_X[col], FLUID_OUTPUT_Y + row * 17)
                        .setFluidRenderer(1_000, false, 10, 16);
                slot.addFluidStack(fluidKey.getFluid(), 1000)
                        .addRichTooltipCallback((recipeSlotView, tooltip) ->
                                tooltip.add(Component.literal("Amount Produced: " + formatAmount(fluidOutputs.get(finalI).amount()) + " mB")));
            }
        }
    }

    @Override
    public void draw(StellarSimulationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx, double mouseX, double mouseY) {
        this.background.draw(gfx);
        Font font = Minecraft.getInstance().font;

        String summary = "[ MK. " + toRoman(recipe.getFieldTier()) + " ]  ──  [ "
                + recipe.getFormattedTime() + " | " + formatEnergy(recipe.getTotalEnergy()) + " AE ]";
        gfx.fill(34, 1, 222, 13, 0xDD20283D);
        float scale = Math.min(0.85f, 180.0f / Math.max(1, font.width(summary)));
        drawScaledCenteredString(gfx, font, summary, WIDTH / 2, 3, 0xFFF4F7FF, scale);
    }

    private void drawScaledCenteredString(GuiGraphics gfx, Font font, String text, int x, int y, int color, float scale) {
        gfx.pose().pushPose();
        gfx.pose().translate(x, y, 0);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(font, text, -font.width(text) / 2, 0, color, true);
        gfx.pose().popPose();
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, StellarSimulationRecipe recipe,
                           IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if ((mouseY >= 68 && mouseY < 79 && mouseX >= 20 && mouseX <= 236)
                || (mouseY >= 1 && mouseY < 13 && mouseX >= 34 && mouseX < 222)) {
            List<Component> tips = new ArrayList<>();
            tips.add(Component.literal("Duration: " + recipe.getFormattedTime()
                    + " (" + recipe.getTime() + " ticks)"));
            tips.add(Component.literal("Field Generator: Mk." + toRoman(recipe.getFieldTier())));
            tips.add(Component.literal("Energy: " + String.format(Locale.ROOT, "%,d", recipe.getTotalEnergy()) + " AE"));
            if (!recipe.getFuelFluid().isEmpty() && recipe.getFuelAmount() > 0) {
                tips.add(Component.literal("Fuel: " + getFuelDisplayName(recipe) + " × "
                        + formatAmount(recipe.getFuelAmount()) + " mB"));
            }
            if (recipe.getCoolantAmount() > 0) {
                tips.add(Component.literal("Coolant: " + getCoolantDisplayName(recipe) + " × "
                        + formatAmount(recipe.getCoolantAmount()) + " mB"));
            }
            tips.add(Component.literal("Outputs directly into the ME network"));
            tooltip.addAll(tips);
        }
    }

    public static String formatAmount(long amount) {
        if (amount >= 1_000_000_000L) {
            double value = amount / 1_000_000_000.0;
            return value == (long) value ? (long) value + "G" : String.format(Locale.ROOT, "%.1fG", value);
        }
        if (amount >= 1_000_000L) {
            double value = amount / 1_000_000.0;
            return value == (long) value ? (long) value + "M" : String.format(Locale.ROOT, "%.1fM", value);
        }
        if (amount >= 1_000L) {
            double value = amount / 1_000.0;
            return value == (long) value ? (long) value + "K" : String.format(Locale.ROOT, "%.1fK", value);
        }
        return String.valueOf(amount);
    }

    private static String formatEnergy(long amount) {
        if (amount >= 1_000_000_000L) {
            return String.format(Locale.ROOT, "%.1fB", amount / 1_000_000_000.0);
        }
        if (amount >= 1_000_000L) {
            return String.format(Locale.ROOT, "%.1fM", amount / 1_000_000.0);
        }
        if (amount >= 1_000L) {
            return String.format(Locale.ROOT, "%.1fK", amount / 1_000.0);
        }
        return String.valueOf(amount);
    }

    private static String formatFluidName(String path) {
        if (path.startsWith("source_")) {
            path = path.substring(7);
        }
        if (path.startsWith("flowing_")) {
            path = path.substring(8);
        }

        String[] words = path.split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return builder.toString().trim();
    }

    private static String getFuelDisplayName(StellarSimulationRecipe recipe) {
        if (recipe.getFuelFluid().isEmpty()) {
            return "None";
        }
        return getFluidDisplayName(ResourceLocation.parse(recipe.getFuelFluid()));
    }

    private static String getCoolantDisplayName(StellarSimulationRecipe recipe) {
        return switch (recipe.getCoolingLevel()) {
            case 1 -> getFluidDisplayName(ResourceLocation.parse("ufo:source_gelid_cryotheum"));
            case 2 -> getFluidDisplayName(ResourceLocation.parse("ufo:source_stable_coolant"));
            case 3 -> getFluidDisplayName(ResourceLocation.parse("ufo:source_bose_einstein_condensate"));
            default -> "None";
        };
    }

    private static String getFluidDisplayName(ResourceLocation fluidId) {
        var fluid = BuiltInRegistries.FLUID.getOptional(fluidId).orElse(null);
        if (fluid == null) {
            return formatFluidName(fluidId.getPath());
        }

        String hoverName = new FluidStack(fluid, 1).getHoverName().getString();
        return hoverName == null || hoverName.isBlank() ? formatFluidName(fluidId.getPath()) : hoverName;
    }

    private static String toRoman(int tier) {
        return switch (tier) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> String.valueOf(tier);
        };
    }
}
