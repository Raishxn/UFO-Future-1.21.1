package com.raishxn.ufo;

// --- IMPORTS CORRIGIDOS ---
import appeng.client.render.crafting.CraftingCubeModel;
import appeng.hooks.BuiltInModelHooks;
// A linha abaixo foi alterada para importar a classe correta
import com.raishxn.ufo.client.render.ModCraftingStorageModelProvider;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = UfoMod.MOD_ID, dist = Dist.CLIENT)
public class UfoModClient {

    public UfoModClient(IEventBus eventBus) {
        eventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Itera sobre todos os teus tiers
            for (var tier : MegaCraftingStorageTier.values()) {
                // Define o nome do modelo "formado" que o datagen espera
                String modelName = tier.getRegistryId() + "_mega_crafting_storage_formed";

                // --- A CORREÇÃO ESTÁ AQUI ---
                // Trocamos ModBlockStateProvider por ModCraftingStorageModelProvider
                BuiltInModelHooks.addBuiltInModel(
                        UfoMod.asResource("block/" + modelName),
                        new CraftingCubeModel(new ModCraftingStorageModelProvider(tier))
                );
            }
        });
    }
}