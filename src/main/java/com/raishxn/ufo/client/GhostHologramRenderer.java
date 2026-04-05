package com.raishxn.ufo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.block.entity.pattern.StellarNexusPatternFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Map;

@EventBusSubscriber(value = Dist.CLIENT)
public class GhostHologramRenderer {

    private static BlockPos activeControllerPos = null;
    private static net.minecraft.core.Direction activeFacing = null;
    
    public static void toggleHologram(BlockPos pos, net.minecraft.core.Direction facing) {
        if (activeControllerPos != null && activeControllerPos.equals(pos)) {
            activeControllerPos = null;
            activeFacing = null;
        } else {
            activeControllerPos = pos;
            activeFacing = facing;
        }
    }
    
    public static boolean isActive(BlockPos pos) {
        return activeControllerPos != null && activeControllerPos.equals(pos);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (activeControllerPos == null || activeFacing == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        if (!mc.level.getBlockState(activeControllerPos).is(com.raishxn.ufo.block.MultiblockBlocks.STELLAR_NEXUS_CONTROLLER.get())) {
            activeControllerPos = null;
            return;
        }

        if (mc.player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(activeControllerPos)) > 64 * 64) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        net.minecraft.world.phys.Vec3 cameraPos = event.getCamera().getPosition();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        MultiblockPattern pattern = StellarNexusPatternFactory.getPattern();
        Map<Character, BlockState> defaults = StellarNexusPatternFactory.getDefaultCreativeStates();

        char[][][] charPattern = pattern.getPattern();
        int controllerCol = pattern.getControllerCol();
        int controllerRow = pattern.getControllerRow();
        int controllerLayer = pattern.getControllerLayer();

        for (int y = 0; y < charPattern.length; y++) {
            for (int z = 0; z < charPattern[y].length; z++) {
                for (int x = 0; x < charPattern[y][z].length; x++) {
                    char c = charPattern[y][z][x];
                    if (c == ' ' || c == pattern.getControllerChar()) continue;

                    BlockState targetState = defaults.get(c);
                    if (targetState == null) continue;

                    int offsetX = x - controllerCol;
                    int offsetY = y - controllerLayer;
                    int offsetZ = z - controllerRow;

                    BlockPos worldPos = getRotatedPos(activeControllerPos, offsetX, offsetY, offsetZ, activeFacing);

                    BlockState existingState = mc.level.getBlockState(worldPos);
                    if (!existingState.isAir() && existingState.getFluidState().isEmpty()) {
                        continue;
                    }

                    poseStack.pushPose();
                    poseStack.translate(worldPos.getX(), worldPos.getY(), worldPos.getZ());
                    
                    RenderType renderType = net.minecraft.client.renderer.RenderType.translucent();
                    com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
                    
                    mc.getBlockRenderer().renderBatched(
                            targetState,
                            worldPos,
                            mc.level,
                            poseStack,
                            vertexConsumer,
                            true,
                            mc.level.getRandom()
                    );
                    
                    poseStack.popPose();
                }
            }
        }
        
        bufferSource.endBatch();
        poseStack.popPose();
    }

    private static BlockPos getRotatedPos(BlockPos center, int localX, int localY, int localZ, net.minecraft.core.Direction facing) {
        switch (facing) {
            case SOUTH: return center.offset(-localX, localY, -localZ);
            case WEST: return center.offset(localZ, localY, -localX);
            case EAST: return center.offset(-localZ, localY, localX);
            case NORTH: default: return center.offset(localX, localY, localZ);
        }
    }
}
