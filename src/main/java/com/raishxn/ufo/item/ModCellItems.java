package com.raishxn.ufo.item;

import appeng.api.stacks.AEKeyType;
import appeng.items.materials.StorageComponentItem;
import appeng.items.storage.StorageTier;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.item.custom.cell.InfinityModCellItem;
import com.raishxn.ufo.item.custom.cell.ModCellItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCellItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(UfoMod.MOD_ID);

    // --- DEFINIÇÕES DAS NOVAS CÉLULAS ---

    // Housings
    public static final DeferredHolder<Item, Item> WHITE_DWARF_ITEM_CELL_HOUSING = ITEMS.register("white_dwarf_item_cell_housing", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NEUTRON_FLUID_CELL_HOUSING = ITEMS.register("neutron_fluid_cell_housing", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PULSAR_CELL_HOUSING = ITEMS.register("pulsar_cell_housing", () -> new Item(new Item.Properties()));

    // Storage Components - CORRIGIDO para DeferredHolder<Item, Item>
    public static final DeferredHolder<Item, Item> CELL_COMPONENT_40M = component("40m", 40 * 1024);
    public static final DeferredHolder<Item, Item> CELL_COMPONENT_100M = component("100m", 100 * 1024);
    public static final DeferredHolder<Item, Item> CELL_COMPONENT_250M = component("250m", 250 * 1024);
    public static final DeferredHolder<Item, Item> CELL_COMPONENT_750M = component("750m", 750 * 1024);
    public static final DeferredHolder<Item, Item> CELL_COMPONENT_INFINITY = component("infinity", Integer.MAX_VALUE);

    // Tiers - Agora recebem o tipo correto de Supplier
    public static final StorageTier TIER_40M = new StorageTier(11, "40m", 40_000_000, 5.5D, CELL_COMPONENT_40M);
    public static final StorageTier TIER_100M = new StorageTier(12, "100m", 100_000_000, 6.0D, CELL_COMPONENT_100M);
    public static final StorageTier TIER_250M = new StorageTier(13, "250m", 250_000_000, 6.5D, CELL_COMPONENT_250M);
    public static final StorageTier TIER_750M = new StorageTier(14, "750m", 750_000_000, 7.0D, CELL_COMPONENT_750M);
    public static final StorageTier TIER_INFINITY = new StorageTier(15, "infinity", Integer.MAX_VALUE, 7.5D, CELL_COMPONENT_INFINITY);

    // Item Cells: White Dwarf
    public static final DeferredHolder<Item, ModCellItem> ITEM_CELL_40M = ITEMS.register("white_dwarf_cell_echo", () -> new ModCellItem(TIER_40M, 1024, AEKeyType.items()));
    public static final DeferredHolder<Item, ModCellItem> ITEM_CELL_100M = ITEMS.register("white_dwarf_cell_beaco", () -> new ModCellItem(TIER_100M, 5 * 1024, AEKeyType.items()));
    public static final DeferredHolder<Item, ModCellItem> ITEM_CELL_250M = ITEMS.register("white_dwarf_cell_nexus", () -> new ModCellItem(TIER_250M, 10 * 1024, AEKeyType.items()));
    public static final DeferredHolder<Item, ModCellItem> ITEM_CELL_750M = ITEMS.register("white_dwarf_cell_core", () -> new ModCellItem(TIER_750M, 25 * 1024, AEKeyType.items()));
    public static final DeferredHolder<Item, InfinityModCellItem> ITEM_CELL_INFINITY = ITEMS.register("white_dwarf_cell_singularity", () -> new InfinityModCellItem(new Item.Properties().stacksTo(1), TIER_INFINITY.idleDrain()));
    // Fluid Cells: Neutron Star Reservoir
    public static final DeferredHolder<Item, ModCellItem> FLUID_CELL_40M = ITEMS.register("neutron_star_reservoir_echo", () -> new ModCellItem(TIER_40M, 1024, AEKeyType.fluids()));
    public static final DeferredHolder<Item, ModCellItem> FLUID_CELL_100M = ITEMS.register("neutron_star_reservoir_beaco", () -> new ModCellItem(TIER_100M, 5 * 1024, AEKeyType.fluids()));
    public static final DeferredHolder<Item, ModCellItem> FLUID_CELL_250M = ITEMS.register("neutron_star_reservoir_nexus", () -> new ModCellItem(TIER_250M, 10 * 1024, AEKeyType.fluids()));
    public static final DeferredHolder<Item, ModCellItem> FLUID_CELL_750M = ITEMS.register("neutron_star_reservoir_core", () -> new ModCellItem(TIER_750M, 25 * 1024, AEKeyType.fluids()));
    public static final DeferredHolder<Item, InfinityModCellItem> FLUID_CELL_INFINITY = ITEMS.register("neutron_star_reservoir_singularity", () -> new InfinityModCellItem(new Item.Properties().stacksTo(1), TIER_INFINITY.idleDrain()));
    // --- Helper Methods ---

    // CORRIGIDO para retornar DeferredHolder<Item, Item>
    private static DeferredHolder<Item, Item> component(String idSuffix, int kibiBytes) {
        String id = "storage_cell_side_" + idSuffix;
        return ITEMS.register(id, () -> new StorageComponentItem(new Item.Properties(), kibiBytes));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}