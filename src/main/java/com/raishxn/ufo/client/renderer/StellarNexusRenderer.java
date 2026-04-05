package com.raishxn.ufo.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.entity.StellarNexusControllerBE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class StellarNexusRenderer implements BlockEntityRenderer<StellarNexusControllerBE> {

    public static final ResourceLocation STAR_MODEL = ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "item/stellar_core");

    public StellarNexusRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(StellarNexusControllerBE blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.isActive()) return;

        // The machine is active, render the Star Model at the center of the multiblock!
        // The controller is at Z=0 (local coords), center is at X=17, Y=17, Z=-17
        poseStack.pushPose();
        
        // Move backwards into the machine depending on rotation
        net.minecraft.core.Direction facing = blockEntity.getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING);
        
        float xOffset = 0.5f;
        float yOffset = 17.5f; // Center Y
        float zOffset = 0.5f;
        
        switch (facing) {
            case NORTH -> zOffset += 17f;
            case SOUTH -> zOffset -= 17f;
            case WEST -> xOffset += 17f;
            case EAST -> xOffset -= 17f;
            default -> {}
        }
        
        poseStack.translate(xOffset, yOffset, zOffset);
        
        // Spin the star
        long time = blockEntity.getLevel().getGameTime();
        float rotation = (time + partialTick) * 2.0f; // 2 degrees per tick
        
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation * 0.5f)); // slight tilt
        
        // Scale it up
        float scale = 5.0f; // Star is massive
        poseStack.scale(scale, scale, scale);

        net.minecraft.client.resources.model.ModelResourceLocation mrl = new net.minecraft.client.resources.model.ModelResourceLocation(STAR_MODEL, "inventory");
        BakedModel bakedModel = Minecraft.getInstance().getModelManager().getModel(mrl);
        
        // Render the model glowing (full bright)
        if (bakedModel != null) {
            Minecraft.getInstance().getItemRenderer().render(
                ItemStack.EMPTY, ItemDisplayContext.NONE, false, 
                poseStack, bufferSource, 
                15728880, // Full brightness Lightmap texture
                packedOverlay, bakedModel
            );
        }
        
        poseStack.popPose();
    }
    
    // We render outside the normal bounding box
    @Override
    public boolean shouldRenderOffScreen(StellarNexusControllerBE blockEntity) {
        return true;
    }
    
    @Override
    public int getViewDistance() {
        return 256;
    }
}
