package com.raishxn.ufo.client.render.shader;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.raishxn.ufo.UfoMod;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;

public class UFOShieldShader {

    private static ShaderInstance shieldShader;

    // Uniforms
    @Nullable
    public static Uniform time;
    @Nullable
    public static Uniform activation;
    @Nullable
    public static Uniform baseColour;

    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        // A linha abaixo foi corrigida para usar ResourceLocation.fromNamespaceAndPath()
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "ufo_shield"), DefaultVertexFormat.POSITION_TEX), shader -> {
            shieldShader = shader;
            // Get uniforms after shader is loaded
            time = shieldShader.getUniform("Time");
            activation = shieldShader.getUniform("Activation");
            baseColour = shieldShader.getUniform("BaseColour");
        });
    }

    public static ShaderInstance getShieldShader() {
        return Objects.requireNonNull(shieldShader, "UFO Shield Shader has not been initialized!");
    }
}