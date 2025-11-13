package com.raishxn.ufo.block.entity;

import com.raishxn.ufo.item.ModItems; // (Você precisará ter 13 catalisadores registrados aqui)
// import com.raishxn.ufo.init.ModSounds; // TODO: Importar seus sons
// import com.raishxn.ufo.UFOConfig; // TODO: Importar seu Config
import com.raishxn.ufo.recipe.DimensionalMatterAssemblerRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Gerencia o sistema híbrido (Tier + Stack + Thermal) do PDF v3.
 * ATUALIZADO com novas zonas de temperatura e efeitos.
 */
public class DMAThermalHandler {

    // A temperatura ambiente (0°C) em Kelvin, que é nosso ponto de partida.
    public static final int AMBIENT_TEMP_K = 273;

    // Constantes de consumo de coolant (ajustáveis)
    private static final int COOLANT_CONSUMPTION_PER_TICK = 5; // mB por tick

    // CORREÇÃO: Constante para o delay de meltdown (10 segundos)
    public static final int MELTDOWN_FUSE_TICKS = 200; // 10 segundos * 20 ticks/segundo

    // O BlockEntity "dono" deste handler
    private final DimensionalMatterAssemblerBlockEntity blockEntity;

    private int temperature = AMBIENT_TEMP_K;

    // CORREÇÃO: Timer para o meltdown
    private int meltdownTicks = 0;

    // --- NOVAS ZONAS DE TEMPERATURA ---
    public enum TemperatureZone {
        SAFE,           // < 4000°C
        OVERHEAT,       // 4000°C - 5999°C (Aviso)
        INEFFICIENCY,   // 6000°C - 8499°C (Coolant -50%)
        DESTABILIZATION,// 8500°C - 9999°C (Chance de falha alta, Alarme)
        MELTDOWN;       // >= 10000°C (Explosão)

        public static TemperatureZone getZone(int celsius) {
            if (celsius >= 10000) return MELTDOWN;
            if (celsius >= 8500) return DESTABILIZATION;
            if (celsius >= 6000) return INEFFICIENCY;
            if (celsius >= 4000) return OVERHEAT;
            return SAFE;
        }
    }

    // Usado pelo BlockEntity para rastrear quando enviar mensagens
    private TemperatureZone lastAnnouncedZone = TemperatureZone.SAFE;


