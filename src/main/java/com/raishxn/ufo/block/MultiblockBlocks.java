package com.raishxn.ufo.block;

import com.mojang.serialization.MapCodec;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.item.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class MultiblockBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(UfoMod.MOD_ID);

    // --- Blocos Simples (não direcionais) ---
    public static final DeferredBlock<Block> ENTROPY_ASSEMBLER_CORE_CASING = registerBlock("entropy_assembler_core_casing",
            () -> new Block(BlockBehaviour.Properties.of().strength(5.0f).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> ENTROPY_SINGULARITY_CASING = registerBlock("entropy_singularity_casing",
            () -> new Block(BlockBehaviour.Properties.of().strength(5.0f).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> ENTROPY_COMPUTER_CONDENSATION_MATRIX = registerBlock("entropy_computer_condensation_matrix",

            () -> new Block(BlockBehaviour.Properties.of().strength(5.0f).requiresCorrectToolForDrops()));



// --- Blocos de Componentes (direcionais) ---

    public static final DeferredBlock<Block> ENTROPY_CONTAINMENT_CHAMBER_COMPONENTS = registerBlock("entropy_containment_chamber_components",

            () -> new OrientableMultiblock(BlockBehaviour.Properties.of().strength(5.0f).requiresCorrectToolForDrops()));


    public static final DeferredBlock<Block> ENTROPY_COOLANT_MATRIX_COMPONENTS = registerBlock("entropy_coolant_matrix_components",

            () -> new OrientableMultiblock(BlockBehaviour.Properties.of().strength(5.0f).requiresCorrectToolForDrops()));


    public static final DeferredBlock<Block> ENTROPY_CATALYST_BANK_COMPONENTS = registerBlock("entropy_catalyst_bank_components",

            () -> new OrientableMultiblock(BlockBehaviour.Properties.of().strength(5.0f).requiresCorrectToolForDrops()));


    // --- Bloco Controlador ---
    public static final DeferredBlock<Block> ENTROPY_SINGULARITY_ARRAY_CONTROLLER = registerBlock("entropy_singularity_array_controller",
            () -> new ControllerBlock(BlockBehaviour.Properties.of().strength(5.0f).requiresCorrectToolForDrops()));


    // --- Métodos de Registro ---
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    // --- O método registerBlockItem foi REMOVIDO ---

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    // O resto da classe (OrientableMultiblock, ControllerBlock) permanece igual...
    public static class OrientableMultiblock extends DirectionalBlock {
        public static final MapCodec<OrientableMultiblock> CODEC = simpleCodec(OrientableMultiblock::new);

        public OrientableMultiblock(Properties properties) {
            super(properties);
            // --- CORREÇÃO: Adiciona o registro do estado padrão ---
            this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        }

        @Override
        protected MapCodec<? extends DirectionalBlock> codec() {
            return CODEC;
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING);
        }
    }

    public static class ControllerBlock extends DirectionalBlock {
        public static final MapCodec<ControllerBlock> CODEC = simpleCodec(ControllerBlock::new);
        public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
        public ControllerBlock(Properties properties) { super(properties); this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false)); }
        @Override protected MapCodec<? extends DirectionalBlock> codec() { return CODEC; }
        @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite()); }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, ACTIVE); }
    }
}