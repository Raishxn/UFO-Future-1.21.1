package com.raishxn.ufo.gametest;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinitions;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.client.GhostHologramRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Development-only frame-time check of the ghost hologram ({@code -Dufo.hologramSmoke=true}).
 *
 * <p>Places a Stellar Nexus controller in open air (every ghost visible), then measures frames
 * with the hologram off, with the cached-mesh hologram, and with the former per-frame path
 * (every ghost block re-tessellated into the immediate translucent buffer each frame), reproduced
 * here. Each phase saves a screenshot to {@code run/screenshots}.</p>
 */
@EventBusSubscriber(modid = "ufo_tests", value = Dist.CLIENT)
public final class GhostHologramClientSmoke {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final BlockPos CONTROLLER = new BlockPos(0, 140, 0);
    private static final long WARMUP_NANOS = 4_000_000_000L;
    private static final long PHASE_NANOS = 10_000_000_000L;
    private static final String[] PHASES = {"off", "cached", "legacy"};

    private static int ticks;
    private static int stage;
    private static int phase = -1;
    private static long phaseStart;
    private static long frames;
    private static long legacyNanos;
    private static final List<BlockPos> legacyPositions = new ArrayList<>();
    private static final List<BlockState> legacyStates = new ArrayList<>();
    private static final StringBuilder report = new StringBuilder();

    private GhostHologramClientSmoke() {
    }

