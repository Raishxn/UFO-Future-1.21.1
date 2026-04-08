package com.raishxn.ufo;

import appeng.client.render.crafting.CraftingCubeModel;
import appeng.hooks.BuiltInModelHooks;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.client.render.ModCoProcessorModelProvider; // <<-- NOVO IMPORT
import com.raishxn.ufo.client.render.ModCraftingStorageModelProvider;
import com.raishxn.ufo.core.MegaCoProcessorTier; // <<-- NOVO IMPORT
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import com.raishxn.ufo.event.ModKeyBindings;
import com.raishxn.ufo.event.ModTooltipEventHandler;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import appeng.init.client.InitScreens;
import com.raishxn.ufo.client.gui.DimensionalMatterAssemblerScreen;
import com.raishxn.ufo.screen.QuantumProcessorAssemblerControllerScreen;
import com.raishxn.ufo.screen.QuantumSlicerControllerScreen;
import com.raishxn.ufo.screen.StellarNexusControllerScreen;
import com.raishxn.ufo.menu.UFOMenus;
import com.raishxn.ufo.init.ModMenus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import com.raishxn.ufo.client.renderer.StellarNexusRenderer;
import com.raishxn.ufo.init.ModBlockEntities;

public class UfoModClient {

    public UfoModClient(IEventBus eventBus) {
        eventBus.addListener(this::onClientSetup);
        NeoForge.EVENT_BUS.register(ModTooltipEventHandler.class);
        eventBus.addListener(this::onRegisterKeyMappings);
        eventBus.addListener(this::registerScreens); // <--- Adicione esta linha
        eventBus.addListener(this::registerRenderers);
        eventBus.addListener(com.raishxn.ufo.client.render.StellarModelRegistry::registerAdditional);
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyBindings.CYCLE_TOOL_FORWARD);
        event.register(ModKeyBindings.CYCLE_TOOL_BACKWARD);
        event.register(ModKeyBindings.CYCLE_MODE);
        event.register(ModKeyBindings.TOGGLE_AUTO_SMELT);
    }


    private void registerScreens(RegisterMenuScreensEvent event) {
        InitScreens.register(event, UFOMenus.DIMENSIONAL_MATTER_ASSEMBLER.get(), DimensionalMatterAssemblerScreen::new, "/screens/dimensional_matter_assembler.json");
        event.register(ModMenus.STELLAR_NEXUS_CONTROLLER_MENU.get(), StellarNexusControllerScreen::new);
        InitScreens.register(event, ModMenus.QMF_CONTROLLER_MENU.get(), com.raishxn.ufo.screen.QmfControllerScreen::new, "/screens/universal_multiblock_controller.json");
        InitScreens.register(event, ModMenus.QUANTUM_SLICER_CONTROLLER_MENU.get(), QuantumSlicerControllerScreen::new, "/screens/universal_multiblock_controller.json");
        InitScreens.register(event, ModMenus.QUANTUM_PROCESSOR_ASSEMBLER_CONTROLLER_MENU.get(), QuantumProcessorAssemblerControllerScreen::new, "/screens/universal_multiblock_controller.json");
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.STELLAR_NEXUS_CONTROLLER_BE.get(), StellarNexusRenderer::new);
    }



    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {

            ItemBlockRenderTypes.setRenderLayer(MultiblockBlocks.ENTROPY_COMPUTER_CONDENSATION_MATRIX.get(), RenderType.cutout());
            // Loop para os Storages (já existente)
            for (var tier : MegaCraftingStorageTier.values()) {
                String modelName = tier.getRegistryId() + "_mega_crafting_storage_formed";
                BuiltInModelHooks.addBuiltInModel(
                        UfoMod.id("block/" + modelName),
                        new CraftingCubeModel(new ModCraftingStorageModelProvider(tier))
                );
            }

            // --- NOVO LOOP PARA OS CO-PROCESSORS ---
            for (var tier : MegaCoProcessorTier.values()) {
                String modelName = tier.getRegistryId() + "_mega_co_processor_formed";
                BuiltInModelHooks.addBuiltInModel(
                        UfoMod.id("block/" + modelName),
                        new CraftingCubeModel(new ModCoProcessorModelProvider(tier))
                );
            }
        });
    }
}
