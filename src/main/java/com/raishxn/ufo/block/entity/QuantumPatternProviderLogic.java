package com.raishxn.ufo.block.entity;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.helpers.patternprovider.PatternProviderLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Routes legacy Hatch plans to DMAs and Pattern Buffer plans to multiblocks. */
public class QuantumPatternProviderLogic extends PatternProviderLogic {

    private final QuantumPatternHatchBE hatch;
    private com.raishxn.ufo.wireless.QuantumWirelessLinks.Target remoteDelivery;

    public boolean hasRemoteDelivery() { return remoteDelivery != null; }

    public appeng.helpers.patternprovider.PatternProviderTarget remoteDeliveryAdapter() {
        if (remoteDelivery == null) return null;
        var be = hatch.wirelessLinks().resolve(hatch, remoteDelivery);
        return be == null ? null : appeng.helpers.patternprovider.PatternProviderTarget.get(
                hatch.getLevel(), remoteDelivery.pos(), be, remoteDelivery.face(),
                new appeng.me.helpers.MachineSource(hatch));
    }

    @Override
    public void writeToNBT(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.writeToNBT(tag, registries);
        tag.remove("ufoRemoteDelivery");
        if (remoteDelivery != null) {
            var pending = new net.minecraft.nbt.CompoundTag();
            pending.putLong("pos", remoteDelivery.pos().asLong());
            pending.putInt("face", remoteDelivery.face().ordinal());
            pending.putUUID("identity", remoteDelivery.identity());
            tag.put("ufoRemoteDelivery", pending);
        }
    }

    @Override
    public void readFromNBT(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.readFromNBT(tag, registries);
        remoteDelivery = null;
        var pending = tag.getCompound("ufoRemoteDelivery");
        int face = pending.getInt("face");
        if (pending.hasUUID("identity") && face >= 0 && face < Direction.values().length) {
            remoteDelivery = new com.raishxn.ufo.wireless.QuantumWirelessLinks.Target(
                    BlockPos.of(pending.getLong("pos")), Direction.values()[face], pending.getUUID("identity"));
        }
    }

