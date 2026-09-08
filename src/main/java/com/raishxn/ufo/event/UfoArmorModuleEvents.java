package com.raishxn.ufo.event;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.armor.UfoArmorModule;
import com.raishxn.ufo.armor.UfoArmorModules;
import com.raishxn.ufo.armor.UfoArmorSetting;
import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.item.custom.UfoArmorItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = UfoMod.MOD_ID)
public final class UfoArmorModuleEvents {
    private static final int EFFECT_DURATION = 720;
    private static final int EFFECT_REFRESH_THRESHOLD = 600;
    private static final String TRANSLOCATOR_COOLDOWN = "ufoMatterTranslocatorCooldown";
    private static final ThreadLocal<Boolean> REFLECTING = ThreadLocal.withInitial(() -> false);
    private static final net.minecraft.resources.ResourceLocation STEP_HEIGHT_MODIFIER = UfoMod.id("phase_step_height");
    private static final net.minecraft.resources.ResourceLocation BLOCK_REACH_MODIFIER = UfoMod.id("phase_block_reach");
    private static final net.minecraft.resources.ResourceLocation ENTITY_REACH_MODIFIER = UfoMod.id("phase_entity_reach");
    private static final net.minecraft.resources.ResourceLocation MOVEMENT_SPEED_MODIFIER = UfoMod.id("kinetic_movement_speed");
    private static final net.minecraft.resources.ResourceLocation LUCK_MODIFIER = UfoMod.id("loot_singularity_luck");

    private UfoArmorModuleEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (REFLECTING.get()) return;

        if (event.getSource().getEntity() instanceof Player attacker
                && UfoArmorModules.active(attacker, UfoArmorModule.SINGULARITY_STRIKE)
                && UfoArmorModules.consume(attacker, UfoArmorModule.SINGULARITY_STRIKE)) {
            int multiplier = UfoArmorItem.moduleSetting(
                    UfoArmorModules.stack(attacker, UfoArmorModule.SINGULARITY_STRIKE),
                    UfoArmorSetting.STRIKE_MULTIPLIER);
            event.setAmount(Math.min(Float.MAX_VALUE, event.getAmount() * multiplier));
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (UfoArmorModules.active(player, UfoArmorModule.REPRISAL_MATRIX)
                && event.getSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker
                && attacker != player
                && UfoArmorModules.consume(player, UfoArmorModule.REPRISAL_MATRIX)) {
            try {
                REFLECTING.set(true);
                attacker.invulnerableTime = 0;
                int multiplier = UfoArmorItem.moduleSetting(
                        UfoArmorModules.stack(player, UfoArmorModule.REPRISAL_MATRIX),
                        UfoArmorSetting.REPRISAL_MULTIPLIER);
                attacker.hurt(player.damageSources().thorns(player),
                        Math.min(Float.MAX_VALUE, event.getAmount() * multiplier));
            } finally {
                REFLECTING.set(false);
            }
        }

        if (UfoArmorModules.active(player, UfoArmorModule.REALITY_ANCHOR)
                && event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)
                && UfoArmorModules.consume(player, UfoArmorModule.REALITY_ANCHOR)) {
            event.setCanceled(true);
            teleportToSafety(player);
            return;
        }

