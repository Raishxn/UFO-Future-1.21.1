package com.raishxn.ufo.util;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

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
}