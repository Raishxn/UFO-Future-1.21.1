package com.raishxn.ufo.compat.mmr;

import com.raishxn.ufo.UfoMod;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

/** Prevents MMR's asynchronous structure scanner from feeding a server that is already stopping. */
@EventBusSubscriber(modid = UfoMod.MOD_ID)
public final class MmrShutdownCompat {
    private static final String MOD_ID = "modular_machinery_reborn";
    private static final String SAVED_DATA = "es.degrassi.mmreborn.api.controller.MMRWorldSavedData";
    private static volatile boolean acceptingAsyncChecks;

    private MmrShutdownCompat() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        acceptingAsyncChecks = true;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerStopping(ServerStoppingEvent event) {
        acceptingAsyncChecks = false;
        if (!ModList.get().isLoaded(MOD_ID)) return;

        try {
            Class<?> savedDataClass = Class.forName(SAVED_DATA, false, MmrShutdownCompat.class.getClassLoader());
            Method getOrCreate = savedDataClass.getMethod("getOrCreate", ServerLevel.class);
            Method releaseExecutor = savedDataClass.getMethod("releaseExecutorService");
            int released = 0;
            for (ServerLevel level : event.getServer().getAllLevels()) {
                Object savedData = getOrCreate.invoke(null, level);
                releaseExecutor.invoke(savedData);
                released++;
            }
            UfoMod.LOGGER.info("Stopped MMR asynchronous structure scanners for {} loaded dimensions", released);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException failure) {
            UfoMod.LOGGER.warn("Could not stop MMR asynchronous structure scanners before world save", failure);
        } catch (InvocationTargetException failure) {
            UfoMod.LOGGER.warn("MMR rejected asynchronous scanner shutdown before world save", failure.getCause());
        } catch (LinkageError failure) {
            UfoMod.LOGGER.warn("MMR asynchronous scanner shutdown is incompatible with this installation", failure);
        }
    }

    public static boolean acceptingAsyncChecks() {
        return acceptingAsyncChecks;
    }
}
