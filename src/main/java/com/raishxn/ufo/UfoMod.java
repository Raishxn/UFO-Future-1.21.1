package com.raishxn.ufo;

import com.mojang.logging.LogUtils;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.event.ModKeyBindings;
import com.raishxn.ufo.init.ModBlockEntities;
import com.raishxn.ufo.item.ModCreativeModeTabs;
import com.raishxn.ufo.item.ModItems;
import com.raishxn.ufo.network.ModPackets;
import com.raishxn.ufo.network.packet.CycleModeKeyPacket;
import com.raishxn.ufo.network.packet.CycleToolKeyPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

@Mod(UfoMod.MOD_ID)
public class UfoMod {
    public static final String MOD_ID = "ufo";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public UfoMod(IEventBus modEventBus, ModContainer modContainer) {
        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModBlockEntities.register(modEventBus);

        // Adiciona os listeners para os eventos corretos
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPackets); // Corrigido

        NeoForge.EVENT_BUS.register(this);
    }

    // Método para registrar os pacotes de rede corretamente
    private void registerPackets(final RegisterPayloadHandlersEvent event) {
        ModPackets.register(event);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Lógica de associação do BlockEntity já está em ModBlockEntities.java
            // Deixe este método vazio por enquanto.
        });
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (ModKeyBindings.CYCLE_TOOL_FORWARD.consumeClick()) {
            ModPackets.sendToServer(new CycleToolKeyPacket(true));
            LOGGER.info("[UFO Mod] Cycle Tool Forward key pressed!");
        }

        if (ModKeyBindings.CYCLE_TOOL_BACKWARD.consumeClick()) {
            ModPackets.sendToServer(new CycleToolKeyPacket(false));
            LOGGER.info("[UFO Mod] Cycle Tool Backward key pressed!");
        }

        if (ModKeyBindings.CYCLE_MODE.consumeClick()) {
            ModPackets.sendToServer(new CycleModeKeyPacket());
            LOGGER.info("[UFO Mod] Cycle Mode key pressed!");
        }
    }

    @EventBusSubscriber(modid = UfoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            event.register(ModKeyBindings.CYCLE_TOOL_FORWARD);
            event.register(ModKeyBindings.CYCLE_TOOL_BACKWARD);
            event.register(ModKeyBindings.CYCLE_MODE);
        }
    }
}