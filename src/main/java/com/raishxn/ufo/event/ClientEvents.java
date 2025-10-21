package com.raishxn.ufo.event;

import com.raishxn.ufo.item.custom.IHasCycleableModes;
import com.raishxn.ufo.item.custom.IHasModeHUD;
import com.raishxn.ufo.item.custom.IEnergyTool;
import com.raishxn.ufo.network.ModPackets;
import com.raishxn.ufo.network.packet.CycleModeKeyPacket;
import com.raishxn.ufo.network.packet.CycleToolKeyPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

// Esta classe é registrada como uma instância no Forge Bus, então não precisa da anotação @EventBusSubscriber
public class ClientEvents {

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // consumeClick() garante que a ação só aconteça uma vez por clique
        if (ModKeyBindings.CYCLE_TOOL_FORWARD.consumeClick()) {
            if (mc.player.getMainHandItem().getItem() instanceof IEnergyTool) {
                ModPackets.sendToServer(new CycleToolKeyPacket(true));
            }
        }

        if (ModKeyBindings.CYCLE_TOOL_BACKWARD.consumeClick()) {
            if (mc.player.getMainHandItem().getItem() instanceof IEnergyTool) {
                ModPackets.sendToServer(new CycleToolKeyPacket(false));
            }
        }

        if (ModKeyBindings.CYCLE_MODE.consumeClick()) {
            if (mc.player.getMainHandItem().getItem() instanceof IHasCycleableModes) {
                ModPackets.sendToServer(new CycleModeKeyPacket());
            }
        }
    }

    // Seu código de HUD está correto e pode continuar aqui.
    @SubscribeEvent
    public void onRenderHud(RenderGuiLayerEvent.Post event) {
        // ... (seu código de renderização do HUD permanece o mesmo)
    }
}