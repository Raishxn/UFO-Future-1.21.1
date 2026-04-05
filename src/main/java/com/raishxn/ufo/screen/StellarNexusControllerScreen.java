package com.raishxn.ufo.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.init.ModRecipes;
import com.raishxn.ufo.network.ModPackets;
import com.raishxn.ufo.network.packet.PacketChangeStellarRecipe;
import com.raishxn.ufo.network.packet.PacketStartStellarOperation;
import com.raishxn.ufo.network.packet.PacketToggleStellarSafeMode;
import com.raishxn.ufo.recipe.StellarSimulationRecipe;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class StellarNexusControllerScreen extends AbstractContainerScreen<StellarNexusControllerMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "textures/gui/stellar_nexus_controller.png");

    private List<RecipeHolder<StellarSimulationRecipe>> availableRecipes = new ArrayList<>();
    private int currentRecipeIndex = 0;
    
    private Button prevButton;
    private Button nextButton;
    private Button startButton;
    private Button safeModeButton;

    public StellarNexusControllerScreen(StellarNexusControllerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 191;
        this.imageHeight = 160;
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000; // hide player inventory label
        this.titleLabelY = 8;
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        if (this.minecraft != null && this.minecraft.level != null) {
            this.availableRecipes = new ArrayList<>(this.minecraft.level.getRecipeManager().getAllRecipesFor(ModRecipes.STELLAR_SIMULATION_TYPE.get()));
            
            // Try to align current index with BE's active recipe
            ResourceLocation activeId = this.menu.getBlockEntity().getActiveRecipeId();
            if (activeId != null) {
                for (int i = 0; i < this.availableRecipes.size(); i++) {
                    if (this.availableRecipes.get(i).id().equals(activeId)) {
                        this.currentRecipeIndex = i;
                        break;
                    }
                }
            }
        }

        int recipeY = this.topPos + 64;
        this.prevButton = this.addRenderableWidget(Button.builder(Component.literal("<"), btn -> cycleRecipe(-1))
                .bounds(this.leftPos + 19, recipeY, 22, 22).build());

        this.nextButton = this.addRenderableWidget(Button.builder(Component.literal(">"), btn -> cycleRecipe(1))
                .bounds(this.leftPos + 150, recipeY, 22, 22).build());
        
        // Start Operation button — positioned between Field Level (13-66,129-139) and Fuel (125-178,129-139)
        // Center horizontally: midpoint of 66 and 125 = 95, width ~50 -> x=70
        this.startButton = this.addRenderableWidget(Button.builder(
                Component.literal("\u25B6 START"), btn -> {
                    ModPackets.sendToServer(new PacketStartStellarOperation(this.menu.getBlockEntity().getBlockPos()));
                })
                .bounds(this.leftPos + 70, this.topPos + 126, 52, 16).build());

        // Safe Mode toggle — small button next to the heat display
        this.safeModeButton = this.addRenderableWidget(Button.builder(
                Component.literal("\u2697"), btn -> {
                    ModPackets.sendToServer(new PacketToggleStellarSafeMode(this.menu.getBlockEntity().getBlockPos()));
                })
                .bounds(this.leftPos + 174, this.topPos + 90, 14, 14).build());
        
        updateButtonTooltips();
    }

    private void updateButtonTooltips() {
        if (this.availableRecipes.isEmpty() || this.prevButton == null || this.nextButton == null) return;
        
        int prevIndex = (this.currentRecipeIndex - 1 + this.availableRecipes.size()) % this.availableRecipes.size();
        int nextIndex = (this.currentRecipeIndex + 1) % this.availableRecipes.size();
        
        ResourceLocation prevId = this.availableRecipes.get(prevIndex).id();
        ResourceLocation nextId = this.availableRecipes.get(nextIndex).id();
        
        Component prevComp = Component.translatable("gui.ufo.previous").append(": ").append(Component.literal(formatRecipeId(prevId)));
        Component nextComp = Component.translatable("gui.ufo.next").append(": ").append(Component.literal(formatRecipeId(nextId)));
        
        this.prevButton.setTooltip(Tooltip.create(prevComp));
        this.nextButton.setTooltip(Tooltip.create(nextComp));
        
        // Update start button state
        if (this.startButton != null) {
            boolean canStart = this.menu.isAssembled() && !this.menu.isRunning() && this.menu.getCooldownTimer() == 0;
            this.startButton.active = canStart;
            
            List<Component> tipLines = new ArrayList<>();
            tipLines.add(Component.literal("§e▶ Start Stellar Simulation"));
            if (!this.menu.isAssembled()) {
                tipLines.add(Component.literal("§c✗ Structure not assembled"));
            }
            if (this.menu.isRunning()) {
                tipLines.add(Component.literal("§c✗ Already in operation"));
            }
            if (this.menu.getCooldownTimer() > 0) {
                int secLeft = this.menu.getCooldownTimer() / 20;
                tipLines.add(Component.literal("§c✗ Cooling down: " + secLeft + "s remaining"));
            }
            if (this.menu.getFuelPercent() < 100) {
                tipLines.add(Component.literal("§c✗ Fuel: " + this.menu.getFuelPercent() + "% (need 100%)"));
            } else {
                tipLines.add(Component.literal("§a✓ Fuel: Ready"));
            }
            
            // Build tooltip
            Component combined = tipLines.get(0);
            for (int i = 1; i < tipLines.size(); i++) {
                combined = combined.copy().append(Component.literal("\n")).append(tipLines.get(i));
            }
            this.startButton.setTooltip(Tooltip.create(combined));
        }
        
        // Safe mode tooltip
        if (this.safeModeButton != null) {
            boolean safe = this.menu.isSafeMode();
            this.safeModeButton.setTooltip(Tooltip.create(
                    Component.literal(safe ? "§aSafe Mode: ON\n§7Auto-shutdown on overheat" : "§cSafe Mode: OFF\n§4WARNING: Explosion on overheat!")
            ));
        }
    }

    private void cycleRecipe(int delta) {
        if (this.availableRecipes.isEmpty()) return;
        
        this.currentRecipeIndex = (this.currentRecipeIndex + delta) % this.availableRecipes.size();
        if (this.currentRecipeIndex < 0) {
            this.currentRecipeIndex += this.availableRecipes.size();
        }

        ResourceLocation newId = this.availableRecipes.get(this.currentRecipeIndex).id();
        ModPackets.sendToServer(new PacketChangeStellarRecipe(this.menu.getBlockEntity().getBlockPos(), newId));
        updateButtonTooltips();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, 191, 160);

        // Progress Bar Calculation
        int p = this.menu.getProgress();
        int max = this.menu.getTotalTime();
        if (max > 0 && p > 0) {
            int progressWidth = (int) ((float) p / max * 157.0f);
            guiGraphics.blit(TEXTURE, this.leftPos + 17, this.topPos + 106, 17, 180, progressWidth, 12);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
        
        // Update button states each frame
        updateButtonTooltips();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        // Status Mode (13,43)
        Component statusText;
        if (this.menu.getCooldownTimer() > 0) {
            int secLeft = this.menu.getCooldownTimer() / 20;
            int minLeft = secLeft / 60;
            int secRemain = secLeft % 60;
            statusText = Component.literal(String.format("Cooldown %dm%02ds", minLeft, secRemain)).withStyle(ChatFormatting.GOLD);
        } else if (this.menu.isRunning()) {
            statusText = Component.literal("Running").withStyle(ChatFormatting.AQUA);
        } else if (this.menu.isAssembled()) {
            statusText = Component.literal("Assembled").withStyle(ChatFormatting.GREEN);
        } else {
            statusText = Component.literal("Offline").withStyle(ChatFormatting.RED);
        }
        guiGraphics.drawString(this.font, statusText, 15, 44, 0xFFFFFF);

        // Thermal status (125,43)
        int heat = this.menu.getHeatLevel();
        float heatPct = heat / 10.0f; // 0-1000 → 0.0%-100.0%
        int heatColor;
        String heatStr;
        if (heat == 0) {
            heatStr = "Stable";
            heatColor = 0x00FFFF;
        } else if (heatPct < 50) {
            heatStr = String.format("%.0f%%", heatPct);
            heatColor = 0xFFFF00;
        } else if (heatPct < 80) {
            heatStr = String.format("%.0f%%", heatPct);
            heatColor = 0xFF8800;
        } else {
            heatStr = String.format("%.0f%% \u26A0", heatPct);
            heatColor = 0xFF0000;
        }
        guiGraphics.drawString(this.font, heatStr, 127, 44, heatColor);

        // Recipe Selection
        Component recipeName = Component.literal("No Program");
        if (!this.availableRecipes.isEmpty()) {
            recipeName = Component.literal(formatRecipeId(this.availableRecipes.get(this.currentRecipeIndex).id()));
        }
        
        int textWidth = this.font.width(recipeName);
        int textX = 44 + ((103 - textWidth) / 2);
        guiGraphics.drawString(this.font, recipeName, textX, 72, 0xFFFFFF);
        
        // Progress Text (22,95)
        int p = this.menu.getProgress();
        int max = this.menu.getTotalTime();
        if (max > 0) {
            String prog = String.format("%.1f %%", (p / (float)max) * 100f);
            guiGraphics.drawString(this.font, prog, 22, 95, 0x00FF00);
        }
        
        // Field Level (13,129 to 66,139)
        int fl = this.menu.getFieldLevel();
        String fieldLevel = fl > 0 ? "Mk." + toRoman(fl) : "N/A";
        guiGraphics.drawString(this.font, fieldLevel, 15, 131, 0xAA00AA);
        
        // Current Fuel (125,129 to 178,139)
        int fuelPct = this.menu.getFuelPercent();
        String fuelAmount = fuelPct + "%";
        int fuelColor = fuelPct >= 100 ? 0x00FF00 : (fuelPct > 50 ? 0xFFFF00 : 0xFF4444);
        guiGraphics.drawString(this.font, fuelAmount, 127, 131, fuelColor);
        
        // Safe Mode indicator (small icon near heat)
        boolean safeMode = this.menu.isSafeMode();
        guiGraphics.drawString(this.font, safeMode ? "§a\u2697" : "§c\u2697", 170, 95, 0xFFFFFF);
    }

    private String formatRecipeId(ResourceLocation id) {
        String path = id.getPath();
        int slashIndex = path.lastIndexOf('/');
        if (slashIndex != -1) {
            path = path.substring(slashIndex + 1);
        }
        if (path.startsWith("stellar_")) {
            path = path.substring("stellar_".length());
        }
        String[] words = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
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
