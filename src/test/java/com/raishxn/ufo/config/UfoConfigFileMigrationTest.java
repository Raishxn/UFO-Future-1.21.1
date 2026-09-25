package com.raishxn.ufo.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UfoConfigFileMigrationTest {
    @TempDir Path gameDir;

    @Test void preservesValuesAcrossGlobalDefaultsAndWorldConfigs() throws Exception {
        Path config = gameDir.resolve("config");
        Path defaults = gameDir.resolve("defaultconfigs");
        Path world = gameDir.resolve("saves/existing/serverconfig");
        Files.createDirectories(config);
        Files.createDirectories(defaults);
        Files.createDirectories(world);
        Files.writeString(config.resolve("ufo-common.toml"), "infinity_cell_energy_cost = 19\n");
        Files.writeString(config.resolve("ufo-server.toml"), "maxExternalAccelerationTicksPerGameTick = 97\n");
        Files.writeString(defaults.resolve("ufo-server.toml"), "flight_speed = 225\n");
        Files.writeString(world.resolve("ufo-server.toml"), "flight_speed = 250\n");

        UfoConfigFileMigration.migrate(config, gameDir, "defaultconfigs");
        UfoConfigFileMigration.migrate(config, gameDir, "defaultconfigs");

        assertEquals("infinity_cell_energy_cost = 19\n", Files.readString(config.resolve("ufo/common.toml")));
        assertEquals("maxExternalAccelerationTicksPerGameTick = 97\n",
                Files.readString(config.resolve("ufo/server.toml")));
        assertEquals("flight_speed = 225\n", Files.readString(defaults.resolve("ufo/server.toml")));
        assertEquals("flight_speed = 250\n", Files.readString(world.resolve("ufo/server.toml")));
        assertFalse(Files.exists(world.resolve("ufo-server.toml")));
    }

    @Test void existingDestinationWinsWithoutDiscardingLegacyFile() throws Exception {
        Path config = gameDir.resolve("config");
        Files.createDirectories(config.resolve("ufo"));
        Files.writeString(config.resolve("ufo-common.toml"), "old value\n");
        Files.writeString(config.resolve("ufo/common.toml"), "new value\n");

        UfoConfigFileMigration.migrate(config, gameDir, "defaultconfigs");

        assertEquals("new value\n", Files.readString(config.resolve("ufo/common.toml")));
        assertEquals("old value\n", Files.readString(config.resolve("ufo-common.toml")));
    }
}
