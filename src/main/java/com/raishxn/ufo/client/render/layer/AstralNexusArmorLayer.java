package com.raishxn.ufo.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.item.custom.AstralNexusArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AstralNexusArmorLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final int COSMIC_TINT = 0x61B8E0FF;
    private static final ResourceLocation OUTER_TEXTURE = UfoMod.id("textures/models/armor/astral_nexus_layer_1.png");
    private static final ResourceLocation INNER_TEXTURE = UfoMod.id("textures/models/armor/astral_nexus_layer_2.png");

    private final HumanoidModel<AbstractClientPlayer> innerModel;
    private final HumanoidModel<AbstractClientPlayer> outerModel;

    public AstralNexusArmorLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer,
                                 EntityModelSet entityModelSet,
                                 boolean slim) {
        super(renderer);
        this.innerModel = new HumanoidModel<>(entityModelSet.bakeLayer(resolveInnerArmorLayer(slim)));
        this.outerModel = new HumanoidModel<>(entityModelSet.bakeLayer(resolveOuterArmorLayer(slim)));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        renderArmorPiece(poseStack, buffer, packedLight, player, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch, EquipmentSlot.HEAD);
        renderArmorPiece(poseStack, buffer, packedLight, player, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch, EquipmentSlot.CHEST);
        renderArmorPiece(poseStack, buffer, packedLight, player, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch, EquipmentSlot.LEGS);
        renderArmorPiece(poseStack, buffer, packedLight, player, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch, EquipmentSlot.FEET);
    }

    private void renderArmorPiece(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                                  float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                                  float netHeadYaw, float headPitch, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        if (!(stack.getItem() instanceof AstralNexusArmorItem armorItem)) {
            return;
        }

        HumanoidModel<AbstractClientPlayer> model = usesInnerModel(slot) ? this.innerModel : this.outerModel;
        this.getParentModel().copyPropertiesTo(model);
        setPartVisibility(model, slot);
        model.prepareMobModel(player, limbSwing, limbSwingAmount, partialTick);
        model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        float time = player.tickCount + partialTick;
        float swirlU = Mth.frac(time * 0.01F);
        float swirlV = Mth.frac(time * 0.005F + slot.ordinal() * 0.17F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.energySwirl(getTexture(armorItem), swirlU, swirlV));
        model.renderToBuffer(
                poseStack,
                consumer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(player, 0.0F),
                COSMIC_TINT
        );
    }

    private static ResourceLocation getTexture(ArmorItem armorItem) {
        return armorItem.getType() == ArmorItem.Type.LEGGINGS ? INNER_TEXTURE : OUTER_TEXTURE;
    }

    private static boolean usesInnerModel(EquipmentSlot slot) {
        return slot == EquipmentSlot.LEGS;
    }

    private static void setPartVisibility(HumanoidModel<?> model, EquipmentSlot slot) {
        model.setAllVisible(false);
        switch (slot) {
            case HEAD -> {
                model.head.visible = true;
                model.hat.visible = true;
            }
            case CHEST -> {
                model.body.visible = true;
                model.rightArm.visible = true;
                model.leftArm.visible = true;
            }
            case LEGS -> {
                model.body.visible = true;
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            case FEET -> {
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            default -> {
            }
        }
    }

    private static ModelLayerLocation resolveInnerArmorLayer(boolean slim) {
        return slim ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR;
    }

    private static ModelLayerLocation resolveOuterArmorLayer(boolean slim) {
        return slim ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR;
    }
}
