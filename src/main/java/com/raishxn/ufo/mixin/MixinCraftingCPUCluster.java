package com.raishxn.ufo.mixin;

import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CraftingCPUCluster.class, priority = 2000, remap = false)
public class MixinCraftingCPUCluster {
    /**
     * Este Mixin intercepta a verificação de limite de threads do AE2.
     * Em vez de retornar o número real de threads do co-processador (que causaria o crash),
     * ele sempre retorna 1 durante a validação.
     * Isso "engana" a verificação, permitindo que blocos com qualquer número de threads sejam adicionados à CPU.
     * O valor real dos threads é usado depois, no cálculo de aceleração.
     */
    @Redirect(
            method = "addBlockEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/blockentity/crafting/CraftingBlockEntity;getAcceleratorThreads()I",
                    ordinal = 1
            ),
            remap = false
    )
    private int ufo$removeThreadLimit(CraftingBlockEntity instance) {
        // Retornar 1 faz com que a verificação `if (threads > 16)` nunca seja verdadeira.
        return 1;
    }
}