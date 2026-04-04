package com.raishxn.ufo.item;

import com.raishxn.ufo.UfoMod;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry; // <-- IMPORTAR REGISTRY
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries; // <-- IMPORTAR REGISTRIES
import net.minecraft.resources.ResourceKey; // <-- IMPORTAR RESOURCEKEY
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class ModArmorMaterials {
    // 1. MATERIAL PARA A THERMAL RESISTOR EXOSUIT
    public static final Holder<ArmorMaterial> THERMAL_EXOSUIT = register(
            "thermal_exosuit", // Nome do material
            new EnumMap<>(ArmorItem.Type.class) {{ // Proteção por peça
                put(ArmorItem.Type.BOOTS, 3);
                put(ArmorItem.Type.LEGGINGS, 6);
                put(ArmorItem.Type.CHESTPLATE, 8);
                put(ArmorItem.Type.HELMET, 3);
            }},
            37, // Multiplicador de Durabilidade (ex: 37, como Netherite)
            SoundEvents.ARMOR_EQUIP_IRON, // Som ao equipar
            Ingredient.of(Tags.Items.INGOTS_IRON), // Ingrediente de reparo (Ex: barra de ferro)
            0.0f, // Resistência a Knockback
            0.0f  // Tenacidade (Toughness)
    ); // <-- Encantabilidade (15) foi REMOVIDA daqui

    // 2. MATERIAL PARA A UFO ARMOR
    public static final Holder<ArmorMaterial> UFO_ARMOR = register(
            "ufo_armor", // Nome do material
            new EnumMap<>(ArmorItem.Type.class) {{ // Proteção
                put(ArmorItem.Type.BOOTS, 4);
                put(ArmorItem.Type.LEGGINGS, 7);
                put(ArmorItem.Type.CHESTPLATE, 9);
                put(ArmorItem.Type.HELMET, 4);
            }},
            40, // Multiplicador de Durabilidade (ex: 40, mais que Netherite)
            SoundEvents.ARMOR_EQUIP_DIAMOND, // Som
            Ingredient.of(Tags.Items.GEMS_DIAMOND), // Ingrediente de reparo (Ex: diamante)
            2.0f, // Resistência a Knockback
            3.0f  // Tenacidade
    ); // <-- Encantabilidade (25) foi REMOVIDA daqui


    // --- Método Helper de Registro ---
    private static Holder<ArmorMaterial> register(
            String name,
            EnumMap<ArmorItem.Type, Integer> defense,
            int durabilityMultiplier, // <-- O multiplicador (ex: 37)
            // 'enchantmentValue' foi REMOVIDO daqui
            Holder<SoundEvent> equipSound,
            Ingredient repairIngredient,
            float knockbackResistance,
            float toughness)
    {
        List<ArmorMaterial.Layer> layers = List.of(
                new ArmorMaterial.Layer(
                        UfoMod.id(name), // Textura principal (ex: thermal_exosuit)
                        "", // Sufixo (deixe vazio)
                        false // false = não tingível
                )
        );

        // CORREÇÃO 1: Construtor corrigido, removendo 'enchantmentValue'
        ArmorMaterial material = new ArmorMaterial(
                defense,
                durabilityMultiplier,
                equipSound,
                () -> repairIngredient,
                layers,
                knockbackResistance,
                toughness
        );

        // CORREÇÃO 2: Criando o ResourceKey e usando o registerHolder correto
        ResourceKey<ArmorMaterial> key = ResourceKey.create(Registries.ARMOR_MATERIAL, UfoMod.id(name));

        // Esta linha pode variar dependendo da sua configuração do NeoForge,
        // mas `Registry.register` é o padrão para registrar um Holder.
        Registry.register(BuiltInRegistries.ARMOR_MATERIAL, key.location(), material);

        // Agora buscamos o Holder que acabamos de registrar
        return BuiltInRegistries.ARMOR_MATERIAL.getHolderOrThrow(key);
    }
}