package com.raishxn.ufo.item;

import appeng.api.client.StorageCellModels;
import appeng.api.storage.StorageCells;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.item.custom.cell.AEBigIntegerCellHandler;
import com.raishxn.ufo.item.custom.cell.InfinityCellInventory;

public class UFORegistryHandler {

    public static final UFORegistryHandler INSTANCE = new UFORegistryHandler();
    public void onInit() {
        this.registerStorageHandler();
        this.registerUpgrades();
    }

    private void registerUpgrades() {
        // Use the block's asItem() to ensure the same Item instance as UpgradeInventories.forMachine() in the BE constructor
        net.minecraft.world.item.Item machineItem = com.raishxn.ufo.block.ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER_BLOCK.get().asItem();
        
        appeng.api.upgrades.Upgrades.add(ModItems.MATTERFLOW_CATALYST_T1.get(), machineItem, 4);
        appeng.api.upgrades.Upgrades.add(ModItems.MATTERFLOW_CATALYST_T2.get(), machineItem, 4);
        appeng.api.upgrades.Upgrades.add(ModItems.MATTERFLOW_CATALYST_T3.get(), machineItem, 4);
        appeng.api.upgrades.Upgrades.add(ModItems.CHRONO_CATALYST_T1.get(), machineItem, 4);
        appeng.api.upgrades.Upgrades.add(ModItems.CHRONO_CATALYST_T2.get(), machineItem, 4);
        appeng.api.upgrades.Upgrades.add(ModItems.CHRONO_CATALYST_T3.get(), machineItem, 4);
        appeng.api.upgrades.Upgrades.add(ModItems.OVERFLUX_CATALYST_T1.get(), machineItem, 4);
        appeng.api.upgrades.Upgrades.add(ModItems.OVERFLUX_CATALYST_T2.get(), machineItem, 4);
        appeng.api.upgrades.Upgrades.add(ModItems.OVERFLUX_CATALYST_T3.get(), machineItem, 4);
        appeng.api.upgrades.Upgrades.add(ModItems.QUANTUM_CATALYST_T1.get(), machineItem, 4);
        appeng.api.upgrades.Upgrades.add(ModItems.QUANTUM_CATALYST_T2.get(), machineItem, 4);
        appeng.api.upgrades.Upgrades.add(ModItems.QUANTUM_CATALYST_T3.get(), machineItem, 4);
        appeng.api.upgrades.Upgrades.add(ModItems.DIMENSIONAL_CATALYST.get(), machineItem, 4);
    }

