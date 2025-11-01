package com.raishxn.ufo;

import com.mojang.logging.LogUtils;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.event.ModKeyBindings;
import com.raishxn.ufo.init.ModBlockEntities;
import com.raishxn.ufo.item.ModCellItems;
import com.raishxn.ufo.item.ModCreativeModeTabs;
import com.raishxn.ufo.item.ModItems;
import com.raishxn.ufo.item.UFORegistryHandler;
import com.raishxn.ufo.network.ModPackets;
import com.raishxn.ufo.network.packet.CycleModeKeyPacket;
import com.raishxn.ufo.network.packet.CycleToolKeyPacket;
import com.raishxn.ufo.util.LazyInits;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

@Mod(UfoMod.MOD_ID)
public class UfoMod {
    public static final String MOD_ID = "ufo";
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Método utilitário para criar um ResourceLocation com o ID do seu mod.
     */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public UfoMod(IEventBus modEventBus, ModContainer modContainer) {
        // --- Registros do Mod (usando DeferredRegister) ---
        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCellItems.register(modEventBus); // <-- ADICIONE ESTA LINHA AQUI
        // Registra o arquivo de configuração do mod (para o custo de energia das células, etc.)
        modContainer.registerConfig(ModConfig.Type.COMMON, UFOConfig.SPEC);

        // --- Listeners do Ciclo de Vida do Mod ---
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::loadComplete);
        modEventBus.addListener(this::registerPackets);

        // Registra os eventos do jogo (como input de teclas)
        NeoForge.EVENT_BUS.register(this);
    }

    private void registerPackets(final RegisterPayloadHandlersEvent event) {
        ModPackets.register(event);
    }

    /**
     * Chamado após o registro de todos os itens. Ideal para interagir com outros mods.
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Registra os handlers da Infinity Cell na API do Applied Energistics 2
            UFORegistryHandler.INSTANCE.onInit();
            // Executa outras tarefas de inicialização comuns
            LazyInits.initCommon();
        });
    }

    /**
     * Chamado após todos os mods serem carregados. Ideal para tarefas que dependem de
     * itens ou blocos de outros mods.
     */
    private void loadComplete(final FMLLoadCompleteEvent event) {
        // Executa as inicializações finais, como obter as AEKeys para as Infinity Cells
        event.enqueueWork(LazyInits::initFinal);
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
    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyBindings.CYCLE_TOOL_FORWARD);
        event.register(ModKeyBindings.CYCLE_TOOL_BACKWARD);
        event.register(ModKeyBindings.CYCLE_MODE);
    }

    public static class ClientModEvents {
        @SubscribeEvent
        public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            event.register(ModKeyBindings.CYCLE_TOOL_FORWARD);
            event.register(ModKeyBindings.CYCLE_TOOL_BACKWARD);
            event.register(ModKeyBindings.CYCLE_MODE);
        }
    }
}
