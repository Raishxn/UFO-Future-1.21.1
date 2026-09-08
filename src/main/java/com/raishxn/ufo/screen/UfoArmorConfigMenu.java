package com.raishxn.ufo.screen;

import com.raishxn.ufo.armor.UfoArmorModule;
import com.raishxn.ufo.init.ModMenus;
import com.raishxn.ufo.item.ModItems;
import com.raishxn.ufo.item.custom.UfoArmorItem;
import com.raishxn.ufo.item.custom.UfoArmorUpgradeItem;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class UfoArmorConfigMenu extends AbstractContainerMenu {
    private static final int INPUT_SLOT = 0;
    private static final int ARMOR_SLOT_START = 1;
    private static final int PLAYER_SLOT_START = 5;

    private final Inventory playerInventory;
    private final SimpleContainer input = new SimpleContainer(1);
    private final DataSlot selectedPiece = DataSlot.standalone();

    public UfoArmorConfigMenu(int containerId, Inventory inventory) {
        super(ModMenus.UFO_ARMOR_CONFIG_MENU.get(), containerId);
        this.playerInventory = inventory;
        this.selectedPiece.set(firstEquippedPiece(inventory));
        addDataSlot(selectedPiece);

        addSlot(new Slot(input, 0, 152, 6) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof UfoArmorUpgradeItem upgrade && upgrade.module() != null;
            }
        });

        for (int row = 0; row < 4; row++) {
            addSlot(new Slot(inventory, 39 - row, 152, 27 + row * 18) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
                @Override public boolean mayPickup(Player player) { return false; }
            });
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 113 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 171));
        }
    }

    public int selectedPiece() {
        return selectedPiece.get();
    }

    public ItemStack selectedArmor() {
        int piece = selectedPiece();
        return piece >= 0 && piece < 4 ? playerInventory.getItem(39 - piece) : ItemStack.EMPTY;
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (playerInventory.player.level().isClientSide() || container != input) return;
        tryInstallInput();
    }

    private void tryInstallInput() {
        ItemStack cardStack = input.getItem(0);
        if (!(cardStack.getItem() instanceof UfoArmorUpgradeItem card) || card.module() == null) return;
        if (UfoArmorItem.installModule(selectedArmor(), card.module())) {
            cardStack.shrink(1);
            input.setItem(0, cardStack);
            broadcastChanges();
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id < 4) {
            if (UfoArmorItem.isUfoArmorPiece(playerInventory.getItem(39 - id))) {
                selectedPiece.set(id);
                tryInstallInput();
                broadcastChanges();
                return true;
            }
            return false;
        }
        int ordinal = id >= 200 ? id - 200 : id >= 100 ? id - 100 : -1;
        if (ordinal < 0 || ordinal >= UfoArmorModule.values().length) return false;
        UfoArmorModule module = UfoArmorModule.values()[ordinal];
        ItemStack armor = selectedArmor();
        if (id >= 200) {
            if (UfoArmorItem.removeModule(armor, module)) {
                player.getInventory().placeItemBackInInventory(new ItemStack(ModItems.moduleCard(module).get()));
                broadcastChanges();
                return true;
            }
        } else if (UfoArmorItem.toggleModule(armor, module)) {
            broadcastChanges();
            return true;
        }
        return false;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return result;
        ItemStack source = slot.getItem();
        result = source.copy();
        if (index == INPUT_SLOT) {
            if (!moveItemStackTo(source, PLAYER_SLOT_START, slots.size(), true)) return ItemStack.EMPTY;
        } else if (index >= PLAYER_SLOT_START && source.getItem() instanceof UfoArmorUpgradeItem upgrade
                && upgrade.module() != null) {
            if (!moveItemStackTo(source, INPUT_SLOT, INPUT_SLOT + 1, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        if (source.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        clearContainer(player, input);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        for (int index = 36; index < 40; index++) {
            if (UfoArmorItem.isUfoArmorPiece(player.getInventory().getItem(index))) return true;
        }
        return false;
    }

    private static int firstEquippedPiece(Inventory inventory) {
        for (int row = 0; row < 4; row++) {
            if (UfoArmorItem.isUfoArmorPiece(inventory.getItem(39 - row))) return row;
        }
        return 0;
    }
}
