package com.raishxn.ufo.screen;

import appeng.client.gui.Icon;
import appeng.client.Point;
import appeng.client.gui.widgets.VerticalButtonBar;
import com.mojang.blaze3d.systems.RenderSystem;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.MassiveOutputHatchBE;
import com.raishxn.ufocore.client.gui.widget.UfoAe2IconButton;
import com.raishxn.ufocore.client.gui.widget.UfoAtlasButton;
import com.raishxn.ufo.client.gui.widget.UfoQuickBuildButton;
import com.raishxn.ufocore.client.gui.widget.UfoStateIconButton;
import com.raishxn.ufo.client.render.FluidTankRenderer;
import com.raishxn.ufo.init.ModRecipes;
import com.raishxn.ufo.network.ModPackets;
import com.raishxn.ufo.network.packet.PacketAutoBuildMultiblock;
import com.raishxn.ufo.network.packet.PacketChangeStellarRecipe;
import com.raishxn.ufo.network.packet.PacketScanStellarStructure;
import com.raishxn.ufo.network.packet.PacketStartStellarOperation;
import com.raishxn.ufo.network.packet.PacketToggleStellarAutoStart;
import com.raishxn.ufo.network.packet.PacketToggleStellarLock;
import com.raishxn.ufo.network.packet.PacketToggleStellarOverclock;
import com.raishxn.ufo.network.packet.PacketToggleStellarPause;
import com.raishxn.ufo.network.packet.PacketToggleStellarSafeMode;
import com.raishxn.ufo.recipe.StellarSimulationRecipe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

