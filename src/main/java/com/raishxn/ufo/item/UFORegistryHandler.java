// Em UFORegistryHandler.java
package com.raishxn.ufo.item;

import appeng.api.client.StorageCellModels;
import appeng.api.storage.StorageCells;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.item.custom.cell.AEBigIntegerCellHandler;
import com.raishxn.ufo.item.custom.cell.InfinityCellInventory;

public class UFORegistryHandler { // Não precisa mais herdar de RegistryHandler

    public static final UFORegistryHandler INSTANCE = new UFORegistryHandler();

    // Este método é chamado durante o commonSetup
    public void onInit() {
        this.registerStorageHandler();
    }

    private void registerStorageHandler() {
        StorageCells.addCellHandler(InfinityCellInventory.HANDLER);
        StorageCells.addCellHandler(AEBigIntegerCellHandler.INSTANCE);
        // Agora referenciamos os itens de ModItems
        StorageCellModels.registerModel(ModItems.INFINITY_WATER_CELL.get(), UfoMod.id("item/infinity_water_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_COBBLESTONE_CELL.get(), UfoMod.id("item/infinity_cobblestone_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_COBBLED_DEEPSLATE_CELL.get(), UfoMod.id("item/infinity_cobbled_deepslate_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_END_STONE_CELL.get(), UfoMod.id("item/infinity_end_stone_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_NETHERRACK_CELL.get(), UfoMod.id("item/infinity_netherrack_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_LAVA_CELL.get(), UfoMod.id("item/infinity_lava_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_SAND_CELL.get(), UfoMod.id("item/infinity_sand_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_SKY_STONE_CELL.get(), UfoMod.id("item/infinity_sky_stone_cell"));
    }
}