package com.raishxn.ufo.event;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.item.ModArmor;
import com.raishxn.ufo.item.custom.UfoArmorItem;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = UfoMod.MOD_ID)
public class AstralNexusEvents {
    private static final ResourceLocation STEP_ASSIST_ID = UfoMod.id("astral_nexus_step_assist");
    private static final String LEGACY_FLIGHT_TAG = "ufoAstralNexusFlight";
    private static final String FLIGHT_OWNER_TAG = "ufoFlightOwner";
    private static final String FLIGHT_POLICY_VERSION_TAG = "ufoFlightPolicyVersion";
    private static final int EFFECT_DURATION = 220;
    private static final int EFFECT_REFRESH_THRESHOLD = 200;
    private static final float REFLECT_MULTIPLIER = 1_000_000.0F;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || !isFullAstralNexus(player)) {
            return;
        }

        event.setCanceled(true);
        player.hurtTime = 0;
        player.deathTime = 0;
        player.setHealth(player.getMaxHealth());

        if (event.getSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker && attacker != player) {
            attacker.invulnerableTime = 0;
            attacker.hurt(player.damageSources().thorns(player), Math.min(Float.MAX_VALUE, event.getAmount() * REFLECT_MULTIPLIER));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player) || !isFullAstralNexus(player)) {
            return;
        }
        event.setCanceled(true);
        player.hurtTime = 0;
        player.deathTime = 0;
        player.setHealth(player.getMaxHealth());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        boolean astralSetActive = isFullAstralNexus(player);
        if (astralSetActive) {
            applySetBonuses(player);
        } else {
            removeSetBonuses(player);
        }
        updateFlightOwnership(player, astralSetActive || UfoArmorItem.hasActiveFlightSet(player));
    }

    private static void applySetBonuses(Player player) {
        if (player.getAirSupply() < player.getMaxAirSupply()) {
            player.setAirSupply(player.getMaxAirSupply());
        }
        refreshEffect(player, MobEffects.NIGHT_VISION);
        refreshEffect(player, MobEffects.WATER_BREATHING);

        var stepHeight = player.getAttribute(Attributes.STEP_HEIGHT);
        if (stepHeight != null && stepHeight.getModifier(STEP_ASSIST_ID) == null) {
            stepHeight.addPermanentModifier(new AttributeModifier(STEP_ASSIST_ID, 0.5D, AttributeModifier.Operation.ADD_VALUE));
        }

        if (ModList.get().isLoaded("mekanism")) {
            IRadiationEntity radiationEntity = player.getCapability(Capabilities.RADIATION_ENTITY);
            if (radiationEntity != null && Double.compare(radiationEntity.getRadiation(), 0.0D) != 0) {
                radiationEntity.set(0);
            }
        }
    }

    private static void refreshEffect(Player player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
        if (ArmorEffectRefreshPolicy.shouldRefresh(
                player.getEffect(effect), 0, EFFECT_REFRESH_THRESHOLD)) {
            player.addEffect(new MobEffectInstance(effect, EFFECT_DURATION, 0, false, false, true));
        }
    }

    private static void removeSetBonuses(Player player) {
        var stepHeight = player.getAttribute(Attributes.STEP_HEIGHT);
        if (stepHeight != null && stepHeight.getModifier(STEP_ASSIST_ID) != null) {
            stepHeight.removeModifier(STEP_ASSIST_ID);
        }

    }

    private static void updateFlightOwnership(Player player, boolean ufoSourceActive) {
        var persistentData = player.getPersistentData();
        int policyVersion = persistentData.getInt(FLIGHT_POLICY_VERSION_TAG);
        boolean ownedByUfo = FlightOwnershipPolicy.trustedStoredOwnership(
                policyVersion, persistentData.getBoolean(FLIGHT_OWNER_TAG));

        if (policyVersion < FlightOwnershipPolicy.CURRENT_VERSION) {
            persistentData.remove(LEGACY_FLIGHT_TAG);
            persistentData.remove(FLIGHT_OWNER_TAG);
            persistentData.putInt(FLIGHT_POLICY_VERSION_TAG, FlightOwnershipPolicy.CURRENT_VERSION);
        }

        var abilities = player.getAbilities();
        var decision = FlightOwnershipPolicy.decide(
                ufoSourceActive,
                abilities.mayfly,
                ownedByUfo,
                abilities.instabuild || player.isSpectator());

        if (decision.ownedAfter()) {
            persistentData.putBoolean(FLIGHT_OWNER_TAG, true);
        } else {
            persistentData.remove(FLIGHT_OWNER_TAG);
        }

        if (decision.grantFlight()) {
            abilities.mayfly = true;
            player.onUpdateAbilities();
        } else if (decision.revokeFlight()) {
            abilities.mayfly = false;
            abilities.flying = false;
            player.onUpdateAbilities();
        }
    }

    public static boolean isFullAstralNexus(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() == ModArmor.ASTRAL_NEXUS_HELMET.get()
                && player.getItemBySlot(EquipmentSlot.CHEST).getItem() == ModArmor.ASTRAL_NEXUS_CHESTPLATE.get()
                && player.getItemBySlot(EquipmentSlot.LEGS).getItem() == ModArmor.ASTRAL_NEXUS_LEGGINGS.get()
                && player.getItemBySlot(EquipmentSlot.FEET).getItem() == ModArmor.ASTRAL_NEXUS_BOOTS.get();
    }
}
