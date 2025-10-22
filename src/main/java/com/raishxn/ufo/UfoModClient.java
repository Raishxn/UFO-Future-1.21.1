package com.raishxn.ufo;

import appeng.client.render.crafting.CraftingCubeModel;
import appeng.hooks.BuiltInModelHooks;
import com.raishxn.ufo.client.render.ModCoProcessorModelProvider; // <<-- NOVO IMPORT
import com.raishxn.ufo.client.render.ModCraftingStorageModelProvider;
import com.raishxn.ufo.core.MegaCoProcessorTier; // <<-- NOVO IMPORT
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import com.raishxn.ufo.event.ModTooltipEventHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = UfoMod.MOD_ID, dist = Dist.CLIENT)
public class UfoModClient {

    public UfoModClient(IEventBus eventBus) {
        eventBus.addListener(this::onClientSetup);
        NeoForge.EVENT_BUS.register(ModTooltipEventHandler.class);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
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