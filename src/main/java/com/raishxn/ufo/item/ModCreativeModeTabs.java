package com.raishxn.ufo.item;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UfoMod.MOD_ID);

    public static final Supplier<CreativeModeTab> UFO_ITEMS_TAB = CREATIVE_MODE_TAB.register("ufo_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BISMUTH.get()))
                    .title(Component.translatable("creativetab.ufomod.ufo_items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.BISMUTH);

                        output.accept(ModItems.UFO_HELMET.get());
                        output.accept(ModItems.UFO_CHESTPLATE.get());
                        output.accept(ModItems.UFO_LEGGINGS.get());
                        output.accept(ModItems.UFO_BOOTS.get());
                        output.accept(ModItems.UFO_STAFF);

                        ModBlocks.CRAFTING_STORAGE_BLOCKS.values().forEach(block -> output.accept(block.get()));

                    }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}