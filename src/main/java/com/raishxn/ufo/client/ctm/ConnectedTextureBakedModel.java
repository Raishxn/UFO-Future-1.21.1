package com.raishxn.ufo.client.ctm;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class ConnectedTextureBakedModel implements IDynamicBakedModel {
    private static final ModelProperty<CtmConnectionState> CONNECTIONS = new ModelProperty<>();
    private static final Direction[] DIRECTIONS = Direction.values();

    private final TextureAtlasSprite baseSprite;
    private final TextureAtlasSprite ctmSprite;
    private final ConnectionPredicate predicate;
    private final ChunkRenderTypeSet renderTypes;
    private final boolean ambientOcclusion;
    private final boolean gui3d;
    private final boolean usesBlockLight;
    private final Map<Integer, List<BakedQuad>> quadCache = new ConcurrentHashMap<>();

    ConnectedTextureBakedModel(TextureAtlasSprite baseSprite, TextureAtlasSprite ctmSprite,
            ConnectionPredicate predicate, ChunkRenderTypeSet renderTypes,
            boolean ambientOcclusion, boolean gui3d, boolean usesBlockLight) {
        this.baseSprite = baseSprite;
        this.ctmSprite = ctmSprite;
        this.predicate = predicate;
        this.renderTypes = renderTypes;
        this.ambientOcclusion = ambientOcclusion;
        this.gui3d = gui3d;
        this.usesBlockLight = usesBlockLight;
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        boolean[] culled = new boolean[DIRECTIONS.length];
        int[] edges = new int[DIRECTIONS.length];
        int[] corners = new int[DIRECTIONS.length];
        for (Direction face : DIRECTIONS) {
            int faceIndex = face.get3DDataValue();
            culled[faceIndex] = predicate.connects(level, pos, state, face);
            for (int edge = 0; edge < 4; edge++) {
                if (predicate.connects(level, pos, state, CtmFaceGeometry.neighbourDirection(face, edge))) {
                    edges[faceIndex] |= 1 << edge;
                }
            }
            for (CtmTileSelector.Quadrant quadrant : CtmTileSelector.Quadrant.values()) {
                if (predicate.connects(level, pos, state,
                        CtmFaceGeometry.cornerPosition(pos, face, quadrant))) {
                    corners[faceIndex] |= 1 << quadrant.ordinal();
                }
            }
        }
        return modelData.derive().with(CONNECTIONS, new CtmConnectionState(culled, edges, corners)).build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random,
            ModelData modelData, @Nullable RenderType renderType) {
        if (side == null) {
            return List.of();
        }
        CtmConnectionState connections = modelData.get(CONNECTIONS);
        if (connections == null) {
            return List.of(CtmFaceGeometry.fullFace(side, baseSprite));
        }
        if (connections.culled(side)) {
            return List.of();
        }
        int edges = connections.edges(side);
        int corners = connections.corners(side);
        int key = side.get3DDataValue() << 8 | edges << 4 | corners;
        return quadCache.computeIfAbsent(key, ignored -> buildConnectedFace(side, edges, corners));
    }

    private List<BakedQuad> buildConnectedFace(Direction side, int edges, int corners) {
        List<BakedQuad> quads = new ArrayList<>(4);
        for (int horizontal = 0; horizontal < 2; horizontal++) {
            for (int vertical = 0; vertical < 2; vertical++) {
                CtmTileSelector.Tile tile = CtmTileSelector.select(
                        CtmTileSelector.quadrant(horizontal, vertical), edges, corners);
                TextureAtlasSprite sprite = tile.source() == CtmTileSelector.Source.BASE ? baseSprite : ctmSprite;
                quads.add(CtmFaceGeometry.quadrant(side, horizontal, vertical, tile, sprite));
            }
        }
        return List.copyOf(quads);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
        return renderTypes;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return ambientOcclusion;
    }

    @Override
    public boolean isGui3d() {
        return gui3d;
    }

    @Override
    public boolean usesBlockLight() {
        return usesBlockLight;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return baseSprite;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
