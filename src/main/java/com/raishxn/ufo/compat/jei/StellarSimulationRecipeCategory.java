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
    private static final int WIDTH = 176;
    private static final int HEIGHT = 120;

    // Input area (left side)
    private static final int INPUT_START_X = 4;
    private static final int INPUT_START_Y = 22;

    // Output area (right side)
    private static final int OUTPUT_START_X = 112;
    private static final int OUTPUT_START_Y = 22;

    // Progress bar
    private static final int PROGRESS_X = 76;
    private static final int PROGRESS_Y = 32;

    // Info bar
    private static final int INFO_BAR_Y = 80;
    private static final int INFO_BAR_H = 36;

    private final IDrawable icon;

    public StellarSimulationRecipeCategory(IJeiHelpers helpers) {
        var guiHelper = helpers.getGuiHelper();
        icon = guiHelper.createDrawableItemStack(
                MultiblockBlocks.STELLAR_NEXUS_CONTROLLER.get().asItem().getDefaultInstance());
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
        // --- INPUTS (left side) ---

        // Item inputs: up to 6 slots in a 3×2 grid
        var itemInputs = recipe.getItemInputs();
        for (int i = 0; i < itemInputs.size() && i < 6; i++) {
            if (!itemInputs.get(i).isEmpty()) {
                int col = i % 3;
                int row = i / 3;
                builder.addInputSlot(INPUT_START_X + (col * 18), INPUT_START_Y + (row * 18))
                        .addIngredients(UfoJeiPlugin.stackOf(itemInputs.get(i)));
            }
        }

        // Fluid inputs: up to 2 tall fluid columns next to item inputs
        var fluidInputs = recipe.getFluidInputs();
        for (int i = 0; i < fluidInputs.size() && i < 2; i++) {
            if (!fluidInputs.get(i).isEmpty()) {
                var slot = builder.addInputSlot(INPUT_START_X + 56 + (i * 16), INPUT_START_Y)
                        .setFluidRenderer(1000000, false, 12, 34);
                slot.addIngredients(NeoForgeTypes.FLUID_STACK, UfoJeiPlugin.stackOf(fluidInputs.get(i)));
            }
        }

        // --- OUTPUTS (right side) ---

        // Item outputs: up to 6 slots in a 3×2 grid
        var itemOutputs = recipe.getItemOutputs();
        for (int i = 0; i < itemOutputs.size() && i < 6; i++) {
            if (itemOutputs.get(i).what() instanceof AEItemKey itemKey) {
                int col = i % 3;
                int row = i / 3;
                // Display count is capped at Integer.MAX_VALUE for ItemStack
                int displayCount = (int) Math.min(itemOutputs.get(i).amount(), Integer.MAX_VALUE);
                builder.addOutputSlot(OUTPUT_START_X + (col * 18), OUTPUT_START_Y + (row * 18))
                        .addItemStack(itemKey.toStack(displayCount));
            }
        }

        // Fluid outputs: column at the right
        var fluidOutputs = recipe.getFluidOutputs();
        for (int i = 0; i < fluidOutputs.size() && i < 2; i++) {
            if (fluidOutputs.get(i).what() instanceof AEFluidKey fluidKey) {
                var slot = builder.addOutputSlot(OUTPUT_START_X + 54 + (i * 16), OUTPUT_START_Y)
                        .setFluidRenderer(1000000, false, 12, 34);
                slot.addFluidStack(fluidKey.getFluid(), (int) Math.min(fluidOutputs.get(i).amount(), Integer.MAX_VALUE));
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

        // ── Background panel ──
        gfx.fill(0, 0, WIDTH, HEIGHT, 0xCC1A1A2E);          // Dark purple-black bg
        gfx.fill(1, 1, WIDTH - 1, HEIGHT - 1, 0xCC0D0D1A);  // Slightly darker inner

        // ── Top title area ──
        gfx.fillGradient(0, 0, WIDTH, 14, 0xFF1E0A3E, 0xFF0E0520);
        gfx.drawCenteredString(font, "§d§l✦ Stellar Simulation ✦", WIDTH / 2, 3, 0xFFE0B0FF);

        // ── Section labels ──
        gfx.drawString(font, "§7Inputs", INPUT_START_X, INPUT_START_Y - 10, 0xFFAAAAAA, false);
        gfx.drawString(font, "§7Outputs", OUTPUT_START_X, OUTPUT_START_Y - 10, 0xFFAAAAAA, false);

        // ── Progress arrow area ──
        // Draw a simple animated arrow placeholder
        gfx.fill(PROGRESS_X, PROGRESS_Y, PROGRESS_X + 24, PROGRESS_Y + 17, 0x44FFFFFF);
        // Animated fill
        int animWidth = (int) ((System.currentTimeMillis() / 40) % 24);
        gfx.fillGradient(PROGRESS_X, PROGRESS_Y, PROGRESS_X + animWidth, PROGRESS_Y + 17,
                0xAA8B5CF6, 0xAA6D28D9);
        gfx.drawCenteredString(font, "§f►", PROGRESS_X + 12, PROGRESS_Y + 5, 0xFFFFFFFF);

        // ── Info bar (bottom section) ──
        int infoY = INFO_BAR_Y;
        gfx.fill(2, infoY, WIDTH - 2, infoY + INFO_BAR_H, 0xCC161625);

        // Row 1: Energy + Time
        String energyStr = "§b⚡ " + formatEnergy(recipe.getEnergy()) + "/t";
        String timeStr = "§e⏱ " + recipe.getFormattedTime();
        gfx.drawString(font, energyStr, 6, infoY + 4, 0xFFFFFFFF, true);
        gfx.drawString(font, timeStr, WIDTH / 2, infoY + 4, 0xFFFFFFFF, true);

        // Row 2: Cooling + Field Tier
        String coolingStr = "§3❄ Cooling: Lv." + recipe.getCoolingLevel();
        String fieldStr = "§d⚛ Field: Mk." + toRoman(recipe.getFieldTier());
        gfx.drawString(font, coolingStr, 6, infoY + 16, 0xFFCCCCCC, true);
        gfx.drawString(font, fieldStr, WIDTH / 2, infoY + 16, 0xFFCCCCCC, true);

        // Row 3: Total energy
        String totalStr = "§8Total: " + formatEnergy((int) Math.min(recipe.getTotalEnergy(), Integer.MAX_VALUE));
        gfx.drawString(font, totalStr, 6, infoY + 27, 0xFF888888, true);
    }

    // ═══════════════════════════════════════════════════════════
    //  Tooltips
    // ═══════════════════════════════════════════════════════════

    @Override
    public List<Component> getTooltipStrings(StellarSimulationRecipe recipe, IRecipeSlotsView recipeSlotsView,
                                              double mouseX, double mouseY) {
        // Tooltip on info bar
        if (mouseY >= INFO_BAR_Y && mouseY <= INFO_BAR_Y + INFO_BAR_H) {
            List<Component> tips = new ArrayList<>();
            tips.add(Component.literal("§b⚡ Energy: " + formatEnergy(recipe.getEnergy()) + " AE per tick"));
            tips.add(Component.literal("§e⏱ Duration: " + recipe.getFormattedTime()
                    + " (" + recipe.getTime() + " ticks)"));
            tips.add(Component.literal("§3❄ Minimum Cooling Level: " + recipe.getCoolingLevel() + "/3"));
            tips.add(Component.literal("§d⚛ Minimum Field Generator: Mk." + toRoman(recipe.getFieldTier())));
            tips.add(Component.literal(""));
            tips.add(Component.literal("§8Total Energy: " + formatEnergy(
                    (int) Math.min(recipe.getTotalEnergy(), Integer.MAX_VALUE))));
            tips.add(Component.literal("§7Outputs are injected directly into the ME Network"));
            return tips;
        }

        // Tooltip on progress arrow
        if (mouseX >= PROGRESS_X && mouseX <= PROGRESS_X + 24
                && mouseY >= PROGRESS_Y && mouseY <= PROGRESS_Y + 17) {
            return List.of(
                    Component.literal("§e" + recipe.getFormattedTime()),
                    Component.literal("§7(" + recipe.getTime() + " ticks)")
            );
        }

        return List.of();
    }

    // ═══════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════

    private static String formatEnergy(int energy) {
        if (energy >= 1_000_000_000) {
            double val = energy / 1_000_000_000.0;
            if (val == (long) val) return (long) val + "G AE";
            return String.format("%.1fG AE", val);
        } else if (energy >= 1_000_000) {
            double val = energy / 1_000_000.0;
            if (val == (long) val) return (long) val + "M AE";
            return String.format("%.1fM AE", val);
        } else if (energy >= 1_000) {
            double val = energy / 1_000.0;
            if (val == (long) val) return (long) val + "K AE";
            return String.format("%.1fK AE", val);
        }
        return energy + " AE";
    }

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
