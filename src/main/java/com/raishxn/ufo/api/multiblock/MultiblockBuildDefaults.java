package com.raishxn.ufo.api.multiblock;

import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.AbstractParallelMultiblockControllerBE;
import com.raishxn.ufo.block.entity.StellarNexusControllerBE;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Service parts that a complete default build must include in addition to the pattern shell. */
public final class MultiblockBuildDefaults {
    private MultiblockBuildDefaults() { }

    public static List<Block> requiredHatches(BlockEntity controller) {
        if (controller instanceof StellarNexusControllerBE) return stellarHatches();
        if (controller instanceof AbstractParallelMultiblockControllerBE) return parallelHatches();
        return List.of();
    }

    public static List<Block> requiredHatches(ResourceLocation id) {
        return switch (id.getPath()) {
            case "stellar_nexus" -> stellarHatches();
            case "quantum_matter_fabricator", "quantum_slicer", "quantum_processor_assembler",
                    "quantum_cryoforge" -> parallelHatches();
            default -> List.of();
        };
    }

    private static List<Block> stellarHatches() {
        return List.of(MultiblockBlocks.ME_MASSIVE_INPUT_HATCH.get(),
                MultiblockBlocks.ME_MASSIVE_OUTPUT_HATCH.get(),
                MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get(),
                MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get());
    }

    private static List<Block> parallelHatches() {
        return List.of(MultiblockBlocks.QUANTUM_PATTERN_BUFFER.get(),
                MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get(),
                MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get());
    }

    public static boolean isCasing(BlockState state) {
        return state.is(MultiblockBlocks.ENTROPY_SINGULARITY_CASING.get())
                || state.is(MultiblockBlocks.QUANTUM_HYPER_MECHANICAL_CASING.get());
    }

    /** Overrides for the empty-world preview, in the same placement order as auto-build. */
    public static Map<MultiblockAutoBuildPlan.LocalPos, BlockState> previewOverrides(
            MultiblockControllerDefinitions.PreviewEntry entry) {
        List<Block> required = requiredHatches(entry.id());
        if (required.isEmpty()) return Map.of();
        MultiblockPattern pattern = entry.definition().pattern();
        var plan = MultiblockAutoBuildPlan.create(pattern.getPattern(), pattern.getControllerChar(),
                pattern.getControllerCol(), pattern.getControllerRow(),
                entry.definition().defaultCreativeStates(), state -> !state.isAir(),
                (local, symbol, target) -> MultiblockAutoBuildPlan.SlotState.EMPTY);
        Map<MultiblockAutoBuildPlan.LocalPos, BlockState> states = new LinkedHashMap<>();
        for (var placement : plan.placements()) states.put(placement.localPos(), placement.target());
        for (Block hatch : required) {
            if (states.values().stream().anyMatch(state -> state.is(hatch))) continue;
            for (var placement : plan.placements()) {
                BlockState current = states.get(placement.localPos());
                if (isCasing(current) && pattern.getDisplayCandidates(placement.symbol()).stream()
                        .anyMatch(candidate -> candidate.is(hatch))) {
                    states.put(placement.localPos(), hatch.defaultBlockState());
                    break;
                }
            }
        }
        Map<MultiblockAutoBuildPlan.LocalPos, BlockState> overrides = new LinkedHashMap<>();
        for (var placement : plan.placements()) {
            BlockState state = states.get(placement.localPos());
            if (state != placement.target()) overrides.put(placement.localPos(), state);
        }
        return Map.copyOf(overrides);
    }
}