    public QuantumPatternProviderLogic(QuantumPatternHatchBE hatch, int patternInventorySize) {
        super(hatch.getMainNode(), hatch, patternInventorySize);
        this.hatch = hatch;
        this.getConfigManager().putSetting(Settings.PATTERN_ACCESS_TERMINAL, YesNo.NO);
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, appeng.api.stacks.KeyCounter[] inputHolder) {
        @Nullable Level level = hatch.getLevel();
        @Nullable BlockPos controllerPos = hatch.getControllerPos();
        var delivery = (com.raishxn.ufo.mixin.InvokerPatternProviderLogic) this;
        // Do not redirect partially delivered inputs if wireless mode or links change.
        if (!delivery.ufo$getSendList().isEmpty()) return false;
        remoteDelivery = null;

        boolean patternBuffer = hatch.isPatternBuffer();

        if (patternBuffer) {
            return pushPatternToMultiblocks(patternDetails, inputHolder, level, controllerPos, delivery);
        }

        // A legacy Hatch never becomes a multiblock endpoint again. Keep this
        // stale-world guard so an old controller link cannot eject elsewhere.
        if (level != null && controllerPos != null) {
            var controllerBe = level.getBlockEntity(controllerPos);
            if (controllerBe instanceof ICraftingMachine machine && machine.acceptsPlans()) {
                Direction direction = hatch.getPushDirectionForController();
                if (machine.pushPattern(patternDetails, inputHolder, direction)) {
                    delivery.ufo$onPushPatternSuccess(patternDetails);
                    return true;
                }
            }

            return false;
        }

        if (hatch.wirelessLinks().enabled()) {
            var invoker = (com.raishxn.ufo.mixin.InvokerPatternProviderLogic) this;
            if (level == null || !hatch.getMainNode().isActive() || !getAvailablePatterns().contains(patternDetails)
                    || !invoker.ufo$getSendList().isEmpty()
                    || getCraftingLockedReason() != appeng.api.config.LockCraftingMode.NONE) return false;
            for (int i = 0; i < Math.min(32, hatch.wirelessLinks().size()); i++) {
                var target = hatch.wirelessLinks().next();
                if (target == null) continue;
                var targetBe = hatch.wirelessLinks().resolve(hatch, target);
                if (targetBe == null) continue;
                if (!(targetBe instanceof DimensionalMatterAssemblerBlockEntity)) continue;
                var machine = ICraftingMachine.of(level, target.pos(), target.face());
                if (machine != null && machine.acceptsPlans()
                        && machine.pushPattern(patternDetails, inputHolder, target.face())) {
                    invoker.ufo$onPushPatternSuccess(patternDetails);
                    return true;
                }
                if (machine != null && machine.acceptsPlans()) continue;
                if (!patternDetails.supportsPushInputsToExternalInventory()) continue;
                remoteDelivery = target;
                var adapter = remoteDeliveryAdapter();
                remoteDelivery = null;
                if (adapter == null) continue;
                var keys = new java.util.HashSet<appeng.api.stacks.AEKey>();
                boolean accepts = true;
                for (var counter : inputHolder) {
                    for (var input : counter) {
                        keys.add(input.getKey().dropSecondary());
                        if (adapter.insert(input.getKey(), input.getLongValue(), appeng.api.config.Actionable.SIMULATE) == 0) {
                            accepts = false;
                        }
                    }
                }
                // AE2 blocking covers every configured pattern, not only this dispatch.
                // Include the supplied keys too, so ingredient substitutions remain protected.
                if (!accepts || (isBlocking() && adapter.containsPatternInput(
                        com.raishxn.ufo.wireless.WirelessBlockingInputs.collect(invoker.ufo$getPatternInputs(), keys)))) continue;
                var destination = hatch.wirelessLinks().resolve(hatch, target);
                if (destination instanceof DimensionalMatterAssemblerBlockEntity dma
                        && !dma.canAcceptWirelessInputs(inputHolder)) continue;
                remoteDelivery = target;
                invoker.ufo$setSendDirection(target.face());
                // Consume into AE2-owned storage before delivery. Unaccepted remainders
                // stay persisted there and are retried by the grid tick service.
                patternDetails.pushInputsToExternalInventory(inputHolder, (key, amount) -> {
                    invoker.ufo$addToSendList(key, amount);
                });
                hatch.setChanged();
                invoker.ufo$onPushPatternSuccess(patternDetails);
                invoker.ufo$sendStacksOut();
                return true;
            }
            return false;
        }

        // Standalone legacy Hatch behavior remains AE2-compatible.
        return super.pushPattern(patternDetails, inputHolder);
    }

    private boolean pushPatternToMultiblocks(
            IPatternDetails patternDetails,
            appeng.api.stacks.KeyCounter[] inputHolder,
            @Nullable Level level,
            @Nullable BlockPos controllerPos,
            com.raishxn.ufo.mixin.InvokerPatternProviderLogic delivery) {
        if (level == null || !hatch.getMainNode().isActive()
                || !getAvailablePatterns().contains(patternDetails)
                || !delivery.ufo$getSendList().isEmpty()
                || getCraftingLockedReason() != appeng.api.config.LockCraftingMode.NONE) {
            return false;
        }

        List<MultiblockRoute> routes = new ArrayList<>();
        if (controllerPos != null && level.getBlockEntity(controllerPos) instanceof ICraftingMachine localMachine) {
            routes.add(new MultiblockRoute(localMachine, hatch.getPushDirectionForController()));
        }

        if (hatch.wirelessLinks().enabled()) {
            for (var target : hatch.wirelessLinks().targets()) {
                var targetBe = hatch.wirelessLinks().resolve(hatch, target);
                if (targetBe instanceof QuantumPatternProxyBE proxy && proxy.bindPatternBuffer(hatch)) {
                    routes.add(new MultiblockRoute(proxy, target.face()));
                }
            }
        }

        int routeCount = routes.size();
        int start = hatch.getPatternDispatchStart(routeCount);
        for (int offset = 0; offset < routeCount; offset++) {
            int routeIndex = (start + offset) % routeCount;
            MultiblockRoute route = routes.get(routeIndex);
            if (route.machine().acceptsPlans()
                    && route.machine().pushPattern(patternDetails, inputHolder, route.direction())) {
                hatch.completePatternDispatch(routeIndex, routeCount);
                delivery.ufo$onPushPatternSuccess(patternDetails);
                return true;
            }
        }
        return false;
    }

    private record MultiblockRoute(ICraftingMachine machine, Direction direction) {
    }
}
