package com.raishxn.ufo.event;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.item.custom.UfoArmorItem;
import com.raishxn.ufo.armor.UfoArmorSetting;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = UfoMod.MOD_ID)
public class AstralNexusEvents {
    private static final String LEGACY_FLIGHT_TAG = "ufoAstralNexusFlight";
    private static final String FLIGHT_OWNER_TAG = "ufoFlightOwner";
    private static final String FLIGHT_POLICY_VERSION_TAG = "ufoFlightPolicyVersion";
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        boolean flightActive = UfoArmorItem.hasActiveFlightSet(player);
        updateFlightOwnership(player, flightActive);
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

        if (ufoSourceActive) {
            var chest = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
            float desiredSpeed = 0.05F * UfoArmorItem.moduleSetting(chest, UfoArmorSetting.FLIGHT_SPEED) / 100.0F;
            if (Math.abs(abilities.getFlyingSpeed() - desiredSpeed) > 0.0001F) {
                abilities.setFlyingSpeed(desiredSpeed);
                player.onUpdateAbilities();
            }
        } else if (ownedByUfo && Math.abs(abilities.getFlyingSpeed() - 0.05F) > 0.0001F) {
            abilities.setFlyingSpeed(0.05F);
            player.onUpdateAbilities();
        }
    }
}
