package com.raishxn.ufo.datagen;

import com.google.common.hash.Hashing;
import com.raishxn.ufo.api.multiblock.MultiblockDefinition;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.pattern.QmfPatternFactory;
import com.raishxn.ufo.block.entity.pattern.QpaPatternFactory;
import com.raishxn.ufo.block.entity.pattern.QuantumCryoforgePatternFactory;
import com.raishxn.ufo.block.entity.pattern.QuantumSlicerPatternFactory;
import com.raishxn.ufo.block.entity.pattern.StellarNexusPatternFactory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Generates the interactive GuideME structures from the canonical runtime patterns. */
public final class GuideStructureProvider implements DataProvider {
    private final Path outputRoot;

    public GuideStructureProvider(PackOutput output) {
        this.outputRoot = output.getOutputFolder()
                .resolve("assets/ufo/ae2guide/assets/assemblies");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<GuideStructure> structures = new ArrayList<>();
        structures.add(fromDefinition("quantum_matter_fabricator", QmfPatternFactory.getDefinition(),
                'H', MultiblockBlocks.QUANTUM_MATTER_FABRICATOR_CONTROLLER.get().defaultBlockState()));
        structures.add(fromDefinition("quantum_slicer", QuantumSlicerPatternFactory.getDefinition(),
                'H', MultiblockBlocks.QUANTUM_SLICER_CONTROLLER.get().defaultBlockState()));
        structures.add(fromDefinition("quantum_processor_assembler", QpaPatternFactory.getDefinition(),
                'H', MultiblockBlocks.QUANTUM_PROCESSOR_ASSEMBLER_CONTROLLER.get().defaultBlockState()));
        structures.add(fromDefinition("quantum_cryoforge", QuantumCryoforgePatternFactory.getDefinition(),
                'C', MultiblockBlocks.QUANTUM_CRYOFORGE_CONTROLLER.get().defaultBlockState()));

        Map<Character, BlockState> stellarStates = new LinkedHashMap<>(StellarNexusPatternFactory.getDefaultCreativeStates());
        stellarStates.put('H', MultiblockBlocks.STELLAR_NEXUS_CONTROLLER.get().defaultBlockState());
        structures.add(new GuideStructure("stellar_nexus", StellarNexusPatternFactory.getPattern(), stellarStates));

        return CompletableFuture.allOf(structures.stream()
                .map(structure -> write(cache, structure))
                .toArray(CompletableFuture[]::new));
    }

    private static GuideStructure fromDefinition(String name, MultiblockDefinition definition,
                                                  char controllerSymbol, BlockState controllerState) {
        Map<Character, BlockState> states = new LinkedHashMap<>(definition.defaultCreativeStates());
        states.put(controllerSymbol, controllerState);
        if (definition.roles().containsKey('P')) {
            states.put('P', MultiblockBlocks.QUANTUM_PATTERN_BUFFER.get().defaultBlockState());
        }
        return new GuideStructure(name, definition.pattern(), states);
    }

    private CompletableFuture<?> write(CachedOutput cache, GuideStructure structure) {
        CompoundTag root = toStructureTag(structure.pattern(), structure.states());
        String snbt = new SnbtPrinterTagVisitor().visit(root) + "\n";
        try {
            // Exercise the exact conversion invoked by ImportStructureElementCompiler.
            // A data entry absent from palette must fail datagen instead of becoming
            // a red error box that is discovered only inside the guide UI.
            NbtUtils.snbtToStructure(snbt);
        } catch (Exception exception) {
            throw new IllegalStateException("GuideME rejected generated structure " + structure.name(), exception);
        }
        byte[] bytes = snbt.getBytes(StandardCharsets.UTF_8);
        Path path = outputRoot.resolve(structure.name() + ".snbt");
        return CompletableFuture.runAsync(() -> {
            try {
                cache.writeIfNeeded(path, bytes, Hashing.sha256().hashBytes(bytes));
            } catch (IOException exception) {
                throw new IllegalStateException("Could not write GuideME structure " + path, exception);
            }
        });
    }

    static CompoundTag toStructureTag(MultiblockPattern pattern, Map<Character, BlockState> states) {
        char[][][] cells = pattern.getPattern();
        int sizeX = 0;
        int sizeZ = 0;
        ListTag data = new ListTag();
        Set<String> paletteStates = new LinkedHashSet<>();

        for (int y = 0; y < cells.length; y++) {
            sizeZ = Math.max(sizeZ, cells[y].length);
            for (int z = 0; z < cells[y].length; z++) {
                sizeX = Math.max(sizeX, cells[y][z].length);
                for (int x = 0; x < cells[y][z].length; x++) {
                    BlockState state = states.get(cells[y][z][x]);
                    if (state == null) continue;
                    CompoundTag entry = new CompoundTag();
                    entry.put("pos", intList(x, y, z));
                    // NbtUtils.snbtToStructure consumes the textual structure format
                    // used by GuideME. Registry ids are sufficient for guide scenes;
                    // omitting runtime-only properties also prevents active controller
                    // visuals and directional hatch state from leaking into the preview.
                    String serializedState = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                    entry.putString("state", serializedState);
                    paletteStates.add(serializedState);
                    data.add(entry);
                }
            }
        }

        CompoundTag root = new CompoundTag();
        root.putInt("DataVersion", 3955);
        root.put("size", intList(sizeX, cells.length, sizeZ));
        root.put("data", data);
        root.put("entities", new ListTag());
        ListTag palette = new ListTag();
        paletteStates.forEach(state -> palette.add(StringTag.valueOf(state)));
        root.put("palette", palette);
        return root;
    }

    private static ListTag intList(int x, int y, int z) {
        ListTag result = new ListTag();
        result.add(IntTag.valueOf(x));
        result.add(IntTag.valueOf(y));
        result.add(IntTag.valueOf(z));
        return result;
    }

    @Override
    public String getName() {
        return "UFO GuideME multiblock structures";
    }

    private record GuideStructure(String name, MultiblockPattern pattern, Map<Character, BlockState> states) {
    }
}
