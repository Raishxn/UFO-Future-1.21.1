package com.raishxn.ufo.screen;

import com.raishxn.ufo.block.entity.IUniversalMultiblockController;
import com.raishxn.ufo.block.entity.UniversalDisplayedRecipe;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public abstract class AbstractUniversalMultiblockControllerMenu<T extends BlockEntity & IUniversalMultiblockController & IUpgradeableObject> extends UpgradeableMenu<T> {
    protected final ContainerLevelAccess levelAccess;

    @GuiSync(20)
    protected int assembled = 0;
    @GuiSync(21)
    protected int running = 0;
    @GuiSync(22)
    protected int progress = 0;
    @GuiSync(23)
    protected int maxProgress = 0;
    @GuiSync(24)
    protected int temperature = 0;
    @GuiSync(25)
    protected int maxTemperature = 1000;
    @GuiSync(26)
    protected int safeMode = 1;
    @GuiSync(27)
    protected int overclocked = 0;
    @GuiSync(28)
    protected int machineTier = 1;

    protected AbstractUniversalMultiblockControllerMenu(net.minecraft.world.inventory.MenuType<?> menuType, int id, Inventory playerInventory,
                                                        T blockEntity, ContainerLevelAccess levelAccess) {
        super(menuType, id, playerInventory, blockEntity);
        this.levelAccess = levelAccess;
    }

    public T getBlockEntity() {
        return this.getHost();
    }

    @Override
    protected void standardDetectAndSendChanges() {
        if (isServerSide()) {
            this.assembled = this.getHost().isGuiAssembled() ? 1 : 0;
            this.running = this.getHost().isGuiRunning() ? 1 : 0;
            this.progress = this.getHost().getGuiProgress();
            this.maxProgress = this.getHost().getGuiMaxProgress();
            this.temperature = this.getHost().getGuiTemperature();
            this.maxTemperature = this.getHost().getGuiMaxTemperature();
            this.safeMode = this.getHost().isGuiSafeMode() ? 1 : 0;
            this.overclocked = this.getHost().isGuiOverclocked() ? 1 : 0;
            this.machineTier = this.getHost().getGuiMachineTier();
        }
        super.standardDetectAndSendChanges();
    }

    public boolean isAssembled() {
        return this.assembled == 1;
    }

    public boolean isRunning() {
        return this.running == 1;
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    public int getTemperature() {
        return this.temperature;
    }

    public int getMaxTemperature() {
        return Math.max(1, this.maxTemperature);
    }

    public boolean isSafeMode() {
        return this.safeMode == 1;
    }

    public boolean isOverclocked() {
        return this.overclocked == 1;
    }

    public int getMachineTier() {
        return Math.max(1, this.machineTier);
    }

    public List<UniversalDisplayedRecipe> getDisplayedRecipes() {
        return this.getHost().getDisplayedRecipes();
    }

    protected abstract Block getValidBlock();
}
