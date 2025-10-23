package com.raishxn.ufo.client.render.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

public class SphereRenderer {

    public static void renderTriangledSphere(PoseStack poseStack, VertexConsumer buffer, float radius, int stacks, int sectors) {
        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < sectors; j++) {
                // Calcula as coordenadas de textura para os quatro cantos de um "quad" na esfera
                float v1 = (float) i / stacks;
                float v2 = (float) (i + 1) / stacks;
                float u1 = (float) j / sectors;
                float u2 = (float) (j + 1) / sectors;

                // Cria dois triângulos para formar o "quad"
                addSphereVertex(poseStack, buffer, radius, u1, v1);
                addSphereVertex(poseStack, buffer, radius, u2, v1);
                addSphereVertex(poseStack, buffer, radius, u2, v2);

                addSphereVertex(poseStack, buffer, radius, u2, v2);
                addSphereVertex(poseStack, buffer, radius, u1, v2);
                addSphereVertex(poseStack, buffer, radius, u1, v1);
            }
        }
    }

    private static void addSphereVertex(PoseStack poseStack, VertexConsumer buffer, float r, float u, float v) {
        float phi = v * (float) Math.PI;
        float theta = u * (float) (Math.PI * 2);

        // Calcula a posição do vértice na esfera
        float x = (float) (Math.cos(theta) * Math.sin(phi));
        float y = (float) Math.cos(phi);
        float z = (float) (Math.sin(theta) * Math.sin(phi));

        // --- CORREÇÃO APLICADA AQUI ---
        // A forma correta de passar os dados para o VertexConsumer é encadeando as chamadas.
        // O código anterior estava chamando os métodos separadamente, o que não funciona.
        Matrix4f matrix = poseStack.last().pose();
        buffer.addVertex(matrix, x * r, y * r, z * r).setUv(u, v).setColor(255, 255, 255, 255).setUv2(240,200);    }
}