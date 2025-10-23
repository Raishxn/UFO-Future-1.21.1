package com.raishxn.ufo; // Corrija o pacote para o seu mod

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class UFOConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Apenas a configuração que precisamos
    private static final ModConfigSpec.DoubleValue INFINITY_CELL_ENERGY = BUILDER
            .comment("ME Infinity Cell idle energy cost (unit: AE/t)")
            .defineInRange("item.infinity_cell_energy_cost", 8.0, 0.1, 64.0);

    public static final ModConfigSpec SPEC = BUILDER.build();

    // Variável que vamos usar no nosso código
    public static double infCellCost;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            // Carrega o valor do arquivo de configuração para a nossa variável
            infCellCost = INFINITY_CELL_ENERGY.get();
        }
    }
}