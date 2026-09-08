package com.raishxn.ufo.client;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.armor.UfoArmorModule;
import com.raishxn.ufo.armor.UfoArmorSetting;
import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.item.custom.UfoArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = UfoMod.MOD_ID, value = Dist.CLIENT)
public final class UfoArmorClientEvents {
    private UfoArmorClientEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getEntity() != minecraft.player || minecraft.player == null
                || !minecraft.player.getAbilities().flying || !UfoArmorItem.hasFullUfoSet(minecraft.player)) return;
        ItemStack chest = minecraft.player.getItemBySlot(EquipmentSlot.CHEST);
        if (!UfoArmorItem.isModuleEnabled(chest, UfoArmorModule.VOID_FLIGHT)
                || chest.getOrDefault(ModDataComponents.ENERGY.get(), 0) < UfoArmorModule.VOID_FLIGHT.energyCost()) return;
        var options = minecraft.options;
        boolean noHorizontalInput = !options.keyUp.isDown() && !options.keyDown.isDown()
                && !options.keyLeft.isDown() && !options.keyRight.isDown();
        if (!noHorizontalInput) return;
        float inertia = UfoArmorItem.moduleSetting(chest, UfoArmorSetting.FLIGHT_INERTIA) / 100.0F;
        var movement = minecraft.player.getDeltaMovement();
        minecraft.player.setDeltaMovement(movement.x * inertia, movement.y, movement.z * inertia);
    }
}
