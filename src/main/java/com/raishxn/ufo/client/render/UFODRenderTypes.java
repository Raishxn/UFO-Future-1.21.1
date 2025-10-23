package com.raishxn.ufo.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.raishxn.ufo.client.render.shader.UFOShieldShader;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class UFODRenderTypes {

    public static final RenderType SHIELD_SPHERE = RenderType.create("ufo_shield_sphere",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.TRIANGLES,
            256,
            false,
            true, // Habilita a mistura de cores para transparência
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(UFOShieldShader::getShieldShader))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));
}