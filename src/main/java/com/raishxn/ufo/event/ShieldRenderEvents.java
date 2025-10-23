package com.raishxn.ufo.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.client.render.UFODRenderTypes;
import com.raishxn.ufo.client.render.shader.UFOShieldShader;
import com.raishxn.ufo.client.render.util.SphereRenderer;
import com.raishxn.ufo.item.custom.UfoArmorItem;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class ShieldRenderEvents {

    @Nullable
    public static ItemCapability<IEnergyStorage, Void> ENERGY_CAPABILITY = null;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (ENERGY_CAPABILITY == null) {
            return;
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) {
                return;
            }

            for (Player player : mc.level.players()) {
                ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
                if (chestStack.getItem() instanceof UfoArmorItem) {
                    IEnergyStorage energyStorage = chestStack.getCapability(ENERGY_CAPABILITY);

                    if (energyStorage != null && energyStorage.getEnergyStored() > 0) {
                        float energyPercentage = (float) energyStorage.getEnergyStored() / (float) energyStorage.getMaxEnergyStored();
                        renderShield(event, player, event.getPartialTick(), energyPercentage);
                    }
                }
            }
        }
    }

    private static void renderShield(RenderLevelStageEvent event, Player player, DeltaTracker deltaTracker, float energyPercentage) {
        Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = event.getPoseStack();

        // --- CORREÇÃO FINAL ESTÁ AQUI ---
        // O método espera um argumento booleano.
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);

        double playerX = Mth.lerp(partialTick, player.xo, player.getX());
        double playerY = Mth.lerp(partialTick, player.yo, player.getY());
        double playerZ = Mth.lerp(partialTick, player.zo, player.getZ());

        poseStack.pushPose();
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
        poseStack.translate(playerX - camPos.x, playerY - camPos.y + (player.getBbHeight() / 2.0), playerZ - camPos.z);

        poseStack.mulPose(Axis.YP.rotationDegrees((float) ((Util.getMillis() / 20.0) % 360)));

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer buffer = bufferSource.getBuffer(UFODRenderTypes.SHIELD_SPHERE);

        if (UFOShieldShader.time != null && UFOShieldShader.activation != null && UFOShieldShader.baseColour != null) {
            UFOShieldShader.time.set((Util.getMillis() % 200000L) / 1000.0f);
            float pulse = (float) Math.sin((Util.getMillis() / 500.0)) * 0.1f + 0.9f;
            UFOShieldShader.activation.set(energyPercentage * pulse);
            UFOShieldShader.baseColour.set(0.2f, 0.8f, 1.0f, 0.6f);
        }

        float shieldRadius = player.getBbWidth() * 1.2f;
        SphereRenderer.renderTriangledSphere(poseStack, buffer, shieldRadius, 24, 48);

        bufferSource.endBatch(UFODRenderTypes.SHIELD_SPHERE);

        poseStack.popPose();
    }
}