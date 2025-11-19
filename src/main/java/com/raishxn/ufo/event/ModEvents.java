package com.raishxn.ufo.event;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.item.custom.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes; // Correct Import
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = UfoMod.MOD_ID)
public class ModEvents {

    // --- PICKAXE AND AXE LOGIC (Block Break) ---
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        ItemStack stack = player.getMainHandItem();

        if (stack.getItem() instanceof UfoEnergyPickaxeItem) {
            int currentFortune = stack.getOrDefault(ModDataComponents.PROGRESSIVE_FORTUNE.get(), 0);
            if (currentFortune < 100) {
                stack.set(ModDataComponents.PROGRESSIVE_FORTUNE.get(), currentFortune + 1);
            }

            if (stack.getOrDefault(ModDataComponents.AUTO_SMELT.get(), false)) {
                Level level = player.level();
                if (!level.isClientSide) {
                    consumeEnergyDirect(stack, 50);
                }
            }
        }
    }

    // --- COMBAT LOGIC (Sword and Axe) ---
    @SubscribeEvent
    public static void onPlayerAttack(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack stack = player.getMainHandItem();

            // 1. Sword Logic
            if (stack.getItem() instanceof UfoEnergySwordItem) {
                int kills = stack.getOrDefault(ModDataComponents.KILL_COUNT.get(), 0);
                float extraDmg = kills * 2.0f;

                long lastHit = stack.getOrDefault(ModDataComponents.LAST_HIT_TIME.get(), 0L);
                int combo = stack.getOrDefault(ModDataComponents.COMBO_COUNT.get(), 0);
                long time = player.level().getGameTime();

                if (time - lastHit < 60) {
                    combo++;
                    if (combo >= 5) {
                        extraDmg *= 1.5f;
                        player.displayClientMessage(Component.literal("COMBO!").withStyle(ChatFormatting.GOLD), true);
                        combo = 0;
                    }
                } else {
                    combo = 1;
                }
                stack.set(ModDataComponents.COMBO_COUNT.get(), combo);
                stack.set(ModDataComponents.LAST_HIT_TIME.get(), time);

                event.setAmount(event.getAmount() + extraDmg);
                event.getEntity().addEffect(new MobEffectInstance(MobEffects.POISON, 60, 2));
            }

            // 2. Greatsword Logic
            if (stack.getItem() instanceof UfoEnergyGreatswordItem) {
                float maxHp = player.getMaxHealth();
                float currentHp = player.getHealth();
                float percent = currentHp / maxHp;

                float multiplier = 1.0f;
                if (percent <= 0.10f) multiplier = 1.5f;
                else if (percent <= 0.30f) multiplier = 1.2f;

                event.setAmount(event.getAmount() * multiplier);
            }

            // 3. Axe Logic
            if (stack.getItem() instanceof UfoEnergyAxeItem) {
                float targetArmor = event.getEntity().getArmorValue();
                if (targetArmor > 0) {
                    event.setAmount(event.getAmount() + (targetArmor * 0.2f));
                }
            }
        }
    }

    // --- ARMOR LOGIC (Protection) ---
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDefend(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!isFullUfoArmor(player)) return;

            // 1. Anti-Void (Corrected Variable: FELL_OUT_OF_WORLD)
            if (event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD) && event.getAmount() < Float.MAX_VALUE) {
                if (consumeArmorEnergyDirect(player, 50000)) {
                    event.setCanceled(true);
                    teleportToSafety(player);
                    return;
                }
            }

            // 2. Anti-Kill Command / Absolute Damage / Infinite Void
            if (event.getSource().is(DamageTypes.GENERIC_KILL) ||
                    event.getAmount() >= Float.MAX_VALUE ||
                    (event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD) && event.getAmount() > 10000f)) {

                if (consumeArmorEnergyDirect(player, 100000)) {
                    event.setCanceled(true);
                    player.setHealth(player.getMaxHealth());
                    player.sendSystemMessage(Component.literal("Anti-Death Protocol Activated!").withStyle(ChatFormatting.GOLD));
                    return;
                }
            }

            // 3. Teleport on Low Health
            if (player.getHealth() - event.getAmount() <= 4.0f) {
                if (consumeArmorEnergyDirect(player, 10000)) {
                    event.setCanceled(true);
                    player.setHealth(player.getMaxHealth() / 2);
                    teleportToSafety(player);
                    player.sendSystemMessage(Component.literal("Emergency Evacuation!").withStyle(ChatFormatting.RED));
                }
            }
        }
    }

    // --- LAST RESORT: PREVENT DEATH (Guaranteed Anti-/kill) ---
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        // Sword Logic (Kill Counter)
        if (event.getSource().getEntity() instanceof Player attacker) {
            ItemStack stack = attacker.getMainHandItem();
            if (stack.getItem() instanceof UfoEnergySwordItem) {
                if (event.getEntity() instanceof Enemy) {
                    int kills = stack.getOrDefault(ModDataComponents.KILL_COUNT.get(), 0);
                    stack.set(ModDataComponents.KILL_COUNT.get(), kills + 1);
                } else if (event.getEntity() instanceof Villager || !(event.getEntity() instanceof Enemy)) {
                    stack.set(ModDataComponents.KILL_COUNT.get(), 0);
                    attacker.sendSystemMessage(Component.literal("The sword's curse has reset your power...").withStyle(ChatFormatting.RED));
                }
            }
        }

        // Armor Logic (Final Anti-Death)
        if (event.getEntity() instanceof ServerPlayer player) {
            if (isFullUfoArmor(player)) {
                if (consumeArmorEnergyDirect(player, 200000)) {
                    event.setCanceled(true);
                    player.setHealth(player.getMaxHealth());
                    player.removeAllEffects();
                    player.sendSystemMessage(Component.literal("Lazarus Protocol Activated: Death Cancelled.").withStyle(ChatFormatting.GOLD));
                }
            }
        }
    }

    // --- HELPER METHODS ---

    private static boolean isFullUfoArmor(Player player) {
        for (ItemStack stack : player.getInventory().armor) {
            if (!(stack.getItem() instanceof UfoArmorItem)) return false;
        }
        return true;
    }

    private static boolean consumeArmorEnergyDirect(Player player, int amountNeeded) {
        int amountLeft = amountNeeded;
        int totalAvailable = 0;

        // 1. Check total energy
        for (ItemStack stack : player.getInventory().armor) {
            if (stack.getItem() instanceof UfoArmorItem) {
                totalAvailable += stack.getOrDefault(ModDataComponents.ENERGY.get(), 0);
            }
        }

        if (totalAvailable < amountNeeded) return false;

        // 2. Consume energy
        for (ItemStack stack : player.getInventory().armor) {
            if (amountLeft <= 0) break;
            if (stack.getItem() instanceof UfoArmorItem) {
                int current = stack.getOrDefault(ModDataComponents.ENERGY.get(), 0);
                int toExtract = Math.min(current, amountLeft);

                stack.set(ModDataComponents.ENERGY.get(), current - toExtract);
                amountLeft -= toExtract;
            }
        }
        return true;
    }

    private static void consumeEnergyDirect(ItemStack stack, int amount) {
        int current = stack.getOrDefault(ModDataComponents.ENERGY.get(), 0);
        if (current >= amount) {
            stack.set(ModDataComponents.ENERGY.get(), current - amount);
        }
    }

    private static void teleportToSafety(ServerPlayer player) {
        BlockPos respawnPos = player.getRespawnPosition();
        ServerLevel respawnLevel = player.server.getLevel(player.getRespawnDimension());

        if (respawnPos != null && respawnLevel != null) {
            player.teleportTo(respawnLevel, respawnPos.getX(), respawnPos.getY(), respawnPos.getZ(), player.getYRot(), player.getXRot());
        } else {
            BlockPos worldSpawn = player.level().getSharedSpawnPos();
            // Teleport high up in the world spawn
            player.teleportTo(worldSpawn.getX(), 300, worldSpawn.getZ());
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1200));
        }
    }
}