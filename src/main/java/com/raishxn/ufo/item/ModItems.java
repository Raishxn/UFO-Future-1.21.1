package com.raishxn.ufo.item;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.core.definitions.AEBlocks;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.item.custom.*;
import com.raishxn.ufo.item.custom.InfinityCell;
import mekanism.common.registries.MekanismItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import appeng.api.ids.AEBlockIds;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import com.raishxn.ufo.item.custom.InfinityCell;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries; // <-- IMPORT CHAVE E CORRETO

 public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(UfoMod.MOD_ID);

    public static final DeferredItem<Item> BISMUTH = ITEMS.register("bismuth",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> DIMENSIONAL_PROCESSOR_PRESS = ITEMS.register("dimensional_processor_press",
            () -> new AnimatedNameItem(new Item.Properties(),
                    ChatFormatting.WHITE,
                    ChatFormatting.GRAY,
                    ChatFormatting.DARK_GRAY,
                    ChatFormatting.BLACK,
                    ChatFormatting.DARK_GRAY,
                    ChatFormatting.GRAY));
    public static final DeferredItem<Item> DIMENSIONAL_PROCESSOR = ITEMS.register("dimensional_processor",
            () -> new AnimatedNameItem(new Item.Properties(),
                    ChatFormatting.WHITE,
                    ChatFormatting.GRAY,
                    ChatFormatting.DARK_GRAY,
                    ChatFormatting.BLACK,
                    ChatFormatting.DARK_GRAY,
                    ChatFormatting.GRAY));
    public static final DeferredItem<Item> PHASE_SHIFT_COMPONENT_MATRIX = ITEMS.register("phase_shift_component_matrix",
            () -> new AnimatedNameItem(new Item.Properties(),
                    ChatFormatting.WHITE,
                    ChatFormatting.RED,
                    ChatFormatting.DARK_RED,
                    ChatFormatting.RED));
    public static final DeferredItem<Item> HYPER_DENSE_COMPONENT_MATRIX = ITEMS.register("hyper_dense_component_matrix",
            () -> new AnimatedNameItem(new Item.Properties(),
                    ChatFormatting.WHITE,
                    ChatFormatting.LIGHT_PURPLE,
                    ChatFormatting.DARK_PURPLE,
                    ChatFormatting.LIGHT_PURPLE));
    public static final DeferredItem<Item> TESSERACT_COMPONENT_MATRIX = ITEMS.register("tesseract_component_matrix",
            () -> new AnimatedNameItem(new Item.Properties(),
                    ChatFormatting.WHITE,
                    ChatFormatting.AQUA,
                    ChatFormatting.DARK_AQUA,
                    ChatFormatting.AQUA));
    public static final DeferredItem<Item> EVENT_HORIZON_COMPONENT_MATRIX = ITEMS.register("event_horizon_component_matrix",
            () -> new AnimatedNameItem(new Item.Properties(),
                    ChatFormatting.WHITE,
                    ChatFormatting.BLUE,
                    ChatFormatting.DARK_BLUE,
                    ChatFormatting.BLUE));
    public static final DeferredItem<Item> COSMIC_STRING_COMPONENT_MATRIX = ITEMS.register("cosmic_string_component_matrix",
            () -> new AnimatedNameItem(new Item.Properties(),
                    ChatFormatting.WHITE,
                    ChatFormatting.GREEN,
                    ChatFormatting.DARK_GREEN,
                    ChatFormatting.GREEN));
    public static final DeferredItem<Item> PRINTED_DIMENSIONAL_PROCESSOR = ITEMS.register("printed_dimensional_processor",
            () -> new AnimatedNameItem(new Item.Properties(),
                    ChatFormatting.WHITE,
                    ChatFormatting.GRAY,
                    ChatFormatting.DARK_GRAY,
                    ChatFormatting.BLACK,
                    ChatFormatting.DARK_GRAY,
                    ChatFormatting.GRAY));
    public static final DeferredItem<Item> WHITE_DWARF_FRAGMENT_INGOT = ITEMS.register("white_dwarf_fragment_ingot",
            () -> new AnimatedNameItem(new Item.Properties(),
                    ChatFormatting.WHITE,
                    ChatFormatting.GRAY,
                    ChatFormatting.DARK_GRAY,
                    ChatFormatting.GRAY));

     public static final DeferredItem<Item> INFINITY_WATER_CELL = ITEMS.register("infinity_water_cell",
             () -> new InfinityCell(() -> AEFluidKey.of(Fluids.WATER),
                     ChatFormatting.WHITE, ChatFormatting.AQUA, ChatFormatting.BLUE, ChatFormatting.AQUA));

     public static final DeferredItem<Item> INFINITY_COBBLESTONE_CELL = ITEMS.register("infinity_cobblestone_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.COBBLESTONE),
                     ChatFormatting.WHITE, ChatFormatting.GRAY, ChatFormatting.DARK_GRAY, ChatFormatting.GRAY));

     public static final DeferredItem<Item> INFINITY_COBBLED_DEEPSLATE_CELL = ITEMS.register("infinity_cobbled_deepslate_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.COBBLED_DEEPSLATE),
                     ChatFormatting.GRAY, ChatFormatting.DARK_GRAY, ChatFormatting.GRAY, ChatFormatting.DARK_GRAY));

     public static final DeferredItem<Item> INFINITY_END_STONE_CELL = ITEMS.register("infinity_end_stone_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.END_STONE),
                     ChatFormatting.WHITE, ChatFormatting.YELLOW, ChatFormatting.GOLD, ChatFormatting.YELLOW));

     public static final DeferredItem<Item> INFINITY_LAVA_CELL = ITEMS.register("infinity_lava_cell",
             () -> new InfinityCell(() -> AEFluidKey.of(Fluids.LAVA),
                     ChatFormatting.WHITE, ChatFormatting.YELLOW, ChatFormatting.GOLD, ChatFormatting.RED));

     public static final DeferredItem<Item> INFINITY_NETHERRACK_CELL = ITEMS.register("infinity_netherrack_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.NETHERRACK),
                     ChatFormatting.RED, ChatFormatting.DARK_RED, ChatFormatting.RED));

     public static final DeferredItem<Item> INFINITY_SAND_CELL = ITEMS.register("infinity_sand_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.SAND),
                     ChatFormatting.YELLOW, ChatFormatting.WHITE, ChatFormatting.GOLD, ChatFormatting.WHITE));

     public static final DeferredItem<Item> INFINITY_SKY_STONE_CELL = ITEMS.register("infinity_sky_stone_cell",
             () -> new InfinityCell(() -> AEItemKey.of(BuiltInRegistries.BLOCK.get(AEBlockIds.SKY_STONE_BLOCK)),
                     ChatFormatting.DARK_GRAY, ChatFormatting.GRAY, ChatFormatting.BLACK, ChatFormatting.GRAY));

     public static final DeferredItem<Item> INFINITY_ANTIMATTER_PELLET_CELL = ITEMS.register("infinity_antimatter_pellet_cell",
             () -> new InfinityCell(() -> AEItemKey.of(MekanismItems.ANTIMATTER_PELLET.get()),
                     ChatFormatting.WHITE, ChatFormatting.LIGHT_PURPLE, ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE));


    public static final DeferredItem<Item> UFO_STAFF = ITEMS.register("ufo_staff",
            () -> new UfoStaffItem(new Item.Properties().component(ModDataComponents.TOOL_MODE_INDEX.get(), 0)));

    public static final DeferredItem<SwordItem> UFO_SWORD = ITEMS.register("ufo_sword",
            () -> new UfoEnergySwordItem(ModToolTiers.UFO, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModToolTiers.UFO, 5, -2.4f))
                    .component(ModDataComponents.TOOL_MODE_INDEX.get(), 0)));


    public static final DeferredItem<PickaxeItem> UFO_PICKAXE = ITEMS.register("ufo_pickaxe",
            () -> new UfoEnergyPickaxeItem(ModToolTiers.UFO, new Item.Properties()
                    .attributes(PickaxeItem.createAttributes(ModToolTiers.UFO, 1.0F, -2.8f))
                    .component(ModDataComponents.TOOL_MODE_INDEX.get(), 0)
                    .component(ModDataComponents.FAST_MODE.get(), false) // Valor padrão: false
            ));

    public static final DeferredItem<ShovelItem> UFO_SHOVEL = ITEMS.register("ufo_shovel",
            () -> new UfoEnergyShovelItem(ModToolTiers.UFO, new Item.Properties()
                    .attributes(ShovelItem.createAttributes(ModToolTiers.UFO, 1.5F, -3.0f))
                    .component(ModDataComponents.TOOL_MODE_INDEX.get(), 0)));

    public static final DeferredItem<AxeItem> UFO_AXE = ITEMS.register("ufo_axe",
            () -> new UfoEnergyAxeItem(ModToolTiers.UFO, new Item.Properties()
                    .attributes(AxeItem.createAttributes(ModToolTiers.UFO, 6.0F, -3.2f))
                    .component(ModDataComponents.TOOL_MODE_INDEX.get(), 0)));

    public static final DeferredItem<HoeItem> UFO_HOE = ITEMS.register("ufo_hoe",
            () -> new UfoEnergyHoeItem(ModToolTiers.UFO, new Item.Properties()
                    .attributes(HoeItem.createAttributes(ModToolTiers.UFO, 0F, -3.0f))
                    .component(ModDataComponents.TOOL_MODE_INDEX.get(), 0)
                    .component(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag()))
            ));

    public static final DeferredItem<FishingRodItem> UFO_FISHING_ROD = ITEMS.register("ufo_fishing_rod",
            () -> new UfoEnergyFishingRodItem(new Item.Properties().durability(500)
                    .component(ModDataComponents.TOOL_MODE_INDEX.get(), 0)));

    public static final DeferredItem<SwordItem> UFO_GREATSWORD = ITEMS.register("ufo_greatsword",
            () -> new UfoEnergyGreatswordItem(ModToolTiers.UFO, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModToolTiers.UFO, 8, -3.0f)) // Corrigido para usar atributos de espada
                    .component(ModDataComponents.TOOL_MODE_INDEX.get(), 0)));

    public static final DeferredItem<HammerItem> UFO_HAMMER = ITEMS.register("ufo_hammer",
            () -> new HammerItem(ModToolTiers.UFO, new Item.Properties()
                    .attributes(AxeItem.createAttributes(ModToolTiers.UFO, 7.0F, -3.4f)) // Atributos sugeridos para um martelo
                    .component(ModDataComponents.TOOL_MODE_INDEX.get(), 0)));

    public static final DeferredItem<BowItem> UFO_BOW = ITEMS.register("ufo_bow",
            () -> new UfoEnergyBowItem(new Item.Properties().durability(5000)
                    .component(ModDataComponents.TOOL_MODE_INDEX.get(), 0)
                    .component(ModDataComponents.BOW_FAST_MODE.get(), false) // Valor padrão: false
            ));
    public static final DeferredItem<Item> UFO_HELMET = ITEMS.register("ufo_helmet",
            () -> new UfoArmorItem(ModArmorMaterials.UFO, ArmorItem.Type.HELMET, new Item.Properties()
                    .component(ModDataComponents.ENERGY.get(), 0))); // << ADICIONE ESTA LINHA
    public static final DeferredItem<Item> UFO_CHESTPLATE = ITEMS.register("ufo_chestplate",
            () -> new UfoArmorItem(ModArmorMaterials.UFO, ArmorItem.Type.CHESTPLATE, new Item.Properties()
                    .component(ModDataComponents.ENERGY.get(), 0))); // << ADICIONE ESTA LINHA
    public static final DeferredItem<Item> UFO_LEGGINGS = ITEMS.register("ufo_leggings",
            () -> new UfoArmorItem(ModArmorMaterials.UFO, ArmorItem.Type.LEGGINGS, new Item.Properties()
                    .component(ModDataComponents.ENERGY.get(), 0))); // << ADICIONE ESTA LINHA
    public static final DeferredItem<Item> UFO_BOOTS = ITEMS.register("ufo_boots",
            () -> new UfoArmorItem(ModArmorMaterials.UFO, ArmorItem.Type.BOOTS, new Item.Properties()
                    .component(ModDataComponents.ENERGY.get(), 0)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}