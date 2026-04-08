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




    // ═══════════════════════════════════════════════════════════
    //  STELLAR NEXUS — Multiblock Components
    // ═══════════════════════════════════════════════════════════

    public static final DeferredBlock<StellarNexusControllerBlock> STELLAR_NEXUS_CONTROLLER = BLOCKS.register("stellar_nexus_controller",
            () -> new StellarNexusControllerBlock(BlockBehaviour.Properties.of()
                    .strength(50.0f, 1200.0f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(StellarNexusControllerBlock.ASSEMBLED) ? 12 : 0)));

    public static final DeferredBlock<MassiveOutputHatchBlock> ME_MASSIVE_OUTPUT_HATCH = BLOCKS.register("me_massive_output_hatch",
            () -> new MassiveOutputHatchBlock(BlockBehaviour.Properties.of()
                    .strength(25.0f, 600.0f)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<MassiveOutputHatchBlock> ME_MASSIVE_FLUID_HATCH = BLOCKS.register("me_massive_fluid_hatch",
            () -> new MassiveOutputHatchBlock(BlockBehaviour.Properties.of()
                    .strength(25.0f, 600.0f)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<MassiveOutputHatchBlock> ME_MASSIVE_INPUT_HATCH = BLOCKS.register("me_massive_input_hatch",
            () -> new MassiveOutputHatchBlock(BlockBehaviour.Properties.of()
                    .strength(25.0f, 600.0f)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<MassiveOutputHatchBlock> AE_ENERGY_INPUT_HATCH = BLOCKS.register("ae_energy_input_hatch",
            () -> new MassiveOutputHatchBlock(BlockBehaviour.Properties.of()
                    .strength(25.0f, 600.0f)
                    .requiresCorrectToolForDrops()));




    public static final DeferredBlock<StellarNexusPartBlock> STELLAR_FIELD_GENERATOR_T1 = BLOCKS.register("stellar_field_generator_t1",
            () -> new StellarNexusPartBlock(BlockBehaviour.Properties.of()
                    .strength(25.0f, 600.0f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 4)));

    public static final DeferredBlock<StellarNexusPartBlock> STELLAR_FIELD_GENERATOR_T2 = BLOCKS.register("stellar_field_generator_t2",
            () -> new StellarNexusPartBlock(BlockBehaviour.Properties.of()
                    .strength(35.0f, 800.0f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 7)));

    public static final DeferredBlock<StellarNexusPartBlock> STELLAR_FIELD_GENERATOR_T3 = BLOCKS.register("stellar_field_generator_t3",
            () -> new StellarNexusPartBlock(BlockBehaviour.Properties.of()
                    .strength(50.0f, 1200.0f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 10)));


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