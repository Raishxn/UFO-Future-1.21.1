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

import java.util.ArrayList;
import java.util.List;

public class StellarNexusControllerScreen extends AbstractContainerScreen<StellarNexusControllerMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "textures/gui/stellar_nexus_controller.png");

    private List<RecipeHolder<StellarSimulationRecipe>> availableRecipes = new ArrayList<>();
    private int currentRecipeIndex = 0;

    public StellarNexusControllerScreen(StellarNexusControllerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 200;
        this.imageHeight = 120;
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

        int recipeY = this.topPos + 45;
        this.addRenderableWidget(Button.builder(Component.literal("◀"), btn -> cycleRecipe(-1))
                .bounds(this.leftPos + 20, recipeY, 20, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("▶"), btn -> cycleRecipe(1))
                .bounds(this.leftPos + this.imageWidth - 40, recipeY, 20, 20).build());
    }

    private void cycleRecipe(int delta) {
        if (this.availableRecipes.isEmpty()) return;
        
        this.currentRecipeIndex = (this.currentRecipeIndex + delta) % this.availableRecipes.size();
        if (this.currentRecipeIndex < 0) {
            this.currentRecipeIndex += this.availableRecipes.size();
        }

        ResourceLocation newId = this.availableRecipes.get(this.currentRecipeIndex).id();
        ModPackets.sendToServer(new PacketChangeStellarRecipe(this.menu.getBlockEntity().getBlockPos(), newId));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        // Draw the background texture (you'll create this)
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Progress Bar Calculation
        int p = this.menu.getProgress();
        int max = this.menu.getTotalTime();
        if (max > 0 && p > 0) {
            int progressWidth = (int) ((float) p / max * 160.0f);
            // Assuming your texture has the filled progress bar below the main GUI at 0, 120
            guiGraphics.blit(TEXTURE, this.leftPos + 20, this.topPos + 80, 0, 120, progressWidth, 10);
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
        // Render Title
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);

        // Render Status
        Component statusText = this.menu.isAssembled() ? 
                Component.literal("Status: Assembled").withStyle(net.minecraft.ChatFormatting.GREEN) : 
                Component.literal("Status: Incomplete").withStyle(net.minecraft.ChatFormatting.RED);
        
        guiGraphics.drawString(this.font, statusText, 10, 25, 0xFFFFFF);

        // Render Recipe Selection
        Component recipeName = Component.literal("No Program");
        if (!this.availableRecipes.isEmpty()) {
            recipeName = Component.translatable("recipe.ufo.stellar_simulation." + this.availableRecipes.get(this.currentRecipeIndex).id().getPath());
        }
        
        int textWidth = this.font.width(recipeName);
        int textX = (this.imageWidth - textWidth) / 2;
        guiGraphics.drawString(this.font, recipeName, textX, 51, 0xFFFFFF); // Centered between the arrows
        
        // Render Progress Text
        int p = this.menu.getProgress();
        int max = this.menu.getTotalTime();
        if (max > 0) {
            String prog = String.format("%.1f %%", (p / (float)max) * 100f);
            guiGraphics.drawString(this.font, prog, 20, 70, 0xAAAAAA);
        }
    }
}
