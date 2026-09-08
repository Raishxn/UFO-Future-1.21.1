package com.raishxn.ufo.wireless;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** Provider-wide inputs, plus actual substitutions in the current dispatch. */
public final class WirelessBlockingInputs {
    private WirelessBlockingInputs() {}
    public static <K> Set<K> collect(Collection<K> configuredInputs, Collection<K> dispatchedInputs) {
        var inputs = new HashSet<>(configuredInputs);
        inputs.addAll(dispatchedInputs);
        return inputs;
    }
}
