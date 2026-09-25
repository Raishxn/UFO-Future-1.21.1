package com.raishxn.ufo.gametest;

import com.raishxn.ufo.UFOConfig;
import com.raishxn.ufo.wireless.QuantumWirelessLinks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class QuantumWirelessRangeGameTests {
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void configuredRangeAppliesToNewAndLegacyHosts(GameTestHelper helper) {
        int original = UFOConfig.WIRELESS_RANGE.get();
        try {
            UFOConfig.WIRELESS_RANGE.set(1280);
            var origin = BlockPos.ZERO;
            var inside = new BlockPos(1000, 0, 0);
            var outside = new BlockPos(1281, 0, 0);

            var fresh = new QuantumWirelessLinks();
            helper.assertTrue(fresh.range() == 1280 && fresh.inRange(origin, inside)
                    && !fresh.inRange(origin, outside),
                    "a new source did not use the configured wireless range");

            var legacyTag = new CompoundTag();
            legacyTag.putInt("ufoWirelessRange", 32);
            var legacy = new QuantumWirelessLinks();
            legacy.load(legacyTag);
            helper.assertTrue(legacy.range() == 1280 && legacy.inRange(origin, inside),
                    "a source saved with the old default remained stuck at 32 blocks");

            legacy.setRange(32);
            var saved = new CompoundTag();
            legacy.save(saved);
            var explicit = new QuantumWirelessLinks();
            explicit.load(saved);
            helper.assertTrue(explicit.range() == 32 && !explicit.inRange(origin, inside),
                    "an explicitly selected 32-block range was not preserved");

            UFOConfig.WIRELESS_RANGE.set(0);
            helper.assertTrue(explicit.inRange(origin, new BlockPos(10_000, 0, 0)),
                    "zero did not disable the global distance limit");
        } finally {
            UFOConfig.WIRELESS_RANGE.set(original);
        }
        helper.succeed();
    }
}
