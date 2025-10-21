package com.raishxn.ufo.item;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.item.custom.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(UfoMod.MOD_ID);

    public static final DeferredItem<Item> BISMUTH = ITEMS.         register("bismuth",
            () -> new Item(new Item.Properties()));

    // --- CORREÇÃO APLICADA A TODOS OS ITENS ABAIXO ---

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