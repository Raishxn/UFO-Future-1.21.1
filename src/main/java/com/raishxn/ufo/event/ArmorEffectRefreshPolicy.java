package com.raishxn.ufo.event;

import net.minecraft.world.effect.MobEffectInstance;

/**
 * Decides when a continuously supplied armor effect actually needs to be sent
 * through Minecraft's effect merge/synchronization path again.
 */
public final class ArmorEffectRefreshPolicy {
    private ArmorEffectRefreshPolicy() {
    }

    public static boolean shouldRefresh(MobEffectInstance current, int targetAmplifier,
                                        int refreshAtOrBelow) {
        return shouldRefresh(
                current != null,
                current != null && current.isInfiniteDuration(),
                current == null ? 0 : current.getAmplifier(),
                current == null ? 0 : current.getDuration(),
                targetAmplifier,
                refreshAtOrBelow);
    }

    static boolean shouldRefresh(boolean present, boolean infinite, int currentAmplifier,
                                 int currentDuration, int targetAmplifier, int refreshAtOrBelow) {
        if (targetAmplifier < 0 || refreshAtOrBelow < 0) {
            throw new IllegalArgumentException("effect thresholds cannot be negative");
        }
        if (!present) {
            return true;
        }
        if (infinite || currentAmplifier > targetAmplifier) {
            return false;
        }
        return currentAmplifier < targetAmplifier || currentDuration <= refreshAtOrBelow;
    }
}
