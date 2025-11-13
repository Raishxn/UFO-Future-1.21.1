package com.raishxn.ufo.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.item.ModArmorMaterials;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import java.util.List;


public class ThermalResistorExosuitItem extends ArmorItem implements IThermalArmor {

    private final Multimap<Holder<Attribute>, AttributeModifier> customAttributeModifiers;

    public ThermalResistorExosuitItem(ArmorItem.Type type, Properties properties) {
        super(ModArmorMaterials.UFO_ARMOR, type, properties.fireResistant());

        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();

        // Adiciona +15% de Velocidade de Mineração
        builder.put(
                Attributes.MINING_EFFICIENCY,
                new AttributeModifier(
                        UfoMod.id("thermal_mining_speed"),
                        0.15,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                )
        );

        this.customAttributeModifiers = builder.build();
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        // Pega os modificadores padrão (armadura, etc.)
        ItemAttributeModifiers defaultModifiers = super.getDefaultAttributeModifiers(stack);

        // Cria um novo builder
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        // Adiciona manualmente CADA modificador padrão ao builder
        for (ItemAttributeModifiers.Entry entry : defaultModifiers.modifiers()) {
            builder.add(entry.attribute(), entry.modifier(), entry.slot());
        }

        // Adiciona os seus modificadores customizados
        this.customAttributeModifiers.forEach((attributeHolder, modifier) -> {
            builder.add(attributeHolder, modifier, EquipmentSlotGroup.bySlot(this.type.getSlot()));
        });

        return builder.build();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);

        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }
        if (player.getItemBySlot(this.type.getSlot()) != stack) {
            return;
        }

        // Imunidade a Fogo / Refrigeração (Remove queimadura)
        if (player.isOnFire()) {
            player.clearFire();
        }

        // Adiciona Resistência II se o set estiver completo
        if (hasFullSet(player)) {
            // <-- ATUALIZADO: Amplificador 1 = Nível II -->
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 220, 1, false, false, true));
        }
    }

    // --- TOOLTIPS COMPLETAMENTE ATUALIZADOS ---
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6Thermal Resistor Exosuit"));
        tooltip.add(Component.literal("§7Projetada para resistir ao impossível."));
        tooltip.add(Component.literal("§8Camadas de matéria densa dissipam calor extremo."));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("§b+100% Resistência Térmica"));
        tooltip.add(Component.literal("§cImune ao Calor do DMA"));
        tooltip.add(Component.literal("§9Refrigeração Integrada"));
        tooltip.add(Component.empty());

        if (Screen.hasShiftDown()) {
            Player player = Minecraft.getInstance().player;
            if (player != null && hasFullSet(player)) {
                tooltip.add(Component.literal("§5[Conjunto Completo Ativo]"));
                tooltip.add(Component.literal("§bImune a Fogo e Lava"));
                tooltip.add(Component.literal("§bImune a Calor Industrial"));
                tooltip.add(Component.literal("§bRemoção Instantânea de Queimaduras"));
                tooltip.add(Component.literal("§bEstabilidade Térmica Total"));
            } else {
                tooltip.add(Component.literal("§8Equipe o conjunto completo para bônus."));
            }
        } else {
            tooltip.add(Component.literal("§8Pressione <SHIFT> para detalhes."));
        }

        super.appendHoverText(stack, context, tooltip, flag);
    }
    // --- FIM DA ATUALIZAÇÃO DOS TOOLTIPS ---

    private boolean hasFullSet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof IThermalArmor &&
                player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof IThermalArmor &&
                player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof IThermalArmor &&
                player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof IThermalArmor;
    }
}