    private void registerStorageHandler() {
        StorageCells.addCellHandler(InfinityCellInventory.HANDLER);
        StorageCells.addCellHandler(AEBigIntegerCellHandler.INSTANCE);
        StorageCellModels.registerModel(ModItems.INFINITY_WATER_CELL.get(), UfoMod.id("item/infinity_water_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_COBBLESTONE_CELL.get(), UfoMod.id("item/infinity_cobblestone_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_COBBLED_DEEPSLATE_CELL.get(), UfoMod.id("item/infinity_cobbled_deepslate_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_END_STONE_CELL.get(), UfoMod.id("item/infinity_end_stone_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_NETHERRACK_CELL.get(), UfoMod.id("item/infinity_netherrack_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_LAVA_CELL.get(), UfoMod.id("item/infinity_lava_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_SAND_CELL.get(), UfoMod.id("item/infinity_sand_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_SKY_STONE_CELL.get(), UfoMod.id("item/infinity_sky_stone_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_ANTIMATTER_PELLET_CELL.get(), UfoMod.id("item/infinity_antimatter_pellet_cell"));

        // Additional infinity cells (Mekanism)
        StorageCellModels.registerModel(ModItems.INFINITY_PLUTONIUM_PELLET_CELL.get(), UfoMod.id("item/infinity_plutonium_pellet_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_POLONIUM_PELLET_CELL.get(), UfoMod.id("item/infinity_polonium_pellet_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_HDPE_PELLET_CELL.get(), UfoMod.id("item/infinity_hdpe_pellet_cell"));

        // Additional infinity cells (Vanilla)
        StorageCellModels.registerModel(ModItems.INFINITY_OBSIDIAN_CELL.get(), UfoMod.id("item/infinity_obsidian_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_GRAVEL_CELL.get(), UfoMod.id("item/infinity_gravel_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_OAK_LOG_CELL.get(), UfoMod.id("item/infinity_oak_log_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_GLASS_CELL.get(), UfoMod.id("item/infinity_glass_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_AMETHYST_SHARD_CELL.get(), UfoMod.id("item/infinity_amethyst_shard_cell"));

        // Dye cells
        StorageCellModels.registerModel(ModItems.INFINITY_WHITE_DYE_CELL.get(), UfoMod.id("item/infinity_white_dye_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_ORANGE_DYE_CELL.get(), UfoMod.id("item/infinity_orange_dye_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_MAGENTA_DYE_CELL.get(), UfoMod.id("item/infinity_magenta_dye_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_LIGHT_BLUE_DYE_CELL.get(), UfoMod.id("item/infinity_light_blue_dye_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_YELLOW_DYE_CELL.get(), UfoMod.id("item/infinity_yellow_dye_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_LIME_DYE_CELL.get(), UfoMod.id("item/infinity_lime_dye_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_PINK_DYE_CELL.get(), UfoMod.id("item/infinity_pink_dye_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_GRAY_DYE_CELL.get(), UfoMod.id("item/infinity_gray_dye_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_LIGHT_GRAY_DYE_CELL.get(), UfoMod.id("item/infinity_light_gray_dye_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_CYAN_DYE_CELL.get(), UfoMod.id("item/infinity_cyan_dye_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_PURPLE_DYE_CELL.get(), UfoMod.id("item/infinity_purple_dye_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_BLUE_DYE_CELL.get(), UfoMod.id("item/infinity_blue_dye_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_BROWN_DYE_CELL.get(), UfoMod.id("item/infinity_brown_dye_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_GREEN_DYE_CELL.get(), UfoMod.id("item/infinity_green_dye_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_RED_DYE_CELL.get(), UfoMod.id("item/infinity_red_dye_cell"));
        StorageCellModels.registerModel(ModItems.INFINITY_BLACK_DYE_CELL.get(), UfoMod.id("item/infinity_black_dye_cell"));

        // --- BigInteger Cells (White Dwarf + Neutron Star) ---
        // These use the 3-layer cellModel from ModItemModelProvider (housing + component + LED)
        StorageCellModels.registerModel(ModCellItems.ITEM_CELL_40M.get(), UfoMod.id("item/" + ModCellItems.ITEM_CELL_40M.getId().getPath()));
        StorageCellModels.registerModel(ModCellItems.ITEM_CELL_100M.get(), UfoMod.id("item/" + ModCellItems.ITEM_CELL_100M.getId().getPath()));
        StorageCellModels.registerModel(ModCellItems.ITEM_CELL_250M.get(), UfoMod.id("item/" + ModCellItems.ITEM_CELL_250M.getId().getPath()));
        StorageCellModels.registerModel(ModCellItems.ITEM_CELL_750M.get(), UfoMod.id("item/" + ModCellItems.ITEM_CELL_750M.getId().getPath()));
        StorageCellModels.registerModel(ModCellItems.ITEM_CELL_SINGULARITY.get(), UfoMod.id("item/" + ModCellItems.ITEM_CELL_SINGULARITY.getId().getPath()));
        StorageCellModels.registerModel(ModCellItems.FLUID_CELL_40M.get(), UfoMod.id("item/" + ModCellItems.FLUID_CELL_40M.getId().getPath()));
        StorageCellModels.registerModel(ModCellItems.FLUID_CELL_100M.get(), UfoMod.id("item/" + ModCellItems.FLUID_CELL_100M.getId().getPath()));
        StorageCellModels.registerModel(ModCellItems.FLUID_CELL_250M.get(), UfoMod.id("item/" + ModCellItems.FLUID_CELL_250M.getId().getPath()));
        StorageCellModels.registerModel(ModCellItems.FLUID_CELL_750M.get(), UfoMod.id("item/" + ModCellItems.FLUID_CELL_750M.getId().getPath()));
        StorageCellModels.registerModel(ModCellItems.FLUID_CELL_SINGULARITY.get(), UfoMod.id("item/" + ModCellItems.FLUID_CELL_SINGULARITY.getId().getPath()));
    }
}