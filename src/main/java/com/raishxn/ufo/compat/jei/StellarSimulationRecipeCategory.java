package com.raishxn.ufo.compat.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.recipe.StellarSimulationRecipe;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI recipe category for Stellar Simulation recipes.
 * <p>
 * Layout (programmatic — no background texture):
 * <pre>
 * ┌─────────────────────────────────────────────────────────┐
 * │  [Title: Simulation Program Name]                       │
 * │                                                         │
 * │  INPUTS              ►►►           OUTPUTS              │
 * │  [slot] [slot]   [progress]   [slot] [slot] [slot]      │
 * │  [slot] [fluid]              [slot] [slot] [fluid]      │
 * │                                                         │
 * │  ═══════════ INFO BAR ═══════════                       │
 * │  ⚡ 5M AE/t  │ ⏱ 20m 00s  │ ❄ Lv.2  │ ⚛ Mk.II       │
 * └─────────────────────────────────────────────────────────┘
 * </pre>
 */
public class StellarSimulationRecipeCategory implements IRecipeCategory<StellarSimulationRecipe> {

    public static final RecipeType<StellarSimulationRecipe> RECIPE_TYPE =
            RecipeType.create(UfoMod.MOD_ID, "stellar_simulation", StellarSimulationRecipe.class);

    // Layout constants
    private static final int WIDTH = 191;
    private static final int HEIGHT = 128;

    // Progress bar
    private static final int PROGRESS_X = 94;
    private static final int PROGRESS_Y = 38;

    private final IDrawable icon;
    private final IDrawable background;

