package com.raishxn.ufo.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.raishxn.ufo.block.entity.StellarNexusControllerBE;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;

/**
 * Renders a glowing, rotating star sphere at the center of the Stellar Nexus
 * multiblock while it is actively processing a simulation.
 * <p>
 * Uses a UV sphere built from triangles with the beacon beam render type
 * for the glowing effect (no external model dependency).
 */
public class StellarNexusRenderer implements BlockEntityRenderer<StellarNexusControllerBE> {

    private static final ResourceLocation BEAM_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");

    // Sphere parameters
    private static final int STACKS = 16;
    private static final int SLICES = 24;
    private static final float RADIUS = 3.5f;

    public StellarNexusRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(StellarNexusControllerBE blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.isActive()) return;
        if (blockEntity.getLevel() == null) return;

        poseStack.pushPose();

        // Calculate center offset based on controller facing direction
        Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.FACING);

        float xOffset = 0.5f;
        float yOffset = 17.5f;
        float zOffset = 0.5f;

        switch (facing) {
            case NORTH -> zOffset += 17f;
            case SOUTH -> zOffset -= 17f;
            case WEST  -> xOffset += 17f;
            case EAST  -> xOffset -= 17f;
            default -> {}
        }

        poseStack.translate(xOffset, yOffset, zOffset);

        // Rotation animation
        long time = blockEntity.getLevel().getGameTime();
        float rotation = (time + partialTick) * 2.0f;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation * 0.3f));

        // Pulsing scale effect
        float pulse = 1.0f + 0.08f * (float) Math.sin((time + partialTick) * 0.1);
        poseStack.scale(pulse, pulse, pulse);

        // == Outer glow layer (larger, semi-transparent) ==
        poseStack.pushPose();
        float outerScale = 1.4f;
        poseStack.scale(outerScale, outerScale, outerScale);
        renderSphere(poseStack, bufferSource, 0.4f, 0.6f, 1.0f, 0.25f); // blue glow
        poseStack.popPose();

        // == Core sphere (bright white-yellow) ==
        renderSphere(poseStack, bufferSource, 1.0f, 0.95f, 0.7f, 0.9f); // warm white core

        // == Inner hot core (small, bright orange) ==
        poseStack.pushPose();
        float innerScale = 0.6f;
        poseStack.scale(innerScale, innerScale, innerScale);
        renderSphere(poseStack, bufferSource, 1.0f, 0.6f, 0.2f, 1.0f); // orange inner
        poseStack.popPose();

        poseStack.popPose();
    }

    /**
     * Renders a UV sphere using the beacon beam render type for that classic MC glow.
     */
    private void renderSphere(PoseStack poseStack, MultiBufferSource bufferSource,
                              float r, float g, float b, float a) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.beaconBeam(BEAM_TEXTURE, true));

        for (int i = 0; i < STACKS; i++) {
            float phi1 = (float) Math.PI * i / STACKS;
            float phi2 = (float) Math.PI * (i + 1) / STACKS;

            for (int j = 0; j < SLICES; j++) {
                float theta1 = 2.0f * (float) Math.PI * j / SLICES;
                float theta2 = 2.0f * (float) Math.PI * (j + 1) / SLICES;

                // Four corners of this quad
                float x1 = RADIUS * (float) (Math.sin(phi1) * Math.cos(theta1));
                float y1 = RADIUS * (float) Math.cos(phi1);
                float z1 = RADIUS * (float) (Math.sin(phi1) * Math.sin(theta1));

                float x2 = RADIUS * (float) (Math.sin(phi1) * Math.cos(theta2));
                float y2 = RADIUS * (float) Math.cos(phi1);
                float z2 = RADIUS * (float) (Math.sin(phi1) * Math.sin(theta2));

                float x3 = RADIUS * (float) (Math.sin(phi2) * Math.cos(theta2));
                float y3 = RADIUS * (float) Math.cos(phi2);
                float z3 = RADIUS * (float) (Math.sin(phi2) * Math.sin(theta2));

                float x4 = RADIUS * (float) (Math.sin(phi2) * Math.cos(theta1));
                float y4 = RADIUS * (float) Math.cos(phi2);
                float z4 = RADIUS * (float) (Math.sin(phi2) * Math.sin(theta1));

                // UV mapping
                float u1 = (float) j / SLICES;
                float u2 = (float) (j + 1) / SLICES;
                float v1 = (float) i / STACKS;
                float v2 = (float) (i + 1) / STACKS;

                PoseStack.Pose pose = poseStack.last();

                // Triangle 1: top-left, bottom-left, bottom-right
                vertex(consumer, pose, x1, y1, z1, u1, v1, r, g, b, a);
                vertex(consumer, pose, x4, y4, z4, u1, v2, r, g, b, a);
                vertex(consumer, pose, x3, y3, z3, u2, v2, r, g, b, a);
                vertex(consumer, pose, x2, y2, z2, u2, v1, r, g, b, a);
            }
        }
    }

    private void vertex(VertexConsumer consumer, PoseStack.Pose pose,
                        float x, float y, float z,
                        float u, float v,
                        float r, float g, float b, float a) {
        consumer.addVertex(pose.pose(), x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, x / RADIUS, y / RADIUS, z / RADIUS);
    }

    @Override
    public boolean shouldRenderOffScreen(StellarNexusControllerBE blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
