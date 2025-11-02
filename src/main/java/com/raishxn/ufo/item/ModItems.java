package com.raishxn.ufo.item;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.core.definitions.AEBlocks;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.item.custom.*;
import com.raishxn.ufo.item.custom.InfinityCell;
import com.raishxn.ufo.util.ColorHelper;
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
     public static final DeferredItem<Item> NEUTRON_STAR_FRAGMENT_INGOT = ITEMS.register("neutron_star_fragment_ingot",
             () -> new AnimatedNameItem(new Item.Properties(),
                     ChatFormatting.WHITE,
                     ChatFormatting.BLUE,
                     ChatFormatting.DARK_BLUE,
                     ChatFormatting.AQUA));
     public static final DeferredItem<Item> PULSAR_FRAGMENT_INGOT = ITEMS.register("pulsar_fragment_ingot",
             () -> new AnimatedNameItem(new Item.Properties(),
                     ChatFormatting.WHITE,
                     ChatFormatting.GREEN,
                     ChatFormatting.DARK_GREEN));

    // infinite items cells

     // --- CÉLULAS INFINITAS EXISTENTES ---
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

     // --- NOVAS CÉLULAS INFINITAS (Mekanism) ---
     public static final DeferredItem<Item> INFINITY_PLUTONIUM_PELLET_CELL = ITEMS.register("infinity_plutonium_pellet_cell",
             () -> new InfinityCell(() -> AEItemKey.of(MekanismItems.PLUTONIUM_PELLET.get()),
                     ChatFormatting.GREEN, ChatFormatting.DARK_GREEN, ChatFormatting.GREEN));
     public static final DeferredItem<Item> INFINITY_POLONIUM_PELLET_CELL = ITEMS.register("infinity_polonium_pellet_cell",
             () -> new InfinityCell(() -> AEItemKey.of(MekanismItems.POLONIUM_PELLET.get()),
                     ChatFormatting.AQUA, ChatFormatting.DARK_AQUA, ChatFormatting.AQUA));
     public static final DeferredItem<Item> INFINITY_HDPE_PELLET_CELL = ITEMS.register("infinity_hdpe_pellet_cell",
             () -> new InfinityCell(() -> AEItemKey.of(MekanismItems.HDPE_PELLET.get()),
                     ChatFormatting.WHITE, ChatFormatting.GRAY, ChatFormatting.WHITE));

     // --- NOVAS CÉLULAS INFINITAS (Minecraft Vanilla) ---
     public static final DeferredItem<Item> INFINITY_OBSIDIAN_CELL = ITEMS.register("infinity_obsidian_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.OBSIDIAN),
                     ChatFormatting.DARK_PURPLE, ChatFormatting.BLACK, ChatFormatting.DARK_PURPLE));
     public static final DeferredItem<Item> INFINITY_GRAVEL_CELL = ITEMS.register("infinity_gravel_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.GRAVEL),
                     ChatFormatting.GRAY, ChatFormatting.DARK_GRAY, ChatFormatting.GRAY));
     public static final DeferredItem<Item> INFINITY_OAK_LOG_CELL = ITEMS.register("infinity_oak_log_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.OAK_LOG),
                     ChatFormatting.GOLD, ChatFormatting.DARK_RED, ChatFormatting.GOLD)); // Usando DARK_RED para simular marrom
     public static final DeferredItem<Item> INFINITY_GLASS_CELL = ITEMS.register("infinity_glass_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.GLASS),
                     ChatFormatting.WHITE, ChatFormatting.AQUA, ChatFormatting.WHITE));
     public static final DeferredItem<Item> INFINITY_AMETHYST_SHARD_CELL = ITEMS.register("infinity_amethyst_shard_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.AMETHYST_SHARD),
                     ChatFormatting.LIGHT_PURPLE, ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE));

     // --- NOVAS CÉLULAS INFINITAS (Corantes) ---
     public static final DeferredItem<Item> INFINITY_WHITE_DYE_CELL = ITEMS.register("infinity_white_dye_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.WHITE_DYE), ChatFormatting.WHITE, ChatFormatting.GRAY));
     public static final DeferredItem<Item> INFINITY_ORANGE_DYE_CELL = ITEMS.register("infinity_orange_dye_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.ORANGE_DYE), ChatFormatting.GOLD, ChatFormatting.RED));
     public static final DeferredItem<Item> INFINITY_MAGENTA_DYE_CELL = ITEMS.register("infinity_magenta_dye_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.MAGENTA_DYE), ChatFormatting.LIGHT_PURPLE, ChatFormatting.RED));
     public static final DeferredItem<Item> INFINITY_LIGHT_BLUE_DYE_CELL = ITEMS.register("infinity_light_blue_dye_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.LIGHT_BLUE_DYE), ChatFormatting.AQUA, ChatFormatting.WHITE));
     public static final DeferredItem<Item> INFINITY_YELLOW_DYE_CELL = ITEMS.register("infinity_yellow_dye_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.YELLOW_DYE), ChatFormatting.YELLOW, ChatFormatting.WHITE));
     public static final DeferredItem<Item> INFINITY_LIME_DYE_CELL = ITEMS.register("infinity_lime_dye_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.LIME_DYE), ChatFormatting.GREEN, ChatFormatting.YELLOW));
     public static final DeferredItem<Item> INFINITY_PINK_DYE_CELL = ITEMS.register("infinity_pink_dye_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.PINK_DYE), ChatFormatting.RED, ChatFormatting.WHITE));
     public static final DeferredItem<Item> INFINITY_GRAY_DYE_CELL = ITEMS.register("infinity_gray_dye_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.GRAY_DYE), ChatFormatting.GRAY, ChatFormatting.DARK_GRAY));
     public static final DeferredItem<Item> INFINITY_LIGHT_GRAY_DYE_CELL = ITEMS.register("infinity_light_gray_dye_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.LIGHT_GRAY_DYE), ChatFormatting.GRAY, ChatFormatting.WHITE));
     public static final DeferredItem<Item> INFINITY_CYAN_DYE_CELL = ITEMS.register("infinity_cyan_dye_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.CYAN_DYE), ChatFormatting.DARK_AQUA, ChatFormatting.AQUA));
     public static final DeferredItem<Item> INFINITY_PURPLE_DYE_CELL = ITEMS.register("infinity_purple_dye_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.PURPLE_DYE), ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE));
     public static final DeferredItem<Item> INFINITY_BLUE_DYE_CELL = ITEMS.register("infinity_blue_dye_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.BLUE_DYE), ChatFormatting.DARK_BLUE, ChatFormatting.BLUE));
     public static final DeferredItem<Item> INFINITY_BROWN_DYE_CELL = ITEMS.register("infinity_brown_dye_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.BROWN_DYE), ChatFormatting.DARK_RED, ChatFormatting.GOLD));
     public static final DeferredItem<Item> INFINITY_GREEN_DYE_CELL = ITEMS.register("infinity_green_dye_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.GREEN_DYE), ChatFormatting.DARK_GREEN, ChatFormatting.GREEN));
     public static final DeferredItem<Item> INFINITY_RED_DYE_CELL = ITEMS.register("infinity_red_dye_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.RED_DYE), ChatFormatting.DARK_RED, ChatFormatting.RED));
     public static final DeferredItem<Item> INFINITY_BLACK_DYE_CELL = ITEMS.register("infinity_black_dye_cell",
             () -> new InfinityCell(() -> AEItemKey.of(Items.BLACK_DYE), ChatFormatting.BLACK, ChatFormatting.DARK_GRAY));



     public static final DeferredItem<Item> UFO_STAFF = ITEMS.register("ufo_staff",
            () -> new UfoStaffItem(new Item.Properties().component(ModDataComponents.TOOL_MODE_INDEX.get(), 0).stacksTo(1)));

    public static final DeferredItem<SwordItem> UFO_SWORD = ITEMS.register("ufo_sword",
            () -> new UfoEnergySwordItem(ModToolTiers.UFO, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModToolTiers.UFO, 5, -2.4f))
                    .component(ModDataComponents.TOOL_MODE_INDEX.get(), 0).stacksTo(1)));

    public static final DeferredItem<PickaxeItem> UFO_PICKAXE = ITEMS.register("ufo_pickaxe",
            () -> new UfoEnergyPickaxeItem(ModToolTiers.UFO, new Item.Properties()
                    .attributes(PickaxeItem.createAttributes(ModToolTiers.UFO, 1.0F, -2.8f))
                    .component(ModDataComponents.TOOL_MODE_INDEX.get(), 0)
                    .component(ModDataComponents.FAST_MODE.get(), false).stacksTo(1) // Valor padrão: false
            ));

    public static final DeferredItem<ShovelItem> UFO_SHOVEL = ITEMS.register("ufo_shovel",
            () -> new UfoEnergyShovelItem(ModToolTiers.UFO, new Item.Properties()
                    .attributes(ShovelItem.createAttributes(ModToolTiers.UFO, 1.5F, -3.0f))
                    .component(ModDataComponents.TOOL_MODE_INDEX.get(), 0).stacksTo(1)));

    public static final DeferredItem<AxeItem> UFO_AXE = ITEMS.register("ufo_axe",
            () -> new UfoEnergyAxeItem(ModToolTiers.UFO, new Item.Properties()
                    .attributes(AxeItem.createAttributes(ModToolTiers.UFO, 6.0F, -3.2f))
                    .component(ModDataComponents.TOOL_MODE_INDEX.get(), 0).stacksTo(1)));

    public static final DeferredItem<HoeItem> UFO_HOE = ITEMS.register("ufo_hoe",
            () -> new UfoEnergyHoeItem(ModToolTiers.UFO, new Item.Properties()
                    .attributes(HoeItem.createAttributes(ModToolTiers.UFO, 0F, -3.0f))
                    .component(ModDataComponents.TOOL_MODE_INDEX.get(), 0)
                    .component(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag())).stacksTo(1)
            ));

    public static final DeferredItem<FishingRodItem> UFO_FISHING_ROD = ITEMS.register("ufo_fishing_rod",
            () -> new UfoEnergyFishingRodItem(new Item.Properties().durability(500)
                    .component(ModDataComponents.TOOL_MODE_INDEX.get(), 0).stacksTo(1)));

    public static final DeferredItem<SwordItem> UFO_GREATSWORD = ITEMS.register("ufo_greatsword",
            () -> new UfoEnergyGreatswordItem(ModToolTiers.UFO, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModToolTiers.UFO, 8, -3.0f)) // Corrigido para usar atributos de espada
                    .component(ModDataComponents.TOOL_MODE_INDEX.get(), 0).stacksTo(1)));

    public static final DeferredItem<HammerItem> UFO_HAMMER = ITEMS.register("ufo_hammer",
            () -> new HammerItem(ModToolTiers.UFO, new Item.Properties()
                    .attributes(AxeItem.createAttributes(ModToolTiers.UFO, 7.0F, -3.4f)) // Atributos sugeridos para um martelo
                    .component(ModDataComponents.TOOL_MODE_INDEX.get(), 0).stacksTo(1)));

    public static final DeferredItem<BowItem> UFO_BOW = ITEMS.register("ufo_bow",
            () -> new UfoEnergyBowItem(new Item.Properties().durability(5000)
                    .component(ModDataComponents.TOOL_MODE_INDEX.get(), 0)
                    .component(ModDataComponents.BOW_FAST_MODE.get(), false).stacksTo(1) // Valor padrão: false
            ));
     public static final DeferredItem<Item> UFO_HELMET = ITEMS.register("ufo_helmet",
             () -> new UfoArmorItem(ModArmorMaterials.UFO, ArmorItem.Type.HELMET, new Item.Properties()
                     .component(ModDataComponents.ENERGY.get(), 0).stacksTo(1))); // Adicione .stacksTo(1)

     public static final DeferredItem<Item> UFO_CHESTPLATE = ITEMS.register("ufo_chestplate",
             () -> new UfoArmorItem(ModArmorMaterials.UFO, ArmorItem.Type.CHESTPLATE, new Item.Properties()
                     .component(ModDataComponents.ENERGY.get(), 0).stacksTo(1))); // Adicione .stacksTo(1)

     public static final DeferredItem<Item> UFO_LEGGINGS = ITEMS.register("ufo_leggings",
             () -> new UfoArmorItem(ModArmorMaterials.UFO, ArmorItem.Type.LEGGINGS, new Item.Properties()
                     .component(ModDataComponents.ENERGY.get(), 0).stacksTo(1))); // Adicione .stacksTo(1)

     public static final DeferredItem<Item> UFO_BOOTS = ITEMS.register("ufo_boots",
             () -> new UfoArmorItem(ModArmorMaterials.UFO, ArmorItem.Type.BOOTS, new Item.Properties()
                     .component(ModDataComponents.ENERGY.get(), 0).stacksTo(1)));


     public static final DeferredItem<Item> ENTROPY_ASSEMBLER_CORE_CASING = ITEMS.register("entropy_assembler_core_casing",
             () -> new AnimatedNameBlockItem(MultiblockBlocks.ENTROPY_ASSEMBLER_CORE_CASING.get(), new Item.Properties(),
                     ChatFormatting.BLUE, ChatFormatting.DARK_BLUE, ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE, ChatFormatting.RED));

     // entropy_singularity_casing: variacoes de cinza e preto
     public static final DeferredItem<Item> ENTROPY_SINGULARITY_CASING = ITEMS.register("entropy_singularity_casing",
             () -> new AnimatedNameBlockItem(MultiblockBlocks.ENTROPY_SINGULARITY_CASING.get(), new Item.Properties(),
                     ChatFormatting.WHITE, ChatFormatting.GRAY, ChatFormatting.DARK_GRAY, ChatFormatting.BLACK, ChatFormatting.DARK_GRAY, ChatFormatting.GRAY));

     // components: cores rainbow
     private static final ChatFormatting[] RAINBOW_COLORS = {
             ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.YELLOW, ChatFormatting.GREEN,
             ChatFormatting.AQUA, ChatFormatting.BLUE, ChatFormatting.LIGHT_PURPLE
     };
     public static final DeferredItem<Item> ENTROPY_CONTAINMENT_CHAMBER_COMPONENTS = ITEMS.register("entropy_containment_chamber_components",
             () -> new AnimatedNameBlockItem(MultiblockBlocks.ENTROPY_CONTAINMENT_CHAMBER_COMPONENTS.get(), new Item.Properties(), RAINBOW_COLORS));

     public static final DeferredItem<Item> ENTROPY_COOLANT_MATRIX_COMPONENTS = ITEMS.register("entropy_coolant_matrix_components",
             () -> new AnimatedNameBlockItem(MultiblockBlocks.ENTROPY_COOLANT_MATRIX_COMPONENTS.get(), new Item.Properties(), RAINBOW_COLORS));

     public static final DeferredItem<Item> ENTROPY_CATALYST_BANK_COMPONENTS = ITEMS.register("entropy_catalyst_bank_components",
             () -> new AnimatedNameBlockItem(MultiblockBlocks.ENTROPY_CATALYST_BANK_COMPONENTS.get(), new Item.Properties(), RAINBOW_COLORS));

     // entropy_computer_condensation_matrix: variacoes de vermelho,ciano e rosa
     public static final DeferredItem<Item> ENTROPY_COMPUTER_CONDENSATION_MATRIX = ITEMS.register("entropy_computer_condensation_matrix",
             () -> new AnimatedNameBlockItem(MultiblockBlocks.ENTROPY_COMPUTER_CONDENSATION_MATRIX.get(), new Item.Properties(),
                     ChatFormatting.RED, ChatFormatting.DARK_RED, ChatFormatting.AQUA, ChatFormatting.DARK_AQUA, ChatFormatting.LIGHT_PURPLE));

     // controller: variacoes de azul,roxo e rosa
     public static final DeferredItem<Item> ENTROPY_SINGULARITY_ARRAY_CONTROLLER = ITEMS.register("entropy_singularity_array_controller",
             () -> new AnimatedNameBlockItem(MultiblockBlocks.ENTROPY_SINGULARITY_ARRAY_CONTROLLER.get(), new Item.Properties(),
                     ChatFormatting.BLUE, ChatFormatting.DARK_BLUE, ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE, ChatFormatting.RED));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}