package com.raishxn.ufo.menu;

import com.raishxn.ufo.UfoMod; // <-- ADICIONADO
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.block.custom.DimensionalMatterAssemblerBlock;
import com.raishxn.ufo.block.entity.DimensionalMatterAssemblerBlockEntity;
import com.raishxn.ufo.init.ModMenus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries; // <-- ADICIONADO
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation; // <-- ADICIONADO
import net.minecraft.tags.TagKey; // <-- ADICIONADO
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item; // <-- ADICIONADO
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class DimensionalMatterAssemblerMenu extends AbstractContainerMenu {
    public final DimensionalMatterAssemblerBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    // --- ADICIONADO: Definição da Tag de Item ---
    private static final TagKey<Item> CATALYST_TAG =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "catalyst"));
    // ------------------------------------------

    private static final int PLAYER_INVENTORY_START = 0;
    private static final int PLAYER_INVENTORY_END = 36;
    private static final int INPUT_SLOTS_START = 36;
    private static final int INPUT_SLOTS_END = 45; // 36 + 9
    private static final int OUTPUT_SLOTS_START = 45;
    private static final int OUTPUT_SLOTS_END = 47; // 45 + 2
    private static final int UPGRADE_SLOTS_START = 47;
    private static final int UPGRADE_SLOTS_END = 51; // 47 + 4

    // Construtor do Cliente
    public DimensionalMatterAssemblerMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(14));
    }

    // Construtor Comum (Servidor e Cliente)
    public DimensionalMatterAssemblerMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenus.DMA_MENU.get(), pContainerId);
        checkContainerSize(inv, 15);
        this.blockEntity = ((DimensionalMatterAssemblerBlockEntity) entity);
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        ItemStackHandler itemInputHandler = this.blockEntity.itemInputHandler;
        ItemStackHandler itemOutputHandler = this.blockEntity.itemOutputHandler;
        ItemStackHandler upgradeHandler = this.blockEntity.upgradeHandler;

        // --- Posição dos Inputs --- (Índice 36-44)
        int inputStartX = 48;
        int inputStartY = 22;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new SlotItemHandler(itemInputHandler, col + row * 3,
                        inputStartX + col * 18, inputStartY + row * 18));
            }
        }

        // --- Slots de Output (Índice 45, 46) ---
        // Posições centralizadas (133, 22) e (133, 50)
        this.addSlot(new SlotItemHandler(itemOutputHandler, 0, 133, 22));
        this.addSlot(new SlotItemHandler(itemOutputHandler, 1, 133, 50));

        // =================================================================================
        // --- INÍCIO DA CORREÇÃO: Slots de Upgrade com Validação ---
        // =================================================================================
        this.addSlot(new SlotItemHandler(upgradeHandler, 0, 174, 23) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                // Checa se o item tem a tag "ufo:catalyst"
                return stack.is(CATALYST_TAG);
            }
        });
        this.addSlot(new SlotItemHandler(upgradeHandler, 0, 175, 23) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                // Checa se o item tem a tag "ufo:catalyst"
                return stack.is(CATALYST_TAG);
            }
        });
        this.addSlot(new SlotItemHandler(upgradeHandler, 1, 175, 41) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(CATALYST_TAG);
            }
        });
        this.addSlot(new SlotItemHandler(upgradeHandler, 2, 175, 59) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(CATALYST_TAG);
            }
        });
        this.addSlot(new SlotItemHandler(upgradeHandler, 3, 175, 77) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(CATALYST_TAG);
            }
        });
        // =================================================================================
        // --- FIM DA CORREÇÃO ---
        // =================================================================================

        addDataSlots(data);
    }

    public int getProgress() { return this.data.get(0); }
    public int getMaxProgress() { return this.data.get(1); }
    public int getEnergy() { return this.data.get(2); }
    public int getMaxEnergy() { return this.data.get(3); }
    public int getTemperature() { return this.data.get(13); }

    public FluidStack getCoolantStack() { return getFluidStackInternal(4, 5); }
    public FluidStack getInputFluidStack() { return getFluidStackInternal(6, 7); }
    public FluidStack getOutputFluid1Stack() { return getFluidStackInternal(8, 9); }
    public FluidStack getOutputFluid2Stack() { return getFluidStackInternal(10, 11); }

    private FluidStack getFluidStackInternal(int idSlot, int amountSlot) {
        int fluidId = this.data.get(idSlot);
        int amount = this.data.get(amountSlot);
        Fluid fluid = BuiltInRegistries.FLUID.byId(fluidId);
        if (fluid == null || amount <= 0) return FluidStack.EMPTY;
        return new FluidStack(fluid, amount);
    }

    public int getProgressPercent() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        return maxProgress > 0 && progress > 0 ? progress * 100 / maxProgress : 0;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Clicou no inventário do jogador (0-35)
        if (pIndex < PLAYER_INVENTORY_END) {

            // --- LÓGICA DE QUICKMOVE ATUALIZADA ---
            // Tenta mover para slots de UPGRADE primeiro, mas SÓ SE TIVER A TAG
            if (sourceStack.is(CATALYST_TAG)) {
                if (moveItemStackTo(sourceStack, UPGRADE_SLOTS_START, UPGRADE_SLOTS_END, false)) {
                    // Sucesso
                    // (O método 'moveItemStackTo' já chama 'mayPlace', então essa checagem é redundante,
                    // mas é uma boa prática para direcionar o item)
                }
            }
            // Senão, tenta mover para slots de INPUT
            else if (moveItemStackTo(sourceStack, INPUT_SLOTS_START, INPUT_SLOTS_END, false)) {
                // Sucesso
            }
            // Se for a hotbar, tenta mover para o inventário principal
            else if (pIndex >= PLAYER_INVENTORY_START + 27) { // Hotbar (27-35)
                if (!moveItemStackTo(sourceStack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_START + 27, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Se for o inventário principal, tenta mover para a hotbar
            else { // Inventário (0-26)
                if (!moveItemStackTo(sourceStack, PLAYER_INVENTORY_START + 27, PLAYER_INVENTORY_END, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }
        // Clicou em um slot da MÁQUINA (36-50)
        else if (pIndex < UPGRADE_SLOTS_END) {
            // Tenta mover da máquina para o inventário do jogador
            if (!moveItemStackTo(sourceStack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
        }
        // Slot inválido
        else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }


    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        // Posição Y 99 (Slots 0-26)
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 99 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        // Posição Y 157 (Slots 27-35)
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 157));
        }
    }
}