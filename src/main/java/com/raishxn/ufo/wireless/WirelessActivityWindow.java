package com.raishxn.ufo.wireless;

import java.util.HashMap;
import java.util.Map;

/** Productive progress only, independent of patterns and Minecraft. */
public final class WirelessActivityWindow<K> {
    public static final int WINDOW_TICKS = 20;
    private final Map<K, Long> lastProgress = new HashMap<>();
    public void mark(K machine, long tick) { lastProgress.put(machine, tick); }
    public boolean active(K machine, long tick) {
        Long last = lastProgress.get(machine);
        return last != null && tick >= last && tick - last < WINDOW_TICKS;
    }
    public void prune(long tick) { lastProgress.keySet().removeIf(key -> !active(key, tick)); }
}
