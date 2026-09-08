package com.raishxn.ufo.client.render;

import appeng.client.render.overlay.OverlayRenderType;
import com.raishxn.ufo.item.custom.QuantumWirelessToolItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** Selected source and face links, following AE2 Lightning's in-world interaction. */
public final class QuantumWirelessRenderer {
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        var stack = mc.player.getMainHandItem();
        if (!(stack.getItem() instanceof QuantumWirelessToolItem)) stack = mc.player.getOffhandItem();
        if (!(stack.getItem() instanceof QuantumWirelessToolItem)) return;
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.hasUUID("ufoWirelessSourceIdentity")
                || !tag.getString("ufoWirelessDimension").equals(mc.level.dimension().location().toString())) return;
        var source = BlockPos.of(tag.getLong("ufoWirelessSource"));
        int range = Math.max(1, tag.getCompound("ufoWirelessView").getInt("ufoWirelessRange"));
        if (!mc.level.hasChunkAt(source) || mc.level.getBlockEntity(source) == null
                || !tag.getCompound("ufoWirelessView").getBoolean("ufoWirelessEnabled")) return;
        var pose = event.getPoseStack();
        var camera = event.getCamera().getPosition();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        var buffers = mc.renderBuffers().bufferSource();
        var cube = buffers.getBuffer(QuantumWirelessRenderTypes.SOURCE);
        for (var side : Direction.values()) {
            Vec3 center = Vec3.atCenterOf(source).add(side.getStepX() * .25, side.getStepY() * .25, side.getStepZ() * .25);
            quad(pose, cube, center, side, .25, 1, 1, 0, .5F);
        }
        buffers.endBatch(QuantumWirelessRenderTypes.SOURCE);
        var links = tag.getCompound("ufoWirelessView").getList("ufoWirelessTargets", net.minecraft.nbt.Tag.TAG_COMPOUND);
        boolean lookingAtLinkedFace = false;
        for (int i = 0; i < Math.min(links.size(), 1024); i++) {
            var link = links.getCompound(i);
            int face = link.getInt("face");
            if (face < 0 || face >= 6) continue;
            var destination = BlockPos.of(link.getLong("pos"));
            if (!mc.level.hasChunkAt(destination) || source.distSqr(destination) > (double) range * range) continue;
            if (mc.hitResult instanceof BlockHitResult hit && hit.getBlockPos().equals(destination)
                    && hit.getDirection() == Direction.values()[face]) lookingAtLinkedFace = true;
            draw(pose, source, destination, Direction.values()[face], 0, .5F, 1);
        }
        if (!lookingAtLinkedFace && mc.hitResult instanceof BlockHitResult hit && !hit.getBlockPos().equals(source)
                && source.distSqr(hit.getBlockPos()) <= (double) range * range
                && hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                && !(mc.level.getBlockEntity(hit.getBlockPos()) instanceof com.raishxn.ufo.wireless.QuantumWirelessHost)
                && mc.level.getBlockEntity(hit.getBlockPos()) != null) {
            draw(pose, source, hit.getBlockPos(), hit.getDirection(), 1, 1, 0);
        }
        pose.popPose();
        buffers.endBatch(OverlayRenderType.getBlockHilightLine());
        buffers.endBatch(OverlayRenderType.getBlockHilightFace());
    }

    private static void draw(com.mojang.blaze3d.vertex.PoseStack pose,
            BlockPos source, BlockPos target,
            Direction face, float r, float g, float b) {
        var lines = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(OverlayRenderType.getBlockHilightLine());
        Vec3 from = Vec3.atCenterOf(source);
        Vec3 to = Vec3.atCenterOf(target).add(face.getStepX() * .502, face.getStepY() * .502, face.getStepZ() * .502);
        Vec3 normal = to.subtract(from).normalize();
        var matrix = pose.last().pose();
        lines.addVertex(matrix, (float) from.x, (float) from.y, (float) from.z).setColor(r,g,b,1).setNormal((float) normal.x,(float) normal.y,(float) normal.z);
        lines.addVertex(matrix, (float) to.x, (float) to.y, (float) to.z).setColor(r,g,b,1).setNormal((float) normal.x,(float) normal.y,(float) normal.z);
        var fill = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(OverlayRenderType.getBlockHilightFace());
        quad(pose, fill, to, face, .5, r, g, b, .375F);
    }

    private static void quad(com.mojang.blaze3d.vertex.PoseStack pose,
            com.mojang.blaze3d.vertex.VertexConsumer fill, Vec3 to, Direction face,
            double size, float r, float g, float b, float alpha) {
        var matrix = pose.last().pose();
        Vec3 u = face.getAxis() == Direction.Axis.X ? new Vec3(0,size,0) : new Vec3(size,0,0);
        Vec3 v = face.getAxis() == Direction.Axis.Z ? new Vec3(0,size,0) : new Vec3(0,0,size);
        for (var corner : new Vec3[]{to.subtract(u).subtract(v), to.add(u).subtract(v), to.add(u).add(v), to.subtract(u).add(v)}) {
            fill.addVertex(matrix, (float) corner.x, (float) corner.y, (float) corner.z)
                    .setColor(r,g,b,alpha).setNormal(face.getStepX(),face.getStepY(),face.getStepZ());
        }
    }
}
