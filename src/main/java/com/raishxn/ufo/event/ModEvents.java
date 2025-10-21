package com.raishxn.ufo.event;

import com.raishxn.ufo.UfoMod; // Importe sua classe principal do mod
import com.raishxn.ufo.item.custom.HammerItem;
import com.raishxn.ufo.util.EnergyToolHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.minecraft.core.Direction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// --- CORREÇÃO ADICIONADA AQUI ---
// Esta anotação registra a classe para ouvir os eventos do JOGO.
@EventBusSubscriber(modid = UfoMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ModEvents {

    private static final Set<BlockPos> HARVESTED_BLOCKS = new HashSet<>();

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getMainHandItem();

        if(mainHandItem.getItem() instanceof HammerItem hammer && player instanceof ServerPlayer serverPlayer) {
            BlockPos initialPos = event.getPos();

            if (HARVESTED_BLOCKS.contains(initialPos)) {
                return;
            }

            int range = HammerItem.getRange(mainHandItem);
            if (range == 0) return;

            // Pega a face do bloco que o jogador está olhando para a lógica de quebra plana
            Direction side = player.getDirection();
            List<BlockPos> blocksToDestroy = HammerItem.getPositions(initialPos, side, range);

            for(BlockPos pos : blocksToDestroy) {
                if(pos.equals(initialPos)) {
                    continue;
                }

                BlockState blockState = player.level().getBlockState(pos);
                if (EnergyToolHelper.hasEnoughEnergy(mainHandItem, hammer.getEnergyPerUse(), false) &&
                        !blockState.isAir() &&
                        mainHandItem.isCorrectToolForDrops(blockState)) {

                    HARVESTED_BLOCKS.add(pos);
                    boolean removed = serverPlayer.gameMode.destroyBlock(pos);
                    HARVESTED_BLOCKS.remove(pos);

                    if (removed) {
                        EnergyToolHelper.extractEnergy(mainHandItem, hammer.getEnergyPerUse(), false);
                    }
                } else if (!EnergyToolHelper.hasEnoughEnergy(mainHandItem, hammer.getEnergyPerUse(), false)) {
                    break;
                }
            }
        }
    }
}