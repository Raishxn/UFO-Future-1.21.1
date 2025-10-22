package com.raishxn.ufo;

import appeng.client.render.crafting.CraftingCubeModel;
import appeng.hooks.BuiltInModelHooks;
import com.raishxn.ufo.client.render.ModCraftingStorageModelProvider;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import com.raishxn.ufo.event.ModTooltipEventHandler; // <<-- 1. IMPORTE A CLASSE
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge; // <<-- 2. IMPORTE O NeoForge

@Mod(value = UfoMod.MOD_ID, dist = Dist.CLIENT)
public class UfoModClient {

    public UfoModClient(IEventBus eventBus) {
        eventBus.addListener(this::onClientSetup);

        // 3. ADICIONE ESTA LINHA PARA GARANTIR O REGISTRO
        NeoForge.EVENT_BUS.register(ModTooltipEventHandler.class);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (var tier : MegaCraftingStorageTier.values()) {
                String modelName = tier.getRegistryId() + "_mega_crafting_storage_formed";
                BuiltInModelHooks.addBuiltInModel(
                        UfoMod.asResource("block/" + modelName),
                        new CraftingCubeModel(new ModCraftingStorageModelProvider(tier))
                );
            }
        });
    }
}