package com.raishxn.ufo.init;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.menu.DimensionalMatterAssemblerMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, UfoMod.MOD_ID);

    public static final Supplier<MenuType<DimensionalMatterAssemblerMenu>> DMA_MENU =
            MENUS.register("dimensional_matter_assembler_menu",
                    () -> IMenuTypeExtension.create(DimensionalMatterAssemblerMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}