package com.raishxn.ufo.api.ae;

import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.PatternEncodingLogic;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.entity.QuantumGridLinkBE;
import com.raishxn.ufo.block.entity.QuantumPatternFabricationMatrixControllerBE;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;

/** Shared upload path for native AE2 and compatible pattern encoding terminals. */
public final class PatternMatrixAutoUpload {
    private PatternMatrixAutoUpload() { }

    public static void upload(PatternEncodingTermMenu menu, PatternEncodingLogic encodingLogic) {
        if (menu.getPlayer().level().isClientSide()) return;
        var node = menu.getGridNode();
        if (node == null || !node.isActive()) return;
        ItemStack encoded = encodingLogic.getEncodedPatternInv().getStackInSlot(0);
        if (!QuantumPatternFabricationMatrixControllerBE.isSupportedPattern(encoded)) return;

        var discovered = new LinkedHashSet<QuantumPatternMatrixHost>();
        for (QuantumPatternMatrixHost target :
                node.getGrid().getActiveMachines(QuantumPatternMatrixHost.class)) {
            discovered.add(target);
        }
        // Some add-on terminals keep a logical grid view whose service-class index can
        // lag one topology update. Node ownership is authoritative and fixes upload in
        // that short window without accepting offline links.
        for (var gridNode : node.getGrid().getNodes()) {
            if (gridNode.isActive() && gridNode.getOwner() instanceof QuantumGridLinkBE link) {
                discovered.add(link);
            }
        }
        var targets = new ArrayList<>(discovered);
        targets.sort(Comparator.comparingInt(QuantumPatternMatrixHost::patternMatrixPriority)
                .reversed()
                .thenComparingLong(QuantumPatternMatrixHost::patternMatrixSortKey));

        for (QuantumPatternMatrixHost target : targets) {
            if (target.insertEncodedPattern(encoded)) {
                encodingLogic.getEncodedPatternInv().setItemDirect(0, ItemStack.EMPTY);
                UfoMod.LOGGER.debug("Auto-uploaded encoded pattern to Matrix {}",
                        target.patternMatrixSortKey());
                return;
            }
        }
        UfoMod.LOGGER.warn("Could not auto-upload encoded pattern: {} active Matrix target(s) rejected it",
                targets.size());
    }
}