    @SubscribeEvent
    public static void afterClientTick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("ufo.hologramSmoke")) return;
        var minecraft = Minecraft.getInstance();
        if (++ticks > 20 * 240) throw new AssertionError("Hologram smoke timed out at stage " + stage);
        if (minecraft.level == null || minecraft.player == null || minecraft.getSingleplayerServer() == null) return;
        var server = minecraft.getSingleplayerServer();
        switch (stage) {
            case 0 -> {
                minecraft.options.enableVsync().set(false);
                minecraft.options.framerateLimit().set(260);
                minecraft.options.hideGui = true;
                var playerId = minecraft.player.getUUID();
                server.execute(() -> {
                    var player = server.getPlayerList().getPlayer(playerId);
                    var level = server.overworld();
                    player.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
                    // The NORTH-facing template extends from the controller towards +Z.
                    player.teleportTo(level, 0.5, 155.5, 55.5, 180.0F, 21.0F);
                });
                stage = 1;
                ticks = 0;
            }
            case 1 -> {
                if (ticks < 60) return;
                server.execute(() -> server.overworld().setBlockAndUpdate(CONTROLLER,
                        MultiblockBlocks.STELLAR_NEXUS_CONTROLLER.get().defaultBlockState()));
                stage = 2;
            }
            case 2 -> {
                var state = minecraft.level.getBlockState(CONTROLLER);
                if (!MultiblockControllerDefinitions.isSupportedController(state)
                        || minecraft.level.getBlockEntity(CONTROLLER) == null) return;
                collectLegacyBlocks(minecraft, state);
                if (legacyPositions.isEmpty()) throw new AssertionError("no hologram blocks for the Stellar Nexus");
                stage = 3;
                startPhase(0);
            }
            case 3 -> {
                long elapsed = System.nanoTime() - phaseStart;
                if (elapsed < WARMUP_NANOS + PHASE_NANOS) return;
                finishPhase(minecraft, elapsed - WARMUP_NANOS);
                if (phase + 1 < PHASES.length) {
                    startPhase(phase + 1);
                } else {
                    LOGGER.info("UFO_HOLOGRAM_SMOKE_PASSED ghostBlocks={}{}", legacyPositions.size(), report);
                    minecraft.stop();
                }
            }
            default -> {
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (stage != 3 || event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        long now = System.nanoTime();
        boolean measuring = now - phaseStart >= WARMUP_NANOS;
        if (measuring) frames++;
        if (!"legacy".equals(PHASES[phase])) return;
        // The former GhostHologramRenderer frame, unchanged.
        var mc = Minecraft.getInstance();
        var poseStack = event.getPoseStack();
        var cameraPos = event.getCamera().getPosition();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        var vertexConsumer = bufferSource.getBuffer(RenderType.translucent());
        for (int index = 0; index < legacyPositions.size(); index++) {
            BlockPos pos = legacyPositions.get(index);
            BlockState existingState = mc.level.getBlockState(pos);
            if (!existingState.isAir() && existingState.getFluidState().isEmpty()) continue;
            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            mc.getBlockRenderer().renderBatched(legacyStates.get(index), pos, mc.level, poseStack,
                    vertexConsumer, true, mc.level.getRandom());
            poseStack.popPose();
        }
        bufferSource.endBatch();
        poseStack.popPose();
        RenderSystem.assertOnRenderThread();
        if (measuring) legacyNanos += System.nanoTime() - now;
    }

    private static void startPhase(int next) {
        var mc = Minecraft.getInstance();
        boolean cachedWanted = "cached".equals(PHASES[next]);
        if (GhostHologramRenderer.isActive(CONTROLLER) != cachedWanted) {
            GhostHologramRenderer.toggleHologram(CONTROLLER, Direction.NORTH);
        }
        if (cachedWanted && !GhostHologramRenderer.isActive(CONTROLLER)) {
            throw new AssertionError("hologram did not activate");
        }
        phase = next;
        phaseStart = System.nanoTime();
        frames = 0;
        legacyNanos = 0;
        LOGGER.info("UFO_HOLOGRAM_SMOKE phase {} started (level loaded: {})", PHASES[next], mc.level != null);
    }

    private static void finishPhase(Minecraft mc, long measuredNanos) {
        double seconds = measuredNanos / 1e9;
        double fps = frames / seconds;
        double frameMs = frames == 0 ? Double.NaN : measuredNanos / 1e6 / frames;
        String line = String.format(java.util.Locale.ROOT,
                " | %s: frames=%d fps=%.1f frameMs=%.2f legacyLoopMsPerFrame=%.2f",
                PHASES[phase], frames, fps, frameMs, frames == 0 ? 0.0 : legacyNanos / 1e6 / frames);
        report.append(line);
        LOGGER.info("UFO_HOLOGRAM_SMOKE{}", line);
        Screenshot.grab(mc.gameDirectory, "ufo-hologram-" + PHASES[phase] + ".png", mc.getMainRenderTarget(),
                message -> LOGGER.info("UFO_HOLOGRAM_SMOKE screenshot {}", message.getString()));
    }

    /** Same block list GhostHologramRenderer builds for a NORTH-facing controller. */
    private static void collectLegacyBlocks(Minecraft mc, BlockState controllerState) {
        var be = mc.level.getBlockEntity(CONTROLLER);
        var definition = MultiblockControllerDefinitions.getDefinition(be).orElseThrow();
        var facing = MultiblockControllerDefinitions.getPatternFacing(be, controllerState);
        if (facing != Direction.NORTH) throw new AssertionError("expected a NORTH-facing controller, got " + facing);
        var pattern = definition.pattern();
        Map<Character, BlockState> defaults = definition.defaultCreativeStates();
        char[][][] chars = pattern.getPattern();
        for (int y = 0; y < chars.length; y++) {
            for (int z = 0; z < chars[y].length; z++) {
                for (int x = 0; x < chars[y][z].length; x++) {
                    char c = chars[y][z][x];
                    if (c == ' ' || c == 'A' || c == pattern.getControllerChar()) continue;
                    BlockState target = defaults.get(c);
                    if (target == null) continue;
                    legacyPositions.add(CONTROLLER.offset(x - pattern.getControllerCol(),
                            y - pattern.getControllerLayer(), z - pattern.getControllerRow()));
                    legacyStates.add(target);
                }
            }
        }
    }
}
