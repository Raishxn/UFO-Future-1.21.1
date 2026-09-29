package com.raishxn.ufo.api.multiblock;

import com.raishxn.ufo.util.LoadedBlockEntityLookup;

/*
 * Auto-build flow adapted from AE2 Lightning Tech's Matrix/Tianshu auto-build.
 * Copyright AE2 Lightning Tech contributors. Licensed under LGPL-3.0.
 */

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Direction;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;
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
import com.raishxn.ufo.item.StructureScannerAe2Link;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.StellarNexusPartBE;
import com.raishxn.ufo.block.entity.StellarNexusControllerBE;
import com.raishxn.ufo.block.entity.QuantumCryoforgeControllerBE;

/** Server-owned gradual auto-build sessions. Wrong occupied blocks are never replaced. */
@EventBusSubscriber(modid = "ufo")
public final class MultiblockAutoBuildService {
    private static final Map<SessionKey, Session> SESSIONS = new HashMap<>();

    private MultiblockAutoBuildService() { }

    public static void start(ServerPlayer player, BlockEntity controllerBlockEntity) {
        start(player, controllerBlockEntity,
                new StructureScannerSettings(StructureScannerSettings.Mode.BUILD, true, 1, false),
                ItemStack.EMPTY);
    }

    public static void start(ServerPlayer player, BlockEntity controllerBlockEntity,
                             StructureScannerSettings settings, ItemStack scanner) {
        StructureScannerSettings.Mode mode = settings.mode();
        if (!(player.level() instanceof ServerLevel level)
                || !(controllerBlockEntity instanceof IMultiblockController controller)) return;
        var definitionOptional = MultiblockControllerDefinitions.getDefinition(controllerBlockEntity);
        if (definitionOptional.isEmpty()) return;
        if (!player.mayUseItemAt(controllerBlockEntity.getBlockPos(), Direction.UP, ItemStack.EMPTY)
                || !level.mayInteract(player, controllerBlockEntity.getBlockPos())) {
            message(player, "Auto-build blocked: you may not modify blocks here.", ChatFormatting.RED);
            return;
        }
        SessionKey key = new SessionKey(level.dimension(), controllerBlockEntity.getBlockPos().immutable());
        if (SESSIONS.containsKey(key)) {
            message(player, "Auto-build is already in progress.", ChatFormatting.YELLOW);
            return;
        }
        if (controller.isAssembled() && mode != StructureScannerSettings.Mode.DEMOLISH
                && mode != StructureScannerSettings.Mode.REPLACE) {
            message(player, "The structure is already complete.", ChatFormatting.GREEN);
            return;
        }

        var definition = definitionOptional.get();
        var pattern = definition.pattern();
        var facing = MultiblockControllerDefinitions.getPatternFacing(
                controllerBlockEntity, controllerBlockEntity.getBlockState());
        var unavailable = new ArrayList<BlockPos>();
        if (mode == StructureScannerSettings.Mode.DEMOLISH) {
            startDemolition(player, level, key, controllerBlockEntity, controller, definition, facing);
            return;
        }
        boolean replace = mode == StructureScannerSettings.Mode.REPLACE;
        int fieldTier = mode == StructureScannerSettings.Mode.BUILD
                ? installedFieldTier(level, pattern, controllerBlockEntity.getBlockPos(), facing,
                        definition.defaultCreativeStates(), settings.fieldTier())
                : settings.fieldTier();
        Map<Character, BlockState> selectedStates = selectFieldTier(
                definition.defaultCreativeStates(), fieldTier);
        var plan = MultiblockAutoBuildPlan.create(
                pattern.getPattern(), pattern.getControllerChar(), pattern.getControllerCol(), pattern.getControllerRow(),
                selectedStates, target -> !target.isAir(), (local, symbol, target) -> {
                    BlockPos world = worldPos(pattern, controllerBlockEntity.getBlockPos(), local, facing);
                    if (!level.isInWorldBounds(world) || !level.hasChunk(SectionPos.blockToSectionCoord(world.getX()), SectionPos.blockToSectionCoord(world.getZ()))) {
                        unavailable.add(world);
                        return MultiblockAutoBuildPlan.SlotState.BLOCKED;
                    }
                    BlockState current = level.getBlockState(world);
                    // A slot can accept several blocks. Keep any valid installed hatch or
                    // casing; only fields must follow the selected uniform tier.
                    if (pattern.matchesSlot(symbol, current, level, world)
                            && (!isFieldGenerator(target) || current.getBlock() == target.getBlock())) {
                        return MultiblockAutoBuildPlan.SlotState.MATCHING;
                    }
                    if (current.isAir()) return MultiblockAutoBuildPlan.SlotState.EMPTY;
                    return replace && canReplaceBlockEntity(level.getBlockEntity(world))
                            ? MultiblockAutoBuildPlan.SlotState.REPLACE
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
        List<Work> work = new ArrayList<>(plan.placements().stream()
                .map(placement -> new Work(
                        worldPos(pattern, controllerBlockEntity.getBlockPos(), placement.localPos(), facing),
                        placement.target(), placement.replace(), false))
                .toList());
        Component missingSlots = addRequiredHatches(level, controllerBlockEntity, pattern, facing,
                work, replace);
        if (missingSlots != null) {
            message(player, Component.translatable(replace
                    ? "message.ufo.autobuild.no_slot_replace" : "message.ufo.autobuild.no_slot",
                    missingSlots), ChatFormatting.RED);
            return;
        }
        Map<Item, Integer> requirements = requirements(work);
        Item unsupported = requirements.keySet().stream().filter(item -> item == Items.AIR).findFirst().orElse(null);
        if (unsupported != null) {
            message(player, "Auto-build stopped: one required block has no placeable item.", ChatFormatting.RED);
            return;
        }
        if (!player.getAbilities().instabuild) {
            List<String> missing = missingRequirements(player, requirements, settings, scanner);
            if (!missing.isEmpty()) {
                message(player, "Missing materials: " + String.join(", ", missing) + ".", ChatFormatting.RED);
                return;
            }
        }
        if (work.isEmpty()) {
            controller.scanStructure(level);
            if (controller.isAssembled()) {
                message(player, Component.translatable("message.ufo.autobuild.complete"), ChatFormatting.GREEN);
            } else {
                message(player, Component.translatable("message.ufo.autobuild.no_placeable"), ChatFormatting.YELLOW);
            }
            return;
        }
        SESSIONS.put(key, new Session(player.getUUID(), mode, List.copyOf(work), 0, 0,
                settings, scanner.copy()));
        message(player, "Auto-build started: " + work.size() + " block(s).", ChatFormatting.GREEN);
    }

    private static void startDemolition(ServerPlayer player, ServerLevel level, SessionKey key,
                                        BlockEntity controllerBlockEntity, IMultiblockController controller,
                                        MultiblockControllerDefinition definition, Direction facing) {
        var scan = StructureTerminalOps.scanDemolition(definition, level, controllerBlockEntity.getBlockPos(), facing);
        if (!scan.available()) {
            message(player, "Demolition stopped: part of the structure is outside loaded chunks or world bounds.", ChatFormatting.RED);
            return;
        }
        List<Work> work = scan.targets().stream()
                .map(target -> new Work(target.position(), target.state(), false, true))
                .toList();
        if (work.isEmpty()) {
            controller.scanStructure(level);
            message(player, "Demolition found no matching structural blocks.", ChatFormatting.GREEN);
            return;
        }
        SESSIONS.put(key, new Session(player.getUUID(), StructureScannerSettings.Mode.DEMOLISH, work, 0, 0,
                StructureScannerSettings.DEFAULT, ItemStack.EMPTY));
        message(player, "Demolition started: " + work.size() + " structural block(s).", ChatFormatting.YELLOW);
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
                BlockState current = level.getBlockState(world);
                if (!current.equals(work.target())) {
                    iterator.remove();
                    message(player, "Demolition interrupted by a changed block at " + position(world) + ".", ChatFormatting.RED);
                    refresh(level, key.controllerPos());
                    continue;
                }
                if (!canBreak(level, player, world)) {
                    iterator.remove();
                    message(player, "Structure operation stopped: no permission at " + position(world) + ".", ChatFormatting.RED);
                    refresh(level, key.controllerPos());
                    continue;
                }
                boolean removed = level.destroyBlock(world, true, player);
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
            if (!current.isAir() && work.replace()
                    && !canReplaceBlockEntity(level.getBlockEntity(world))) {
                iterator.remove();
                message(player, "Auto-build stopped: block data at " + position(world)
                        + " cannot be safely replaced.", ChatFormatting.RED);
                refresh(level, key.controllerPos());
                continue;
            }
            if ((!current.isAir() && !canBreak(level, player, world)) || !canPlace(level, player, world)) {
                iterator.remove();
                message(player, "Structure operation stopped: no permission at " + position(world) + ".", ChatFormatting.RED);
                refresh(level, key.controllerPos());
                continue;
            }
            Item item = work.target().getBlock().asItem();
            boolean consumedFromAe = false;
            if (!player.getAbilities().instabuild && !consume(player, item)) {
                consumedFromAe = session.settings().useAeNetwork()
                        && StructureScannerAe2Link.extractOne(session.scanner(), player, item);
                if (!consumedFromAe) {
                    iterator.remove();
                    message(player, "Auto-build interrupted: missing " + item.getDescription().getString() + ".", ChatFormatting.RED);
                    refresh(level, key.controllerPos());
                    continue;
                }
            }
            // Do not use destroyBlock(..., true) for replacement. Besides spawning loose
            // entities between two steps of the operation, that made the old field generator
            // easy to lose to magnets, void pickup rules or a full inventory. Capture its
            // exact loot first and only hand it back after the new block was placed.
            List<ItemStack> replacedDrops = current.isAir()
                    ? List.of()
                    : Block.getDrops(current, level, world, level.getBlockEntity(world), player, ItemStack.EMPTY);
            if (!level.setBlock(world, work.target(), Block.UPDATE_ALL)) {
                if (!player.getAbilities().instabuild) {
                    if (!consumedFromAe || !StructureScannerAe2Link.insertOne(session.scanner(), player, item)) {
                        giveBack(player, item);
                    }
                }
                iterator.remove();
                message(player, "Auto-build could not place a block at " + position(world) + ".", ChatFormatting.RED);
                refresh(level, key.controllerPos());
                continue;
            }
            for (ItemStack replacedDrop : replacedDrops) {
                giveBack(player, replacedDrop);
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
                session.index() + 1, session.placed() + (placed ? 1 : 0),
                session.settings(), session.scanner());
        if (next.index() < next.work().size()) {
            entry.setValue(next);
            return;
        }
        iterator.remove();
        refresh(level, key.controllerPos());
        String verb = next.mode() == StructureScannerSettings.Mode.DEMOLISH ? "removed" : "placed/replaced";
        boolean assembled = LoadedBlockEntityLookup.get(level, key.controllerPos())
                instanceof IMultiblockController controller && controller.isAssembled();
        message(player, "Structure operation " + verb + " " + next.placed() + " block(s).",
                next.mode() == StructureScannerSettings.Mode.DEMOLISH || assembled
                        ? ChatFormatting.GREEN : ChatFormatting.YELLOW);
        if (next.mode() != StructureScannerSettings.Mode.DEMOLISH && !assembled) {
            message(player, Component.translatable("message.ufo.autobuild.incomplete_after"), ChatFormatting.YELLOW);
        }
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

    /** Add the service parts required by controller validation to otherwise ordinary casing slots. */
    private static Component addRequiredHatches(ServerLevel level, BlockEntity controller,
            MultiblockPattern pattern, Direction facing,
            List<Work> work, boolean replace) {
        List<Block> required = MultiblockBuildDefaults.requiredHatches(controller);
        if (required.isEmpty()) return null;

        // Inspect the pattern rather than assuming a hatch has a fixed location.
        // J/K are illustrative defaults; an existing valid service hatch counts anywhere.
        char[][][] template = pattern.getPattern();
        Map<BlockPos, Character> symbols = new LinkedHashMap<>();
        String eligible = controller instanceof StellarNexusControllerBE ? "BFGJK"
                : controller instanceof QuantumCryoforgeControllerBE ? "BJK" : "CJK";
        for (int y = 0; y < template.length; y++) for (int z = 0; z < template[y].length; z++) {
            for (int x = 0; x < template[y][z].length; x++) {
                char symbol = template[y][z][x];
                if (symbol == pattern.getControllerChar() || eligible.indexOf(symbol) < 0) continue;
                var local = new MultiblockAutoBuildPlan.LocalPos(x, y, z);
                symbols.put(worldPos(pattern, controller.getBlockPos(), local, facing), symbol);
            }
        }
        for (Block hatch : required) {
            BlockState target = hatch.defaultBlockState();
            boolean present = symbols.entrySet().stream().anyMatch(entry -> {
                BlockPos pos = entry.getKey();
                BlockState state = level.getBlockState(pos);
                boolean sameHatch = state.is(hatch) || hatch == MultiblockBlocks.QUANTUM_PATTERN_BUFFER.get()
                        && state.is(MultiblockBlocks.QUANTUM_PATTERN_PROXY.get());
                return sameHatch
                        && pattern.matchesSlot(entry.getValue(), state, level, pos);
            }) || work.stream().anyMatch(entry -> entry.target().is(hatch));
            if (present) continue;

            int candidate = -1;
            for (int i = 0; i < work.size(); i++) {
                Work entry = work.get(i);
                Character symbol = symbols.get(entry.position());
                if (symbol != null && MultiblockBuildDefaults.isCasing(entry.target())
                        && pattern.matchesSlot(symbol, target, level, entry.position())) {
                    candidate = i;
                    break;
                }
            }
            if (candidate >= 0) {
                Work old = work.get(candidate);
                work.set(candidate, new Work(old.position(), target, old.replace(), false));
                continue;
            }
            if (replace) {
                for (var entry : symbols.entrySet()) {
                    BlockPos pos = entry.getKey();
                    BlockState current = level.getBlockState(pos);
                    if (MultiblockBuildDefaults.isCasing(current) && pattern.matchesSlot(entry.getValue(), target, level, pos)
                            && work.stream().noneMatch(slot -> slot.position().equals(pos))) {
                        work.add(new Work(pos, target, true, false));
                        candidate = work.size() - 1;
                        break;
                    }
                }
            }
            if (candidate < 0) return hatch.getName();
        }
        return null;
    }

    private static Map<Item, Integer> requirements(List<Work> placements) {
        Map<Item, Integer> result = new LinkedHashMap<>();
        for (var placement : placements) result.merge(placement.target().getBlock().asItem(), 1, Integer::sum);
        return result;
    }

    private static List<String> missingRequirements(ServerPlayer player, Map<Item, Integer> requirements,
                                                    StructureScannerSettings settings, ItemStack scanner) {
        List<String> missing = new ArrayList<>();
        for (var entry : requirements.entrySet()) {
            long available = player.getInventory().countItem(entry.getKey());
            if (settings.useAeNetwork()) {
                available = Math.min(Integer.MAX_VALUE,
                        available + StructureScannerAe2Link.available(scanner, player, entry.getKey()));
            }
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
        giveBack(player, new ItemStack(item));
    }

    private static void giveBack(ServerPlayer player, ItemStack stack) {
        ItemStack returned = stack.copy();
        if (!player.getInventory().add(returned) && !returned.isEmpty()) {
            player.drop(returned, false);
        }
    }

    private static Map<Character, BlockState> selectFieldTier(Map<Character, BlockState> defaults, int tier) {
        BlockState selected = switch (Math.clamp(tier, 1, 3)) {
            case 2 -> MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get().defaultBlockState();
            case 3 -> MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get().defaultBlockState();
            default -> MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get().defaultBlockState();
        };
        Map<Character, BlockState> result = new HashMap<>(defaults);
        result.replaceAll((symbol, state) -> isFieldGenerator(state) ? selected : state);
        return result;
    }

    /** Continue a partially built uniform field with its installed tier. */
    private static int installedFieldTier(ServerLevel level, MultiblockPattern pattern, BlockPos controller,
            Direction facing, Map<Character, BlockState> defaults, int fallback) {
        int found = 0;
        for (var entry : defaults.entrySet()) {
            if (!isFieldGenerator(entry.getValue())) continue;
            for (BlockPos pos : pattern.getExpectedPositions(controller, facing, entry.getKey())) {
                if (!level.isLoaded(pos)) continue;
                BlockState state = level.getBlockState(pos);
                int tier = state.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get()) ? 1
                        : state.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get()) ? 2
                        : state.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get()) ? 3 : 0;
                if (tier == 0) continue;
                if (found != 0 && found != tier) return fallback;
                found = tier;
            }
        }
        return found == 0 ? fallback : found;
    }

    private static boolean isFieldGenerator(BlockState state) {
        return state.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get())
                || state.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get())
                || state.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get());
    }

    private static String position(BlockPos pos) { return pos.getX() + ", " + pos.getY() + ", " + pos.getZ(); }

    /** Only this structural entity stores disposable controller-link metadata. */
    private static boolean canReplaceBlockEntity(BlockEntity blockEntity) {
        return blockEntity == null || blockEntity.getClass() == StellarNexusPartBE.class;
    }

    private static boolean canBreak(ServerLevel level, ServerPlayer player, BlockPos pos) {
        if (!player.mayInteract(level, pos)
                || !player.mayUseItemAt(pos, Direction.UP, ItemStack.EMPTY)) {
            return false;
        }
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, level.getBlockState(pos), player);
        return !NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    private static boolean canPlace(ServerLevel level, ServerPlayer player, BlockPos pos) {
        if (!player.mayInteract(level, pos)
                || !player.mayUseItemAt(pos, Direction.UP, ItemStack.EMPTY)) {
            return false;
        }
        BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, pos);
        BlockEvent.EntityPlaceEvent event = new BlockEvent.EntityPlaceEvent(snapshot, level.getBlockState(pos), player);
        return !NeoForge.EVENT_BUS.post(event).isCanceled();
    }
    private static void message(ServerPlayer player, String text, ChatFormatting color) {
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(text).withStyle(color), false);
    }
    private static void message(ServerPlayer player, Component text, ChatFormatting color) {
        player.displayClientMessage(text.copy().withStyle(color), false);
    }

    @SubscribeEvent public static void onServerStopped(ServerStoppedEvent event) { SESSIONS.clear(); }

    private record SessionKey(ResourceKey<Level> dimension, BlockPos controllerPos) { }
    private record Work(BlockPos position, BlockState target, boolean replace, boolean removeOnly) { }
    private record Session(UUID playerId, StructureScannerSettings.Mode mode,
                           List<Work> work, int index, int placed,
                           StructureScannerSettings settings, ItemStack scanner) { }
}
