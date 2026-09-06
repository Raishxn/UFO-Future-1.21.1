package com.raishxn.ufo.diagnostic;

/** Server-side, on-demand UI data. Efficiency is heatNumerator/millibucketDenominator. */
public record CoolantStatus(long hatchPosition, String fluidId, long storedMillibuckets,
                            long capacityMillibuckets, long heatNumerator,
                            long millibucketDenominator, long maxMillibucketsPerTick) {}
