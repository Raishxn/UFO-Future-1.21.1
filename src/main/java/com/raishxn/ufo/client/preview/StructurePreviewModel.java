package com.raishxn.ufo.client.preview;

import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinitions;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/** Immutable client model shared by structure viewers and future build planning. */
public record StructurePreviewModel(ResourceLocation id, Component title, int sizeX, int sizeY, int sizeZ,
                                    List<Cell> cells, List<Material> materials, List<ItemStack> focusStacks) {
    public StructurePreviewModel {
        cells = List.copyOf(cells);
        materials = List.copyOf(materials);
        focusStacks = focusStacks.stream().map(ItemStack::copy).toList();
    }

    public static StructurePreviewModel from(MultiblockControllerDefinitions.PreviewEntry entry) {
        var definition = entry.definition();
        MultiblockPattern pattern = definition.pattern();
        char[][][] template = pattern.getPattern();
        int sizeY = template.length;
        int sizeZ = sizeY == 0 ? 0 : template[0].length;
        int sizeX = sizeZ == 0 ? 0 : template[0][0].length;
        BlockState controllerState = entry.iconStack().getItem() instanceof BlockItem blockItem
                ? blockItem.getBlock().defaultBlockState() : Blocks.IRON_BLOCK.defaultBlockState();
        List<Cell> cells = new ArrayList<>();
        Map<String, Material> materials = new LinkedHashMap<>();
        LinkedHashSet<net.minecraft.world.level.block.Block> focusBlocks = new LinkedHashSet<>();
        for (int y = 0; y < sizeY; y++) for (int z = 0; z < sizeZ; z++) for (int x = 0; x < sizeX; x++) {
            char symbol = template[y][z][x];
            BlockState state = symbol == pattern.getControllerChar() ? controllerState
                    : definition.defaultCreativeStates().get(symbol);
            if (state == null) state = Blocks.AIR.defaultBlockState();
            List<BlockState> alternatives = pattern.getDisplayCandidates(symbol);
            if (alternatives.isEmpty()) alternatives = List.of(state);
            boolean hasVisibleAlternative = alternatives.stream().anyMatch(candidate -> !candidate.isAir());
            if (!state.isAir() || hasVisibleAlternative) {
                cells.add(new Cell(new BlockPos(x, y, z), symbol, state, pattern.getLegendName(symbol), alternatives,
                        isStructuralShell(state, symbol, pattern.getControllerChar())));
            }
            if (!state.isAir()) {
                focusBlocks.add(state.getBlock());
                ItemStack stack = state.getBlock().asItem().getDefaultInstance();
                if (!stack.isEmpty()) {
                    String key = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                    materials.compute(key, (ignored, current) -> current == null
                            ? new Material(stack.copyWithCount(1), 1)
                            : new Material(current.stack(), current.count() + 1));
                }
            }
            alternatives.stream().filter(candidate -> !candidate.isAir())
                    .forEach(candidate -> focusBlocks.add(candidate.getBlock()));
        }
        List<Material> sorted = materials.values().stream()
                .sorted(Comparator.comparingInt(Material::count).reversed()).toList();
        List<ItemStack> focus = focusBlocks.stream().map(block -> block.asItem().getDefaultInstance())
                .filter(stack -> !stack.isEmpty()).toList();
        return new StructurePreviewModel(entry.id(), definition.name(), sizeX, sizeY, sizeZ, cells, sorted, focus);
    }

    /** Matches AE2LT's semantic shell flag instead of treating a sparse template's volume boundary as its shell. */
    private static boolean isStructuralShell(BlockState state, char symbol, char controllerSymbol) {
        if (state.isAir() || symbol == controllerSymbol) return false;
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return path.contains("casing") || path.contains("glass") || path.contains("frame")
                || path.contains("structure") || path.contains("reinforced_alloy");
    }

    public record Cell(BlockPos localPos, char symbol, BlockState state, Component role,
                       List<BlockState> alternatives, boolean shell) {
        public Cell { alternatives = List.copyOf(alternatives); }
    }

    public record Material(ItemStack stack, int count) {
        public Material { stack = stack.copy(); }
    }
}
