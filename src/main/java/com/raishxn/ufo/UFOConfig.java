package com.raishxn.ufo; // Corrija o pacote para o seu mod

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class UFOConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Apenas a configuração que precisamos
    private static final ModConfigSpec.DoubleValue INFINITY_CELL_ENERGY = BUILDER
            .comment("ME Infinity Cell idle energy cost (unit: AE/t)")
            .defineInRange("item.infinity_cell_energy_cost", 8.0, 0.1, 64.0);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue STELLAR_EXPLOSION_BLOCK_GRIEF = SERVER_BUILDER
            .comment("Explicit opt-in for the Stellar Nexus catastrophic block-destruction wave. Default is local damage/visuals only.")
            .define("stellar.explosion.enableBlockGrief", false);
    public static final ModConfigSpec.IntValue STELLAR_EXPLOSION_MAX_RADIUS = SERVER_BUILDER
            .comment("Maximum destructive radius when block grief is enabled. Hard-capped at 64.")
            .defineInRange("stellar.explosion.maxRadius", 24, 1, 64);
    public static final ModConfigSpec.IntValue STELLAR_EXPLOSION_MAX_BLOCKS_PER_TICK = SERVER_BUILDER
            .comment("Maximum block changes in one tick during an opted-in destructive wave.")
            .defineInRange("stellar.explosion.maxBlockChangesPerTick", 256, 1, 4096);
    public static final ModConfigSpec.IntValue STELLAR_EXPLOSION_MAX_TOTAL_BLOCKS = SERVER_BUILDER
            .comment("Maximum total block changes for one destructive wave.")
            .defineInRange("stellar.explosion.maxTotalBlockChanges", 8192, 1, 1_000_000);
    public static final ModConfigSpec.LongValue STELLAR_EXPLOSION_MAX_NANOS_PER_TICK = SERVER_BUILDER
            .comment("CPU time budget per tick for the destructive wave.")
            .defineInRange("stellar.explosion.maxNanosPerTick", 2_000_000L, 100_000L, 50_000_000L);
    public static final ModConfigSpec.BooleanValue STELLAR_EXPLOSION_CREATE_LAVA = SERVER_BUILDER
            .comment("Allow the opted-in destructive wave to replace inner blocks with lava.")
            .define("stellar.explosion.createLava", false);
    public static final ModConfigSpec.BooleanValue STELLAR_EXPLOSION_SECONDARY_EXPLOSIONS = SERVER_BUILDER
            .comment("Allow secondary block explosions during the opted-in destructive wave.")
            .define("stellar.explosion.secondaryExplosions", false);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> STELLAR_EXPLOSION_ALLOWED_DIMENSIONS = SERVER_BUILDER
            .comment("Dimension IDs where opted-in block grief is allowed. Other dimensions always use local-only mode.")
            .defineListAllowEmpty("stellar.explosion.allowedDimensions", List.of("minecraft:overworld"), UFOConfig::isResourceLocation);

    public static final ModConfigSpec.LongValue STELLAR_COOLANT_GELID_EFFICIENCY = SERVER_BUILDER
            .comment("Gelid Cryotheum efficiency in the Stellar Nexus coolant formula. Zero disables this coolant for the Nexus.")
            .defineInRange("stellar.coolant.gelidCryotheumEfficiency", 1L, 0L, 1_000_000L);
    public static final ModConfigSpec.LongValue STELLAR_COOLANT_STABLE_EFFICIENCY = SERVER_BUILDER
            .comment("Stable Coolant efficiency in the Stellar Nexus coolant formula. Zero disables this coolant for the Nexus.")
            .defineInRange("stellar.coolant.stableCoolantEfficiency", 4L, 0L, 1_000_000L);
    public static final ModConfigSpec.LongValue STELLAR_COOLANT_TEMPORAL_EFFICIENCY = SERVER_BUILDER
            .comment("Temporal Fluid efficiency in the Stellar Nexus coolant formula. Zero disables this coolant for the Nexus.")
            .defineInRange("stellar.coolant.temporalFluidEfficiency", 8L, 0L, 1_000_000L);
    public static final ModConfigSpec.IntValue STELLAR_COOLANT_BUFFER_INTAKE_PER_TICK = SERVER_BUILDER
            .comment("Maximum coolant transferred from the ME grid into each Stellar fluid-hatch tank per tick.")
            .defineInRange("stellar.coolant.bufferIntakePerTick", 100_000, 1, 16_000_000);
    public static final ModConfigSpec.IntValue STELLAR_PASSIVE_DISSIPATION_PER_SECOND = SERVER_BUILDER
            .comment("Heat units passively dissipated by an idle Stellar Nexus each second. Zero disables passive dissipation.")
            .defineInRange("stellar.thermal.passiveDissipationPerSecond", 1, 0, 1_000);
    public static final ModConfigSpec.IntValue STELLAR_IDLE_COOLANT_INTERVAL_TICKS = SERVER_BUILDER
            .comment("Ticks between coolant attempts while the Stellar Nexus is idle. Zero disables idle coolant use.")
            .defineInRange("stellar.thermal.idleCoolantIntervalTicks", 20, 0, 1_200);

    public static final ModConfigSpec SERVER_SPEC = SERVER_BUILDER.build();

    // Variável que vamos usar no nosso código
    public static double infCellCost;

    private static boolean isResourceLocation(Object value) {
        return value instanceof String text && net.minecraft.resources.ResourceLocation.tryParse(text) != null;
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            // Carrega o valor do arquivo de configuração para a nossa variável
            infCellCost = INFINITY_CELL_ENERGY.get();
        }
    }
}
