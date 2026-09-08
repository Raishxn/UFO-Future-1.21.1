package com.raishxn.ufo.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.raishxn.ufo.client.render.QuantumWirelessRenderTypes;
import com.raishxn.ufo.item.ModArmor;
import com.raishxn.ufo.armor.UfoArmorModule;
import com.raishxn.ufo.item.custom.UfoArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AstralNexusWingsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public AstralNexusWingsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean legacyNexus = chestStack.is(ModArmor.ASTRAL_NEXUS_CHESTPLATE.get());
        boolean modularWings = chestStack.is(ModArmor.UFO_CHESTPLATE.get())
                && UfoArmorItem.hasFullUfoSet(player)
                && player.getAbilities().flying
                && UfoArmorItem.isModuleEnabled(chestStack, UfoArmorModule.ASTRAL_WINGS);
        if (!legacyNexus && !modularWings) {
            return;
        }

        if (modularWings) {
            renderUfoThrusters(poseStack, buffer, player, ageInTicks);
            return;
        }

        poseStack.pushPose();
        this.getParentModel().body.translateAndRotate(poseStack);
        poseStack.translate(0.0F, -0.05F, 0.16F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                chestStack,
                ItemDisplayContext.HEAD,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                player.level(),
                player.getId()
        );
        poseStack.popPose();
    }

    private void renderUfoThrusters(PoseStack poseStack, MultiBufferSource buffer,
                                    AbstractClientPlayer player, float ageInTicks) {
        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);
        poseStack.translate(0.0F, 0.05F, 0.12F);
        VertexConsumer vertices = buffer.getBuffer(QuantumWirelessRenderTypes.ARMOR_HOLOGRAM);
        float pulse = 0.82F + 0.18F * (float) Math.sin(ageInTicks * 0.18F + player.getId());
        int cyanAlpha = Math.round(185 * pulse);
        int violetAlpha = Math.round(145 * pulse);

        // Three sharp energy vanes per side, shaped like compact UFO thrusters rather than feathers.
        vane(vertices, poseStack, -1, 0x71, 0xE7, 0xF2, cyanAlpha);
        vane(vertices, poseStack, 1, 0x71, 0xE7, 0xF2, cyanAlpha);
        lowerVane(vertices, poseStack, -1, 0xA6, 0x7A, 0xF4, violetAlpha);
        lowerVane(vertices, poseStack, 1, 0xA6, 0x7A, 0xF4, violetAlpha);
        spine(vertices, poseStack, -1, 0xD9, 0xFB, 0xFF, 220);
        spine(vertices, poseStack, 1, 0xD9, 0xFB, 0xFF, 220);
        quad(vertices, poseStack, -0.10F, -0.12F, 0.17F, 0.0F, -0.24F, 0.18F,
                0.10F, -0.12F, 0.17F, 0.0F, 0.02F, 0.18F, 0xD9, 0xFB, 0xFF, 225);
        poseStack.popPose();
    }

    private static void vane(VertexConsumer v, PoseStack pose, int side, int r, int g, int b, int a) {
        float s = side;
        quad(v, pose,
                0.12F * s, -0.18F, 0.15F,
                0.34F * s, -0.38F, 0.14F,
                0.82F * s, -0.06F, 0.12F,
                0.30F * s, 0.12F, 0.14F, r, g, b, a);
    }

    private static void lowerVane(VertexConsumer v, PoseStack pose, int side, int r, int g, int b, int a) {
        float s = side;
        quad(v, pose,
                0.18F * s, 0.00F, 0.14F,
                0.42F * s, 0.10F, 0.13F,
                0.62F * s, 0.65F, 0.11F,
                0.27F * s, 0.36F, 0.14F, r, g, b, a);
    }

    private static void spine(VertexConsumer v, PoseStack pose, int side, int r, int g, int b, int a) {
        float s = side;
        quad(v, pose,
                0.15F * s, -0.12F, 0.18F,
                0.19F * s, -0.15F, 0.18F,
                0.58F * s, 0.53F, 0.17F,
                0.53F * s, 0.50F, 0.17F, r, g, b, a);
    }

    private static void quad(VertexConsumer v, PoseStack pose,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             int r, int g, int b, int a) {
        var matrix = pose.last().pose();
        v.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(0, 0, -1);
        v.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(0, 0, -1);
        v.addVertex(matrix, x3, y3, z3).setColor(r, g, b, a).setNormal(0, 0, -1);
        v.addVertex(matrix, x4, y4, z4).setColor(r, g, b, a).setNormal(0, 0, -1);
    }
}
