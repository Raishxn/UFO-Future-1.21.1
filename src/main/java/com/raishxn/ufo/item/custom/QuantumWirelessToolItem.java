package com.raishxn.ufo.item.custom;

import com.raishxn.ufo.wireless.QuantumWirelessHost;
import com.raishxn.ufo.wireless.QuantumWirelessLinks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

/** Select a source, then toggle remote faces. Sneak-use a source to change mode. */
public class QuantumWirelessToolItem extends Item {
    public QuantumWirelessToolItem(Properties properties) { super(properties.stacksTo(1)); }

    @Override
    public void inventoryTick(net.minecraft.world.item.ItemStack stack, net.minecraft.world.level.Level level,
            net.minecraft.world.entity.Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || level.getGameTime() % 10 != 0
                || !(entity instanceof net.minecraft.world.entity.player.Player player)
                || (!selected && player.getOffhandItem() != stack)) return;
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.hasUUID("ufoWirelessSourceIdentity")
                || !tag.getString("ufoWirelessDimension").equals(level.dimension().location().toString())) return;
        var pos = BlockPos.of(tag.getLong("ufoWirelessSource"));
        if (!level.hasChunkAt(pos)) return;
        var be = level.getBlockEntity(pos);
        if (!(be instanceof QuantumWirelessHost host)
                || !QuantumWirelessLinks.matches(be, tag.getUUID("ufoWirelessSourceIdentity"))) return;
        var snapshot = new net.minecraft.nbt.CompoundTag();
        host.wirelessLinks().save(snapshot);
        snapshot.putInt("ufoWirelessRange", host.wirelessLinks().range());
        if (!snapshot.equals(tag.getCompound("ufoWirelessView"))) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> {
                data.put("ufoWirelessView", snapshot);
                data.putInt("ufoWirelessSourceLinks", host.wirelessLinks().size());
            });
        }
    }

    @Override
    public void appendHoverText(net.minecraft.world.item.ItemStack stack, TooltipContext context,
            java.util.List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ufo.wireless_tool"));
        tooltip.addAll(selectionTooltip(stack));
    }

    public static java.util.List<Component> selectionTooltip(net.minecraft.world.item.ItemStack stack) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.hasUUID("ufoWirelessSourceIdentity")) return java.util.List.of();
        var pos = BlockPos.of(tag.getLong("ufoWirelessSource"));
        return java.util.List.of(Component.translatable("gui.ufo.wireless.source",
                        Component.translatable(tag.getString("ufoWirelessSourceName"))),
                Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ()
                        + " | " + tag.getString("ufoWirelessDimension")),
                Component.translatable("gui.ufo.wireless.links_info", tag.getInt("ufoWirelessSourceLinks")));
    }

    @Override
    public InteractionResult onItemUseFirst(net.minecraft.world.item.ItemStack stack, UseOnContext context) {
        return useOn(context);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var player = context.getPlayer();
        if (player == null || !player.mayBuild() || !level.mayInteract(player, context.getClickedPos())) return InteractionResult.FAIL;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        var target = level.getBlockEntity(context.getClickedPos());
        if (target == null) return InteractionResult.PASS;
        var stack = context.getItemInHand();
        if (target instanceof QuantumWirelessHost host) {
            var node = host.getMainNode().getNode();
            if (node == null) return InteractionResult.FAIL;
            if (player.isShiftKeyDown()) {
                host.wirelessLinks().toggleMode();
                target.setChanged();
                player.displayClientMessage(Component.translatable(host.wirelessLinks().enabled()
                        ? "message.ufo.wireless.enabled" : "message.ufo.wireless.disabled"), true);
            } else {
                // Pattern Buffers exist specifically to dispatch through proxies.
                // Selecting one for the first time enables that route immediately.
                if (!host.wirelessLinks().enabled()
                        && target.getBlockState().is(com.raishxn.ufo.block.MultiblockBlocks.QUANTUM_PATTERN_BUFFER.get())) {
                    host.wirelessLinks().toggleMode();
                    target.setChanged();
                }
                if (!host.wirelessLinks().enabled()) {
                    player.displayClientMessage(Component.translatable("message.ufo.wireless.enable_first"), true);
                    return InteractionResult.FAIL;
                }
                CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
                    tag.putLong("ufoWirelessSource", target.getBlockPos().asLong());
                    tag.putString("ufoWirelessDimension", level.dimension().location().toString());
                    tag.putUUID("ufoWirelessSourceIdentity", QuantumWirelessLinks.identity(target));
                    tag.putString("ufoWirelessSourceName", target.getBlockState().getBlock().getDescriptionId());
                    tag.putInt("ufoWirelessSourceLinks", host.wirelessLinks().size());
                    var snapshot = new net.minecraft.nbt.CompoundTag();
                    host.wirelessLinks().save(snapshot);
                    snapshot.putInt("ufoWirelessRange", host.wirelessLinks().range());
                    tag.put("ufoWirelessView", snapshot);
                });
                player.displayClientMessage(Component.translatable("message.ufo.wireless.selected"), true);
            }
            return InteractionResult.SUCCESS;
        }
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.hasUUID("ufoWirelessSourceIdentity") || !tag.getString("ufoWirelessDimension").equals(level.dimension().location().toString())) return InteractionResult.FAIL;
        var sourcePos = BlockPos.of(tag.getLong("ufoWirelessSource"));
        if (!level.hasChunkAt(sourcePos) || !level.mayInteract(player, sourcePos)) return InteractionResult.FAIL;
        var source = level.getBlockEntity(sourcePos);
        if (!(source instanceof QuantumWirelessHost host) || !QuantumWirelessLinks.matches(source, tag.getUUID("ufoWirelessSourceIdentity"))) return InteractionResult.FAIL;
        if (!host.wirelessLinks().enabled()) {
            player.displayClientMessage(Component.translatable("message.ufo.wireless.enable_first"), true);
            return InteractionResult.FAIL;
        }
        var node = host.getMainNode().getNode();
        if (node == null) return InteractionResult.FAIL;
        if (source instanceof com.raishxn.ufo.block.entity.QuantumPatternHatchBE provider) {
            boolean validTarget = provider.isPatternBuffer()
                    ? target instanceof com.raishxn.ufo.block.entity.QuantumPatternProxyBE
                    : target instanceof com.raishxn.ufo.block.entity.DimensionalMatterAssemblerBlockEntity;
            if (!validTarget) {
                player.displayClientMessage(Component.translatable(provider.isPatternBuffer()
                        ? "message.ufo.pattern_buffer.proxy_only"
                        : "message.ufo.pattern_hatch.dma_only"), true);
                return InteractionResult.FAIL;
            }
        }
        if (!host.wirelessLinks().inRange(sourcePos, target.getBlockPos())) {
            player.displayClientMessage(Component.translatable("message.ufo.wireless.out_of_range", host.wirelessLinks().range()), true);
            return InteractionResult.FAIL;
        }
        var remoteHost = level.getCapability(appeng.api.AECapabilities.IN_WORLD_GRID_NODE_HOST, target.getBlockPos(), null);
        if (remoteHost != null) {
            var remote = remoteHost.getGridNode(context.getClickedFace());
            if (remote != null && remote.getGrid().size() > 1 && remote.getGrid() != node.getGrid()) return InteractionResult.FAIL;
        }
        if (!host.wirelessLinks().toggle(target, context.getClickedFace())) return InteractionResult.FAIL;
        if (source instanceof com.raishxn.ufo.block.entity.QuantumPatternHatchBE provider
                && provider.isPatternBuffer()
                && target instanceof com.raishxn.ufo.block.entity.QuantumPatternProxyBE proxy) {
            boolean linked = host.wirelessLinks().targets().stream().anyMatch(candidate ->
                    candidate.pos().equals(target.getBlockPos())
                            && candidate.face() == context.getClickedFace());
            if (linked) {
                proxy.bindPatternBuffer(provider);
            } else {
                proxy.unbindPatternBuffer(provider);
            }
        }
        source.setChanged();
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> {
            data.putInt("ufoWirelessSourceLinks", host.wirelessLinks().size());
            var snapshot = new net.minecraft.nbt.CompoundTag();
            host.wirelessLinks().save(snapshot);
            snapshot.putInt("ufoWirelessRange", host.wirelessLinks().range());
            data.put("ufoWirelessView", snapshot);
        });
        player.displayClientMessage(Component.translatable("message.ufo.wireless.links", host.wirelessLinks().size()), true);
        return InteractionResult.SUCCESS;
    }
}
