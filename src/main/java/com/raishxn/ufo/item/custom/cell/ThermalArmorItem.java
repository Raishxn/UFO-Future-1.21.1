package com.raishxn.ufo.item.custom;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

// Esta classe estende ArmorItem E implementa nossa interface
public class ThermalArmorItem extends ArmorItem implements IThermalArmor {

    // Construtor padrão para o ArmorItem
    public ThermalArmorItem(Holder<ArmorMaterial> material, ArmorItem.Type type, Item.Properties properties) {
        super(material, type, properties);
    }
}