package com.raishxn.ufo.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/** Moves the old NeoForge default filenames before the new config paths are loaded. */
public final class UfoConfigFileMigration {
    private static final System.Logger LOGGER = System.getLogger(UfoConfigFileMigration.class.getName());

    private UfoConfigFileMigration() {}

    public static void migrate(Path configDir, Path gameDir, String defaultConfigPath) {
        move(configDir, "ufo-common.toml", "ufo/common.toml");
        move(configDir, "ufo-server.toml", "ufo/server.toml");
        move(gameDir.resolve(defaultConfigPath), "ufo-server.toml", "ufo/server.toml");

        // Server configs normally live inside the save. Dedicated servers may use a
        // custom world directory directly below gameDir; clients use saves/<world>.
        migrateWorld(gameDir);
        forEachDirectory(gameDir, UfoConfigFileMigration::migrateWorld);
        forEachDirectory(gameDir.resolve("saves"), UfoConfigFileMigration::migrateWorld);
    }

    private static void migrateWorld(Path world) {
        move(world.resolve("serverconfig"), "ufo-server.toml", "ufo/server.toml");
    }

    private static void forEachDirectory(Path parent, java.util.function.Consumer<Path> action) {
        if (!Files.isDirectory(parent)) return;
        try (Stream<Path> children = Files.list(parent)) {
            children.filter(Files::isDirectory).forEach(action);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not inspect UFO server config directories under " + parent, e);
        }
    }

    private static void move(Path base, String oldName, String newName) {
        Path oldFile = base.resolve(oldName);
        if (!Files.isRegularFile(oldFile)) return;
        Path newFile = base.resolve(newName);
        if (Files.exists(newFile)) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Both UFO config files exist; keeping {0} and leaving {1} untouched", newFile, oldFile);
            return;
        }
        try {
            Files.createDirectories(newFile.getParent());
            Files.move(oldFile, newFile);
            LOGGER.log(System.Logger.Level.INFO, "Migrated UFO config from {0} to {1}", oldFile, newFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not migrate UFO config from " + oldFile + " to " + newFile, e);
        }
    }
}