    public DMAThermalHandler(DimensionalMatterAssemblerBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    // (O restante da lógica do catalisador - CatalystItemData, CATALYST_ITEM_DATA, ActiveModifiers, getSoftCapMultiplier, calculateAllModifiers, applyPostCraftHeat - permanece O MESMO)
    // ... (copie e cole todo o seu código de lógica de catalisador daqui) ...
    // --- CONSTRUTOR ---
    // (O construtor que você já tinha)

    // =================================================================================
    // --- LÓGICA DO CATALISADOR (Híbrido Tier+Stack) ---
    // =================================================================================

    // (ASSUMPÇÃO) Stats base para cada família (multiplicados por Tiers/Stack)
    private static final double MATTERFLOW_BASE_ENERGY = -0.10; // -10%
    private static final double CHRONO_BASE_SPEED = +0.25;      // +25%
    private static final double OVERFLUX_BASE_FAILURE = -0.10;  // -10%
    private static final double QUANTUM_BASE_DROP = +0.10;      // +10%

    /**
     * Armazena os dados de CADA UM dos 12 itens T1-T3.
     */
    private record CatalystItemData(
            String family,     // "matterflow", "chrono", "overflux", "quantum"
            int tier,          // 1, 2, or 3
            double tierMultiplier, // 1.0, 2.5, ou 5.0
            int staticHeat     // +50C, +400C, -200C, etc
    ) {
        public static final CatalystItemData EMPTY = new CatalystItemData("none", 0, 0, 0);
    }

    // Mapa para guardar os dados dos 12 catalisadores T1-T3
    private static final Map<String, CatalystItemData> CATALYST_ITEM_DATA = new HashMap<>();

    static {
        // IDs dos seus 12 itens
        // Matterflow
        CATALYST_ITEM_DATA.put("ufo:matterflow_catalyst_t1", new CatalystItemData("matterflow", 1, 1.0, +50));
        CATALYST_ITEM_DATA.put("ufo:matterflow_catalyst_t2", new CatalystItemData("matterflow", 2, 2.5, +100));
        CATALYST_ITEM_DATA.put("ufo:matterflow_catalyst_t3", new CatalystItemData("matterflow", 3, 5.0, +200));

        // Chrono
        CATALYST_ITEM_DATA.put("ufo:chrono_catalyst_t1", new CatalystItemData("chrono", 1, 1.0, +100));
        CATALYST_ITEM_DATA.put("ufo:chrono_catalyst_t2", new CatalystItemData("chrono", 2, 2.5, +250));
        CATALYST_ITEM_DATA.put("ufo:chrono_catalyst_t3", new CatalystItemData("chrono", 3, 5.0, +400));

        // Overflux
        CATALYST_ITEM_DATA.put("ufo:overflux_catalyst_t1", new CatalystItemData("overflux", 1, 1.0, -50));
        CATALYST_ITEM_DATA.put("ufo:overflux_catalyst_t2", new CatalystItemData("overflux", 2, 2.5, -100));
        CATALYST_ITEM_DATA.put("ufo:overflux_catalyst_t3", new CatalystItemData("overflux", 3, 5.0, -200));

        // Quantum
        CATALYST_ITEM_DATA.put("ufo:quantum_catalyst_t1", new CatalystItemData("quantum", 1, 1.0, +75));
        CATALYST_ITEM_DATA.put("ufo:quantum_catalyst_t2", new CatalystItemData("quantum", 2, 2.5, +150));
        CATALYST_ITEM_DATA.put("ufo:quantum_catalyst_t3", new CatalystItemData("quantum", 3, 5.0, +300));
    }

    /**
     * Cache para armazenar os modificadores TOTAIS (com soft cap)
     */
    public record ActiveModifiers(
            double totalEnergyMod,
            double totalSpeedMod,
            double totalFailureMod,
            double totalDropChance,
            double coolantEfficiencyMod,
            boolean hasQuantum,
            boolean isCreative // Flag para o modo Criativo
    ) {
        // Stats Padrão (sem catalisadores)
        public static final ActiveModifiers NONE = new ActiveModifiers(1.0, 1.0, 1.0, 0.0, 1.0, false, false);
        // Stats do Modo Criativo
        public static final ActiveModifiers CREATIVE = new ActiveModifiers(0.0, Double.MAX_VALUE, 0.0, 1.0, 1.0, false, true);
    }

    private ActiveModifiers activeModifiers = ActiveModifiers.NONE;

    public ActiveModifiers getActiveModifiers() {
        return this.activeModifiers;
    }

    /**
     * Retorna o multiplicador do "Soft Cap" baseado na contagem
     */
    private double getSoftCapMultiplier(int count) {
        return switch (count) {
            case 1 -> 1.00; // 100%
            case 2 -> 1.75; // 175%
            case 3 -> 2.25; // 225%
            case 4 -> 2.50; // 250%
            default -> 0.0;
        };
    }

    /**
     * Calcula TODOS os modificadores de uma vez. Chamado a cada tick.
     */
    public void calculateAllModifiers() {
        Map<String, Integer> familyCounts = new HashMap<>();
        Map<String, Double> weightedTierSum = new HashMap<>();
        Set<String> activeFamilies = new HashSet<>();
        boolean allAreT3 = true; // Assumir que sim até achar um que não seja
        int totalCatalysts = 0;

        for (int i = 0; i < blockEntity.upgradeHandler.getSlots(); i++) {
            ItemStack stack = blockEntity.upgradeHandler.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

            // 1. Checar por Modo Criativo
            if (itemId.equals("ufo:dimensional_catalyst")) {
                this.activeModifiers = ActiveModifiers.CREATIVE;
                return;
            }

            // 2. Coletar dados de Tiers e Stacks
            CatalystItemData data = CATALYST_ITEM_DATA.get(itemId);
            if (data != null && data != CatalystItemData.EMPTY) {
                totalCatalysts++;
                if (data.tier() != 3) allAreT3 = false;

                String family = data.family();
                activeFamilies.add(family);
                familyCounts.merge(family, 1, Integer::sum);
                weightedTierSum.merge(family, data.tierMultiplier(), Double::sum);
            }
        }

        if (totalCatalysts == 0) {
            this.activeModifiers = ActiveModifiers.NONE;
            return;
        }

        // 3. Calcular Buffs (com Média Ponderada de Tier e Soft Cap)
        double energy = 1.0;
        double speed = 1.0;
        double failure = 1.0;
        double drop = 0.0;
        double coolantMod = 1.0;

        for (String family : activeFamilies) {
            int count = Math.min(familyCounts.get(family), 4); // Limite de 4 por tipo
            if (count == 0) continue;

            double avgTierMulti = weightedTierSum.get(family) / familyCounts.get(family); // Média ponderada
            double stackSoftCapMulti = getSoftCapMultiplier(count); // Multiplicador do stack
            double finalMulti = avgTierMulti * stackSoftCapMulti;

            switch (family) {
                case "matterflow" -> energy += (MATTERFLOW_BASE_ENERGY * finalMulti);
                case "chrono" -> {
                    speed += (CHRONO_BASE_SPEED * finalMulti);
                    coolantMod -= 0.10; // Penalidade do Chrono
                }
                case "overflux" -> {
                    failure += (OVERFLUX_BASE_FAILURE * finalMulti);
                    coolantMod += 0.15; // Bônus do Overflux
                }
                case "quantum" -> drop += (QUANTUM_BASE_DROP * finalMulti);
            }
        }


        boolean hasMatterflow = activeFamilies.contains("matterflow");
        boolean hasChrono = activeFamilies.contains("chrono");
        boolean hasOverflux = activeFamilies.contains("overflux");
        boolean hasQuantum = activeFamilies.contains("quantum");

        if (hasMatterflow && hasChrono) { speed += 0.10; energy -= 0.05; }
        if (hasOverflux && hasQuantum) { failure -= 0.10; drop += 0.10; }
        if (hasChrono && hasQuantum) { speed += 0.20; energy += 0.10; }
        if (hasMatterflow && hasOverflux) { energy -= 0.15; /* PDF v3 não menciona -10% calor */ }

        // Bônus "Todos os 4 T3"
        if (hasMatterflow && hasChrono && hasOverflux && hasQuantum && allAreT3) {
            energy *= (1.0 - 0.25);
            speed *= 1.25;
            failure *= (1.0 - 0.25);
            drop *= 1.25;
            // (Buffs de 25% aplicados multiplicativamente)
        }

        // 5. Salvar no cache
        this.activeModifiers = new ActiveModifiers(
                Math.max(0.01, energy),
                Math.max(0.1, speed),
                Math.max(0.0, failure),
                Math.max(0.0, drop),
                Math.max(0.1, coolantMod), // Eficiência do coolant
                hasQuantum,
                false
        );
    }

    /**
     * Aplica o "Impacto Térmico" de burst APÓS um craft ser concluído.
     * Chamado pelo BlockEntity.
     */
    public void applyPostCraftHeat() {
        if (this.activeModifiers.isCreative()) {
            this.temperature = AMBIENT_TEMP_K; // Modo criativo zera o calor
            return;
        }

        int staticHeatAdd = 0;
        boolean hasQuantum = false;

        for (int i = 0; i < blockEntity.upgradeHandler.getSlots(); i++) {
            ItemStack stack = blockEntity.upgradeHandler.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            CatalystItemData data = CATALYST_ITEM_DATA.get(itemId);

            if (data != null && data != CatalystItemData.EMPTY) {
                // Soma o calor fixo de cada catalisador
                staticHeatAdd += data.staticHeat();
                if (data.family().equals("quantum")) {
                    hasQuantum = true;
                }
            }
        }

        // Aplica o pico de calor aleatório do Quantum
        if (hasQuantum) {
            int randomSpike = blockEntity.getLevel().random.nextInt(-100, 101);
            staticHeatAdd += randomSpike;
        }

        // Aplica o calor total da operação
        this.temperature += staticHeatAdd;
    }


    // =================================================================================
    // --- LÓGICA DE TICK E RESFRIAMENTO (ATUALIZADA) ---
    // =================================================================================

    public void tick(Level level, BlockPos pos, BlockState state, boolean isActive, @Nullable DimensionalMatterAssemblerRecipe recipe) {
        if (level.isClientSide) return;

        // 1. Recalcula os bônus CADA tick (caso um item mude)
        this.calculateAllModifiers();

        // 2. Se for criativo, travar a temperatura no ambiente
        if (this.activeModifiers.isCreative()) {
            this.temperature = AMBIENT_TEMP_K;
            this.meltdownTicks = 0; // Zera o timer
            this.lastAnnouncedZone = TemperatureZone.SAFE;
            return;
        }

        // 3. Aplicar resfriamento passivo ou ativo
        int cooling = applyCooling(); // <-- Lógica de ineficiência aplicada aqui
        int netChange = -cooling; // Resfriamento é a base

        // 4. Aplicar aquecimento (baseado na Zona de Temperatura)
        // (O calor principal agora vem do 'applyPostCraftHeat')
        // (Este 'tick' agora lida principalmente com o resfriamento)

        this.temperature += netChange;

        // 5. Resfriamento passivo (se não houver coolant)
        if (cooling == 0 && this.temperature > AMBIENT_TEMP_K) {
            this.temperature--; // Perde 1K/tick para o ambiente
        }

        // 6. Garante que a temperatura não caia abaixo do ambiente
        if (this.temperature < AMBIENT_TEMP_K) {
            this.temperature = AMBIENT_TEMP_K;
        }

        // 7. CORREÇÃO: Lógica do Timer de Meltdown
        if (getZone() == TemperatureZone.MELTDOWN) {
            this.meltdownTicks++; // Incrementa o timer
        } else {
            this.meltdownTicks = 0; // Reseta o timer se esfriar
        }

        // 8. Checar Meltdown (A explosão real, agora com delay)
        checkTemperatureEvents(level, pos);
    }

    /**
     * Aplica resfriamento baseado no coolant no tanque.
     * AGORA INCLUI A PENALIDADE DE INEFICIÊNCIA.
     */
    private int applyCooling() {
        if (this.temperature <= AMBIENT_TEMP_K) return 0;

        FluidStack coolant = this.blockEntity.coolantInputTank.getFluid();
        if (coolant.isEmpty() || coolant.getAmount() < COOLANT_CONSUMPTION_PER_TICK) {
            return 0;
        }

        Fluid fluid = coolant.getFluid();
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid);
        String fluidName = fluidId.getPath();

        int baseCooling = 0;

        // Valores baseados no PDF
        switch (fluidName) {
            case "temporal_fluid" -> baseCooling = 20;
            case "spatial_fluid" -> baseCooling = 35;
            case "liquid_starlight" -> baseCooling = 30;
            case "gelid_cryotheum" -> baseCooling = 60;
            case "transcending_matter_fluid" -> baseCooling = 100;
        }
        if (baseCooling == 0) return 0; // Não é um coolant válido

        // Aplica modificadores do Chrono/Overflux
        int finalCooling = (int) (baseCooling * this.activeModifiers.coolantEfficiencyMod());

        // --- NOVA MECÂNICA: Ineficiência ---
        // Se estiver na zona INEFFICIENCY ou pior, perde 50%
        if (getZone().ordinal() >= TemperatureZone.INEFFICIENCY.ordinal()) {
            finalCooling *= 0.5;
        }

        // Consome o coolant
        this.blockEntity.coolantInputTank.drain(COOLANT_CONSUMPTION_PER_TICK, IFluidHandler.FluidAction.EXECUTE);

        return finalCooling;
    }

