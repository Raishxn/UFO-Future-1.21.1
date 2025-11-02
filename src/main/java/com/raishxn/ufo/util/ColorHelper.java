package com.raishxn.ufo.util;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

import java.awt.Color;

public class ColorHelper {

    /**
     * Cria um componente de texto com cores animadas em estilo de onda.
     * @param text O texto a ser colorido.
     * @param colors As cores a serem usadas na animação.
     * @return Um MutableComponent com o texto animado.
     */
    public static MutableComponent getAnimatedColoredText(String text, ChatFormatting... colors) {
        if (colors == null || colors.length == 0) {
            return Component.literal(text);
        }

        MutableComponent coloredName = Component.empty();
        long time = Util.getMillis();

        for (int i = 0; i < text.length(); i++) {
            int colorIndex = (int) (i * 0.5 + time / 200.0) % colors.length;
            coloredName.append(Component.literal(String.valueOf(text.charAt(i))).withStyle(colors[colorIndex]));
        }

        return coloredName;
    }

    public static int getRainbowColor(int offset) {
        float hue = (System.currentTimeMillis() / 10 % 360 + offset) / 360f;
        return Color.HSBtoRGB(hue, 0.8f, 1.0f);
    }

    /**
     * Ciclo de cores variando entre tons de azul, roxo e rosa.
     */
    public static int getBluePurplePinkColor(int offset) {
        float hue = (System.currentTimeMillis() / 10 % 360 + offset) / 360f;
        if (hue > 0.83f) { // Faixa do Rosa/Roxo (aprox. 300 graus em diante)
            hue = 0.66f + (hue - 0.83f) * 0.5f; // Mapeia para Roxo/Azul
        } else if (hue < 0.66f) { // Faixa antes do Azul
            hue = 0.66f; // Trava no azul
        }
        return java.awt.Color.HSBtoRGB(hue, 0.7f, 1.0f);
    }

    /**
     * Ciclo de cores variando entre tons de cinza e preto.
     */
    public static int getGrayBlackColor(int offset) {
        long time = System.currentTimeMillis() / 2;
        float brightness = 0.5f + 0.5f * (float) Math.sin((time + offset) / 500.0); // Oscila entre 0.0 e 1.0
        brightness = Mth.clamp(brightness * 0.6f, 0.1f, 0.6f); // Limita o brilho para tons escuros
        return new java.awt.Color(brightness, brightness, brightness).getRGB();
    }

    /**
     * Ciclo de cores variando entre tons de vermelho, ciano e rosa.
     */
    public static int getRedCyanPinkColor(int offset) {
        float hue = (System.currentTimeMillis() / 15 % 360 + offset) / 360f;
        // Mapeia o ciclo para as 3 cores
        if (hue < 0.33f) { // Vermelho
            hue = 0.0f;
        } else if (hue < 0.66f) { // Ciano
            hue = 0.5f;
        } else { // Rosa/Magenta
            hue = 0.83f;
        }
        return java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f);
    }
}