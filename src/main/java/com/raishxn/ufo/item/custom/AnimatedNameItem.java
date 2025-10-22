package com.raishxn.ufo.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AnimatedNameItem extends Item {

    private final ChatFormatting[] colors;

    /**
     * Um item customizado que renderiza seu nome com cores animadas.
     * @param props As propriedades do item.
     * @param colors As cores que serão usadas na animação.
     */
    public AnimatedNameItem(Properties props, ChatFormatting... colors) {
        super(props);
        this.colors = colors;
    }

    @Override
    public Component getName(ItemStack stack) {
        // Pega o nome padrão do item (definido no en_us.json)
        MutableComponent name = Component.translatable(stack.getDescriptionId());
        String text = name.getString();

        // Se não houver cores definidas, retorna o nome padrão
        if (this.colors == null || this.colors.length == 0) {
            return name;
        }

        MutableComponent coloredName = Component.empty();
        long time = Util.getMillis();

        // Itera por cada letra do nome e aplica uma cor do array
        for (int i = 0; i < text.length(); i++) {
            // A fórmula de tempo cria o efeito de "onda" de cores
            int colorIndex = (int) (i * 0.5 + time / 200.0) % this.colors.length;
            coloredName.append(Component.literal(String.valueOf(text.charAt(i))).withStyle(this.colors[colorIndex]));
        }

        return coloredName;
    }
}