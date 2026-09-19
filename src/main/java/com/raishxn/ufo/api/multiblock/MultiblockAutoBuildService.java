package com.raishxn.ufo.api.multiblock;

import com.raishxn.ufo.util.LoadedBlockEntityLookup;

/*
 * Auto-build flow adapted from AE2 Lightning Tech's Matrix/Tianshu auto-build.
 * Copyright AE2 Lightning Tech contributors. Licensed under LGPL-3.0.
 */

import net.minecraft.ChatFormatting;
import net.minecraft.core.SectionPos;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.raishxn.ufo.item.StructureScannerSettings;

/** Server-owned gradual auto-build sessions. Wrong occupied blocks are never replaced. */
@EventBusSubscriber(modid = "ufo")
public final class MultiblockAutoBuildService {
    private static final Map<SessionKey, Session> SESSIONS = new HashMap<>();

    private MultiblockAutoBuildService() { }

    public static void start(ServerPlayer player, BlockEntity controllerBlockEntity) {
        start(player, controllerBlockEntity, StructureScannerSettings.Mode.BUILD, true);
    }

    public static void start(ServerPlayer player, BlockEntity controllerBlockEntity,
                             StructureScannerSettings.Mode mode, boolean hatchMode) {
        if (!(player.level() instanceof ServerLevel level)
                || !(controllerBlockEntity instanceof IMultiblockController controller)) return;
        var definitionOptional = MultiblockControllerDefinitions.getDefinition(controllerBlockEntity);
        if (definitionOptional.isEmpty()) return;
        SessionKey key = new SessionKey(level.dimension(), controllerBlockEntity.getBlockPos().immutable());
        if (SESSIONS.containsKey(key)) {
            message(player, "Auto-build is already in progress.", ChatFormatting.YELLOW);
            return;
        }
        if (controller.isAssembled() && mode != StructureScannerSettings.Mode.DEMOLISH
                && !(mode == StructureScannerSettings.Mode.REPLACE && !hatchMode)) {
            message(player, "The structure is already complete.", ChatFormatting.GREEN);
            return;
        }

        var definition = definitionOptional.get();
        var pattern = definition.pattern();
        var facing = MultiblockControllerDefinitions.getPatternFacing(
                controllerBlockEntity, controllerBlockEntity.getBlockState());
        var unavailable = new ArrayList<BlockPos>();
        if (mode == StructureScannerSettings.Mode.DEMOLISH) {
            startDemolition(player, level, key, controllerBlockEntity, controller, pattern, facing);
            return;
        }
        boolean replace = mode == StructureScannerSettings.Mode.REPLACE;
        var plan = MultiblockAutoBuildPlan.create(
                pattern.getPattern(), pattern.getControllerChar(), pattern.getControllerCol(), pattern.getControllerRow(),
                definition.defaultCreativeStates(), target -> !target.isAir(), (local, symbol, target) -> {
                    BlockPos world = worldPos(pattern, controllerBlockEntity.getBlockPos(), local, facing);
                    if (!level.isInWorldBounds(world) || !level.hasChunk(SectionPos.blockToSectionCoord(world.getX()), SectionPos.blockToSectionCoord(world.getZ()))) {
                        unavailable.add(world);
                        return MultiblockAutoBuildPlan.SlotState.BLOCKED;
                    }
                    BlockState current = level.getBlockState(world);
                    if (pattern.matchesSlot(symbol, current, level, world)
                            && (hatchMode || current.getBlock() == target.getBlock())) {
                        return MultiblockAutoBuildPlan.SlotState.MATCHING;
                    }
                    if (current.isAir()) return MultiblockAutoBuildPlan.SlotState.EMPTY;
                    return replace ? MultiblockAutoBuildPlan.SlotState.REPLACE
                            : MultiblockAutoBuildPlan.SlotState.BLOCKED;
                });
        if (!unavailable.isEmpty()) {
            message(player, "Auto-build stopped: part of the structure is outside loaded chunks or world bounds.", ChatFormatting.RED);
            return;
        }
        if (!plan.blocked().isEmpty()) {
            BlockPos first = worldPos(pattern, controllerBlockEntity.getBlockPos(), plan.blocked().getFirst(), facing);
            message(player, "Auto-build blocked by " + plan.blocked().size() + " occupied position(s); first at "
                    + position(first) + ".", ChatFormatting.RED);
            return;
        }
        List<Work> work = plan.placements().stream()
                .map(placement -> new Work(
                        worldPos(pattern, controllerBlockEntity.getBlockPos(), placement.localPos(), facing),
                        placement.target(), placement.replace(), false))
                .toList();
        Map<Item, Integer> requirements = requirements(work);
        Item unsupported = requirements.keySet().stream().filter(item -> item == Items.AIR).findFirst().orElse(null);
        if (unsupported != null) {
            message(player, "Auto-build stopped: one required block has no placeable item.", ChatFormatting.RED);
            return;
        }
        if (!player.getAbilities().instabuild) {
            List<String> missing = missingRequirements(player, requirements);
            if (!missing.isEmpty()) {
                message(player, "Missing materials: " + String.join(", ", missing) + ".", ChatFormatting.RED);
                return;
            }
        }
        if (plan.placements().isEmpty()) {
            controller.scanStructure(level);
            message(player, "No structural blocks need to be placed.", ChatFormatting.GREEN);
            return;
        }
        SESSIONS.put(key, new Session(player.getUUID(), mode, List.copyOf(work), 0, 0));
        message(player, "Auto-build started: " + plan.placements().size() + " block(s).", ChatFormatting.GREEN);
    }

