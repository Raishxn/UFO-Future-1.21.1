package com.raishxn.ufo.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinition;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinitions;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(value = Dist.CLIENT)
public class GhostHologramRenderer {

    private static BlockPos activeControllerPos;
    private static net.minecraft.core.Direction activeFacing;
    private static final List<HologramBlock> cachedBlocks = new ArrayList<>();

    /*
     * The ghost blocks are tessellated once into a GPU buffer and redrawn from it. Re-tessellating
     * every block model each frame (thousands for the Stellar Nexus) dominated the frame time while
     * the hologram was on. The mesh is rebuilt when the set of hidden ghosts changes (a real block
     * was placed or removed), and periodically so baked light and face culling follow the world.
     */
    private static final int VISIBILITY_CHECK_TICKS = 1;
    private static final int MIN_REBUILD_INTERVAL_TICKS = 10;
    private static final int REFRESH_INTERVAL_TICKS = 200;
    private static final double RESORT_DISTANCE_SQR = 1.0D;
    private static final RandomSource MODEL_RANDOM = RandomSource.create();
    @Nullable private static VertexBuffer meshBuffer;
    @Nullable private static MeshData.SortState meshSortState;
    @Nullable private static ClientLevel meshLevel;
    @Nullable private static BitSet shownBlocks;
    @Nullable private static BitSet meshBlocks;
    @Nullable private static Vec3 meshSortCamera;
    private static boolean meshEmpty;
    private static boolean meshDirty = true;
    private static long lastVisibilityTick = Long.MIN_VALUE;
    private static long lastRebuildTick = Long.MIN_VALUE;

    private record HologramBlock(BlockPos pos, BlockState state) {
    }

    public static void toggleHologram(BlockPos pos, net.minecraft.core.Direction facing) {
        if (activeControllerPos != null && activeControllerPos.equals(pos)) {
            activeControllerPos = null;
            activeFacing = null;
            cachedBlocks.clear();
            releaseMesh();
            return;
        }

        activeControllerPos = pos;
        activeFacing = getPatternFacing(pos, facing);
        rebuildCache();
    }

