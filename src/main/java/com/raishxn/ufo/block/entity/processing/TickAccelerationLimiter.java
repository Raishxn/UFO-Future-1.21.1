package com.raishxn.ufo.block.entity.processing;

/**
 * Allows a bounded number of machine updates during one Minecraft game tick.
 * This permits external time accelerators while preventing a high multiplier
 * from turning a single block entity into hundreds of full server updates.
 */
public final class TickAccelerationLimiter {
    private long gameTime = Long.MIN_VALUE;
    private int acceptedUpdates;

    /** Returns true while this game tick has remaining update budget. */
    public boolean tryAcquire(long currentGameTime, int maxUpdatesPerGameTick) {
        if (currentGameTime != gameTime) {
            gameTime = currentGameTime;
            acceptedUpdates = 0;
        }
        if (acceptedUpdates >= maxUpdatesPerGameTick) {
            return false;
        }
        acceptedUpdates++;
        return true;
    }

    /** One-based ordinal of the most recently accepted update in this game tick. */
    public int acceptedUpdates() {
        return acceptedUpdates;
    }
}