    /**
     * Verifica se a temperatura atual dispara algum evento especial.
     */
    private void checkTemperatureEvents(Level level, BlockPos pos) {
        // CORREÇÃO: Checa se a zona é MELTDOWN e se o timer passou de 10 segundos
        if (getZone() == TemperatureZone.MELTDOWN && this.meltdownTicks > MELTDOWN_FUSE_TICKS) {
            handleMeltdown(level, pos);
            this.meltdownTicks = 0; // Reseta para não explodir de novo
        }
    }

    /**
     * Lida com a explosão (agora checa o config)
     */
    private void handleMeltdown(Level level, BlockPos pos) {
        if (!level.isClientSide) {

            // TODO: Substitua 'true' pela sua checagem de config
            // Ex: boolean canExplode = UFOConfig.COMMON.enableDmaMeltdown.get();
            boolean canExplode = true;

            if (canExplode) {
                level.destroyBlock(pos, false);
                // CORREÇÃO: Aumenta o raio da explosão para 8.0f
                level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8.0f, Level.ExplosionInteraction.BLOCK); // Explosão forte
            }
        }
    }

    /**
     * Chamado pelo BlockEntity ANTES de craftar.
     * ATUALIZADO com novas chances de falha.
     */
    public boolean shouldCraftFail() {
        if (this.activeModifiers.isCreative()) return false; // Criativo nunca falha

        Level level = this.blockEntity.getLevel();
        if (level == null || level.isClientSide) return false;

        // 1. Chance de falha da Zona de Temperatura (Novos valores)
        double baseFailureChance = 0.0;
        switch (getZone()) {
            case OVERHEAT -> baseFailureChance = 0.05; // 5%
            case INEFFICIENCY -> baseFailureChance = 0.15; // 15%
            case DESTABILIZATION -> baseFailureChance = 0.40; // 40% (Destabilização)
            case MELTDOWN -> baseFailureChance = 1.0; // 100% (Falha antes de explodir)
        }
        if (baseFailureChance == 0.0) return false;

        // 2. Aplica modificador do Overflux/Sinergias
        double finalChance = baseFailureChance * this.activeModifiers.totalFailureMod();

        return level.random.nextDouble() < finalChance;
    }

    /**
     * Chamado pelo BlockEntity DEPOIS de uma falha.
     */
    public void handleCraftFailure(Level level, BlockPos pos, DimensionalMatterAssemblerRecipe recipe) {
        // (Lógica de falha dos PDFs anteriores, pois este não especifica)
        // (Ex: explosão pequena, perda de item, etc.)
        if (level.isClientSide) return;

        TemperatureZone zone = getZone();
        // Aumenta as explosões de falha
        if (zone == TemperatureZone.DESTABILIZATION && level.random.nextFloat() < 0.25f) {
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2.5f, Level.ExplosionInteraction.NONE);
        } else if (zone == TemperatureZone.INEFFICIENCY && level.random.nextFloat() < 0.1f) {
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.5f, Level.ExplosionInteraction.NONE);
        }
    }


    // --- HELPERS DE UNIDADE ---

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperatureK) {
        this.temperature = temperatureK;
    }

    /** Retorna a temperatura atual em Celsius (para cálculos de lógica) */
    public int getTemperatureCelsius() {
        // Converte de K para °C
        return this.temperature - AMBIENT_TEMP_K;
    }

    /** Converte um valor de Celsius para Kelvin (para armazenar) */
    private int convertCelsiusToKelvin(int celsius) {
        return celsius + AMBIENT_TEMP_K;
    }

    public TemperatureZone getZone() {
        return TemperatureZone.getZone(getTemperatureCelsius());
    }

    // --- Getters/Setters para o BE rastrear mudanças ---
    public TemperatureZone getLastAnnouncedZone() {
        return lastAnnouncedZone;
    }

    public void setLastAnnouncedZone(TemperatureZone zone) {
        this.lastAnnouncedZone = zone;
    }

    // --- CORREÇÃO: Getters/Setters para o timer de meltdown ---
    public int getMeltdownTicks() {
        return this.meltdownTicks;
    }

    public void setMeltdownTicks(int ticks) {
        this.meltdownTicks = ticks;
    }


    // --- SAVE/LOAD NBT ---

    public void saveNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("dma_temperature", this.temperature);
        // Salva a última zona para evitar spam de chat ao carregar
        tag.putInt("dma_last_zone", this.lastAnnouncedZone.ordinal());
        // CORREÇÃO: Salva o timer de meltdown
        tag.putInt("dma_meltdown_ticks", this.meltdownTicks);
    }

    public void loadNBT(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("dma_temperature")) {
            this.temperature = tag.getInt("dma_temperature");
            if (this.temperature < AMBIENT_TEMP_K) this.temperature = AMBIENT_TEMP_K;
        } else if (tag.contains("temperature")) { // Carrega do NBT antigo
            this.temperature = tag.getInt("temperature");
            if (this.temperature < AMBIENT_TEMP_K) this.temperature = AMBIENT_TEMP_K;
        } else {
            this.temperature = AMBIENT_TEMP_K;
        }

        // CORREÇÃO: Carrega o timer de meltdown
        this.meltdownTicks = tag.getInt("dma_meltdown_ticks");

        if (tag.contains("dma_last_zone")) {
            this.lastAnnouncedZone = TemperatureZone.values()[tag.getInt("dma_last_zone")];
        } else {
            this.lastAnnouncedZone = getZone(); // Sincroniza ao carregar
        }
    }
}