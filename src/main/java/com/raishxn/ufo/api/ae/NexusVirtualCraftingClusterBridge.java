package com.raishxn.ufo.api.ae;

/** Mixin bridge used to turn an unformed AE2 cluster into a virtual job CPU. */
public interface NexusVirtualCraftingClusterBridge {
    void ufo$configureVirtualCpu(NexusVirtualCpuHost host, long storage, int coProcessors);
    void ufo$setVirtualCoProcessors(int coProcessors);
}
