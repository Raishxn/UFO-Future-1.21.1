package com.raishxn.ufo.gametest;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergyService;
import com.raishxn.ufo.crafting.NexusCraftingEnergy;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Cover for the default Nexus energy waiver: pattern extraction reports success without ever
 * reaching the real grid service, while every other energy read still does.
 */
@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class NexusCraftingEnergyGameTests {
    private NexusCraftingEnergyGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void patternEnergyIsWaivedWithoutTouchingTheGrid(GameTestHelper helper) {
        RecordingEnergy grid = new RecordingEnergy();
        NexusCraftingEnergy view = new NexusCraftingEnergy(grid);

        helper.assertTrue(view.extractAEPower(64.0D, Actionable.SIMULATE, PowerMultiplier.CONFIG) == 64.0D,
                "Nexus pattern simulation did not report a full buffer");
        helper.assertTrue(view.extractAEPower(64.0D, Actionable.MODULATE, PowerMultiplier.CONFIG) == 64.0D,
                "Nexus pattern extraction did not report success");
        helper.assertTrue(grid.extractions == 0,
                "Nexus pattern energy reached the real grid service");
        helper.assertTrue(view.getStoredPower() == 123.0D && view.getMaxStoredPower() == 456.0D,
                "Nexus energy view stopped delegating grid reads");
        helper.succeed();
    }

    private static final class RecordingEnergy implements IEnergyService {
        private int extractions;

        @Override
        public double extractAEPower(double amount, Actionable mode, PowerMultiplier usePowerMultiplier) {
            extractions++;
            return 0.0D;
        }

        @Override public double getIdlePowerUsage() { return 1.0D; }
        @Override public double getChannelPowerUsage() { return 2.0D; }
        @Override public double getAvgPowerUsage() { return 3.0D; }
        @Override public double getAvgPowerInjection() { return 4.0D; }
        @Override public boolean isNetworkPowered() { return true; }
        @Override public double injectPower(double amount, Actionable mode) { return amount; }
        @Override public double getStoredPower() { return 123.0D; }
        @Override public double getMaxStoredPower() { return 456.0D; }
        @Override public double getEnergyDemand(double maxRequired) { return maxRequired; }
    }
}
