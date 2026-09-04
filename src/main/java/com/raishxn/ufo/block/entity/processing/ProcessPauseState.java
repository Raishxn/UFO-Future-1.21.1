package com.raishxn.ufo.block.entity.processing;

/** Small persistence-friendly state holder for manual per-process pause. */
public final class ProcessPauseState {
    private boolean paused;

    public boolean isPaused() {
        return this.paused;
    }

    public void toggle(boolean active) {
        if (active) {
            this.paused = !this.paused;
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void clear() {
        this.paused = false;
    }
}
