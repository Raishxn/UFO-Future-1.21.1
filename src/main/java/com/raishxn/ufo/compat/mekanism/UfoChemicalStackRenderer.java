package com.raishxn.ufo.compat.mekanism;

import appeng.api.client.AEKeyRenderHandler;
import appeng.api.client.AEKeyRendering;
import appeng.client.gui.style.Blitter;
import appeng.util.Platform;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.ArrayList;
import java.util.List;

public final class UfoChemicalStackRenderer implements AEKeyRenderHandler<UfoMekanismKey> {
    public static void initialize(IEventBus bus) {
        bus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() ->
                AEKeyRendering.register(UfoMekanismKeyType.TYPE, UfoMekanismKey.class, new UfoChemicalStackRenderer())));
    }

    @Override
    public void drawInGui(Minecraft minecraft, GuiGraphics guiGraphics, int x, int y, UfoMekanismKey what) {
        var stack = what.getStack();
        Blitter.sprite(Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(stack.getChemical().getIcon()))
                .colorRgb(stack.getChemicalTint())
                .blending(false)
                .dest(x, y, 16, 16)
                .blit(guiGraphics);
    }

    @Override
    public void drawOnBlockFace(PoseStack poseStack, MultiBufferSource buffers, UfoMekanismKey what, float scale, int combinedLight, Level level) {
        var stack = what.getStack();
        var sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(stack.getChemical().getIcon());
        var color = stack.getChemicalTint();

        poseStack.pushPose();
        poseStack.translate(0, 0, 0.01f);
        var buffer = buffers.getBuffer(RenderType.solid());
        scale -= 0.05f;
        var x0 = -scale / 2;
        var y0 = scale / 2;
        var x1 = scale / 2;
        var y1 = -scale / 2;
        var transform = poseStack.last().pose();
        buffer.addVertex(transform, x0, y1, 0).setColor(color).setUv(sprite.getU0(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0, 0, 1);
        buffer.addVertex(transform, x1, y1, 0).setColor(color).setUv(sprite.getU1(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0, 0, 1);
        buffer.addVertex(transform, x1, y0, 0).setColor(color).setUv(sprite.getU1(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0, 0, 1);
        buffer.addVertex(transform, x0, y0, 0).setColor(color).setUv(sprite.getU0(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0, 0, 1);
        poseStack.popPose();
    }

    @Override
    public Component getDisplayName(UfoMekanismKey stack) {
        return stack.getDisplayName();
    }

    @Override
    public List<Component> getTooltip(UfoMekanismKey stack) {
        var tooltip = new ArrayList<Component>();
        tooltip.add(getDisplayName(stack));
        stack.getStack().appendHoverText(Item.TooltipContext.EMPTY, tooltip, TooltipFlag.NORMAL);
        var modName = Platform.formatModName(stack.getModId());
        if (tooltip.isEmpty() || !tooltip.getLast().getString().equals(modName)) {
            tooltip.add(Component.literal(modName));
        }
        return tooltip;
    }
}
