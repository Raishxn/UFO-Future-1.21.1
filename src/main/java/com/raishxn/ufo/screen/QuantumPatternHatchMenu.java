package com.raishxn.ufo.screen;

import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.helpers.patternprovider.PatternProviderReturnInventory;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.ToolboxMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.RestrictedInputSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class QuantumPatternHatchMenu extends AEBaseMenu {
    protected final PatternProviderLogic logic;
    private final ToolboxMenu toolbox;

    @GuiSync(3)
    public YesNo blockingMode = YesNo.NO;
    @GuiSync(4)
    public YesNo showInAccessTerminal = YesNo.YES;
    @GuiSync(5)
    public LockCraftingMode lockCraftingMode = LockCraftingMode.NONE;
    @GuiSync(6)
    public LockCraftingMode craftingLockedReason = LockCraftingMode.NONE;
    @GuiSync(7)
    public GenericStack unlockStack = null;
    @GuiSync(20)
    public boolean quantumWireless;
    @GuiSync(21)
    public boolean hasQuantumWireless;
    @GuiSync(22)
    public int wirelessLinkCount;
    @GuiSync(31) public int linkRange = 32;
    @GuiSync(26) public double bonusSpeed = 1;
    @GuiSync(27) public double bonusEnergy = 1;
    @GuiSync(28) public double bonusHeat = 1;
    @GuiSync(29) public int bonusMachines;
    @GuiSync(32) public int connectedMachines;
    @GuiSync(33) public int activeMultiblocks;
    @GuiSync(34) public double multiSpeed = 1;
    @GuiSync(35) public double multiEnergy = 1;
    @GuiSync(36) public double multiHeat = 1;
    @GuiSync(37) public boolean patternBuffer;
    private final com.raishxn.ufo.wireless.QuantumWirelessHost wirelessHost;

    public QuantumPatternHatchMenu(MenuType<? extends QuantumPatternHatchMenu> menuType, int id, Inventory playerInventory,
            PatternProviderLogicHost host) {
        super(menuType, id, playerInventory, host);
        this.toolbox = new ToolboxMenu(this);
        this.createPlayerInventorySlots(playerInventory);
        this.logic = host.getLogic();
        this.wirelessHost = host instanceof com.raishxn.ufo.wireless.QuantumWirelessHost wireless ? wireless : null;
        registerClientAction("quantumWireless", this::toggleQuantumWireless);
        registerClientAction("quantumRange", Integer.class, this::adjustRange);
        if (logic instanceof IUpgradeableObject upgradeable) {
            setupUpgrades(upgradeable.getUpgrades());
        }

        var patternInv = logic.getPatternInv();
        for (int slot = 0; slot < patternInv.size(); slot++) {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.PROVIDER_PATTERN, patternInv, slot),
                    SlotSemantics.ENCODED_PATTERN);
        }

        var returnInv = logic.getReturnInv().createMenuWrapper();
        for (int slot = 0; slot < PatternProviderReturnInventory.NUMBER_OF_SLOTS; slot++) {
            if (slot < returnInv.size()) {
                this.addSlot(new AppEngSlot(returnInv, slot), SlotSemantics.STORAGE);
            }
        }
    }

    public QuantumPatternHatchMenu(int id, Inventory playerInventory, PatternProviderLogicHost host) {
        this(host instanceof com.raishxn.ufo.block.entity.QuantumPatternHatchBE provider && provider.isPatternBuffer()
                        ? com.raishxn.ufo.init.ModMenus.QUANTUM_PATTERN_BUFFER_MENU.get()
                        : com.raishxn.ufo.init.ModMenus.QUANTUM_PATTERN_HATCH_MENU.get(),
                id, playerInventory, host);
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide()) {
            hasQuantumWireless = wirelessHost != null;
            wirelessLinkCount = wirelessHost == null ? 0 : wirelessHost.wirelessLinks().size();
            linkRange = wirelessHost == null ? 32 : wirelessHost.wirelessLinks().range();
            connectedMachines = wirelessHost == null ? 0 : wirelessHost.wirelessLinks().connectedMachines(wirelessHost);
            if (wirelessHost instanceof com.raishxn.ufo.block.entity.QuantumPatternHatchBE hatch) {
                patternBuffer = hatch.isPatternBuffer();
                var profile = hatch.bonusProfile;
                com.raishxn.ufo.wireless.QuantumWirelessActivity.refresh(hatch.getLevel());
                bonusSpeed = profile.dmaBonus.speed(); bonusEnergy = profile.dmaBonus.energy(); bonusHeat = profile.dmaBonus.heat();
                bonusMachines = profile.dmaMachines;
                activeMultiblocks = profile.multiblockMachines;
                multiSpeed = profile.multiblockBonus.speed(); multiEnergy = profile.multiblockBonus.energy(); multiHeat = profile.multiblockBonus.heat();
            }
            quantumWireless = wirelessHost != null && wirelessHost.wirelessLinks().enabled();
            blockingMode = logic.getConfigManager().getSetting(Settings.BLOCKING_MODE);
            showInAccessTerminal = logic.getConfigManager().getSetting(Settings.PATTERN_ACCESS_TERMINAL);
            lockCraftingMode = logic.getConfigManager().getSetting(Settings.LOCK_CRAFTING_MODE);
            craftingLockedReason = logic.getCraftingLockedReason();
            unlockStack = logic.getUnlockStack();
        }
        this.toolbox.tick();
        super.broadcastChanges();
    }

    public GenericStackInv getReturnInv() {
        return logic.getReturnInv();
    }

    public void toggleQuantumWireless() {
        if (isClientSide()) { sendClientAction("quantumWireless"); return; }
        if (wirelessHost != null) {
            wirelessHost.wirelessLinks().toggleMode();
            wirelessHost.getBlockEntity().setChanged();
        }
    }
    public void adjustRange(int delta) {
        if (delta != 1 && delta != -1) return;
        if (isClientSide()) { sendClientAction("quantumRange", delta); return; }
        if (wirelessHost != null) {
            wirelessHost.wirelessLinks().setRange(wirelessHost.wirelessLinks().range() + delta);
            wirelessHost.getBlockEntity().setChanged();
        }
    }

    public YesNo getBlockingMode() {
        return blockingMode;
    }

    public LockCraftingMode getLockCraftingMode() {
        return lockCraftingMode;
    }

    public LockCraftingMode getCraftingLockedReason() {
        return craftingLockedReason;
    }

    public GenericStack getUnlockStack() {
        return unlockStack;
    }

    public YesNo getShowInAccessTerminal() {
        return showInAccessTerminal;
    }

    public IUpgradeInventory getUpgrades() {
        return logic instanceof IUpgradeableObject upgradeable ? upgradeable.getUpgrades()
                : appeng.api.upgrades.UpgradeInventories.empty();
    }

    public ToolboxMenu getToolbox() {
        return toolbox;
    }
}
