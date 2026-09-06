package com.raishxn.ufo.diagnostic;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.raishxn.ufo.UfoMod;
import net.neoforged.fml.loading.FMLPaths;
import com.raishxn.ufo.block.entity.AbstractSimpleMultiblockControllerBE;
import com.raishxn.ufo.block.entity.AbstractParallelMultiblockControllerBE;
import com.raishxn.ufo.block.entity.StellarNexusControllerBE;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.TreeMap;
import java.nio.file.Files;
import java.io.IOException;
import java.time.Instant;

public final class UfoDebugCommands {
    private UfoDebugCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ufo")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("debug")
                        .then(Commands.literal("perf")
                                .executes(context -> showPerformance(context.getSource()))
                                .then(Commands.literal("reset").executes(context -> resetPerformance(context.getSource())))
                                .then(Commands.literal("export")
                                        .then(Commands.argument("scenario", StringArgumentType.word())
                                                .executes(context -> exportPerformance(context.getSource(),
                                                        StringArgumentType.getString(context, "scenario"))))))
                        .then(Commands.literal("machine")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> showMachine(
                                                context.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(context, "pos")))))));
    }

    private static int showPerformance(CommandSourceStack source) {
        var snapshots = MachinePerformanceRegistry.INSTANCE.snapshots();
        if (snapshots.isEmpty()) {
            source.sendSuccess(() -> Component.literal("UFO perf: nenhuma amostra coletada."), false);
            return 0;
        }

        record TypeSummary(long machines, long ticks, long totalAverageNanos, long worstP95, long worstP99,
                           long scans, long blocks, long storageOps, long energyAttempts,
                           long energyRequested, long energyAccepted, long syncEvents, long syncBytes) {
            TypeSummary add(MachinePerformanceRegistry.Snapshot snapshot) {
                return new TypeSummary(
                        machines + 1,
                        ticks + snapshot.tickCount(),
                        totalAverageNanos + snapshot.averageTickNanos(),
                        Math.max(worstP95, snapshot.p95TickNanos()),
                        Math.max(worstP99, snapshot.p99TickNanos()),
                        scans + snapshot.scanCount(),
                        blocks + snapshot.blocksTested(),
                        storageOps + snapshot.storageOperations(),
                        energyAttempts + snapshot.energyTransferAttempts(),
                        energyRequested + snapshot.energyRequested(),
                        energyAccepted + snapshot.energyAccepted(),
                        syncEvents + snapshot.syncEvents(),
                        syncBytes + snapshot.syncBytes());
            }
        }

        var byType = new TreeMap<String, TypeSummary>();
        for (var snapshot : snapshots) {
            byType.compute(snapshot.key().machineType(), (type, current) ->
                    (current == null ? new TypeSummary(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0) : current)
                            .add(snapshot));
        }

        source.sendSuccess(() -> Component.literal("UFO perf: " + snapshots.size() + " máquinas instrumentadas"), false);
        byType.forEach((type, summary) -> source.sendSuccess(() -> Component.literal(String.format(
                "%s count=%d avg=%.2fµs worstP95=%.2fµs worstP99=%.2fµs scans=%d blocks=%d storageOps=%d energy=%d/%dAE attempts=%d sync=%d/%dB",
                type,
                summary.machines(),
                summary.machines() == 0 ? 0.0D : summary.totalAverageNanos() / summary.machines() / 1_000.0D,
                summary.worstP95() / 1_000.0D,
                summary.worstP99() / 1_000.0D,
                summary.scans(), summary.blocks(), summary.storageOps(),
                summary.energyAccepted(), summary.energyRequested(), summary.energyAttempts(),
                summary.syncEvents(), summary.syncBytes())), false));
        return snapshots.size();
    }

    private static int showMachine(CommandSourceStack source, BlockPos pos) {
        String dimension = source.getLevel().dimension().location().toString();
        var snapshot = MachinePerformanceRegistry.INSTANCE.snapshot(dimension, pos.asLong());
        if (snapshot.isEmpty()) {
            source.sendFailure(Component.literal("Nenhuma métrica UFO para " + pos.toShortString()));
            return 0;
        }

        var value = snapshot.get();
        var blockEntity = source.getLevel().getBlockEntity(pos);
        String runtime;
        if (blockEntity instanceof AbstractParallelMultiblockControllerBE parallel) {
            runtime = (parallel.usesCompiledDefinition() ? " state=" + parallel.getRuntimeState().name() : "")
                    + " temp=" + parallel.getGuiTemperature() + "/" + parallel.getGuiMaxTemperature()
                    + " chemicalPorts=" + parallel.getChemicalPortCount()
                    + " pendingOutput=" + parallel.getPendingPromisedOutputAmount()
                    + " pendingByproduct=" + parallel.getPendingByproductAmount();
        } else if (blockEntity instanceof AbstractSimpleMultiblockControllerBE controller
                && controller.usesCompiledDefinition()) {
            runtime = " state=" + controller.getRuntimeState().name()
                    + " temp=" + controller.getGuiTemperature() + "/" + controller.getGuiMaxTemperature();
        } else if (blockEntity instanceof StellarNexusControllerBE stellar) {
            var energy = stellar.getEnergyDebugSnapshot();
            var coolant = stellar.getCoolantDebugSnapshot();
            runtime = (stellar.hasPendingOutputs()
                    ? " state=OUTPUT_BLOCKED pendingOutput=" + stellar.getPendingOutputAmount()
                    : "")
                    + String.format(
                            " energy=%d/%dAE ports=%d rate=%dAE/t last=%d/%dAE target=%dAE etaConfigured=%s etaObserved=%s"
                                    + " heat=%d/%d coolantPorts=%d coolantLast=%d/%dmB itemPorts=%d/%d",
                            energy.stored(), energy.capacity(), energy.portCount(), energy.configuredRate(),
                            energy.lastAccepted(), energy.lastRequested(), energy.activeRecipeCost(),
                            formatTicks(energy.configuredTicksToRecipe()),
                            formatTicks(energy.observedTicksToRecipe()),
                            coolant.heat(), coolant.maxHeat(), coolant.portCount(),
                            coolant.lastAccepted(), coolant.lastRequested(),
                            stellar.getItemInputPortCount(), stellar.getItemOutputPortCount());
        } else {
            runtime = "";
        }
        source.sendSuccess(() -> Component.literal(String.format(
                "%s %s%s avg=%.2fµs p95=%.2fµs p99=%.2fµs ticks=%d scans=%d blocks=%d storageOps=%d sync=%d/%dB",
                value.key().machineType(), pos.toShortString(),
                runtime,
                value.averageTickNanos() / 1_000.0D,
                value.p95TickNanos() / 1_000.0D,
                value.p99TickNanos() / 1_000.0D,
                value.tickCount(), value.scanCount(), value.blocksTested(), value.storageOperations(),
                value.syncEvents(), value.syncBytes())), false);
        return 1;
    }

    private static int resetPerformance(CommandSourceStack source) {
        MachinePerformanceRegistry.INSTANCE.reset();
        source.sendSuccess(() -> Component.literal("Métricas de performance UFO zeradas."), true);
        return 1;
    }

    private static int exportPerformance(CommandSourceStack source, String scenario) {
        var snapshots = MachinePerformanceRegistry.INSTANCE.snapshots();
        if (snapshots.isEmpty()) {
            source.sendFailure(Component.literal("Nenhuma amostra UFO para exportar."));
            return 0;
        }
        try {
            var directory = FMLPaths.GAMEDIR.get().resolve("ufo-diagnostics");
            Files.createDirectories(directory);
            // User labels are metadata only, never part of a filesystem path.
            var output = Files.createTempFile(directory, "perf-", ".json");
            Files.writeString(output, MachinePerformanceReport.toJson(scenario, Instant.now().toString(), snapshots));
            source.sendSuccess(() -> Component.literal("Diagnóstico UFO exportado: " + output.toAbsolutePath()), false);
            return snapshots.size();
        } catch (IOException exception) {
            UfoMod.LOGGER.error("Could not export UFO performance report", exception);
            source.sendFailure(Component.literal("Falha ao exportar diagnóstico UFO; consulte o log do servidor."));
            return 0;
        }
    }

    private static String formatTicks(long ticks) {
        if (ticks < 0L) {
            return "unavailable";
        }
        long seconds = (ticks + 19L) / 20L;
        long minutes = seconds / 60L;
        long remainingSeconds = seconds % 60L;
        return minutes + "m" + remainingSeconds + "s";
    }
}
