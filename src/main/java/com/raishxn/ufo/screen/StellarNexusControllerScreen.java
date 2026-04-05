package com.raishxn.ufo.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.init.ModRecipes;
import com.raishxn.ufo.network.ModPackets;
import com.raishxn.ufo.network.packet.PacketChangeStellarRecipe;
import com.raishxn.ufo.recipe.StellarSimulationRecipe;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.client.gui.components.Tooltip;

import java.util.ArrayList;
import java.util.List;

public class StellarNexusControllerScreen extends AbstractContainerScreen<StellarNexusControllerMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "textures/gui/stellar_nexus_controller.png");

    private List<RecipeHolder<StellarSimulationRecipe>> availableRecipes = new ArrayList<>();
    private int currentRecipeIndex = 0;
    
    private Button prevButton;
    private Button nextButton;

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
        
        // Draw the background texture (you'll create this)
        // Draw 191x160 from texture.
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, 191, 160);

        // Progress Bar Calculation
        int p = this.menu.getProgress();
        int max = this.menu.getTotalTime();
        if (max > 0 && p > 0) {
            int progressWidth = (int) ((float) p / max * 157.0f); // 174 - 17 = 157
            // Target Box: (17, 106). Source Box: u=17, v=180, width=157, height=12
            guiGraphics.blit(TEXTURE, this.leftPos + 17, this.topPos + 106, 17, 180, progressWidth, 12);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        // Removed Title rendering as requested.

        // Render Status Mode (13,43)
        Component statusText = this.menu.isAssembled() ? 
                Component.literal("Assembled").withStyle(net.minecraft.ChatFormatting.GREEN) : 
                Component.literal("Offline").withStyle(net.minecraft.ChatFormatting.RED);
        
        guiGraphics.drawString(this.font, statusText, 15, 44, 0xFFFFFF);

        // Render Coolant Placeholder (125,43)
        guiGraphics.drawString(this.font, "Stable", 127, 44, 0x00FFFF);

        // Render Recipe Selection
        Component recipeName = Component.literal("No Program");
        if (!this.availableRecipes.isEmpty()) {
            recipeName = Component.literal(formatRecipeId(this.availableRecipes.get(this.currentRecipeIndex).id()));
        }
        
        int textWidth = this.font.width(recipeName);
        // Center space is from X=44 to X=147 (width 103)
        int textX = 44 + ((103 - textWidth) / 2);
        // Box is Y=66 to 86, so center is roughly 71
        guiGraphics.drawString(this.font, recipeName, textX, 72, 0xFFFFFF);
        
        // Render Progress Text (22,95)
        int p = this.menu.getProgress();
        int max = this.menu.getTotalTime();
        if (max > 0) {
            String prog = String.format("%.1f %%", (p / (float)max) * 100f);
            guiGraphics.drawString(this.font, prog, 22, 95, 0x00FF00); // Green color for %.
        }
        
        // Render Field Level (13,129 to 66,139)
        String fieldLevel = "Max"; // To be synced later
        guiGraphics.drawString(this.font, fieldLevel, 15, 131, 0xAA00AA);
        
        // Render Current Fuel (125,129 to 178,139)
        String fuelAmount = "100%"; // To be synced later
        guiGraphics.drawString(this.font, fuelAmount, 127, 131, 0x00AAAA);
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
}
