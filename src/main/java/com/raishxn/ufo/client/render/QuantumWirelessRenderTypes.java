package com.raishxn.ufo.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

/** Adapted from AE2 Lightning Tech Ae2ltRenderTypes. Copyright its contributors, LGPL-3.0. */
public final class QuantumWirelessRenderTypes extends RenderType {
    private QuantumWirelessRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode,
            int size, boolean crumbling, boolean sorted, Runnable setup, Runnable clear) {
        super(name, format, mode, size, crumbling, sorted, setup, clear);
    }

    public static final RenderType SOURCE = create("ufo_wireless_source",
            DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.QUADS, 65536, false, false,
            CompositeState.builder().setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE).setLightmapState(NO_LIGHTMAP)
                    .setDepthTestState(GREATER_DEPTH_TEST).setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL).setShaderState(POSITION_COLOR_SHADER).createCompositeState(false));

    public static final RenderType ARMOR_HOLOGRAM = create("ufo_armor_hologram",
            DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.QUADS, 2048, false, true,
            CompositeState.builder().setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE).setLightmapState(NO_LIGHTMAP)
                    .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL).setShaderState(POSITION_COLOR_SHADER).createCompositeState(false));
}
