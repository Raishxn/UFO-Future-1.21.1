package com.raishxn.ufo.block.entity.processing;

/** FE input converted once to AE; fractional AE survives extraction and saves. */
public final class ExternalEnergyBuffer {
    private final long capacity;
    private double stored;

    public ExternalEnergyBuffer(long capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity");
        this.capacity = capacity;
    }

    public int receiveFe(int offered, double aePerFe, boolean simulate) {
        if (offered <= 0 || !Double.isFinite(aePerFe) || aePerFe <= 0) return 0;
        int accepted = (int) Math.min(offered, Math.floor((capacity - stored) / aePerFe));
        if (!simulate && accepted > 0) stored = Math.min(capacity, stored + accepted * aePerFe);
        return Math.max(0, accepted);
    }

    public long extract(long requested, boolean simulate) {
        return extract(requested, 1D, simulate);
    }

    public long extract(long requested, double multiplier, boolean simulate) {
        if (requested <= 0 || !Double.isFinite(multiplier) || multiplier <= 0) return 0;
        long accepted = Math.min(requested, (long) Math.floor(stored / multiplier));
        if (!simulate) stored = Math.max(0, stored - accepted * multiplier);
        return accepted;
    }

    public double stored() { return stored; }
    public long capacity() { return capacity; }
    public void restore(double value) { stored = Double.isFinite(value) ? Math.max(0, Math.min(capacity, value)) : 0; }
}
