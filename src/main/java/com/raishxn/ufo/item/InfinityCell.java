package com.raishxn.ufo.item.custom;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.items.AEBaseItem;
import appeng.items.storage.StorageCellTooltipComponent;
import com.raishxn.ufo.util.LazyInits;
import net.minecraft.ChatFormatting;
import net.minecraft.Util; // <-- Import necessário para a animação
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent; // <-- Import necessário
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class InfinityCell extends AEBaseItem {

    private final Supplier<AEKey> type;
    private AEKey record;
    private final ChatFormatting[] nameFormatting;

    public InfinityCell(@NotNull Supplier<AEKey> type, ChatFormatting... nameFormatting) {
        super(new Item.Properties().stacksTo(1));
        this.type = type;
        this.nameFormatting = nameFormatting;
        LazyInits.addFinal(() -> this.record = this.type.get());
    }

    public AEKey getRecord() {
        return this.record;
    }

    // --- MUDANÇA PRINCIPAL APLICADA AQUI ---
    @Override
    public @NotNull Component getName(@NotNull ItemStack is) {
        if (getRecord() == null) {
            return Component.translatable("item.ufo.infinity_cell_invalid");
        }

        // 1. Pega o nome base da célula (ex: "Infinity Water Cell")
        MutableComponent baseName = Component.translatable("item.ufo.infinity_cell_name", getRecord().getDisplayName());
        String text = baseName.getString();

        // 2. Se não houver cores definidas, retorna o nome padrão
        if (this.nameFormatting == null || this.nameFormatting.length == 0) {
            return baseName;
        }

        // 3. Lógica de animação por letra (copiada da sua classe AnimatedNameItem)
        MutableComponent coloredName = Component.empty();
        long time = Util.getMillis();

        for (int i = 0; i < text.length(); i++) {
            // A fórmula cria o efeito de "onda" de cores
            int colorIndex = (int) (i * 0.5 + time / 200.0) % this.nameFormatting.length;
            coloredName.append(Component.literal(String.valueOf(text.charAt(i))).withStyle(this.nameFormatting[colorIndex]));
        }

        return coloredName;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack is, Item.@NotNull TooltipContext ctx, @NotNull List<Component> lines, @NotNull TooltipFlag adv) {
        if (getRecord() != null) {
            lines.add(Component.translatable("item.ufo.infinity_cell_tooltip").withStyle(ChatFormatting.GREEN));
        }
    }

    @NotNull
    @Override
    public Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        if (getRecord() == null) {
            return Optional.empty();
        }
        var content = Collections.singletonList(new GenericStack(this.record, getAsIntMax(this.record)));
        return Optional.of(new StorageCellTooltipComponent(List.of(), content, false, true));
    }

    public static long getAsIntMax(AEKey key) {
        if (key == null) return 0;
        return (long) Integer.MAX_VALUE * key.getAmountPerUnit();
    }
}