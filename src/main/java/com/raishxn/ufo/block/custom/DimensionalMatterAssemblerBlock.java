package com.raishxn.ufo.block.custom;

import com.mojang.serialization.MapCodec;
import com.raishxn.ufo.block.entity.DimensionalMatterAssemblerBlockEntity;
import com.raishxn.ufo.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
// IMPORT CRÍTICO FALTANTE:
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

public class DimensionalMatterAssemblerBlock extends BaseEntityBlock {
    public static final MapCodec<DimensionalMatterAssemblerBlock> CODEC = simpleCodec(DimensionalMatterAssemblerBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public DimensionalMatterAssemblerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DimensionalMatterAssemblerBlockEntity(pos, state);
    }

    // =================================================================================
    // --- NOVO MÉTODO DE INTERAÇÃO (1.21.1) ---
    // Este método substitui 'useWithoutItem' e 'useItemOn'
    // =================================================================================

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                           Player player, InteractionHand hand, BlockHitResult hitResult) {

        // 1. Tente a interação com fluídos (baldes, etc.) PRIMEIRO.
        // Esta função DEVE ser chamada antes de qualquer checagem de level.isClientSide()
        if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hitResult.getDirection())) {
            // Se o FluidUtil lidou com a interação, retornamos SUCCESS em ambos os lados.
            return ItemInteractionResult.SUCCESS;
        }

        // 2. Se a interação com fluído falhou (ex: não era um balde),
        // agora podemos separar a lógica de cliente e servidor.

        // No CLIENTE, se não for um balde, só queremos a animação de "clique"
        // e não tentamos abrir a GUI.
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        // No SERVIDOR, abrimos a GUI.
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DimensionalMatterAssemblerBlockEntity machine && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu((MenuProvider) machine, pos);
            // Usamos CONSUME para indicar que o clique foi "gasto" abrindo o menu.
            return ItemInteractionResult.CONSUME;
        }

        // Se algo deu errado (ex: não achou o BlockEntity no servidor), passamos.
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // --- O MÉTODO ANTIGO 'useWithoutItem' FOI REMOVIDO ---

    // =================================================================================

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;

        // --- CORRIGIDO: Ticker para o BlockEntity ---
        // (Assumindo que seu registro se chama DMA_BE como no import)
        return createTickerHelper(blockEntityType, ModBlockEntities.DMA_BE.get(), DimensionalMatterAssemblerBlockEntity::tick);
    }
}