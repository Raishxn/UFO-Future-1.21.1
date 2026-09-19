package com.raishxn.ufo.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.event.ArmorEffectRefreshPolicy;
import com.raishxn.ufo.item.ModArmorMaterials;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.ChatFormatting;
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
        super(ModArmorMaterials.THERMAL_EXOSUIT, type, properties.fireResistant());

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
        if (this.type == Type.CHESTPLATE && hasFullSet(player)
                && ArmorEffectRefreshPolicy.shouldRefresh(
                player.getEffect(MobEffects.DAMAGE_RESISTANCE), 1, 200)) {
            // <-- ATUALIZADO: Amplificador 1 = Nível II -->
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 220, 1, false, false, true));
        }
    }

    // --- TOOLTIPS COMPLETAMENTE ATUALIZADOS ---
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ufo.thermal_exosuit.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.ufo.thermal_exosuit.description").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ufo.thermal_exosuit.layers").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.ufo.thermal_exosuit.resistance").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.ufo.thermal_exosuit.dma_immunity").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("tooltip.ufo.thermal_exosuit.cooling").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.empty());

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.ufo.thermal_exosuit.full_set").withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.translatable("tooltip.ufo.thermal_exosuit.fire_lava").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.ufo.thermal_exosuit.industrial_heat").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.ufo.thermal_exosuit.extinguish").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.ufo.thermal_exosuit.stability").withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.ufo.thermal_exosuit.shift").withStyle(ChatFormatting.DARK_GRAY));
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
