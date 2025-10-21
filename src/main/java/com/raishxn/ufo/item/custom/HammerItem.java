package com.raishxn.ufo.item.custom;

import com.raishxn.ufo.util.EnergyToolHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

public class HammerItem extends DiggerItem implements IEnergyTool, IHasModeHUD, IHasCycleableModes {

    private static final String TAG_RANGE = "range";
    private static final int[] RANGES = {0, 1, 2, 3}; // 0=1x1, 1=3x3, 2=5x5, 3=7x7

    public HammerItem(Tier pTier, Properties pProperties) {
        super(pTier, BlockTags.MINEABLE_WITH_PICKAXE, pProperties);
    }

    // --- LÓGICA DE QUEBRA EM ÁREA (CORRIGIDA) ---
    @Override
    public boolean mineBlock(ItemStack pStack, Level pLevel, BlockState pState, BlockPos pPos, LivingEntity pEntityLiving) {
        // A lógica só roda no servidor e se o jogador for um player
        if (!pLevel.isClientSide && pEntityLiving instanceof ServerPlayer player) {
            int range = getRange(pStack);
            // Se o modo for 1x1, não faz nada extra
            if (range == 0) {
                return super.mineBlock(pStack, pLevel, pState, pPos, pEntityLiving);
            }

            // Pega a lista de blocos a serem destruídos
            List<BlockPos> blocksToBreak = getPositions(pPos, player.getDirection(), range);

            // Quebra cada bloco na lista, consumindo energia para cada um
            for (BlockPos pos : blocksToBreak) {
                if (pos.equals(pPos)) continue; // Não quebra o bloco original de novo

                BlockState blockState = pLevel.getBlockState(pos);
                if (!blockState.isAir() && pStack.isCorrectToolForDrops(blockState)) {
                    if (consumeEnergy(pStack)) {
                        pLevel.destroyBlock(pos, true, player);
                    } else {
                        // Para de quebrar se a energia acabar
                        break;
                    }
                }
            }
        }
        return super.mineBlock(pStack, pLevel, pState, pPos, pEntityLiving);
    }

    // --- MÉTODOS DE TROCA DE MODO E VISUAIS ---

    @Override
    public void cycleMode(ItemStack stack, Player player) {
        int currentRange = getRange(stack);
        int currentIndex = 0;
        for (int i = 0; i < RANGES.length; i++) { if (RANGES[i] == currentRange) { currentIndex = i; break; } }
        int nextIndex = (currentIndex + 1) % RANGES.length;
        int newRange = RANGES[nextIndex];

        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt(TAG_RANGE, newRange);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static int getRange(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.copyTag().contains(TAG_RANGE)) {
            return customData.copyTag().getInt(TAG_RANGE);
        }
        return RANGES[0];
    }

    public static List<BlockPos> getPositions(BlockPos initialPos, Direction side, int range) {
        List<BlockPos> positions = new ArrayList<>();
        if (side == Direction.DOWN || side == Direction.UP) {
            for (int x = -range; x <= range; x++) for (int z = -range; z <= range; z++) positions.add(initialPos.offset(x, 0, z));
        } else if (side == Direction.NORTH || side == Direction.SOUTH) {
            for (int x = -range; x <= range; x++) for (int y = -range; y <= range; y++) positions.add(initialPos.offset(x, y, 0));
        } else if (side == Direction.EAST || side == Direction.WEST) {
            for (int y = -range; y <= range; y++) for (int z = -range; z <= range; z++) positions.add(initialPos.offset(0, y, z));
        }
        return positions;
    }

    // Arquivo: HammerItem.java

    @Override
    public Component getModeHudComponent(ItemStack stack) {
        int range = getRange(stack);
        int dimension = (range == 0) ? 1 : (range * 2) + 1;
        String areaText = dimension + "x" + dimension;

        // Define a cor baseada no range atual
        ChatFormatting color;
        switch (range) {
            case 1:  // 3x3
                color = ChatFormatting.GREEN;
                break;
            case 2:  // 5x5
                color = ChatFormatting.YELLOW;
                break;
            case 3:  // 7x7
                color = ChatFormatting.RED;
                break;
            default: // 1x1 (range 0)
                color = ChatFormatting.WHITE;
                break;
        }

        // Cria um componente para o texto da área e aplica a cor
        Component coloredArea = Component.literal(areaText).withStyle(color);

        // Retorna o componente final usando a nova chave de tradução
        return Component.translatable("tooltip.ufo.area_mode", coloredArea);
    }

    // --- MÉTODO ADICIONADO PARA O NOME ARCO-ÍRIS ---
    @Override
    public Component getName(ItemStack stack) {
        return IEnergyTool.super.getName(stack);
    }

    @Override
    public int getEnergyPerUse() {
        return 0; // Você talvez queira ajustar isso para o martelo
    }

    // ... (O resto dos seus métodos: appendHoverText, isBarVisible, etc. se estiverem aqui, continuam)
}