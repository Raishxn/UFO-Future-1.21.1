package com.raishxn.ufo;


import com.mojang.logging.LogUtils;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.client.gui.DimensionalMatterAssemblerScreen;
import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.event.ModKeyBindings;
import com.raishxn.ufo.fluid.ModFluidTypes;
import com.raishxn.ufo.fluid.ModFluids;
import com.raishxn.ufo.init.ModBlockEntities;
import com.raishxn.ufo.init.ModMenus;
import com.raishxn.ufo.init.ModRecipes;
import com.raishxn.ufo.init.ModSounds;
import com.raishxn.ufo.item.ModCellItems;
import com.raishxn.ufo.item.ModCreativeModeTabs;
import com.raishxn.ufo.item.ModItems;
import com.raishxn.ufo.item.UFORegistryHandler;
import com.raishxn.ufo.menu.DimensionalMatterAssemblerMenu;
import com.raishxn.ufo.network.ModPackets;
import com.raishxn.ufo.network.packet.CycleModeKeyPacket;
import com.raishxn.ufo.network.packet.CycleToolKeyPacket;
import com.raishxn.ufo.util.LazyInits;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

@Mod(UfoMod.MOD_ID)
public class UfoMod {
    public static final String MOD_ID = "ufo";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public UfoMod(IEventBus modEventBus, ModContainer modContainer) {
        // --- Registros do Mod (usando DeferredRegister) ---
        ModDataComponents.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);

        // --- 2. Fluidos (tem que vir antes de blocos e itens) ---
        com.raishxn.ufo.fluid.ModFluidTypes.register(modEventBus);
        com.raishxn.ufo.fluid.ModFluids.register(modEventBus);

        // --- 3. Blocos (tem que vir antes de itens) ---
        ModBlocks.register(modEventBus);
        MultiblockBlocks.register(modEventBus);

        // --- 4. Itens (dependem de blocos e fluidos) ---
        ModItems.register(modEventBus);
        ModCellItems.register(modEventBus);

        // --- 5. O Resto (dependem do que já foi registrado acima) ---
        ModBlockEntities.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModMenus.register(modEventBus);
        ModSounds.register(modEventBus);
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
            UFORegistryHandler.INSTANCE.onInit();
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