    private static void startDemolition(ServerPlayer player, ServerLevel level, SessionKey key,
                                        BlockEntity controllerBlockEntity, IMultiblockController controller,
                                        MultiblockPattern pattern, net.minecraft.core.Direction facing) {
        MultiblockPattern.MatchResult result = pattern.match(level, controllerBlockEntity.getBlockPos(), facing);
        List<Work> work = result.allErrors().stream()
                .map(MultiblockPattern.PatternError::pos)
                .filter(level::isLoaded)
                .filter(pos -> !level.getBlockState(pos).isAir())
                .map(pos -> new Work(pos.immutable(), null, false, true))
                .toList();
        if (work.isEmpty()) {
            controller.scanStructure(level);
            message(player, "Demolition found no invalid occupied structure slots.", ChatFormatting.GREEN);
            return;
        }
        SESSIONS.put(key, new Session(player.getUUID(), StructureScannerSettings.Mode.DEMOLISH, work, 0, 0));
        message(player, "Demolition started: " + work.size() + " invalid block(s).", ChatFormatting.YELLOW);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tick(event.getServer());
    }

    private static void tick(MinecraftServer server) {
        Iterator<Map.Entry<SessionKey, Session>> iterator = SESSIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            SessionKey key = entry.getKey();
            Session session = entry.getValue();
            ServerLevel level = server.getLevel(key.dimension());
            ServerPlayer player = server.getPlayerList().getPlayer(session.playerId());
            if (level == null || player == null || player.level() != level
                    || !(LoadedBlockEntityLookup.get(level, key.controllerPos()) instanceof IMultiblockController)) {
                iterator.remove();
                continue;
            }
            if (session.index() >= session.work().size()) { iterator.remove(); continue; }
            Work work = session.work().get(session.index());
            BlockPos world = work.position();
            if (!level.isLoaded(world)) continue;
            if (work.removeOnly()) {
                boolean removed = level.getBlockState(world).isAir() || level.destroyBlock(world, true, player);
                advance(entry, session, removed, player, level, key, iterator);
                continue;
            }
            BlockState current = level.getBlockState(world);
            if (current.equals(work.target())) {
                advance(entry, session, false, player, level, key, iterator);
                continue;
            }
            if (!current.isAir() && !work.replace()) {
                iterator.remove();
                message(player, "Auto-build interrupted by an occupied block at " + position(world) + ".", ChatFormatting.RED);
                refresh(level, key.controllerPos());
                continue;
            }
            Item item = work.target().getBlock().asItem();
            if (!player.getAbilities().instabuild && !consume(player, item)) {
                iterator.remove();
                message(player, "Auto-build interrupted: missing " + item.getDescription().getString() + ".", ChatFormatting.RED);
                refresh(level, key.controllerPos());
                continue;
            }
            if (!current.isAir()) level.destroyBlock(world, true, player);
            if (!level.setBlock(world, work.target(), Block.UPDATE_ALL)) {
                if (!player.getAbilities().instabuild) giveBack(player, item);
                iterator.remove();
                message(player, "Auto-build could not place a block at " + position(world) + ".", ChatFormatting.RED);
                refresh(level, key.controllerPos());
                continue;
            }
            var sound = work.target().getSoundType(level, world, player);
            level.playSound(null, world, sound.getPlaceSound(), SoundSource.BLOCKS,
                    (sound.getVolume() + 1F) / 4F, sound.getPitch() * (.82F + level.random.nextFloat() * .12F));
            advance(entry, session, true, player, level, key, iterator);
        }
    }

    private static void advance(Map.Entry<SessionKey, Session> entry, Session session, boolean placed,
            ServerPlayer player, ServerLevel level, SessionKey key,
            Iterator<Map.Entry<SessionKey, Session>> iterator) {
        Session next = new Session(session.playerId(), session.mode(), session.work(),
                session.index() + 1, session.placed() + (placed ? 1 : 0));
        if (next.index() < next.work().size()) {
            entry.setValue(next);
            return;
        }
        iterator.remove();
        refresh(level, key.controllerPos());
        String verb = next.mode() == StructureScannerSettings.Mode.DEMOLISH ? "removed" : "placed/replaced";
        message(player, "Structure operation " + verb + " " + next.placed() + " block(s).",
                ChatFormatting.GREEN);
    }

    private static void refresh(ServerLevel level, BlockPos pos) {
        if (LoadedBlockEntityLookup.get(level, pos) instanceof IMultiblockController controller) {
            controller.scanStructure(level);
        }
    }

    private static BlockPos worldPos(MultiblockPattern pattern, BlockPos controller,
            MultiblockAutoBuildPlan.LocalPos local, net.minecraft.core.Direction facing) {
        return MultiblockPattern.getRotatedPos(controller,
                local.x() - pattern.getControllerCol(),
                local.y() - pattern.getControllerLayer(),
                local.z() - pattern.getControllerRow(), facing);
    }

    private static Map<Item, Integer> requirements(List<Work> placements) {
        Map<Item, Integer> result = new LinkedHashMap<>();
        for (var placement : placements) result.merge(placement.target().getBlock().asItem(), 1, Integer::sum);
        return result;
    }

    private static List<String> missingRequirements(ServerPlayer player, Map<Item, Integer> requirements) {
        List<String> missing = new ArrayList<>();
        for (var entry : requirements.entrySet()) {
            int available = player.getInventory().countItem(entry.getKey());
            if (available < entry.getValue()) {
                missing.add(entry.getKey().getDescription().getString() + " x" + (entry.getValue() - available));
            }
        }
        return missing;
    }

    private static boolean consume(ServerPlayer player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item) && !stack.isEmpty()) { stack.shrink(1); return true; }
        }
        return false;
    }

    private static void giveBack(ServerPlayer player, Item item) {
        if (!player.getInventory().add(new ItemStack(item))) player.drop(new ItemStack(item), false);
    }

    private static String position(BlockPos pos) { return pos.getX() + ", " + pos.getY() + ", " + pos.getZ(); }
    private static void message(ServerPlayer player, String text, ChatFormatting color) {
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(text).withStyle(color), false);
    }

    @SubscribeEvent public static void onServerStopped(ServerStoppedEvent event) { SESSIONS.clear(); }

    private record SessionKey(ResourceKey<Level> dimension, BlockPos controllerPos) { }
    private record Work(BlockPos position, BlockState target, boolean replace, boolean removeOnly) { }
    private record Session(UUID playerId, StructureScannerSettings.Mode mode,
                           List<Work> work, int index, int placed) { }
}