    private static net.minecraft.core.Direction getPatternFacing(BlockPos pos, net.minecraft.core.Direction fallbackFacing) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return fallbackFacing;
        }

        BlockEntity be = mc.level.getBlockEntity(pos);
        BlockState state = mc.level.getBlockState(pos);
        return MultiblockControllerDefinitions.getPatternFacing(be, state);
    }

    public static boolean isActive(BlockPos pos) {
        return activeControllerPos != null && activeControllerPos.equals(pos);
    }

    private static void rebuildCache() {
        cachedBlocks.clear();
        releaseMesh();
        if (activeControllerPos == null || activeFacing == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        Optional<MultiblockControllerDefinition> definitionOpt = getDefinition(mc.level.getBlockEntity(activeControllerPos));
        if (definitionOpt.isEmpty()) {
            return;
        }

        MultiblockControllerDefinition definition = definitionOpt.get();
        MultiblockPattern pattern = definition.pattern();
        Map<Character, BlockState> defaults = definition.defaultCreativeStates();

        char[][][] charPattern = pattern.getPattern();
        int controllerCol = pattern.getControllerCol();
        int controllerRow = pattern.getControllerRow();
        int controllerLayer = pattern.getControllerLayer();

        for (int y = 0; y < charPattern.length; y++) {
            for (int z = 0; z < charPattern[y].length; z++) {
                for (int x = 0; x < charPattern[y][z].length; x++) {
                    char c = charPattern[y][z][x];
                    if (c == ' ' || c == 'A' || c == pattern.getControllerChar()) {
                        continue;
                    }

                    BlockState targetState = defaults.get(c);
                    if (targetState == null) {
                        continue;
                    }

                    int offsetX = x - controllerCol;
                    int offsetY = y - controllerLayer;
                    int offsetZ = z - controllerRow;

                    BlockPos worldPos = getRotatedPos(activeControllerPos, offsetX, offsetY, offsetZ, activeFacing);
                    cachedBlocks.add(new HologramBlock(worldPos, targetState));
                }
            }
        }
    }

    private static Optional<MultiblockControllerDefinition> getDefinition(BlockEntity be) {
        return be == null ? Optional.empty() : MultiblockControllerDefinitions.getDefinition(be);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (activeControllerPos == null || activeFacing == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        if (!MultiblockControllerDefinitions.isSupportedController(mc.level.getBlockState(activeControllerPos))) {
            activeControllerPos = null;
            activeFacing = null;
            cachedBlocks.clear();
            releaseMesh();
            return;
        }

        if (mc.player.distanceToSqr(Vec3.atCenterOf(activeControllerPos)) > 64 * 64) {
            return;
        }

        if (meshLevel != mc.level) {
            releaseMesh();
            meshLevel = mc.level;
        }

        Vec3 cameraPos = event.getCamera().getPosition();
        long gameTime = mc.level.getGameTime();
        updateShownBlocks(mc.level, gameTime);
        if (meshBuffer == null || meshDirty) {
            if (meshBuffer == null || lastRebuildTick == Long.MIN_VALUE || gameTime < lastRebuildTick
                    || gameTime - lastRebuildTick >= MIN_REBUILD_INTERVAL_TICKS) {
                rebuildMesh(mc, cameraPos, gameTime);
            }
        } else if (gameTime - lastRebuildTick >= REFRESH_INTERVAL_TICKS) {
            rebuildMesh(mc, cameraPos, gameTime);
        }
        if (meshBuffer == null || meshEmpty) {
            return;
        }
        resortIfCameraMoved(cameraPos);

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(activeControllerPos.getX() - cameraPos.x,
                activeControllerPos.getY() - cameraPos.y,
                activeControllerPos.getZ() - cameraPos.z);
        // Same transform the immediate path got: the pose applied on the CPU, then the
        // render system's model-view and projection in the shader (see BufferUploader).
        Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(poseStack.last().pose());
        poseStack.popPose();

        RenderType renderType = RenderType.translucent();
        renderType.setupRenderState();
        ShaderInstance shader = RenderSystem.getShader();
        if (shader != null) {
            meshBuffer.bind();
            meshBuffer.drawWithShader(modelView, RenderSystem.getProjectionMatrix(), shader);
            VertexBuffer.unbind();
        }
        renderType.clearRenderState();
    }

    /** A ghost is shown where the world still has air or a fluid, as before; checked per tick, not per frame. */
    private static void updateShownBlocks(ClientLevel level, long gameTime) {
        if (shownBlocks != null && gameTime >= lastVisibilityTick
                && gameTime - lastVisibilityTick < VISIBILITY_CHECK_TICKS) {
            return;
        }
        lastVisibilityTick = gameTime;
        BitSet shown = new BitSet(cachedBlocks.size());
        for (int index = 0; index < cachedBlocks.size(); index++) {
            BlockState existingState = level.getBlockState(cachedBlocks.get(index).pos());
            if (existingState.isAir() || !existingState.getFluidState().isEmpty()) {
                shown.set(index);
            }
        }
        if (!shown.equals(meshBlocks)) {
            meshDirty = true;
        }
        shownBlocks = shown;
    }

    private static void rebuildMesh(Minecraft mc, Vec3 cameraPos, long gameTime) {
        lastRebuildTick = gameTime;
        meshDirty = false;
        BitSet shown = shownBlocks == null ? new BitSet() : shownBlocks;
        meshBlocks = (BitSet) shown.clone();
        BlockPos origin = activeControllerPos;
        try (ByteBufferBuilder vertexBytes = new ByteBufferBuilder(RenderType.translucent().bufferSize());
             ByteBufferBuilder indexBytes = new ByteBufferBuilder(4096)) {
            BufferBuilder builder = new BufferBuilder(vertexBytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            PoseStack poseStack = new PoseStack();
            for (int index = shown.nextSetBit(0); index >= 0; index = shown.nextSetBit(index + 1)) {
                HologramBlock hb = cachedBlocks.get(index);
                poseStack.pushPose();
                poseStack.translate(hb.pos().getX() - origin.getX(), hb.pos().getY() - origin.getY(),
                        hb.pos().getZ() - origin.getZ());
                mc.getBlockRenderer().renderBatched(
                        hb.state(),
                        hb.pos(),
                        mc.level,
                        poseStack,
                        builder,
                        true,
                        MODEL_RANDOM);
                poseStack.popPose();
            }
            MeshData mesh = builder.build();
            if (mesh == null) {
                meshEmpty = true;
                meshSortState = null;
                return;
            }
            meshSortState = mesh.sortQuads(indexBytes, sortingFor(cameraPos));
            meshSortCamera = cameraPos;
            if (meshBuffer == null) {
                meshBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            }
            meshBuffer.bind();
            meshBuffer.upload(mesh);
            VertexBuffer.unbind();
            meshEmpty = false;
        }
    }

    /** Translucent quads are drawn back to front, like the per-frame sort of the immediate path. */
    private static void resortIfCameraMoved(Vec3 cameraPos) {
        if (meshSortState == null || meshBuffer == null
                || meshSortCamera != null && meshSortCamera.distanceToSqr(cameraPos) < RESORT_DISTANCE_SQR) {
            return;
        }
        meshSortCamera = cameraPos;
        try (ByteBufferBuilder indexBytes = new ByteBufferBuilder(4096)) {
            ByteBufferBuilder.Result indices = meshSortState.buildSortedIndexBuffer(indexBytes, sortingFor(cameraPos));
            if (indices != null) {
                meshBuffer.bind();
                meshBuffer.uploadIndexBuffer(indices);
                VertexBuffer.unbind();
            }
        }
    }

    private static VertexSorting sortingFor(Vec3 cameraPos) {
        return VertexSorting.byDistance(new Vector3f(
                (float) (cameraPos.x - activeControllerPos.getX()),
                (float) (cameraPos.y - activeControllerPos.getY()),
                (float) (cameraPos.z - activeControllerPos.getZ())));
    }

    private static void releaseMesh() {
        if (meshBuffer != null) {
            meshBuffer.close();
            meshBuffer = null;
        }
        meshSortState = null;
        meshLevel = null;
        shownBlocks = null;
        meshBlocks = null;
        meshSortCamera = null;
        meshEmpty = false;
        meshDirty = true;
        lastVisibilityTick = Long.MIN_VALUE;
        lastRebuildTick = Long.MIN_VALUE;
    }

    private static BlockPos getRotatedPos(BlockPos center, int localX, int localY, int localZ, net.minecraft.core.Direction facing) {
        return switch (facing) {
            case SOUTH -> center.offset(-localX, localY, -localZ);
            case WEST -> center.offset(localZ, localY, -localX);
            case EAST -> center.offset(-localZ, localY, localX);
            case NORTH, UP, DOWN -> center.offset(localX, localY, localZ);
        };
    }
}
