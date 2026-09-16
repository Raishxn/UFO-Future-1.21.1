package com.raishxn.ufo.gametest;

import com.raishxn.ufo.armor.UfoArmorModule;
import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.item.ModArmor;
import com.raishxn.ufo.item.custom.IEnergyTool;
import com.raishxn.ufo.item.custom.IThermalArmor;
import com.raishxn.ufo.item.custom.ThermalResistorExosuitItem;
import com.raishxn.ufo.item.custom.UfoArmorItem;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Runtime registry contract separating the passive Thermal set from powered UFO armor. */
@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class ArmorIsolationGameTests {
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void thermalSuitRejectsUfoModulesAndEnergy(GameTestHelper helper) {
        List<Item> thermalPieces = List.of(
                ModArmor.THERMAL_RESISTOR_MASK.get(), ModArmor.THERMAL_RESISTOR_CHEST.get(),
                ModArmor.THERMAL_RESISTOR_PANTS.get(), ModArmor.THERMAL_RESISTOR_BOOTS.get());

        for (Item item : thermalPieces) {
            ItemStack stack = new ItemStack(item);
            helper.assertTrue(item instanceof ThermalResistorExosuitItem && item instanceof IThermalArmor,
                    item + " is not registered as Thermal armor");
            helper.assertTrue(!(item instanceof UfoArmorItem) && !(item instanceof IEnergyTool),
                    item + " inherited powered UFO armor behavior");
            helper.assertTrue(stack.getCapability(Capabilities.EnergyStorage.ITEM) == null,
                    item + " exposes an energy capability");
            helper.assertTrue(!stack.has(ModDataComponents.ENERGY.get()),
                    item + " has a default energy component");
        }

        ItemStack thermalChest = new ItemStack(ModArmor.THERMAL_RESISTOR_CHEST.get());
        helper.assertTrue(!UfoArmorItem.installModule(thermalChest, UfoArmorModule.VOID_FLIGHT),
                "Thermal chest accepted a UFO armor module");
        helper.assertTrue(UfoArmorItem.installedModules(thermalChest).isEmpty(),
                "Thermal chest persisted a UFO armor module");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void ufoArmorKeepsPoweredModuleContract(GameTestHelper helper) {
        ItemStack ufoChest = new ItemStack(ModArmor.UFO_CHESTPLATE.get());
        helper.assertTrue(ufoChest.getItem() instanceof UfoArmorItem
                        && ufoChest.getItem() instanceof IEnergyTool,
                "UFO chest lost its powered armor implementation");
        helper.assertTrue(ufoChest.getCapability(Capabilities.EnergyStorage.ITEM) != null,
                "UFO chest lost its energy capability");
        helper.assertTrue(UfoArmorItem.installModule(ufoChest, UfoArmorModule.VOID_FLIGHT),
                "UFO chest rejected its compatible module");
        helper.assertTrue(UfoArmorItem.installedModules(ufoChest).contains(UfoArmorModule.VOID_FLIGHT),
                "UFO chest did not persist its module");
        helper.succeed();
    }
}
