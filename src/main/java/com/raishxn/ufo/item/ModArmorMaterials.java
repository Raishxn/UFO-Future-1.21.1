package com.raishxn.ufo.item;

import com.raishxn.ufo.UfoMod;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class ModArmorMaterials {

    public static final Holder<ArmorMaterial> UFO = register("ufo",
            new EnumMap<>(java.util.Map.of(
                    ArmorItem.Type.BOOTS, 5,
                    ArmorItem.Type.LEGGINGS, 8,
                    ArmorItem.Type.CHESTPLATE, 10,
                    ArmorItem.Type.HELMET, 5,
                    ArmorItem.Type.BODY, 10
            )),
            25,
            // --- CORREÇÃO APLICADA AQUI ---
            // Passamos o Holder diretamente, sem o .get()
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            4.0f,
            0.2f,
            () -> ModItems.BISMUTH.get()
    );

    private static Holder<ArmorMaterial> register(String name, EnumMap<ArmorItem.Type, Integer> defense,
                                                  int enchantability, Holder<SoundEvent> equipSound, float toughness,
                                                  float knockbackResistance, Supplier<Item> repairIngredient) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, name);
        // A textura que a armadura usará no corpo do jogador
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(location));
        Supplier<Ingredient> ingredient = () -> Ingredient.of(repairIngredient.get());

        ArmorMaterial material = new ArmorMaterial(defense, enchantability, equipSound, ingredient, layers, toughness, knockbackResistance);
        return Registry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL, location, material);
    }
}