/** Compact control screen for the Stellar Nexus. */
public class StellarNexusControllerScreen extends AbstractContainerScreen<StellarNexusControllerMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("ae2", "textures/guis/stellarnexus2.png");
    private static final ResourceLocation UNIVERSAL_WIDGETS =
            ResourceLocation.fromNamespaceAndPath("ae2", "textures/guis/universalgui2.png");
    private static final ResourceLocation AE2_STATES =
            ResourceLocation.fromNamespaceAndPath("ae2", "textures/guis/states.png");
    private static final ResourceLocation REQUIREMENTS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("ae2", "textures/guis/resourcesrequirementswidget.png");
    private static final int ATLAS_SIZE = 256;
    private static final int BACKGROUND_U = 0;
    private static final int BACKGROUND_V = 78;
    private static final int PANEL_WIDTH = 256;
    private static final int PANEL_HEIGHT = 178;
    // The supplied "17,40" tank origin is interpreted as atlas 17,140: the
    // panel starts at y=78 and every neighboring supplied coordinate is in the
    // atlas' 136-231 lower panel.
    private static final int COOLANT_X = 17;
    private static final int COOLANT_Y = 62;
    // Coordinates 17..31 are inclusive in the texture specification.
    private static final int COOLANT_WIDTH = 15;
    private static final int COOLANT_HEIGHT = 31;
    // Share the panel's right border, matching the one-pixel docking used by
    // AE2's left toolbar instead of looking like a detached window.
    private static final int REQUIREMENTS_X = PANEL_WIDTH - 1;
    private static final int REQUIREMENTS_Y = 34;
    private static final int REQUIREMENTS_WIDTH = 63;
    private static final int REQUIREMENTS_HEIGHT = 110;
    private static final int REQUIREMENT_ROW_HEIGHT = 14;
    private static final int MAX_REQUIREMENT_ROWS = 7;
    private static final int TEMP_GRAPH_X = 178;
    private static final int TEMP_GRAPH_Y = 128;
    private static final int TEMP_GRAPH_WIDTH = 59;
    private static final int TEMP_GRAPH_HEIGHT = 24;
    private static final int HEAT_SAMPLE_INTERVAL = 4;
    private static final FluidTankRenderer COOLANT_TANK_RENDERER = new FluidTankRenderer(
            MassiveOutputHatchBE.COOLANT_CAPACITY, COOLANT_WIDTH, COOLANT_HEIGHT);

    private List<RecipeHolder<StellarSimulationRecipe>> availableRecipes = new ArrayList<>();
    private List<RequirementEntry> displayedRequirements = List.of();
    private boolean requirementsSafeMode;
    private boolean requirementsOverclocked;
    private int currentRecipeIndex;
    private final int[] heatHistory = new int[TEMP_GRAPH_WIDTH];
    private int heatSampleTicks;
    private boolean heatHistoryInitialized;
    private UfoAtlasButton prevButton;
    private UfoAtlasButton nextButton;
    private UfoAtlasButton startPauseButton;
    private UfoAe2IconButton scanButton;
    private UfoStateIconButton safeModeButton;
    private UfoStateIconButton autoStartButton;
    private UfoStateIconButton lockButton;
    private UfoStateIconButton overclockButton;
    private Button autoBuildButton;
    /** The same AE2 component used by the universal multiblock screens. */
    private VerticalButtonBar leftToolbar;

    public StellarNexusControllerScreen(StellarNexusControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = PANEL_WIDTH;
        this.imageHeight = PANEL_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10_000;
        this.titleLabelY = 10_000;
        // init() runs again whenever the game window is resized. The AE2 bar
        // owns its button list, so it must be fresh for each screen layout.
        this.leftToolbar = new VerticalButtonBar();

        this.scanButton = new UfoAe2IconButton(Icon.SCHEDULING_DEFAULT,
                Component.literal("Scan multiblock structure"), button -> scanStructure());
        this.leftToolbar.add(this.scanButton);

        var quickBuild = new UfoQuickBuildButton(button -> ModPackets.sendToServer(
                new PacketAutoBuildMultiblock(this.menu.getBlockEntity().getBlockPos())));
        quickBuild.setTooltip(Tooltip.create(Component.literal("Auto-build structure")));
        this.autoBuildButton = quickBuild;
        this.leftToolbar.add(quickBuild);

        if (this.minecraft != null && this.minecraft.level != null) {
            this.availableRecipes = new ArrayList<>(this.minecraft.level.getRecipeManager()
                    .getAllRecipesFor(ModRecipes.STELLAR_SIMULATION_TYPE.get()));
            ResourceLocation activeId = this.menu.getBlockEntity().getActiveRecipeId();
            if (activeId != null) {
                for (int index = 0; index < this.availableRecipes.size(); index++) {
                    if (this.availableRecipes.get(index).id().equals(activeId)) {
                        this.currentRecipeIndex = index;
                        break;
                    }
                }
            }
        }
        rebuildRequirements();
        if (!this.heatHistoryInitialized) {
            Arrays.fill(this.heatHistory, this.menu.getHeatLevel());
            this.heatSampleTicks = 0;
            this.heatHistoryInitialized = true;
        }

        this.prevButton = this.addRenderableWidget(new UfoAtlasButton(
                this.leftPos + 11, this.topPos + 24, 21, 21, TEXTURE, ATLAS_SIZE, ATLAS_SIZE,
                90, 0, Component.literal("Previous simulation"), button -> cycleRecipe(-1)));
        this.nextButton = this.addRenderableWidget(new UfoAtlasButton(
                this.leftPos + 223, this.topPos + 24, 21, 21, TEXTURE, ATLAS_SIZE, ATLAS_SIZE,
                113, 0, Component.literal("Next simulation"), button -> cycleRecipe(1)));
        this.startPauseButton = this.addRenderableWidget(new UfoAtlasButton(
                this.leftPos + 106, this.topPos + 90, 43, 43, TEXTURE, ATLAS_SIZE, ATLAS_SIZE,
                0, 0, Component.literal("Start simulation"), button -> toggleStartPause()));

        this.safeModeButton = new UfoStateIconButton(Component.literal("Safe Mode"), button ->
                ModPackets.sendToServer(new PacketToggleStellarSafeMode(this.menu.getBlockEntity().getBlockPos())));
        this.leftToolbar.add(this.safeModeButton);

        this.autoStartButton = new UfoStateIconButton(Component.literal("Auto-start"), button ->
                ModPackets.sendToServer(new PacketToggleStellarAutoStart(this.menu.getBlockEntity().getBlockPos())));
        this.leftToolbar.add(this.autoStartButton);

        this.lockButton = new UfoStateIconButton(Component.literal("Simulation lock"), button ->
                ModPackets.sendToServer(new PacketToggleStellarLock(this.menu.getBlockEntity().getBlockPos())));
        this.leftToolbar.add(this.lockButton);

        this.overclockButton = new UfoStateIconButton(Component.literal("Overclock"), button ->
                ModPackets.sendToServer(new PacketToggleStellarOverclock(this.menu.getBlockEntity().getBlockPos())));
        this.leftToolbar.add(this.overclockButton);
        this.leftToolbar.setPosition(new Point(3, 1));
        this.leftToolbar.populateScreen(this::addRenderableWidget,
                new Rect2i(this.leftPos, this.topPos, this.imageWidth, this.imageHeight), null);
        updateButtonState();
        this.leftToolbar.updateBeforeRender();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.requirementsSafeMode != this.menu.isSafeMode()
                || this.requirementsOverclocked != this.menu.isOverclocked()) {
            rebuildRequirements();
        }
        if (this.autoBuildButton != null) {
            this.autoBuildButton.visible = !this.menu.isAssembled();
            this.autoBuildButton.active = !this.menu.isAssembled();
        }
        updateButtonState();
        this.leftToolbar.updateBeforeRender();
        sampleHeatHistory();
    }

    private void sampleHeatHistory() {
        if (++this.heatSampleTicks < HEAT_SAMPLE_INTERVAL) {
            return;
        }
        this.heatSampleTicks = 0;
        System.arraycopy(this.heatHistory, 1, this.heatHistory, 0, this.heatHistory.length - 1);
        this.heatHistory[this.heatHistory.length - 1] = this.menu.getHeatLevel();
    }

    private void toggleStartPause() {
        BlockPos pos = this.menu.getBlockEntity().getBlockPos();
        if (this.menu.isRunning()) {
            ModPackets.sendToServer(new PacketToggleStellarPause(pos));
        } else {
            ModPackets.sendToServer(new PacketStartStellarOperation(pos));
        }
    }

    private void scanStructure() {
        ModPackets.sendToServer(new PacketScanStellarStructure(this.menu.getBlockEntity().getBlockPos()));
        if (this.minecraft == null || this.minecraft.level == null || this.minecraft.player == null) return;

        BlockPos pos = this.menu.getBlockEntity().getBlockPos();
        var state = this.minecraft.level.getBlockState(pos);
        var facing = state.hasProperty(net.minecraft.world.level.block.DirectionalBlock.FACING)
                ? state.getValue(net.minecraft.world.level.block.DirectionalBlock.FACING)
                : net.minecraft.core.Direction.NORTH;
        var result = com.raishxn.ufo.block.entity.pattern.StellarNexusPatternFactory.getPattern()
                .match(this.minecraft.level, pos, facing);
        if (result.isValid() && this.menu.isAssembled()) {
            this.minecraft.player.displayClientMessage(
                    Component.translatable("message.ufo.structure_formed").withStyle(ChatFormatting.GREEN), true);
        } else if (!result.isValid()) {
            for (var error : result.allErrors().stream().limit(50).toList()) {
                com.raishxn.ufo.client.render.StructureHighlightRenderer.highlight(error.pos(), 5000);
            }
        } else {
            this.minecraft.player.displayClientMessage(Component.literal(
                    "§e[Stellar Nexus] §cStructure shape is valid, but hatch requirements are not met."), false);
        }
    }

    private void updateButtonState() {
        if (this.prevButton == null || this.nextButton == null || this.startPauseButton == null) return;
        boolean canChangeRecipe = !this.availableRecipes.isEmpty()
                && !this.menu.isSimulationLocked() && !this.menu.isRunning();
        this.prevButton.active = canChangeRecipe;
        this.nextButton.active = canChangeRecipe;

        if (this.menu.isRunning()) {
            boolean paused = this.menu.isPaused();
            this.startPauseButton.setSource(paused ? 0 : 45, 0);
            this.startPauseButton.active = true;
            this.startPauseButton.setTooltip(Tooltip.create(Component.literal(paused
                    ? "§aResume stellar simulation" : "§ePause stellar simulation")));
        } else {
            this.startPauseButton.setSource(0, 0);
            this.startPauseButton.active = canStart();
            this.startPauseButton.setTooltip(Tooltip.create(Component.literal(startTooltip())));
        }

        if (!this.availableRecipes.isEmpty()) {
            int previous = Math.floorMod(this.currentRecipeIndex - 1, this.availableRecipes.size());
            int next = (this.currentRecipeIndex + 1) % this.availableRecipes.size();
            this.prevButton.setTooltip(Tooltip.create(Component.literal("Previous: "
                    + getRecipeDisplayName(this.availableRecipes.get(previous)))));
            this.nextButton.setTooltip(Tooltip.create(Component.literal("Next: "
                    + getRecipeDisplayName(this.availableRecipes.get(next)))));
        }
        updateSideButtonTooltips();
    }

    private boolean canStart() {
        return this.menu.isAssembled() && !this.menu.isRunning()
                && this.menu.getCooldownTimer() == 0 && !this.availableRecipes.isEmpty();
    }

    private String startTooltip() {
        if (this.availableRecipes.isEmpty()) return "§cNo stellar simulation selected";
        if (!this.menu.isAssembled()) return "§cStructure not assembled";
        if (this.menu.getCooldownTimer() > 0) return "§eCooling down";
        return "§aStart stellar simulation";
    }

    private void updateSideButtonTooltips() {
        boolean safe = this.menu.isSafeMode();
        if (safe) {
            this.safeModeButton.setAtlasSprite(UNIVERSAL_WIDGETS, 256, 256, 0, 47, 14, 12);
        } else {
            this.safeModeButton.setAe2Icon(Icon.INVALID);
        }
        this.safeModeButton.setTooltip(Tooltip.create(Component.literal(safe
                ? "§aSafe Mode: ON\n§7Auto-shutdown on overheat"
                : "§cSafe Mode: OFF\n§4Local containment blast on overheat")));
        boolean auto = this.menu.isAutoStart();
        this.autoStartButton.setAtlasSprite(AE2_STATES, 256, 256, 16, 240, 16, 16);
        this.autoStartButton.setTooltip(Tooltip.create(Component.literal(auto ? "§aAuto-start: ON" : "§cAuto-start: OFF")));
        boolean locked = this.menu.isSimulationLocked();
        this.lockButton.setAe2Icon(locked ? Icon.LOCKED : Icon.UNLOCKED);
        this.lockButton.setTooltip(Tooltip.create(Component.literal(locked ? "§aSimulation locked" : "§cSimulation unlocked")));
        boolean overclocked = this.menu.isOverclocked();
        this.overclockButton.setAtlasSprite(UNIVERSAL_WIDGETS, 256, 256, 0, 33, 14, 14);
        this.overclockButton.setTooltip(Tooltip.create(Component.literal(overclocked
                ? "§aOverclock: ON\n§78x energy, 5x heat/fuel/speed" : "§cOverclock: OFF")));
    }

    private void cycleRecipe(int delta) {
        if (this.availableRecipes.isEmpty() || this.menu.isSimulationLocked() || this.menu.isRunning()) return;
        this.currentRecipeIndex = Math.floorMod(this.currentRecipeIndex + delta, this.availableRecipes.size());
        rebuildRequirements();
        ModPackets.sendToServer(new PacketChangeStellarRecipe(this.menu.getBlockEntity().getBlockPos(),
                this.availableRecipes.get(this.currentRecipeIndex).id()));
        updateButtonState();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(TEXTURE, this.leftPos, this.topPos, BACKGROUND_U, BACKGROUND_V,
                PANEL_WIDTH, PANEL_HEIGHT, ATLAS_SIZE, ATLAS_SIZE);
        graphics.blit(REQUIREMENTS_TEXTURE, this.leftPos + REQUIREMENTS_X, this.topPos + REQUIREMENTS_Y,
                0, 0, REQUIREMENTS_WIDTH, REQUIREMENTS_HEIGHT, ATLAS_SIZE, ATLAS_SIZE);
        this.leftToolbar.drawBackgroundLayer(graphics,
                new Rect2i(this.leftPos, this.topPos, this.imageWidth, this.imageHeight), Point.ZERO);

        FluidStack coolant = getCoolantStack();
        COOLANT_TANK_RENDERER.render(graphics, this.leftPos + COOLANT_X, this.topPos + COOLANT_Y, coolant);
        graphics.fill(this.leftPos + 93, this.topPos + 146,
                this.leftPos + 93 + fractionWidth(this.menu.getProgress(), this.menu.getTotalTime(), 69),
                this.topPos + 153, 0xFF35C759);
        graphics.fill(this.leftPos + 176, this.topPos + 96,
                this.leftPos + 176 + fractionWidth(this.menu.getEnergyBuffer(), this.menu.getEnergyCapacity(), 63),
                this.topPos + 98, 0xFF35C759);
        renderTemperatureGraph(graphics);
        renderRequirementRows(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String state = !this.menu.isAssembled() ? "INCOMPLETE"
                : this.menu.getCooldownTimer() > 0 ? "COOLDOWN"
                : this.menu.isPaused() ? "PAUSED"
                : this.menu.isRunning() ? "RUNNING" : "ASSEMBLED";
        int stateColor = !this.menu.isAssembled() || this.menu.getCooldownTimer() > 0
                ? 0xFFFF5555 : this.menu.isPaused() ? 0xFFFFAA00 : 0xFF55FF55;
        drawCenteredStatusLine(graphics, 35, 15, 185, 6, List.of(
                new StatusSegment(state, stateColor),
                new StatusSegment(this.menu.isSafeMode() ? "SAFE" : "RISK",
                        this.menu.isSafeMode() ? 0xFF55FF55 : 0xFFFF5555),
                new StatusSegment(this.menu.isOverclocked() ? "OC" : "STD",
                        this.menu.isOverclocked() ? 0xFFFF5555 : 0xFF55FF55),
                new StatusSegment(this.menu.isAssembled() ? "ONLINE" : "OFFLINE",
                        this.menu.isAssembled() ? 0xFF55FF55 : 0xFFFF5555)));

        String recipeName = this.availableRecipes.isEmpty() ? "No simulation"
                : getRecipeDisplayName(this.availableRecipes.get(this.currentRecipeIndex));
        drawCenteredFittedText(graphics, recipeName, 36, 26, 183, 17, 0xFFFFFFFF);

        drawCenteredFittedText(graphics, "Coolant", 38, 59, 43, 6, 0xFF101010);
        FluidStack coolant = getCoolantStack();
        if (coolant.isEmpty()) {
            drawCenteredFittedText(graphics, "Empty", 41, 70, 38, 24, 0xFFAAAAAA);
        } else {
            // This area is black in the supplied texture; light text is needed
            // to remain readable, especially after fitting long coolant names.
            drawCenteredFittedText(graphics, coolant.getHoverName().getString(), 41, 70, 38, 12, 0xFFF2F2F2);
            drawCenteredFittedText(graphics, compactAmount(coolant.getAmount()), 41, 82, 38, 12, 0xFF55FFFF);
        }
        renderFieldTier(graphics);

        drawCenteredFittedText(graphics, "Energy", 175, 58, 65, 10, 0xFF101010);
        drawCenteredFittedText(graphics, compactAmount(this.menu.getEnergyBuffer()) + "/"
                + compactAmount(this.menu.getEnergyCapacity()), 177, 73, 60, 16, 0xFFFFFFFF);
        drawCenteredFittedText(graphics, "TEMP " + this.menu.getHeatLevel() / 10 + "%", 176, 117, 63, 10,
                heatColor(this.menu.getHeatLevel()));
    }

    private void renderFieldTier(GuiGraphics graphics) {
        int tier = this.menu.getFieldLevel();
        if (tier < 1 || tier > 3) {
            drawCenteredFittedText(graphics, "NO FIELD", 16, 118, 63, 35, 0xFF555555);
            return;
        }
        ItemStack fieldGenerator = new ItemStack(switch (tier) {
            case 1 -> MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get();
            case 2 -> MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get();
            default -> MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get();
        });
        graphics.renderItem(fieldGenerator, 18, 126);
        drawCenteredFittedText(graphics, "MK." + toRoman(tier), 38, 118, 41, 35,
                tier == 3 ? 0xFFFF55FF : tier == 2 ? 0xFFAA55FF : 0xFF55AAFF);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);
        renderCustomTooltips(graphics, mouseX, mouseY);
    }

    private void renderCustomTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        int localX = mouseX - this.leftPos;
        int localY = mouseY - this.topPos;
        if (inside(localX, localY, COOLANT_X, COOLANT_Y, COOLANT_WIDTH, COOLANT_HEIGHT)) {
            FluidStack coolant = getCoolantStack();
            String contents = coolant.isEmpty() ? "Empty" : coolant.getHoverName().getString();
            graphics.renderTooltip(this.font, List.of(Component.literal("§bCoolant tank"),
                    Component.literal("§7" + contents), Component.literal("§7" + formatAmount(coolant.getAmount())
                            + " / " + formatAmount(MassiveOutputHatchBE.COOLANT_CAPACITY) + " mB")),
                    java.util.Optional.empty(), mouseX, mouseY);
        } else if (inside(localX, localY, 16, 118, 63, 35)) {
            int tier = this.menu.getFieldLevel();
            graphics.renderTooltip(this.font, Component.literal(tier > 0
                    ? "Stellar Field Generator Mk." + toRoman(tier) : "No Stellar Field Generator detected"), mouseX, mouseY);
        } else if (inside(localX, localY, 175, 58, 65, 40)) {
            graphics.renderTooltip(this.font, Component.literal("Energy: " + String.format("%,d", this.menu.getEnergyBuffer())
                    + " / " + String.format("%,d", this.menu.getEnergyCapacity()) + " AE"), mouseX, mouseY);
        } else if (inside(localX, localY, 176, 117, 63, 37)) {
            int minimum = Arrays.stream(this.heatHistory).min().orElse(this.menu.getHeatLevel());
            int maximum = Arrays.stream(this.heatHistory).max().orElse(this.menu.getHeatLevel());
            int trendDelta = this.heatHistory[this.heatHistory.length - 1]
                    - this.heatHistory[Math.max(0, this.heatHistory.length - 6)];
            String trend = trendDelta > 2 ? "§cRising" : trendDelta < -2 ? "§bCooling" : "§aStable";
            graphics.renderTooltip(this.font, List.of(
                    Component.literal("Heat: " + String.format("%.1f%%", this.menu.getHeatLevel() / 10.0F)),
                    Component.literal("§7Range: " + String.format("%.1f%%", minimum / 10.0F)
                            + " - " + String.format("%.1f%%", maximum / 10.0F)),
                    Component.literal("§7Trend: " + trend)), java.util.Optional.empty(), mouseX, mouseY);
        } else if (inside(localX, localY, 93, 146, 69, 7) && this.menu.getTotalTime() > 0) {
            graphics.renderTooltip(this.font, Component.literal("Simulation progress: " + Math.round(
                    this.menu.getProgress() * 100.0F / this.menu.getTotalTime()) + "%"), mouseX, mouseY);
        } else if (inside(localX, localY, REQUIREMENTS_X + 2, REQUIREMENTS_Y + 3, 59, 102)) {
            int row = (localY - REQUIREMENTS_Y - 3) / REQUIREMENT_ROW_HEIGHT;
            if (row >= 0 && row < this.displayedRequirements.size() && row < MAX_REQUIREMENT_ROWS) {
                RequirementEntry requirement = this.displayedRequirements.get(row);
                long available = this.menu.getRequirementAvailable(row);
                boolean complete = available >= requirement.amount();
                String unit = requirement.fluid() ? " mB" : " items";
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(requirement.name());
                tooltip.add(Component.literal((complete ? "§a" : "§c")
                        + formatAmount(available) + " / " + formatAmount(requirement.amount()) + unit));
                if (requirement.includesFuel()) {
                    tooltip.add(Component.literal("§6Includes stellar fuel"));
                }
                graphics.renderTooltip(this.font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
            }
        }
    }

    private FluidStack getCoolantStack() {
        return this.menu.getBufferedCoolant();
    }

    private void rebuildRequirements() {
        this.requirementsSafeMode = this.menu.isSafeMode();
        this.requirementsOverclocked = this.menu.isOverclocked();
        if (this.availableRecipes.isEmpty()
                || this.currentRecipeIndex < 0 || this.currentRecipeIndex >= this.availableRecipes.size()) {
            this.displayedRequirements = List.of();
            return;
        }

        StellarSimulationRecipe recipe = this.availableRecipes.get(this.currentRecipeIndex).value();
        Map<String, RequirementEntry> aggregated = new LinkedHashMap<>();
        for (var input : recipe.getItemInputs()) {
            if (input.isEmpty()) continue;
            ItemStack[] samples = input.getIngredient().getItems();
            if (samples.length == 0 || samples[0].isEmpty()) continue;
            ItemStack icon = samples[0].copy();
            icon.setCount(1);
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(icon.getItem());
            addRequirement(aggregated, "item:" + id, icon, icon.getHoverName(), input.getAmount(), false, false);
        }
        for (var input : recipe.getFluidInputs()) {
            if (input.isEmpty()) continue;
            FluidStack[] samples = input.getIngredient().getStacks();
            if (samples.length == 0 || samples[0].isEmpty()) continue;
            addFluidRequirement(aggregated, samples[0].getFluid(), input.getAmount(), false);
        }
        recipe.getFuelFluidRL().ifPresent(id -> {
            Fluid fluid = BuiltInRegistries.FLUID.get(id);
            if (fluid != null && fluid != Fluids.EMPTY && recipe.getFuelAmount() > 0L) {
                double multiplier = this.menu.isSafeMode() ? 2.5D : 1.0D;
                if (this.menu.isOverclocked()) multiplier *= 5.0D;
                addFluidRequirement(aggregated, fluid, (long) (recipe.getFuelAmount() * multiplier), true);
            }
        });
        this.displayedRequirements = List.copyOf(aggregated.values());
    }

    private static void addFluidRequirement(Map<String, RequirementEntry> requirements,
            Fluid fluid, long amount, boolean fuel) {
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        ItemStack icon = new ItemStack(fluid.getBucket());
        Component name = new FluidStack(fluid, 1).getHoverName();
        addRequirement(requirements, "fluid:" + id, icon, name, amount, true, fuel);
    }

    private static void addRequirement(Map<String, RequirementEntry> requirements, String key,
            ItemStack icon, Component name, long amount, boolean fluid, boolean fuel) {
        if (amount <= 0L) return;
        RequirementEntry current = requirements.get(key);
        if (current == null) {
            requirements.put(key, new RequirementEntry(icon, name, amount, fluid, fuel));
            return;
        }
        long combined = current.amount() > Long.MAX_VALUE - amount ? Long.MAX_VALUE : current.amount() + amount;
        requirements.put(key, new RequirementEntry(
                current.icon(), current.name(), combined, current.fluid(), current.includesFuel() || fuel));
    }

    private void renderRequirementRows(GuiGraphics graphics) {
        int panelX = this.leftPos + REQUIREMENTS_X;
        int panelY = this.topPos + REQUIREMENTS_Y;
        int visibleRows = Math.min(this.displayedRequirements.size(), MAX_REQUIREMENT_ROWS);
        for (int row = 0; row < visibleRows; row++) {
            int rowY = panelY + 3 + row * REQUIREMENT_ROW_HEIGHT;
            if (row == MAX_REQUIREMENT_ROWS - 1 && this.displayedRequirements.size() > MAX_REQUIREMENT_ROWS) {
                drawCenteredFittedText(graphics,
                        "+" + (this.displayedRequirements.size() - MAX_REQUIREMENT_ROWS + 1),
                        panelX + 2, rowY, 59, REQUIREMENT_ROW_HEIGHT, 0xFFAAAAAA);
                break;
            }

            RequirementEntry requirement = this.displayedRequirements.get(row);
            long available = this.menu.getRequirementAvailable(row);
            int amountColor = available >= requirement.amount() ? 0xFF55FF55 : 0xFFFF5555;
            if (!requirement.icon().isEmpty()) {
                graphics.pose().pushPose();
                graphics.pose().translate(panelX + 4, rowY + 1, 0.0F);
                graphics.pose().scale(0.75F, 0.75F, 1.0F);
                graphics.renderItem(requirement.icon(), 0, 0);
                graphics.pose().popPose();
            }
            drawCenteredFittedText(graphics, "×" + compactAmount(requirement.amount()),
                    panelX + 18, rowY + 1, 41, 12, amountColor);
        }
    }

    private void renderTemperatureGraph(GuiGraphics graphics) {
        int x = this.leftPos + TEMP_GRAPH_X;
        int y = this.topPos + TEMP_GRAPH_Y;
        int bottom = y + TEMP_GRAPH_HEIGHT - 1;

        // Dark plotting surface with a compact scientific-chart grid.
        graphics.fill(x, y, x + TEMP_GRAPH_WIDTH, y + TEMP_GRAPH_HEIGHT, 0x70101720);
        for (int division = 1; division < 4; division++) {
            int gridY = y + division * TEMP_GRAPH_HEIGHT / 4;
            graphics.fill(x, gridY, x + TEMP_GRAPH_WIDTH, gridY + 1, 0x405A6478);
            int gridX = x + division * TEMP_GRAPH_WIDTH / 4;
            graphics.fill(gridX, y, gridX + 1, y + TEMP_GRAPH_HEIGHT, 0x305A6478);
        }

        // Dashed 50% warning and 80% danger references.
        drawDashedHorizontalLine(graphics, x, x + TEMP_GRAPH_WIDTH,
                heatGraphY(y, bottom, 500), 0x8099AA33);
        drawDashedHorizontalLine(graphics, x, x + TEMP_GRAPH_WIDTH,
                heatGraphY(y, bottom, 800), 0x80CC3333);

        int previousY = heatGraphY(y, bottom, this.heatHistory[0]);
        for (int index = 0; index < this.heatHistory.length; index++) {
            int pointY = heatGraphY(y, bottom, this.heatHistory[index]);
            int pointColor = heatColor(this.heatHistory[index]);
            graphics.fill(x + index, pointY + 1, x + index + 1, bottom + 1,
                    (pointColor & 0x00FFFFFF) | 0x38000000);
            if (index > 0) {
                drawPixelLine(graphics, x + index - 1, previousY + 1, x + index, pointY + 1,
                        0xA0000000);
                drawPixelLine(graphics, x + index - 1, previousY, x + index, pointY,
                        pointColor);
            }
            previousY = pointY;
        }

        int latestX = x + TEMP_GRAPH_WIDTH - 1;
        int latestY = heatGraphY(y, bottom, this.heatHistory[this.heatHistory.length - 1]);
        graphics.fill(latestX - 1, latestY - 1, latestX + 2, latestY + 2, 0xFFE8F5FF);
        graphics.fill(latestX, latestY, latestX + 1, latestY + 1,
                heatColor(this.heatHistory[this.heatHistory.length - 1]));
    }

    private static void drawDashedHorizontalLine(GuiGraphics graphics, int startX, int endX, int y, int color) {
        for (int dashX = startX; dashX < endX; dashX += 4) {
            graphics.fill(dashX, y, Math.min(dashX + 2, endX), y + 1, color);
        }
    }

    private static int heatGraphY(int top, int bottom, int heat) {
        float fraction = Math.clamp(heat / 1000.0F, 0.0F, 1.0F);
        return bottom - Math.round(fraction * (bottom - top));
    }

    private static void drawPixelLine(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0);
        int sy = y0 < y1 ? 1 : -1;
        int error = dx + dy;
        while (true) {
            graphics.fill(x0, y0, x0 + 1, y0 + 1, color);
            if (x0 == x1 && y0 == y1) break;
            int doubled = error * 2;
            if (doubled >= dy) {
                error += dy;
                x0 += sx;
            }
            if (doubled <= dx) {
                error += dx;
                y0 += sy;
            }
        }
    }

    private void drawCenteredStatusLine(GuiGraphics graphics, int x, int y,
            int maxWidth, int maxHeight, List<StatusSegment> segments) {
        String separator = " | ";
        int separatorWidth = this.font.width(separator);
        int textWidth = Math.max(0, segments.size() - 1) * separatorWidth;
        for (StatusSegment segment : segments) {
            textWidth += this.font.width(segment.text());
        }
        if (textWidth <= 0) return;

        float scale = Math.min(1.0F, Math.min(
                maxWidth / (float) textWidth,
                maxHeight / (float) this.font.lineHeight));
        float renderedHeight = this.font.lineHeight * scale;
        graphics.pose().pushPose();
        graphics.pose().translate(x + (maxWidth - textWidth * scale) / 2.0F,
                y + (maxHeight - renderedHeight) / 2.0F, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        int cursor = 0;
        for (int index = 0; index < segments.size(); index++) {
            if (index > 0) {
                graphics.drawString(this.font, separator, cursor, 0, 0xFFAAAAAA, false);
                cursor += separatorWidth;
            }
            StatusSegment segment = segments.get(index);
            graphics.drawString(this.font, segment.text(), cursor, 0, segment.color(), false);
            cursor += this.font.width(segment.text());
        }
        graphics.pose().popPose();
    }

    private record RequirementEntry(
            ItemStack icon, Component name, long amount, boolean fluid, boolean includesFuel) {
    }

    private record StatusSegment(String text, int color) {
    }

    /** Area occupied by the attached resource panel, used by JEI exclusion handling. */
    public Rect2i getRequirementsExclusionArea() {
        return new Rect2i(
                this.leftPos + REQUIREMENTS_X,
                this.topPos + REQUIREMENTS_Y,
                REQUIREMENTS_WIDTH,
                REQUIREMENTS_HEIGHT);
    }

    /** Draws text centered on both axes, fitting it fully inside its assigned region. */
    private void drawCenteredFittedText(GuiGraphics graphics, String text, int x, int y,
            int maxWidth, int maxHeight, int color) {
        int width = this.font.width(text);
        if (width <= 0) return;
        float scale = Math.min(1.0F, Math.min(
                maxWidth / (float) width,
                maxHeight / (float) this.font.lineHeight));
        float renderedHeight = this.font.lineHeight * scale;
        graphics.pose().pushPose();
        graphics.pose().translate(x + (maxWidth - width * scale) / 2.0F,
                y + (maxHeight - renderedHeight) / 2.0F, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(this.font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private static boolean inside(int x, int y, int areaX, int areaY, int width, int height) {
        return x >= areaX && x < areaX + width && y >= areaY && y < areaY + height;
    }

    private static int fractionWidth(long current, long total, int width) {
        if (current <= 0L || total <= 0L) return 0;
        return Math.clamp((int) Math.ceil(current / (double) total * width), 0, width);
    }

    private static int heatColor(int heat) {
        float fraction = Math.clamp(heat / 1000.0F, 0.0F, 1.0F);
        int red = (int) (53 + 202 * fraction);
        int green = (int) (199 - 150 * fraction);
        return 0xFF000000 | red << 16 | green << 8;
    }

    private String getRecipeDisplayName(RecipeHolder<StellarSimulationRecipe> holder) {
        String name = holder.value().getSimulationName();
        return name == null || name.isEmpty() ? formatRecipeId(holder.id()) : name;
    }

    private static String formatRecipeId(ResourceLocation id) {
        String path = id.getPath();
        int slash = path.lastIndexOf('/');
        if (slash >= 0) path = path.substring(slash + 1);
        if (path.startsWith("stellar_")) path = path.substring("stellar_".length());
        StringBuilder result = new StringBuilder();
        for (String word : path.split("_")) {
            if (!word.isEmpty()) result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(' ');
        }
        return result.toString().trim();
    }

    private static String compactAmount(long amount) {
        if (amount >= 1_000_000_000L) return amount / 1_000_000_000L + "B";
        if (amount >= 1_000_000L) return amount / 1_000_000L + "M";
        if (amount >= 1_000L) return amount / 1_000L + "K";
        return Long.toString(amount);
    }

    private static String formatAmount(long amount) {
        return String.format("%,d", amount);
    }

    private static String toRoman(int tier) {
        return switch (tier) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> Integer.toString(tier);
        };
    }
}
