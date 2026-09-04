package com.raishxn.ufo.network;

import com.raishxn.ufo.block.entity.IUniversalMultiblockController;
import com.raishxn.ufo.block.entity.DimensionalMatterAssemblerBlockEntity;
import com.raishxn.ufo.menu.DimensionalMatterAssemblerMenu;
import com.raishxn.ufo.block.entity.StellarNexusControllerBE;
import com.raishxn.ufo.screen.AbstractUniversalMultiblockControllerMenu;
import com.raishxn.ufo.screen.StellarNexusControllerMenu;
import com.raishxn.ufocore.api.network.MachineAction;
import com.raishxn.ufocore.neoforge.network.UfoMachinePacketGuard;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/** Central validation for every client-triggered machine action. */
public final class MachinePacketGuard {
    private MachinePacketGuard() {
    }

    public enum Action {
        CHANGE_RECIPE(1L),
        START_OPERATION(4L),
        TOGGLE_SAFE_MODE(4L),
        TOGGLE_AUTO_START(4L),
        TOGGLE_LOCK(4L),
        TOGGLE_OVERCLOCK(4L),
        TOGGLE_STELLAR_PAUSE(2L),
        TOGGLE_PROCESS_PAUSE(2L),
        CHANGE_SIDE_CONFIG(2L),
        SCAN_STRUCTURE(20L),
        AUTO_BUILD(20L);

        private final MachineAction coreAction;

        Action(long minimumIntervalTicks) {
            this.coreAction = new MachineAction("ufo:" + name().toLowerCase(Locale.ROOT), minimumIntervalTicks);
        }
    }

    public static @Nullable StellarNexusControllerBE requireStellar(
            IPayloadContext context,
            BlockPos pos,
            Action action) {
        return UfoMachinePacketGuard.require(context, pos, StellarNexusControllerMenu.class,
                StellarNexusControllerMenu::getBlockEntity, action.coreAction);
    }

    public static @Nullable IUniversalMultiblockController requireUniversal(
            IPayloadContext context,
            BlockPos pos,
            Action action) {
        BlockEntity current = UfoMachinePacketGuard.require(context, pos,
                AbstractUniversalMultiblockControllerMenu.class,
                AbstractUniversalMultiblockControllerMenu::getBlockEntity, action.coreAction);
        return current instanceof IUniversalMultiblockController controller ? controller : null;
    }

    public static @Nullable DimensionalMatterAssemblerBlockEntity requireDma(
            IPayloadContext context,
            BlockPos pos,
            Action action) {
        return UfoMachinePacketGuard.require(context, pos, DimensionalMatterAssemblerMenu.class,
                DimensionalMatterAssemblerMenu::getHost, action.coreAction);
    }
}
