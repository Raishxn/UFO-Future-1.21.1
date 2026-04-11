package com.raishxn.ufo.compat.jei;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SceneWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinition;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinitions;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiblockPreviewWidget extends WidgetGroup {

    private static final int REGION_SIZE = 128;
    private static int nextRegionIndex = 0;
    private static TrackedDummyWorld PREVIEW_LEVEL;
    private final MultiblockControllerDefinitions.PreviewEntry entry;
    private final SceneWidget sceneWidget;
    private final SimpleContainer candidateItems = new SimpleContainer(3);
    private final List<BlockPos> allPositions = new ArrayList<>();
    private final List<Integer> layers = new ArrayList<>();
    private final Map<BlockPos, Character> symbolByPos = new HashMap<>();
    private final BlockPos origin;

    private boolean initializedScene;
    private int currentLayer = -1;
    private String selectedLabel = "Click a field slot to see variants";

    public MultiblockPreviewWidget(MultiblockControllerDefinitions.PreviewEntry entry) {
        super(0, 0, 184, 184);
        setClientSideWidget();
        this.entry = entry;
        ensurePreviewLevel();

        this.origin = allocateRegion();

        addWidget(new ImageWidget(4, 4, 176, 12,
                new TextTexture(() -> entry.definition().name().getString())
                        .setDropShadow(true)
                        .setWidth(176)));

        this.sceneWidget = new SceneWidget(4, 20, 176, 118, PREVIEW_LEVEL)
                .setHoverTips(true)
                .setRenderFacing(false)
                .setRenderSelect(true)
                .setOnSelected((pos, facing) -> updateCandidates(pos));
        this.sceneWidget.setBackground(new ColorRectTexture(0xFF151A24));
        addWidget(sceneWidget);

        addWidget(new ButtonWidget(132, 142, 22, 16,
                new GuiTextureGroup(
                        new ColorRectTexture(0xFF2A3140),
                        new TextTexture("ALL").setDropShadow(true)),
                click -> {
                    currentLayer = -1;
                    applyLayer();
                }).setHoverBorderTexture(1, 0xFFFFFFFF)
                .setHoverTooltips(Component.literal("Show all layers")));

        addWidget(new ButtonWidget(156, 142, 24, 16,
                new GuiTextureGroup(
                        new ColorRectTexture(0xFF2A3140),
                        new TextTexture(() -> currentLayer < 0 ? "L:*" : "L:" + currentLayer).setDropShadow(true)),
                click -> cycleLayer()).setHoverBorderTexture(1, 0xFFFFFFFF)
                .setHoverTooltips(Component.literal("Cycle visible layer")));

        addWidget(new ImageWidget(4, 142, 124, 16,
                new TextTexture(() -> selectedLabel)
                        .setDropShadow(true)
                        .setWidth(124)));

        addWidget(new ImageWidget(4, 160, 176, 20, new ColorRectTexture(0x66131A26)));
        for (int i = 0; i < 3; i++) {
            SlotWidget slot = new SlotWidget(candidateItems, i, 66 + (i * 18), 162, false, false)
                    .setIngredientIO(IngredientIO.INPUT)
                    .setBackgroundTexture(new ColorRectTexture(0xAA2C3444));
            addWidget(slot);
        }

        initializeScene();
    }

    private static BlockPos allocateRegion() {
        int index = nextRegionIndex++;
        return new BlockPos((index % 8) * REGION_SIZE, 64, (index / 8) * REGION_SIZE);
    }

    private static void ensurePreviewLevel() {
        if (PREVIEW_LEVEL == null) {
            if (Minecraft.getInstance().level == null) {
                throw new IllegalStateException("JEI multiblock preview initialized before client level load");
            }
            PREVIEW_LEVEL = new TrackedDummyWorld();
        }
    }

    private void initializeScene() {
        MultiblockControllerDefinition definition = entry.definition();
        MultiblockPattern pattern = definition.pattern();
        char[][][] chars = pattern.getPattern();
        BlockState controllerState = resolveControllerState(entry.iconStack());

        Map<BlockPos, BlockInfo> blockMap = new HashMap<>();
        for (int y = 0; y < chars.length; y++) {
            for (int z = 0; z < chars[y].length; z++) {
                for (int x = 0; x < chars[y][z].length; x++) {
                    char symbol = chars[y][z][x];
                    BlockState state = symbol == pattern.getControllerChar()
                            ? controllerState
                            : definition.defaultCreativeStates().get(symbol);
                    if (state == null || state.isAir()) {
                        continue;
                    }
                    state = adaptPreviewState(state);

                    BlockPos pos = origin.offset(x, y, z);
                    blockMap.put(pos, BlockInfo.fromBlockState(state));
                    allPositions.add(pos);
                    symbolByPos.put(pos, symbol);
                    layers.add(pos.getY());
                }
            }
        }

        PREVIEW_LEVEL.addBlocks(blockMap);
        layers.stream().distinct().sorted().forEachOrdered(y -> {
        });
        layers.sort(Comparator.naturalOrder());
    }

    private static BlockState resolveControllerState(ItemStack iconStack) {
        if (iconStack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState();
        }
        return Blocks.IRON_BLOCK.defaultBlockState();
    }

    private static BlockState adaptPreviewState(BlockState state) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (key != null && "ae2".equals(key.getNamespace()) && "quartz_vibrant_glass".equals(key.getPath())) {
            return Blocks.GLASS.defaultBlockState();
        }
        return state;
    }

    private void cycleLayer() {
        List<Integer> uniqueLayers = layers.stream().distinct().sorted().toList();
        if (uniqueLayers.isEmpty()) {
            currentLayer = -1;
        } else if (currentLayer < 0) {
            currentLayer = 0;
        } else {
            currentLayer++;
            if (currentLayer >= uniqueLayers.size()) {
                currentLayer = -1;
            }
        }
        applyLayer();
    }

    private void applyLayer() {
        List<Integer> uniqueLayers = layers.stream().distinct().sorted().toList();
        if (currentLayer < 0 || currentLayer >= uniqueLayers.size()) {
            sceneWidget.setRenderedCore(allPositions);
            return;
        }

        int worldY = uniqueLayers.get(currentLayer);
        List<BlockPos> layerPositions = allPositions.stream()
                .filter(pos -> pos.getY() == worldY)
                .toList();
        sceneWidget.setRenderedCore(layerPositions);
    }

    private void updateCandidates(BlockPos pos) {
        char symbol = symbolByPos.getOrDefault(pos, '\0');
        List<BlockState> candidates = entry.definition().pattern().getDisplayCandidates(symbol);

        for (int i = 0; i < candidateItems.getContainerSize(); i++) {
            candidateItems.setItem(i, ItemStack.EMPTY);
        }

        if (candidates.isEmpty()) {
            selectedLabel = "Fixed block";
            return;
        }

        selectedLabel = "Allowed variants";
        for (int i = 0; i < Math.min(candidateItems.getContainerSize(), candidates.size()); i++) {
            Block block = candidates.get(i).getBlock();
            candidateItems.setItem(i, new ItemStack(block));
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (!initializedScene) {
            sceneWidget.setRenderedCore(allPositions);
            sceneWidget.setCameraYawAndPitch(-45f, 22f);
            applyLayer();
            initializedScene = true;
        }
    }
}
