package com.raishxn.ufo.block;

import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.api.multiblock.MultiblockCasingStyle;
import com.raishxn.ufo.block.entity.AbstractSimpleMultiblockControllerBE;
import com.raishxn.ufo.block.entity.MassiveOutputHatchBE;
import com.raishxn.ufo.block.entity.StellarNexusControllerBE;
import com.raishxn.ufo.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * ME Massive Output Hatch — an AE2-integrated output port for the Stellar Nexus.
 * <p>
 * This block is directional (the face indicates which side connects to AE2 cables)
 * and uses a specialized BlockEntity ({@link MassiveOutputHatchBE}) that extends
 * AE2's {@code AENetworkedBlockEntity} to establish a real grid connection.
 * <p>
 * Unlike the generic {@link StellarNexusPartBlock}, this block has a dedicated
 * BE type because it needs to hold an AE2 grid node and implement
 * {@link com.raishxn.ufo.api.ae.IMassiveInjector}.
 */
public class MassiveOutputHatchBlock extends DirectionalBlock implements net.minecraft.world.level.block.EntityBlock {
    public static final EnumProperty<MultiblockCasingStyle> CASING_STYLE =
            EnumProperty.create("casing_style", MultiblockCasingStyle.class);

    public MassiveOutputHatchBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(CASING_STYLE, MultiblockCasingStyle.DEFAULT));
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends DirectionalBlock> codec() {
        return com.mojang.serialization.MapCodec.unit(
                () -> new MassiveOutputHatchBlock(BlockBehaviour.Properties.of()));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CASING_STYLE);
    }

    // --- Block Entity ---

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MassiveOutputHatchBE(ModBlockEntities.ME_MASSIVE_OUTPUT_HATCH_BE.get(), pos, state);
    }

    // --- Interactions ---

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MassiveOutputHatchBE be) {
            net.minecraft.network.chat.MutableComponent status = be.isNetworkReady()
                    ? net.minecraft.network.chat.Component.literal(
                            be.isLinked() ? "Online — Linked to Controller" : "Online — Standalone")
                            .withStyle(net.minecraft.ChatFormatting.GREEN)
                    : net.minecraft.network.chat.Component.literal("Offline — No ME Network")
                            .withStyle(net.minecraft.ChatFormatting.RED);
            player.displayClientMessage(
                    state.getBlock().getName().copy()
                            .append(net.minecraft.network.chat.Component.literal(": "))
                            .append(status)
                            .append(coolantStatus(be)),
                    true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static net.minecraft.network.chat.Component coolantStatus(MassiveOutputHatchBE be) {
        if (!be.supportsFluidInput()) {
            return net.minecraft.network.chat.Component.empty();
        }
        FluidStack stored = be.getStoredCoolant();
        if (stored.isEmpty()) {
            return net.minecraft.network.chat.Component.literal(" — Coolant tank empty")
                    .withStyle(net.minecraft.ChatFormatting.GRAY);
        }
        return net.minecraft.network.chat.Component.literal(" — ")
                .append(stored.getHoverName())
                .append(net.minecraft.network.chat.Component.literal(
                        " " + stored.getAmount() + "/" + MassiveOutputHatchBE.COOLANT_CAPACITY + " mB"))
                .withStyle(net.minecraft.ChatFormatting.AQUA);
    }

    // --- Multiblock integration ---

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof MassiveOutputHatchBE hatch) {
                BlockPos controllerPos = hatch.getControllerPos();
                if (controllerPos != null && level.getBlockEntity(controllerPos) instanceof IMultiblockController controller) {
                    controller.removePart(pos);
                    controller.scanStructure(level);
                }
                hatch.unlinkFromController();
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block changedBlock, BlockPos changedPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, changedBlock, changedPos, isMoving);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MassiveOutputHatchBE hatch) {
            hatch.refreshGridConnection();
            BlockPos controllerPos = hatch.getControllerPos();
            if (controllerPos != null) {
                markControllerDirty(level, controllerPos);
            }
        }
    }

    private static void markControllerDirty(Level level, BlockPos controllerPos) {
        BlockEntity entity = level.getBlockEntity(controllerPos);
        if (entity instanceof AbstractSimpleMultiblockControllerBE controller) {
            controller.markStructureDirty();
        } else if (entity instanceof StellarNexusControllerBE controller) {
            controller.markStructureDirty();
        } else if (entity instanceof IMultiblockController controller) {
            controller.scanStructure(level);
        }
    }
}
