package com.raishxn.ufo.block.entity;

import com.raishxn.ufo.block.custom.DimensionalMatterAssemblerBlock;
import com.raishxn.ufo.init.ModBlockEntities;
import com.raishxn.ufo.item.ModItems;
import com.raishxn.ufo.init.ModSounds;
import com.raishxn.ufo.init.ModRecipes;
import com.raishxn.ufo.menu.DimensionalMatterAssemblerMenu;
import com.raishxn.ufo.recipe.DimensionalMatterAssemblerRecipe;
import com.raishxn.ufo.util.ConfigType;
import com.raishxn.ufo.util.IOMode;
import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// --- IMPORTS DA ARMADURA ---
import com.raishxn.ufo.item.custom.IThermalArmor; // <-- NOSSA NOVA INTERFACE
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.damagesource.DamageTypes; // <-- CORREÇÃO: Pacote correto
import net.minecraft.world.damagesource.DamageSource;
// --- FIM DOS IMPORTS ---

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DimensionalMatterAssemblerBlockEntity extends BlockEntity implements MenuProvider {

    // --- Handlers (Item, Fluid, Energy) ---
    private final Object[] itemHandlers = new Object[6];
    private final Object[] fluidHandlers = new Object[6];
    private final Object[] energyHandlers = new Object[6];
    public final ItemStackHandler itemInputHandler = new ItemStackHandler(9) { @Override protected void onContentsChanged(int slot) { setChanged(); } };
    public final ItemStackHandler itemOutputHandler = new ItemStackHandler(2) { @Override protected void onContentsChanged(int slot) { setChanged(); } };
    public final ItemStackHandler upgradeHandler = new ItemStackHandler(4) { @Override protected void onContentsChanged(int slot) { setChanged(); } };
    public final FluidTank fluidInputTank = new FluidTank(16000) { @Override protected void onContentsChanged() { setChanged(); } };
    public final FluidTank coolantInputTank = new FluidTank(16000) { @Override protected void onContentsChanged() { setChanged(); } };
    public final FluidTank fluidOutputTank1 = new FluidTank(16000) { @Override protected void onContentsChanged() { setChanged(); } };
    public final FluidTank fluidOutputTank2 = new FluidTank(16000) { @Override protected void onContentsChanged() { setChanged(); } };
    public final EnergyStorage energyStorage = new EnergyStorage(1000000000, 50000000, 0) { @Override public int receiveEnergy(int maxReceive, boolean simulate) { setChanged(); return super.receiveEnergy(maxReceive, simulate); } };

    // --- Configs de I/O ---
    private final IOMode[][] sideConfigs = new IOMode[ConfigType.values().length][6];
    private final boolean[] autoEject = new boolean[ConfigType.values().length];
    private final boolean[] autoInput = new boolean[ConfigType.values().length];

    // --- Handler de Lógica (Calor e Catalisadores) ---
    public final DMAThermalHandler thermalHandler = new DMAThermalHandler(this);

    // --- Variáveis de Processo ---
    protected final ContainerData data;
    public int progress = 0;
    public int maxProgress = 0;

    // --- Rastreamento de estado para sons ---
    private boolean wasActive = false;


    // --- Construtor do ContainerData ---
    {
        data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> progress;
                    case 1 -> maxProgress;
                    case 2 -> energyStorage.getEnergyStored();
                    case 3 -> energyStorage.getMaxEnergyStored();
                    case 4 -> BuiltInRegistries.FLUID.getId(coolantInputTank.getFluid().getFluid());
                    case 5 -> coolantInputTank.getFluidAmount();
                    case 6 -> BuiltInRegistries.FLUID.getId(fluidInputTank.getFluid().getFluid());
                    case 7 -> fluidInputTank.getFluidAmount();
                    case 8 -> BuiltInRegistries.FLUID.getId(fluidOutputTank1.getFluid().getFluid());
                    case 9 -> fluidOutputTank1.getFluidAmount();
                    case 10 -> BuiltInRegistries.FLUID.getId(fluidOutputTank2.getFluid().getFluid());
                    case 11 -> fluidOutputTank2.getFluidAmount();
                    case 12 -> (autoEject[0] ? 1 : 0) | (autoEject[1] ? 2 : 0) | (autoEject[2] ? 4 : 0) | (autoEject[3] ? 8 : 0);
                    case 13 -> thermalHandler.getTemperature();
                    // CORREÇÃO: Adiciona o timer de meltdown ao data
                    case 14 -> thermalHandler.getMeltdownTicks();
                    default -> 0;
                };
            }
            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> progress = pValue;
                    case 1 -> maxProgress = pValue;
                    case 12 -> {
                        autoEject[0] = (pValue & 1) != 0;
                        autoEject[1] = (pValue & 2) != 0;
                        autoEject[2] = (pValue & 4) != 0;
                        autoEject[3] = (pValue & 8) != 0;
                    }
                    case 13 -> thermalHandler.setTemperature(pValue);
                    // CORREÇÃO: Adiciona o timer de meltdown ao data
                    case 14 -> thermalHandler.setMeltdownTicks(pValue);
                }
            }
            // CORREÇÃO: Aumenta a contagem para 15
            @Override public int getCount() { return 15; }
        };
    }

    // --- Construtor do BlockEntity ---
    public DimensionalMatterAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DMA_BE.get(), pos, state);
        for (ConfigType type : ConfigType.values()) {
            Arrays.fill(sideConfigs[type.ordinal()], IOMode.NONE);
            autoEject[type.ordinal()] = false;
        }
        sideConfigs[ConfigType.ITEM.ordinal()][Direction.DOWN.ordinal()] = IOMode.ITEM_OUT;
        sideConfigs[ConfigType.FLUID.ordinal()][Direction.UP.ordinal()] = IOMode.FLUID_IN;
        sideConfigs[ConfigType.COOLANT.ordinal()][Direction.NORTH.ordinal()] = IOMode.COOLANT_IN;
        sideConfigs[ConfigType.COOLANT.ordinal()][Direction.SOUTH.ordinal()] = IOMode.COOLANT_IN;
        sideConfigs[ConfigType.ENERGY.ordinal()][Direction.EAST.ordinal()] = IOMode.ENERGY;
        sideConfigs[ConfigType.ENERGY.ordinal()][Direction.WEST.ordinal()] = IOMode.ENERGY;
    }

    // --- Capacidades (Capabilities) ---
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getCapability(BlockCapability<T, Direction> capability, @Nullable Direction side) {

        if (side == null) {
            return null;
        }
        int sideIndex = side.ordinal();

        if (capability == Capabilities.ItemHandler.BLOCK) {
            if (itemHandlers[sideIndex] == null) {
                itemHandlers[sideIndex] = new SidedItemHandler(side);
            }
            return (T) itemHandlers[sideIndex];
        }

        if (capability == Capabilities.FluidHandler.BLOCK) {
            if (fluidHandlers[sideIndex] == null) {
                fluidHandlers[sideIndex] = new SidedFluidHandler(side);
            }
            return (T) fluidHandlers[sideIndex];
        }

        if (capability == Capabilities.EnergyStorage.BLOCK) {
            if (energyHandlers[sideIndex] == null) {
                energyHandlers[sideIndex] = new SidedEnergyStorage(side);
            }
            return (T) energyHandlers[sideIndex];
        }
        return null;
    }

    // --- MenuProvider ---
    @Override public @NotNull Component getDisplayName() { return Component.translatable("block.ufo.dimensional_matter_assembler"); }

    @Nullable @Override public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return new DimensionalMatterAssemblerMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    // =================================================================================
    // --- LÓGICA DE TICK (COM DANO DE CALOR) ---
    // =================================================================================
    public static void tick(Level level, BlockPos pos, BlockState state, DimensionalMatterAssemblerBlockEntity be) {
        if (level.isClientSide) return;

        // --- AUTO I/O (NO TOPO, PARA FUNCIONAR) ---
        if (be.autoInput[ConfigType.ITEM.ordinal()]) be.tryPullItems(level, pos);
        be.tryPullFluids(level, pos);
        if (be.autoEject[ConfigType.ITEM.ordinal()]) be.tryEjectItems(level, pos);
        if (be.autoEject[ConfigType.FLUID.ordinal()]) be.tryEjectFluids(level, pos, ConfigType.FLUID);


        boolean isActive = false;
        Optional<DimensionalMatterAssemblerRecipe> recipeOptional = be.getRecipe();

        // --- 1. Tick do Handler (Calcula bônus, aplica resfriamento, CHECA MELTDOWN) ---
        // A lógica de explosão agora está DENTRO do thermalHandler.tick()
        be.thermalHandler.tick(level, pos, state, be.progress > 0, recipeOptional.orElse(null));

        // --- 1b. Lida com efeitos de temperatura (Avisos, Sons) ---
        // Isso é chamado ANTES da explosão (que agora tem delay)
        be.handleTemperatureEffects(level, pos);

        // --- 1c. NOVO: Lida com dano de calor em players ---
        be.handleHeatDamage(level, pos);


        DMAThermalHandler.ActiveModifiers mods = be.thermalHandler.getActiveModifiers();

        // --- 2. Tick Criativo ---
        if (mods.isCreative()) {
            if (recipeOptional.isPresent() && be.canProcess(recipeOptional.get())) {
                be.craft(recipeOptional.get(), mods);
                be.thermalHandler.applyPostCraftHeat();
            }
            be.progress = 0;
            if (!state.getValue(DimensionalMatterAssemblerBlock.ACTIVE)) {
                level.setBlock(pos, state.setValue(DimensionalMatterAssemblerBlock.ACTIVE, true), 3);
            }
            isActive = true;
            be.setChanged();
        } else {

            // --- 3. Tick Normal (Não-Criativo) ---
            if (recipeOptional.isPresent()) {
                DimensionalMatterAssemblerRecipe recipe = recipeOptional.get();
                if (be.canProcess(recipe)) {

                    double zoneSpeedMod = 1.0;
                    double zoneEnergyMod = 1.0;

                    be.maxProgress = (int) (recipe.getProcessTime() / (mods.totalSpeedMod() * zoneSpeedMod));
                    int baseEnergyPerTick = be.maxProgress > 0 ? recipe.getEnergy() / be.maxProgress : recipe.getEnergy();
                    int energyPerTick = (int) (baseEnergyPerTick * mods.totalEnergyMod() * zoneEnergyMod);


                    if (be.energyStorage.getEnergyStored() >= energyPerTick) {
                        isActive = true;
                        be.energyStorage.extractEnergy(energyPerTick, false);
                        be.progress++;

                        if (be.progress >= be.maxProgress) {

                            if (be.thermalHandler.shouldCraftFail()) {
                                be.thermalHandler.handleCraftFailure(level, pos, recipe);
                            } else {
                                be.craft(recipe, mods);
                                be.thermalHandler.applyPostCraftHeat();
                            }
                            be.progress = 0;
                        }
                    } else {
                        be.progress = 0;
                    }
                } else {
                    be.progress = 0;
                }
            } else {
                be.progress = 0;
            }
        }

        // Não para de funcionar se estiver em meltdown
        if (be.thermalHandler.getZone() == DMAThermalHandler.TemperatureZone.MELTDOWN) {
            isActive = true;
        }

        if (be.progress == 0 && !mods.isCreative() && !isActive) isActive = false;

        // Atualiza o estado do bloco (se mudou)
        if (state.getValue(DimensionalMatterAssemblerBlock.ACTIVE) != isActive) {
            level.setBlock(pos, state.setValue(DimensionalMatterAssemblerBlock.ACTIVE, isActive), 3);
        }

        // --- 4. Lida com o som ambiente ---
        be.handleAmbientSound(level, pos, isActive);
        be.wasActive = isActive; // Atualiza o estado anterior

        be.setChanged();
    }

    // =================================================================================
    // --- MÉTODOS DE EFEITOS (SOM/CHAT/TELA) ---
    // =================================================================================

    /**
     * Toca o som de funcionamento (início/parada).
     */
    private void handleAmbientSound(Level level, BlockPos pos, boolean isActive) {
        if (isActive && !this.wasActive) {
            // Toca o som "WORK" quando a máquina liga
            level.playSound(null, pos, ModSounds.DMA_WORK.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        } else if (!isActive && this.wasActive) {
            // (Som de parada, se houver)
        }
    }

    /**
     * Envia avisos de chat, títulos de tela e toca o alarme.
     */
    private void handleTemperatureEffects(Level level, BlockPos pos) {
        DMAThermalHandler.TemperatureZone currentZone = this.thermalHandler.getZone();
        DMAThermalHandler.TemperatureZone lastZone = this.thermalHandler.getLastAnnouncedZone();

        if (currentZone == lastZone) return;

        Component chatMsg = null;
        switch (currentZone) {
            case OVERHEAT ->
                    chatMsg = Component.literal("DMA Overheat Warning (>4000°C)").withStyle(ChatFormatting.YELLOW);
            case INEFFICIENCY ->
                    chatMsg = Component.literal("DMA Coolant Inefficiency (>6000°C)").withStyle(ChatFormatting.GOLD);
            case DESTABILIZATION ->
                    chatMsg = Component.literal("!! DMA Matter Destabilization (>8500°C) !!").withStyle(ChatFormatting.RED);
            case MELTDOWN ->
                    chatMsg = Component.literal(">>> !!! DMA MELTDOWN IMMINENT (>10000°C) !!! <<<").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
        }

        AABB warningArea = new AABB(pos).inflate(64);
        List<ServerPlayer> playersNearby = level.getEntitiesOfClass(ServerPlayer.class, warningArea);

        if (!playersNearby.isEmpty()) {
            Component posMsg = Component.literal(" at [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "] in " + level.dimension().location().getPath())
                    .withStyle(ChatFormatting.GRAY);

            for (ServerPlayer player : playersNearby) {
                if (chatMsg != null) {
                    player.sendSystemMessage(chatMsg.copy().append(posMsg));
                }
                if (currentZone == DMAThermalHandler.TemperatureZone.MELTDOWN) {
                    Component title = Component.literal("⚠️ MELTDOWN ⚠️").withStyle(ChatFormatting.DARK_RED);
                    Component subtitle = Component.literal("Evacuate Immediately! (10s)").withStyle(ChatFormatting.RED); // Avisa do tempo
                    player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
                    player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                    player.connection.send(new ClientboundSetTitleTextPacket(title));
                }
            }
        }

        if (currentZone.ordinal() >= DMAThermalHandler.TemperatureZone.DESTABILIZATION.ordinal() &&
                lastZone.ordinal() < DMAThermalHandler.TemperatureZone.DESTABILIZATION.ordinal()) {

            level.playSound(null, pos, ModSounds.DMA_ALARM.get(), SoundSource.BLOCKS, 2.0f, 1.0f);
        }

        this.thermalHandler.setLastAnnouncedZone(currentZone);
    }

    // =================================================================================
    // --- NOVO MÉTODO DE DANO POR CALOR (COM CHECAGEM DE ARMADURA) ---
    // =================================================================================

    /**
     * Aplica dano de calor a players próximos baseado na temperatura
     * e na armadura de proteção do player.
     */
    private void handleHeatDamage(Level level, BlockPos pos) {
        // 1. Controle de Frequência: Só executa a cada 20 ticks (1 segundo)
        if (level.getGameTime() % 20 != 0) {
            return;
        }

        DMAThermalHandler.TemperatureZone zone = this.thermalHandler.getZone();

        // 2. Define o dano base da zona
        float damageAmount = 0;
        switch (zone) {
            case OVERHEAT:        damageAmount = 1.0f; break; // 0.5 coração
            case INEFFICIENCY:    damageAmount = 2.0f; break; // 1 coração
            case DESTABILIZATION: damageAmount = 4.0f; break; // 2 corações
            case MELTDOWN:        damageAmount = 8.0f; break; // 4 corações
            case SAFE:
            default:
                return; // Se está seguro, para aqui.
        }

        // 3. Define a área de dano (ex: 4 blocos)
        AABB damageArea = new AABB(pos).inflate(4.0);
        List<Player> playersNearby = level.getEntitiesOfClass(Player.class, damageArea);

        // 4. Pega a fonte de dano (tipo "pisar em magma")
        // --- CORREÇÃO: (Erro 2) Pega a DamageSource de "hotFloor" diretamente ---
        DamageSource heatDamage = level.damageSources().hotFloor();

        for (Player player : playersNearby) {
            if (player.isCreative() || player.isSpectator()) {
                continue;
            }

            // 6. VERIFICA A ARMADURA (MITIGAÇÃO)
            float mitigation = 0.0f; // 0.0 = 0% mitigado, 1.0 = 100% mitigado

            // Checa se o item em CADA slot implementa nossa interface IThermalArmor
            // Cada peça dá 25% de proteção contra o calor do DMA
            if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof IThermalArmor) {
                mitigation += 0.25f;
            }
            if (player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof IThermalArmor) {
                mitigation += 0.25f;
            }
            if (player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof IThermalArmor) {
                mitigation += 0.25f;
            }
            if (player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof IThermalArmor) {
                mitigation += 0.25f;
            }
            // Se o jogador estiver com o set completo, mitigation = 1.0 (100%)

            // 7. Aplica o dano final
            float finalDamage = damageAmount * (1.0f - mitigation);

            if (finalDamage > 0.0f) {
                player.hurt(heatDamage, finalDamage);
            }
        }
    }

    // =================================================================================
    // --- LÓGICA DE AUTO I/O (SEM MUDANÇAS) ---
    // =================================================================================

    private void tryPullItems(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            IOMode mode = getSideConfig(ConfigType.ITEM, dir);
            if (mode == IOMode.ITEM_IN || mode == IOMode.ITEM_IO || mode == IOMode.ITEM_IO2) {
                IItemHandler neighbor = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(dir), dir.getOpposite());
                if (neighbor != null) {
                    for (int i = 0; i < neighbor.getSlots(); i++) {
                        ItemStack extracted = neighbor.extractItem(i, 64, true);
                        if (!extracted.isEmpty()) {
                            ItemStack remaining = ItemHandlerHelper.insertItem(this.itemInputHandler, extracted, false);
                            int toExtract = extracted.getCount() - remaining.getCount();
                            if (toExtract > 0) {
                                neighbor.extractItem(i, toExtract, false);
                            }
                        }
                    }
                }
            }
        }
    }

    private void tryPullFluids(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (autoInput[ConfigType.FLUID.ordinal()]) {
                IOMode fluidMode = getSideConfig(ConfigType.FLUID, dir);
                if (fluidMode == IOMode.FLUID_IN || fluidMode == IOMode.FLUID_IO_1 || fluidMode == IOMode.FLUID_IO_2) {
                    IFluidHandler neighbor = level.getCapability(Capabilities.FluidHandler.BLOCK, pos.relative(dir), dir.getOpposite());
                    if (neighbor != null) {
                        FluidStack drained = neighbor.drain(1000, IFluidHandler.FluidAction.SIMULATE);
                        if (!drained.isEmpty()) {
                            int filled = this.fluidInputTank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                            neighbor.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                        }
                    }
                }
            }

            if (autoInput[ConfigType.COOLANT.ordinal()]) {
                IOMode coolantMode = getSideConfig(ConfigType.COOLANT, dir);
                if (coolantMode == IOMode.COOLANT_IN) {
                    IFluidHandler neighbor = level.getCapability(Capabilities.FluidHandler.BLOCK, pos.relative(dir), dir.getOpposite());
                    if (neighbor != null) {
                        FluidStack drained = neighbor.drain(1000, IFluidHandler.FluidAction.SIMULATE);
                        if (!drained.isEmpty()) {
                            int filled = this.coolantInputTank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                            neighbor.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                        }
                    }
                }
            }
        }
    }

    private void tryEjectItems(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            IOMode mode = getSideConfig(ConfigType.ITEM, dir);
            if (mode == IOMode.ITEM_OUT || mode == IOMode.ITEM_OUT2 || mode == IOMode.ITEM_IO || mode == IOMode.ITEM_IO2) {
                IItemHandler neighbor = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(dir), dir.getOpposite());
                if (neighbor != null) {
                    for (int i = 0; i < itemOutputHandler.getSlots(); i++) {
                        ItemStack stack = itemOutputHandler.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            ItemStack remaining = ItemHandlerHelper.insertItem(neighbor, stack.copy(), false);
                            itemOutputHandler.setStackInSlot(i, remaining);
                        }
                    }
                }
            }
        }
    }

    private void tryEjectFluids(Level level, BlockPos pos, ConfigType type) {
        for (Direction dir : Direction.values()) {
            IOMode mode = getSideConfig(type, dir);
            if (mode == IOMode.FLUID_OUT_1 || mode == IOMode.FLUID_OUT_2) {
                FluidTank tank = (mode == IOMode.FLUID_OUT_1) ? fluidOutputTank1 : fluidOutputTank2;
                if (tank.getFluidAmount() > 0) {
                    IFluidHandler neighbor = level.getCapability(Capabilities.FluidHandler.BLOCK, pos.relative(dir), dir.getOpposite());
                    if (neighbor != null) {
                        FluidStack drained = tank.drain(1000, IFluidHandler.FluidAction.SIMULATE);
                        if (!drained.isEmpty()) {
                            int filled = neighbor.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                            tank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                        }
                    }
                }
            }
        }
    }


    // =================================================================================
    // --- LÓGICA DE RECEITA (SEM MUDANÇAS) ---
    // =================================================================================
    private Optional<DimensionalMatterAssemblerRecipe> getRecipe() {
        if (level == null) return Optional.empty();
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.DMA_TYPE.get()).stream()
                .map(RecipeHolder::value)
                .filter(this::matchesRecipe)
                .findFirst();
    }

    private boolean matchesRecipe(DimensionalMatterAssemblerRecipe recipe) {

        IngredientStack.Fluid requiredFluidStack = recipe.getFluidInput();
        long requiredFluidAmount = requiredFluidStack.getAmount();

        if (requiredFluidAmount > 0) {
            if (fluidInputTank.isEmpty() || !requiredFluidStack.getIngredient().test(fluidInputTank.getFluid()) || fluidInputTank.getFluidAmount() < requiredFluidAmount) {
                return false;
            }
        }

        IngredientStack.Fluid requiredCoolantStack = recipe.getCoolantInput();
        long requiredCoolantAmount = requiredCoolantStack.getAmount();

        if (requiredCoolantAmount > 0) {
            if (coolantInputTank.isEmpty() || !requiredCoolantStack.getIngredient().test(coolantInputTank.getFluid()) || coolantInputTank.getFluidAmount() < requiredCoolantAmount) {
                return false;
            }
        }

        NonNullList<ItemStack> inventoryCopy = NonNullList.withSize(itemInputHandler.getSlots(), ItemStack.EMPTY);
        for (int i = 0; i < itemInputHandler.getSlots(); i++) {
            inventoryCopy.set(i, itemInputHandler.getStackInSlot(i).copy());
        }

        for (var recipeIngredientStack : recipe.getItemInputs()) {
            Ingredient ingredient = recipeIngredientStack.getIngredient();
            long requiredAmount = recipeIngredientStack.getAmount();

            for (int i = 0; i < inventoryCopy.size(); i++) {
                ItemStack stackInSlot = inventoryCopy.get(i);
                if (ingredient.test(stackInSlot)) {
                    long toConsume = Math.min(stackInSlot.getCount(), requiredAmount);
                    stackInSlot.shrink((int) toConsume);
                    requiredAmount -= toConsume;

                    if (requiredAmount <= 0) {
                        break;
                    }
                }
            }

            if (requiredAmount > 0) {
                return false;
            }
        }
        return true;
    }

    private boolean canProcess(DimensionalMatterAssemblerRecipe recipe) {
        List<ItemStack> itemOutputs = recipe.getItemOutputs();
        for (int i = 0; i < itemOutputs.size(); i++) {
            if (i >= itemOutputHandler.getSlots()) return false;

            ItemStack recipeOutput = itemOutputs.get(i);
            if (recipeOutput.isEmpty()) continue;

            ItemStack currentOutput = itemOutputHandler.getStackInSlot(i);

            if (!currentOutput.isEmpty()) {
                if (!ItemStack.isSameItemSameComponents(currentOutput, recipeOutput)) {
                    return false;
                }
                int maxStack = itemOutputHandler.getSlotLimit(i);
                if (currentOutput.getCount() + recipeOutput.getCount() > maxStack) {
                    return false;
                }
            }
        }

        List<FluidStack> fluidOutputs = recipe.getFluidOutputs();
        if (fluidOutputs.size() > 0 && !fluidOutputs.get(0).isEmpty()) {
            FluidStack recipeFluid = fluidOutputs.get(0);
            int filledAmount = fluidOutputTank1.fill(recipeFluid, IFluidHandler.FluidAction.SIMULATE);
            if (filledAmount < recipeFluid.getAmount()) {
                return false;
            }
        }

        if (fluidOutputs.size() > 1 && !fluidOutputs.get(1).isEmpty()) {
            FluidStack recipeFluid = fluidOutputs.get(1);
            int filledAmount = fluidOutputTank2.fill(recipeFluid, IFluidHandler.FluidAction.SIMULATE);
            if (filledAmount < recipeFluid.getAmount()) {
                return false;
            }
        }
        return true;
    }

    private void craft(DimensionalMatterAssemblerRecipe recipe, DMAThermalHandler.ActiveModifiers mods) {
        if (!mods.isCreative()) {
            IngredientStack.Fluid fluidToConsume = recipe.getFluidInput();
            if (fluidToConsume.getAmount() > 0) {
                fluidInputTank.drain((int)fluidToConsume.getAmount(), IFluidHandler.FluidAction.EXECUTE);
            }

            IngredientStack.Fluid coolantToConsume = recipe.getCoolantInput();
            if (coolantToConsume.getAmount() > 0) {
                coolantInputTank.drain((int)coolantToConsume.getAmount(), IFluidHandler.FluidAction.EXECUTE);
            }

            for (var ingredientStack : recipe.getItemInputs()) {
                long remaining = ingredientStack.getAmount();
                for (int i = 0; i < itemInputHandler.getSlots(); i++) {
                    if (remaining <= 0) break;
                    ItemStack stack = itemInputHandler.getStackInSlot(i);
                    if (!stack.isEmpty() && ingredientStack.getIngredient().test(stack)) {
                        int toExtract = (int) Math.min(stack.getCount(), remaining);
                        itemInputHandler.extractItem(i, toExtract, false);
                        remaining -= toExtract;
                    }
                }
            }
        }

        List<ItemStack> itemOutputs = recipe.getItemOutputs();
        for (int i = 0; i < itemOutputs.size(); i++) {
            itemOutputHandler.insertItem(i, itemOutputs.get(i).copy(), false);
        }
        List<FluidStack> fluidOutputs = recipe.getFluidOutputs();
        if (fluidOutputs.size() > 0) fluidOutputTank1.fill(fluidOutputs.get(0).copy(), IFluidHandler.FluidAction.EXECUTE);
        if (fluidOutputs.size() > 1) fluidOutputTank2.fill(fluidOutputs.get(1).copy(), IFluidHandler.FluidAction.EXECUTE);

        double bonusDropChance = mods.totalDropChance();
        while (bonusDropChance > 0 && this.level != null) {
            if (mods.isCreative() || this.level.random.nextDouble() < bonusDropChance) {
                ItemStack bonusDrop = new ItemStack(ModItems.SCAR.get());
                this.itemOutputHandler.insertItem(1, bonusDrop, false);
            }
            bonusDropChance -= 1.0;
        }
    }

    // =================================================================================
    // --- I/O, NBT, NETWORKING, CLASSES INTERNAS (SEM MUDANÇAS) ---
    // =================================================================================

    public IOMode getSideConfig(ConfigType type, Direction side) {
        return sideConfigs[type.ordinal()][side.ordinal()];
    }

    public void setSideConfig(ConfigType type, Direction side, IOMode mode) {
        sideConfigs[type.ordinal()][side.ordinal()] = mode;
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
            level.updateNeighborsAt(worldPosition, state.getBlock());
        }
    }

    public boolean isAutoEject(ConfigType type) { return autoEject[type.ordinal()]; }

    public void setAutoEject(ConfigType type, boolean active) {
        if (this.autoEject[type.ordinal()] != active) {
            this.autoEject[type.ordinal()] = active;
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    public boolean isAutoInput(ConfigType type) { return autoInput[type.ordinal()]; }

    public void setAutoInput(ConfigType type, boolean enabled) {
        if (this.autoInput[type.ordinal()] != enabled) {
            this.autoInput[type.ordinal()] = enabled;
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    public void resetAllSideConfigs() { /* ... */ }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("input_items", itemInputHandler.serializeNBT(registries));
        tag.put("output_items", itemOutputHandler.serializeNBT(registries));
        tag.put("upgrades", upgradeHandler.serializeNBT(registries));
        tag.put("input_fluid", fluidInputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("input_coolant", coolantInputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("output_fluid1", fluidOutputTank1.writeToNBT(registries, new CompoundTag()));
        tag.put("output_fluid2", fluidOutputTank2.writeToNBT(registries, new CompoundTag()));
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("progress", progress);
        thermalHandler.saveNBT(tag, registries);

        for (ConfigType type : ConfigType.values()) {
            int idx = type.ordinal();
            tag.putBoolean("auto_eject_" + idx, autoEject[idx]);
            tag.putBoolean("auto_input_" + idx, autoInput[idx]);
            int[] sideOrdinals = new int[6];
            for (int i = 0; i < 6; i++) { sideOrdinals[i] = sideConfigs[idx][i].ordinal(); }
            tag.putIntArray("side_config_" + idx, sideOrdinals);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemInputHandler.deserializeNBT(registries, tag.getCompound("input_items"));
        itemOutputHandler.deserializeNBT(registries, tag.getCompound("output_items"));
        upgradeHandler.deserializeNBT(registries, tag.getCompound("upgrades"));
        fluidInputTank.readFromNBT(registries, tag.getCompound("input_fluid"));
        coolantInputTank.readFromNBT(registries, tag.getCompound("input_coolant"));
        fluidOutputTank1.readFromNBT(registries, tag.getCompound("output_fluid1"));
        fluidOutputTank2.readFromNBT(registries, tag.getCompound("output_fluid2"));
        if (tag.contains("energy")) {
            energyStorage.receiveEnergy(tag.getInt("energy") - energyStorage.getEnergyStored(), false);
        }
        progress = tag.getInt("progress");
        thermalHandler.loadNBT(tag, registries);

        for (ConfigType type : ConfigType.values()) {
            int idx = type.ordinal();
            autoEject[idx] = tag.getBoolean("auto_eject_" + idx);
            autoInput[idx] = tag.getBoolean("auto_input_" + idx);
            if (tag.contains("side_config_" + idx)) {
                int[] sideOrdinals = tag.getIntArray("side_config_" + idx);
                for (int i = 0; i < 6; i++) {
                    if (i < sideConfigs[idx].length) {
                        int safeOrdinal = Math.min(Math.max(sideOrdinals[i], 0), IOMode.values().length - 1);
                        sideConfigs[idx][i] = IOMode.values()[safeOrdinal];
                    }
                }
            }
        }
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        if (level != null && level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    public IEnergyStorage getEnergyStorage() { return energyStorage; }
    public FluidTank getFluidInputTank() { return this.fluidInputTank; }
    public FluidTank getCoolantInputTank() { return this.coolantInputTank; }
    public FluidTank getFluidOutputTank1() { return this.fluidOutputTank1; }
    public FluidTank getFluidOutputTank2() { return this.fluidOutputTank2; }

    public class SidedItemHandler implements IItemHandler {
        private final Direction side;
        public SidedItemHandler(Direction side) { this.side = side; }

        @Override public int getSlots() { return itemInputHandler.getSlots() + itemOutputHandler.getSlots() + upgradeHandler.getSlots(); }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            if (slot < itemInputHandler.getSlots()) return itemInputHandler.getStackInSlot(slot);
            int offsetSlot = slot - itemInputHandler.getSlots();
            if (offsetSlot < itemOutputHandler.getSlots()) return itemOutputHandler.getStackInSlot(offsetSlot);
            offsetSlot -= itemOutputHandler.getSlots();
            return upgradeHandler.getStackInSlot(offsetSlot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            IOMode mode = getSideConfig(ConfigType.ITEM, side);
            if (mode != IOMode.ITEM_IN && mode != IOMode.ITEM_IO && mode != IOMode.ITEM_IO2) {
                return stack;
            }
            if (slot < itemInputHandler.getSlots()) {
                return itemInputHandler.insertItem(slot, stack, simulate);
            }
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            IOMode mode = getSideConfig(ConfigType.ITEM, side);
            if (mode != IOMode.ITEM_OUT && mode != IOMode.ITEM_OUT2 && mode != IOMode.ITEM_IO && mode != IOMode.ITEM_IO2) {
                return ItemStack.EMPTY;
            }
            if (slot >= itemInputHandler.getSlots() && slot < (itemInputHandler.getSlots() + itemOutputHandler.getSlots())) {
                return itemOutputHandler.extractItem(slot - itemInputHandler.getSlots(), amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override public int getSlotLimit(int slot) { return 64; }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot < itemInputHandler.getSlots()) return itemInputHandler.isItemValid(slot, stack);
            int offsetSlot = slot - itemInputHandler.getSlots();
            if (offsetSlot < itemOutputHandler.getSlots()) return itemOutputHandler.isItemValid(offsetSlot, stack);
            offsetSlot -= itemOutputHandler.getSlots();
            return upgradeHandler.isItemValid(offsetSlot, stack);
        }
    }

    public class SidedFluidHandler implements IFluidHandler {
        private final Direction side;
        public SidedFluidHandler(Direction side) { this.side = side; }

        @Override public int getTanks() { return 4; }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return switch (tank) {
                case 0 -> fluidInputTank.getFluid();
                case 1 -> coolantInputTank.getFluid();
                case 2 -> fluidOutputTank1.getFluid();
                case 3 -> fluidOutputTank2.getFluid();
                default -> FluidStack.EMPTY;
            };
        }

        @Override
        public int getTankCapacity(int tank) {
            return switch (tank) {
                case 0 -> fluidInputTank.getCapacity();
                case 1 -> coolantInputTank.getCapacity();
                case 2 -> fluidOutputTank1.getCapacity();
                case 3 -> fluidOutputTank2.getCapacity();
                default -> 0;
            };
        }

        @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return true; }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            int filled = 0;
            if (getSideConfig(ConfigType.COOLANT, side) == IOMode.COOLANT_IN) {
                filled = coolantInputTank.fill(resource, action);
            }
            if (filled == 0) {
                IOMode fluidMode = getSideConfig(ConfigType.FLUID, side);
                if (fluidMode == IOMode.FLUID_IN || fluidMode == IOMode.FLUID_IO_1 || fluidMode == IOMode.FLUID_IO_2) {
                    filled = fluidInputTank.fill(resource, action);
                }
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            IOMode fluidMode = getSideConfig(ConfigType.FLUID, side);
            if ((fluidMode == IOMode.FLUID_OUT_1 || fluidMode == IOMode.FLUID_IO_1) && !fluidOutputTank1.isEmpty()) {
                return fluidOutputTank1.drain(resource, action);
            }
            if ((fluidMode == IOMode.FLUID_OUT_2 || fluidMode == IOMode.FLUID_IO_2) && !fluidOutputTank2.isEmpty()) {
                return fluidOutputTank2.drain(resource, action);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            IOMode fluidMode = getSideConfig(ConfigType.FLUID, side);
            if (fluidMode == IOMode.FLUID_OUT_1 || fluidMode == IOMode.FLUID_IO_1) {
                return fluidOutputTank1.drain(maxDrain, action);
            }
            if (fluidMode == IOMode.FLUID_OUT_2 || fluidMode == IOMode.FLUID_IO_2) {
                return fluidOutputTank2.drain(maxDrain, action);
            }
            return FluidStack.EMPTY;
        }
    }

    public class SidedEnergyStorage implements IEnergyStorage {
        private final Direction side;
        public SidedEnergyStorage(Direction side) { this.side = side; }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (getSideConfig(ConfigType.ENERGY, this.side) == IOMode.ENERGY) {
                return energyStorage.receiveEnergy(maxReceive, simulate);
            }
            return 0;
        }
        @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return energyStorage.getEnergyStored(); }
        @Override public int getMaxEnergyStored() { return energyStorage.getMaxEnergyStored(); }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    }
}