        int shieldCost = Math.max(UfoArmorModule.AEGIS_SINGULARITY.energyCost(), (int) Math.ceil(event.getAmount() * 1_000.0));
        if (UfoArmorModules.active(player, UfoArmorModule.AEGIS_SINGULARITY)
                && UfoArmorModules.consume(player, UfoArmorModule.AEGIS_SINGULARITY, shieldCost)) {
            event.setCanceled(true);
            player.hurtTime = 0;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (UfoArmorModules.active(player, UfoArmorModule.REALITY_ANCHOR)
                && UfoArmorModules.consume(player, UfoArmorModule.REALITY_ANCHOR)) {
            event.setCanceled(true);
            player.deathTime = 0;
            player.setHealth(player.getMaxHealth());
            teleportToSafety(player);
        }
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player
                && UfoArmorModules.active(player, UfoArmorModule.PHASE_STEP)) {
            event.setCanceled(true);
            UfoArmorModules.consume(player, UfoArmorModule.PHASE_STEP);
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)
                || !UfoArmorModules.active(player, UfoArmorModule.LOOT_SINGULARITY)) return;
        int luck = UfoArmorItem.moduleSetting(
                UfoArmorModules.stack(player, UfoArmorModule.LOOT_SINGULARITY), UfoArmorSetting.LUCK_LEVEL);
        for (ItemEntity drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            long multiplied = (long) stack.getCount() * (luck + 1L);
            stack.setCount((int) Math.min(Integer.MAX_VALUE, multiplied));
        }
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        Player player = event.getAttackingPlayer();
        if (player == null || !UfoArmorModules.active(player, UfoArmorModule.LOOT_SINGULARITY)) return;
        int luck = UfoArmorItem.moduleSetting(
                UfoArmorModules.stack(player, UfoArmorModule.LOOT_SINGULARITY), UfoArmorSetting.LUCK_LEVEL);
        long multiplied = (long) event.getDroppedExperience() * (luck + 1L);
        event.setDroppedExperience((int) Math.min(Integer.MAX_VALUE, multiplied));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onTargetChange(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Enemy
                && event.getNewAboutToBeSetTarget() instanceof Player player
                && UfoArmorModules.active(player, UfoArmorModule.CLOAKING_FIELD)) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof Player player
                && event.getEffectInstance().is(MobEffects.DARKNESS)
                && UfoArmorModules.active(player, UfoArmorModule.ABYSSAL_SIGHT)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        syncModuleAttributes(player);
        if (!UfoArmorItem.hasFullUfoSet(player)) return;
        long tick = player.level().getGameTime();

        if (UfoArmorModules.active(player, UfoArmorModule.CHRONO_REGENERATOR)) {
            int regeneration = UfoArmorItem.moduleSetting(
                    UfoArmorModules.stack(player, UfoArmorModule.CHRONO_REGENERATOR),
                    UfoArmorSetting.REGENERATION_LEVEL);
            refresh(player, MobEffects.REGENERATION, regeneration - 1);
            if (tick % 20 == 0) {
                player.getFoodData().setFoodLevel(20);
                player.getFoodData().setSaturation(20.0F);
                UfoArmorModules.consume(player, UfoArmorModule.CHRONO_REGENERATOR);
            }
        }
        if (UfoArmorModules.active(player, UfoArmorModule.ABYSSAL_SIGHT)) {
            refresh(player, MobEffects.NIGHT_VISION, 0);
            player.removeEffect(MobEffects.DARKNESS);
            if (tick % 20 == 0) UfoArmorModules.consume(player, UfoArmorModule.ABYSSAL_SIGHT);
        }
        if (UfoArmorModules.active(player, UfoArmorModule.ADAPTIVE_BIOSPHERE)) {
            refresh(player, MobEffects.FIRE_RESISTANCE, 0);
            refresh(player, MobEffects.WATER_BREATHING, 0);
            player.setAirSupply(player.getMaxAirSupply());
            player.clearFire();
            player.removeEffect(MobEffects.POISON);
            player.removeEffect(MobEffects.WITHER);
            if (tick % 20 == 0) UfoArmorModules.consume(player, UfoArmorModule.ADAPTIVE_BIOSPHERE);
        }
        if (UfoArmorModules.active(player, UfoArmorModule.KINETIC_OVERDRIVE)) {
            int speed = UfoArmorItem.moduleSetting(
                    UfoArmorModules.stack(player, UfoArmorModule.KINETIC_OVERDRIVE),
                    UfoArmorSetting.KINETIC_SPEED);
            int jump = UfoArmorItem.moduleSetting(
                    UfoArmorModules.stack(player, UfoArmorModule.KINETIC_OVERDRIVE),
                    UfoArmorSetting.KINETIC_JUMP);
            refresh(player, MobEffects.JUMP, jump - 1);
            int speedTier = Math.max(1, Math.min(10, (speed + 99) / 100));
            refresh(player, MobEffects.DIG_SPEED, speedTier - 1);
            refresh(player, MobEffects.DOLPHINS_GRACE, Math.max(0, speedTier / 2 - 1));
            if (tick % 20 == 0) UfoArmorModules.consume(player, UfoArmorModule.KINETIC_OVERDRIVE);
        }
        if (UfoArmorModules.active(player, UfoArmorModule.LOOT_SINGULARITY)) {
            if (tick % 20 == 0) UfoArmorModules.consume(player, UfoArmorModule.LOOT_SINGULARITY);
        }
        if (UfoArmorModules.active(player, UfoArmorModule.CLOAKING_FIELD)) {
            clearHostileTargets(player);
            if (tick % 20 == 0) UfoArmorModules.consume(player, UfoArmorModule.CLOAKING_FIELD);
        }
        if (UfoArmorModules.active(player, UfoArmorModule.VOID_FLIGHT)
                && player.getAbilities().flying) {
            UfoArmorModules.consume(player, UfoArmorModule.VOID_FLIGHT);
        }
        if (tick % 5 == 0 && UfoArmorModules.active(player, UfoArmorModule.ENTROPY_MAGNET)) {
            magnet(player);
            UfoArmorModules.consume(player, UfoArmorModule.ENTROPY_MAGNET);
        }
        if (tick % 5 == 0 && UfoArmorModules.active(player, UfoArmorModule.QUANTUM_RELAY)) {
            rechargeInventory(player);
        }
        if (tick % 20 == 0 && UfoArmorModules.active(player, UfoArmorModule.MATTER_TRANSLOCATOR)) {
            int threshold = UfoArmorItem.moduleSetting(
                    UfoArmorModules.stack(player, UfoArmorModule.MATTER_TRANSLOCATOR),
                    UfoArmorSetting.TRANSLOCATOR_THRESHOLD);
            if (player.getHealth() <= player.getMaxHealth() * threshold / 100.0F) {
                long cooldown = player.getPersistentData().getLong(TRANSLOCATOR_COOLDOWN);
                if (tick >= cooldown && UfoArmorModules.consume(player, UfoArmorModule.MATTER_TRANSLOCATOR)) {
                    player.getPersistentData().putLong(TRANSLOCATOR_COOLDOWN, tick + 200);
                    int heal = UfoArmorItem.moduleSetting(
                            UfoArmorModules.stack(player, UfoArmorModule.MATTER_TRANSLOCATOR),
                            UfoArmorSetting.TRANSLOCATOR_HEAL);
                    player.setHealth(Math.max(player.getHealth(), player.getMaxHealth() * heal / 100.0F));
                    teleportToSafety((ServerPlayer) player);
                }
            }
        }
    }

    private static void refresh(Player player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int amplifier) {
        MobEffectInstance current = player.getEffect(effect);
        if (current == null || current.getDuration() < EFFECT_REFRESH_THRESHOLD || current.getAmplifier() != amplifier) {
            player.addEffect(new MobEffectInstance(effect, EFFECT_DURATION, amplifier, false, false, true));
        }
    }

    private static void magnet(Player player) {
        int range = UfoArmorItem.moduleSetting(
                UfoArmorModules.stack(player, UfoArmorModule.ENTROPY_MAGNET), UfoArmorSetting.MAGNET_RANGE);
        var area = player.getBoundingBox().inflate(range);
        for (ItemEntity item : player.level().getEntitiesOfClass(ItemEntity.class, area)) {
            if (!item.hasPickUpDelay()) item.playerTouch(player);
        }
        for (ExperienceOrb orb : player.level().getEntitiesOfClass(ExperienceOrb.class, area)) {
            orb.playerTouch(player);
        }
    }

    private static void clearHostileTargets(Player player) {
        int range = UfoArmorItem.moduleSetting(
                UfoArmorModules.stack(player, UfoArmorModule.CLOAKING_FIELD), UfoArmorSetting.CLOAKING_RANGE);
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(range),
                mob -> mob instanceof Enemy && mob.getTarget() == player)) {
            mob.setTarget(null);
        }
    }

    private static void rechargeInventory(Player player) {
        ItemStack source = UfoArmorModules.stack(player, UfoArmorModule.QUANTUM_RELAY);
        int available = source.getOrDefault(ModDataComponents.ENERGY.get(), 0);
        if (available <= UfoArmorModule.QUANTUM_RELAY.energyCost()) return;
        for (ItemStack target : player.getInventory().items) {
            IEnergyStorage storage = target.getCapability(Capabilities.EnergyStorage.ITEM);
            if (storage == null || !storage.canReceive()) continue;
            int rate = UfoArmorItem.moduleSetting(source, UfoArmorSetting.RELAY_TRANSFER_RATE);
            int offered = Math.min(rate, available - UfoArmorModule.QUANTUM_RELAY.energyCost());
            int accepted = storage.receiveEnergy(offered, false);
            if (accepted > 0) {
                source.set(ModDataComponents.ENERGY.get(), available - accepted - UfoArmorModule.QUANTUM_RELAY.energyCost());
                return;
            }
        }
    }

    private static void syncModuleAttributes(Player player) {
        if (UfoArmorModules.active(player, UfoArmorModule.PHASE_STEP)) {
            ItemStack boots = UfoArmorModules.stack(player, UfoArmorModule.PHASE_STEP);
            int stepHeight = UfoArmorItem.moduleSetting(boots, UfoArmorSetting.STEP_HEIGHT);
            int reach = UfoArmorItem.moduleSetting(boots, UfoArmorSetting.REACH_DISTANCE);
            setAttribute(player, Attributes.STEP_HEIGHT, STEP_HEIGHT_MODIFIER, Math.max(0.0, stepHeight - 0.6));
            setAttribute(player, Attributes.BLOCK_INTERACTION_RANGE, BLOCK_REACH_MODIFIER, reach);
            setAttribute(player, Attributes.ENTITY_INTERACTION_RANGE, ENTITY_REACH_MODIFIER, reach);
        } else {
            removeAttribute(player, Attributes.STEP_HEIGHT, STEP_HEIGHT_MODIFIER);
            removeAttribute(player, Attributes.BLOCK_INTERACTION_RANGE, BLOCK_REACH_MODIFIER);
            removeAttribute(player, Attributes.ENTITY_INTERACTION_RANGE, ENTITY_REACH_MODIFIER);
        }

        if (UfoArmorModules.active(player, UfoArmorModule.LOOT_SINGULARITY)) {
            int luck = UfoArmorItem.moduleSetting(
                    UfoArmorModules.stack(player, UfoArmorModule.LOOT_SINGULARITY), UfoArmorSetting.LUCK_LEVEL);
            setAttribute(player, Attributes.LUCK, LUCK_MODIFIER, luck);
        } else {
            removeAttribute(player, Attributes.LUCK, LUCK_MODIFIER);
        }

        if (UfoArmorModules.active(player, UfoArmorModule.KINETIC_OVERDRIVE)) {
            int speedPercent = UfoArmorItem.moduleSetting(
                    UfoArmorModules.stack(player, UfoArmorModule.KINETIC_OVERDRIVE), UfoArmorSetting.KINETIC_SPEED);
            setAttribute(player, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_MODIFIER,
                    speedPercent / 100.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        } else {
            removeAttribute(player, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_MODIFIER);
        }
    }

    private static void setAttribute(Player player,
                                     net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                     net.minecraft.resources.ResourceLocation id, double amount) {
        setAttribute(player, attribute, id, amount, AttributeModifier.Operation.ADD_VALUE);
    }

    private static void setAttribute(Player player,
                                     net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                     net.minecraft.resources.ResourceLocation id, double amount,
                                     AttributeModifier.Operation operation) {
        var instance = player.getAttribute(attribute);
        if (instance != null) {
            AttributeModifier current = instance.getModifier(id);
            if (current != null && Double.compare(current.amount(), amount) == 0 && current.operation() == operation) return;
            instance.addOrUpdateTransientModifier(
                    new AttributeModifier(id, amount, operation));
        }
    }

    private static void removeAttribute(Player player,
                                        net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                        net.minecraft.resources.ResourceLocation id) {
        var instance = player.getAttribute(attribute);
        if (instance != null) instance.removeModifier(id);
    }

    private static void teleportToSafety(ServerPlayer player) {
        BlockPos pos = player.getRespawnPosition();
        ServerLevel level = player.server.getLevel(player.getRespawnDimension());
        if (pos != null && level != null) {
            player.teleportTo(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, player.getYRot(), player.getXRot());
        } else {
            ServerLevel current = player.serverLevel();
            BlockPos spawn = current.getSharedSpawnPos();
            player.teleportTo(current, spawn.getX() + 0.5, spawn.getY() + 1.0, spawn.getZ() + 0.5, player.getYRot(), player.getXRot());
        }
    }
}
