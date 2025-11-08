package com.raishxn.ufo.menu;

import appeng.api.implementations.blockentities.IUpgradeableBlockEntity;
import appeng.api.inventories.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slots.OutputSlot;
import appeng.menu.slots.RestrictedInputSlot;
import appeng.menu.widgets.ProgressWidget;
import net.minecraft.world.entity.player.Player;

// Estendemos UpgradeableMenu conforme o tutorial
public class DmaMenu extends UpgradeableMenu<DimensionalMatterAssemblerBlockEntity> {

    // Widgets (como a barra de progresso)
    private final ProgressWidget progress;

    // Valores Sincronizados (para a GUI saber o que desenhar)
    @GuiSync(9)
    public int heat; // 0-100 (Estabilidade)
    @GuiSync(10)
    public boolean isOverloaded;

    public DmaMenu(int id, Player player, DimensionalMatterAssemblerBlockEntity host) {
        super(ModMenuTypes.DIMENSIONAL_MATTER_ASSEMBLER_MENU.get(), id, player, host);

        // Adiciona o widget de progresso
        this.progress = new ProgressWidget(this, host::getProcessTime, host::getMaxProcessTime);
        this.progress.setTop(40).setLeft(82); // Posições do seu PDF (ajuste se necessário)
        this.addWidget(this.progress);
    }

    // Este método é crucial para o MenuTypeBuilder.create
    public static IMenuHost getMenuHostInterface() {
        return IUpgradeableBlockEntity.class;
    }

    // Sincroniza nossos valores customizados (calor, overload) com o cliente
    @Override
    public void broadcastChanges() {
        this.heat = this.getHost().getHeat();
        this.isOverloaded = this.getHost().isOverloaded();
        super.broadcastChanges();
    }

    // Define os slots de upgrade
    @Override
    protected void setupConfig() {
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.InputType.UPGRADES, getHost().getUpgrades(), 0), SlotSemantics.UPGRADE);
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.InputType.UPGRADES, getHost().getUpgrades(), 1), SlotSemantics.UPGRADE);
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.InputType.UPGRADES, getHost().getUpgrades(), 2), SlotSemantics.UPGRADE);
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.InputType.UPGRADES, getHost().getUpgrades(), 3), SlotSemantics.UPGRADE);
    }

    // Define os slots principais da máquina
    @Override
    protected void setupInventorySlots() {
        var inv = getHost().getInternalInventory();

        // Slots de Input (3) - Baseado no seu PDF
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.InputType.DEFAULT, inv, 0), SlotSemantics.MACHINE_INPUT); // Input 1
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.InputType.DEFAULT, inv, 1), SlotSemantics.MACHINE_INPUT); // Input 2
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.InputType.DEFAULT, inv, 2), SlotSemantics.MACHINE_INPUT); // Input 3

        // Slot Catalisador
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.InputType.DEFAULT, inv, 3), SlotSemantics.MACHINE_PROCESSING); // Catalyst

        // Slot Coolant (usando um semantic customizado se necessário, mas FLUID_INPUT funciona)
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.InputType.DEFAULT, inv, 4), SlotSemantics.FLUID_INPUT); // Coolant

        // Slot Output
        this.addSlot(new OutputSlot(inv, 5), SlotSemantics.MACHINE_OUTPUT); // Output
    }
}