    public StellarSimulationRecipeCategory(IJeiHelpers helpers) {
        var guiHelper = helpers.getGuiHelper();
        icon = guiHelper.createDrawableItemStack(
                MultiblockBlocks.STELLAR_NEXUS_CONTROLLER.get().asItem().getDefaultInstance());
        background = guiHelper.createDrawable(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "textures/gui/stellar_nexus_jei.png"),
                0, 0, WIDTH, HEIGHT);
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
        return icon;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    // ═══════════════════════════════════════════════════════════
    //  Recipe Layout — Slots
    // ═══════════════════════════════════════════════════════════

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, StellarSimulationRecipe recipe, IFocusGroup focuses) {
        // --- INPUTS ---

        // Item inputs: 3x3 grid spanning from (11, 16) to (64, 69)
        var itemInputs = recipe.getItemInputs();
        for (int i = 0; i < itemInputs.size() && i < 9; i++) {
            if (!itemInputs.get(i).isEmpty()) {
                int col = i % 3;
                int row = i / 3;
                int finalI = i;
                builder.addInputSlot(11 + (col * 18), 16 + (row * 18))
                        .addIngredients(UfoJeiPlugin.stackOf(itemInputs.get(i)))
                        .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                            var stacks = UfoJeiPlugin.stackOf(itemInputs.get(finalI));
                            if (!stacks.isEmpty()) {
                                tooltip.add(Component.literal("§7Amount Required: §e" + stacks.getItems()[0].getCount()));
                            }
                        });
            }
        }

        // Fluid inputs: 3 slots vertically: 71,16 | 71,36 | 71,56 (11x14 dimensions)
        var fluidInputs = recipe.getFluidInputs();
        for (int i = 0; i < fluidInputs.size() && i < 3; i++) {
            if (!fluidInputs.get(i).isEmpty()) {
                int yPos = 16 + (i * 20);
                int finalI = i;
                var slot = builder.addInputSlot(71, yPos)
                        .setFluidRenderer(1000000, false, 11, 14);
                slot.addIngredients(NeoForgeTypes.FLUID_STACK, UfoJeiPlugin.stackOf(fluidInputs.get(i)))
                    .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                         var fluids = UfoJeiPlugin.stackOf(fluidInputs.get(finalI));
                         if (!fluids.isEmpty()) {
                             tooltip.add(Component.literal("§7Amount Required: §b" + fluids.get(0).getAmount() + " mB"));
                         }
                    });
            }
        }

        // --- OUTPUTS ---

        // Item outputs: 3x3 grid starting at (127, 16)
        var itemOutputs = recipe.getItemOutputs();
        for (int i = 0; i < itemOutputs.size() && i < 9; i++) {
            if (itemOutputs.get(i).what() instanceof AEItemKey itemKey) {
                int col = i % 3;
                int row = i / 3;
                int finalI = i;
                builder.addOutputSlot(127 + (col * 18), 16 + (row * 18))
                        .addItemStack(itemKey.toStack(1)) // Show 1 visually to prevent huge text clipping
                        .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                            tooltip.add(Component.literal("§7Amount Produced: §e" + formatAmount(itemOutputs.get(finalI).amount())));
                        });
            }
        }

        // Fluid outputs: up to 6 slots
        var fluidOutputs = recipe.getFluidOutputs();
        for (int i = 0; i < fluidOutputs.size() && i < 6; i++) {
            if (fluidOutputs.get(i).what() instanceof AEFluidKey fluidKey) {
                int col = i % 3;
                int row = i / 3;
                int finalI = i;
                int xOffsets[] = {127, 148, 169}; // Defined specifically for fluid layout
                var slot = builder.addOutputSlot(xOffsets[col], 75 + (row * 20))
                        .setFluidRenderer(1000000, false, 11, 14);
                slot.addFluidStack(fluidKey.getFluid(), 1000) // Visual minimum to show texture
                    .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                         tooltip.add(Component.literal("§7Amount Produced: §b" + formatAmount(fluidOutputs.get(finalI).amount()) + " mB"));
                    });
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  Drawing — Programmatic layout (no background texture)
    // ═══════════════════════════════════════════════════════════

    @Override
    public void draw(
            StellarSimulationRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics gfx,
            double mouseX,
            double mouseY) {

        var font = Minecraft.getInstance().font;

        // Note: The UI image 'stellar_nexus_jei.png' already has titles and backgrounds baked in!
        // So we only draw dynamic text that is not covered by the background texture.

        // ── Progress area ──
        // From 94, 38 to 114, 49
        int pWidth = 20;
        int animWidth = (int) ((System.currentTimeMillis() / 40) % pWidth);
        gfx.fillGradient(94, 38, 94 + animWidth, 49, 0x558B5CF6, 0x556D28D9);

        // We DO NOT draw Energy, Cooling, Time here! The user requested we move this information 
        // to Tooltips or draw them ONLY if we are hovering certain boxes. Wait, the user said:
        // "só mostrs os valores dos outputs/ intpus das recipes ao passar o mouse por cima no JEI porque os valores que você tinha colocado estão muito grandes. ou então formate para ficar perfeito."
        // That means the values LIKE ENERGY/COOLING should be formatted. The user explicitly defined "box onde mostra o consumo". Let's draw formatted strings centered in those boxes!

        // Box: AE/t: 10,74 to 49,84 -> center is (29, 75)
        String energyStr = formatAmount(recipe.getEnergy()) + "/t";
        drawScaledCenteredString(gfx, font, energyStr, 29, 76, 0x00FFFF, 0.8f);

        // Box: Cooling: 53,74 to 98,84 -> center is (75, 75)
        String coolingStr = "Lv." + recipe.getCoolingLevel();
        drawScaledCenteredString(gfx, font, coolingStr, 75, 76, 0x00FFFF, 0.8f);

        // Box: Total Time: 10,86 to 49,96 -> center is (29, 87)
        String timeStr = recipe.getFormattedTime();
        drawScaledCenteredString(gfx, font, timeStr, 29, 88, 0xFFFFFF, 0.8f);

        // Box: Field Level: 53,86 to 92,96 -> center is (73, 87)
        String fieldStr = "Mk." + toRoman(recipe.getFieldTier());
        drawScaledCenteredString(gfx, font, fieldStr, 73, 88, 0xFFFFFF, 0.8f);

        // Box: Total usage of energy: 32,98 to 71,108 -> center is (51, 99)
        String totalStr = formatAmount((long) recipe.getTotalEnergy());
        drawScaledCenteredString(gfx, font, totalStr, 51, 100, 0xFFDF00, 0.8f);
    }
    
    // Helper to draw text slightly scaled down to fit constraints beautifully
    private void drawScaledCenteredString(GuiGraphics gfx, net.minecraft.client.gui.Font font, String text, int x, int y, int color, float scale) {
        gfx.pose().pushPose();
        gfx.pose().translate(x, y, 0);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(font, text, -font.width(text) / 2, 0, color, false);
        gfx.pose().popPose();
    }

    @Override
    public List<Component> getTooltipStrings(StellarSimulationRecipe recipe, IRecipeSlotsView recipeSlotsView,
                                              double mouseX, double mouseY) {
        List<Component> tips = new ArrayList<>();

        // Add tooltips matching the box layout for clarity:
        if (mouseY >= 74 && mouseY <= 84 && mouseX >= 10 && mouseX <= 49) {
            tips.add(Component.literal("§b⚡ Energy Demand: " + formatAmount(recipe.getEnergy()) + " AE/t"));
            return tips;
        }
        if (mouseY >= 74 && mouseY <= 84 && mouseX >= 53 && mouseX <= 98) {
            tips.add(Component.literal("§3❄ Minimum Cooling Level: " + recipe.getCoolingLevel() + "/3"));
            return tips;
        }
        if (mouseY >= 86 && mouseY <= 96 && mouseX >= 10 && mouseX <= 49) {
            tips.add(Component.literal("§e⏱ Duration: " + recipe.getFormattedTime() + " (" + recipe.getTime() + " ticks)"));
            return tips;
        }
        if (mouseY >= 86 && mouseY <= 96 && mouseX >= 53 && mouseX <= 92) {
            tips.add(Component.literal("§d⚛ Required Stellar Field Generator: Mk." + toRoman(recipe.getFieldTier())));
            return tips;
        }
        if (mouseY >= 98 && mouseY <= 108 && mouseX >= 32 && mouseX <= 71) {
            tips.add(Component.literal("§8Total Energy Required: " + formatAmount((long) recipe.getTotalEnergy()) + " AE"));
            return tips;
        }

        // Tooltip on progress bar
        if (mouseX >= 94 && mouseX <= 114 && mouseY >= 38 && mouseY <= 49) {
            return List.of(
                    Component.literal("§e" + recipe.getFormattedTime()),
                    Component.literal("§7(" + recipe.getTime() + " ticks)"),
                    Component.literal("§7Outputs directly into ME Network")
            );
        }

        return List.of();
    }

    // ═══════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════

    // Helper isn't needed anymore with the updated formating logic.

    /**
     * Formats an amount for display (e.g., 5000000 → "5M").
     */
    public static String formatAmount(long amount) {
        if (amount >= 1_000_000) {
            double val = amount / 1_000_000.0;
            if (val == (long) val) return (long) val + "M";
            return String.format("%.1fM", val);
        } else if (amount >= 1_000) {
            double val = amount / 1_000.0;
            if (val == (long) val) return (long) val + "K";
            return String.format("%.1fK", val);
        }
        return String.valueOf(amount);
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
