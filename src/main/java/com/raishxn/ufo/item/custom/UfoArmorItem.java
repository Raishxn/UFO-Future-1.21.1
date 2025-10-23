package com.raishxn.ufo.item.custom;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.util.EnergyToolHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;

public class UfoArmorItem extends ArmorItem implements IEnergyTool {

    private static final int ENERGY_COST_PER_TICK = 20;
    private static final ResourceLocation HEALTH_BOOST_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "health_boost");
    private static final ResourceLocation STEP_HEIGHT_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "step_height_boost");

    public UfoArmorItem(Holder<ArmorMaterial> pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);
    }

    // --- MÉTODOS DE ENCANTAMENTO ---
    @Override
    public void onCraftedBy(ItemStack pStack, Level pLevel, Player pPlayer) {
        if (!pLevel.isClientSide()) {
            addGodEnchantments(pStack, this.type, pLevel);
        }
        super.onCraftedBy(pStack, pLevel, pPlayer);
    }

    public static void addGodEnchantments(ItemStack stack, ArmorItem.Type type, Level level) {
        RegistryAccess registryAccess = level.registryAccess();
        var enchantmentRegistry = registryAccess.registryOrThrow(Registries.ENCHANTMENT);
        stack.enchant(enchantmentRegistry.getHolderOrThrow(Enchantments.PROTECTION), 10);
        stack.enchant(enchantmentRegistry.getHolderOrThrow(Enchantments.FIRE_PROTECTION), 10);
        stack.enchant(enchantmentRegistry.getHolderOrThrow(Enchantments.BLAST_PROTECTION), 10);
        stack.enchant(enchantmentRegistry.getHolderOrThrow(Enchantments.PROJECTILE_PROTECTION), 10);
        stack.enchant(enchantmentRegistry.getHolderOrThrow(Enchantments.THORNS), 10);
        stack.enchant(enchantmentRegistry.getHolderOrThrow(Enchantments.UNBREAKING), 10);
        stack.enchant(enchantmentRegistry.getHolderOrThrow(Enchantments.MENDING), 10);
        switch (type) {
            case HELMET:
                stack.enchant(enchantmentRegistry.getHolderOrThrow(Enchantments.RESPIRATION), 10);
                stack.enchant(enchantmentRegistry.getHolderOrThrow(Enchantments.AQUA_AFFINITY), 10);
                break;
            case LEGGINGS:
                stack.enchant(enchantmentRegistry.getHolderOrThrow(Enchantments.SWIFT_SNEAK), 10);
                break;
            case BOOTS:
                stack.enchant(enchantmentRegistry.getHolderOrThrow(Enchantments.FEATHER_FALLING), 10);
                stack.enchant(enchantmentRegistry.getHolderOrThrow(Enchantments.DEPTH_STRIDER), 10);
                stack.enchant(enchantmentRegistry.getHolderOrThrow(Enchantments.SOUL_SPEED), 10);
                stack.enchant(enchantmentRegistry.getHolderOrThrow(Enchantments.FROST_WALKER), 10);
                break;
            default:
                break;
        }
    }

    public static void addGodEnchantments(ItemStack stack, ArmorItem.Type type, HolderLookup.Provider lookupProvider) {
        var enchantmentLookup = lookupProvider.lookupOrThrow(Registries.ENCHANTMENT);
        stack.enchant(enchantmentLookup.getOrThrow(Enchantments.PROTECTION), 10);
        stack.enchant(enchantmentLookup.getOrThrow(Enchantments.FIRE_PROTECTION), 10);
        stack.enchant(enchantmentLookup.getOrThrow(Enchantments.BLAST_PROTECTION), 10);
        stack.enchant(enchantmentLookup.getOrThrow(Enchantments.PROJECTILE_PROTECTION), 10);
        stack.enchant(enchantmentLookup.getOrThrow(Enchantments.THORNS), 10);
        stack.enchant(enchantmentLookup.getOrThrow(Enchantments.UNBREAKING), 10);
        stack.enchant(enchantmentLookup.getOrThrow(Enchantments.MENDING), 10);
        switch (type) {
            case HELMET:
                stack.enchant(enchantmentLookup.getOrThrow(Enchantments.RESPIRATION), 10);
                stack.enchant(enchantmentLookup.getOrThrow(Enchantments.AQUA_AFFINITY), 10);
                break;
            case LEGGINGS:
                stack.enchant(enchantmentLookup.getOrThrow(Enchantments.SWIFT_SNEAK), 10);
                break;
            case BOOTS:
                stack.enchant(enchantmentLookup.getOrThrow(Enchantments.FEATHER_FALLING), 10);
                stack.enchant(enchantmentLookup.getOrThrow(Enchantments.DEPTH_STRIDER), 10);
                stack.enchant(enchantmentLookup.getOrThrow(Enchantments.SOUL_SPEED), 10);
                stack.enchant(enchantmentLookup.getOrThrow(Enchantments.FROST_WALKER), 10);
                break;
            default:
                break;
        }
    }

    // --- LÓGICA PRINCIPAL DA ARMADURA ---
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // A lógica só roda no servidor e para jogadores
        if (!level.isClientSide && entity instanceof Player player) {

            // Primeiro, verificamos se o jogador está usando nosso peitoral.
            // O peitoral será o "controlador" principal dos efeitos.
            boolean chestPlateEquipped = player.getInventory().getArmor(2).getItem() instanceof UfoArmorItem;

            if (chestPlateEquipped) {
                // Se o peitoral estiver equipado, SÓ ELE vai gerenciar os efeitos.
                // Isso evita que o código rode 4x (uma para cada peça de armadura).
                if (this.getType() == Type.CHESTPLATE) {
                    if (hasFullSuitOfArmorOn(player) && hasEnoughEnergy(player)) {
                        // Se o conjunto está completo e com energia, aplica os efeitos e drena energia.
                        applyAllEffects(player);
                        drainEnergy(player);
                    } else {
                        // Se o conjunto está incompleto ou sem energia, remove os efeitos.
                        removeAllEffects(player);
                    }
                }
            } else {
                // Se o peitoral NÃO estiver equipado, o conjunto está quebrado.
                // Qualquer outra peça da armadura que ainda esteja equipada (e portanto, "tickando")
                // irá forçar a remoção dos efeitos.
                removeAllEffects(player);
            }
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    private void applyAllEffects(Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 9, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 9, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 9, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 200, 9, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false, true));

        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }

        AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute != null && healthAttribute.getModifier(HEALTH_BOOST_MODIFIER_ID) == null) {
            healthAttribute.addPermanentModifier(new AttributeModifier(HEALTH_BOOST_MODIFIER_ID, 40.0, AttributeModifier.Operation.ADD_VALUE));
        }

        AttributeInstance stepHeightAttribute = player.getAttribute(Attributes.STEP_HEIGHT);
        if (stepHeightAttribute != null && stepHeightAttribute.getModifier(STEP_HEIGHT_MODIFIER_ID) == null) {
            stepHeightAttribute.addPermanentModifier(new AttributeModifier(STEP_HEIGHT_MODIFIER_ID, 0.4D, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private void removeAllEffects(Player player) {
        AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute != null && healthAttribute.getModifier(HEALTH_BOOST_MODIFIER_ID) != null) {
            healthAttribute.removeModifier(HEALTH_BOOST_MODIFIER_ID);
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }

        if (!player.getAbilities().instabuild && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }

        AttributeInstance stepHeightAttribute = player.getAttribute(Attributes.STEP_HEIGHT);
        if (stepHeightAttribute != null && stepHeightAttribute.getModifier(STEP_HEIGHT_MODIFIER_ID) != null) {
            stepHeightAttribute.removeModifier(STEP_HEIGHT_MODIFIER_ID);
        }
    }

    // --- MÉTODOS DE SUPORTE (QUE ESTAVAM FALTANDO) ---
    private void drainEnergy(Player player) {
        for (ItemStack armorStack : player.getInventory().armor) {
            if (armorStack.getItem() instanceof UfoArmorItem) {
                int currentEnergy = armorStack.getOrDefault(ModDataComponents.ENERGY.get(), 0);
                int newEnergy = currentEnergy - ENERGY_COST_PER_TICK;
                if (newEnergy < 0) {
                    newEnergy = 0;
                }
                armorStack.set(ModDataComponents.ENERGY.get(), newEnergy);
            }
        }
    }

    private boolean hasEnoughEnergy(Player player) {
        for (ItemStack armorStack : player.getInventory().armor) {
            if (armorStack.getItem() instanceof UfoArmorItem) {
                int currentEnergy = armorStack.getOrDefault(ModDataComponents.ENERGY.get(), 0);
                if (currentEnergy < ENERGY_COST_PER_TICK) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasFullSuitOfArmorOn(Player player) {
        for (ItemStack armorStack : player.getInventory().armor) {
            if (!(armorStack.getItem() instanceof UfoArmorItem)) {
                return false;
            }
        }
        return true;
    }

    // --- MÉTODOS DA INTERFACE IEnergyTool ---
    @Override
    public Component getName(ItemStack stack) {
        return IEnergyTool.super.getName(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        return EnergyToolHelper.getBarWidth(pStack);
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        return EnergyToolHelper.getBarColor(pStack);
    }

    @Override
    public int getEnergyPerUse() {
        return 0; // Você precisa implementar este método
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        if (Screen.hasShiftDown()) {
            IEnergyStorage energyStorage = pStack.getCapability(Capabilities.EnergyStorage.ITEM);
            if (energyStorage != null) {
                String energyText = String.format("%,d / %,d RF", energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored());
                pTooltipComponents.add(Component.literal(energyText).withStyle(ChatFormatting.GRAY));
            }
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.ufo.press_shift").withStyle(ChatFormatting.AQUA));
        }
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}