/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.optimizations;

import de.neemann.digital.hdl.model2.*;

/**
 * Remove all constant signals which are only used in entity instantiations.
 * Use only if hdl creator supports constant inlining at entity usages.
 */
public class RemoveConstantSignals implements Optimization {
    @Override
    public void optimize(HDLCircuit circuit) {
        circuit.getNets().removeIf(net -> {
            final boolean remove = net.isConstant() != null && isOnlyUsedInEntityInstantiation(net);
            if (remove)
                circuit.getNodes().remove(net.getOutput().getParent());
            return remove;
        });
    }

    private boolean isOnlyUsedInEntityInstantiation(HDLNet net) {
        for (HDLPort p : net.getInputs())
            if (!(p.getParent() instanceof HDLNodeBuildIn || p.getParent() instanceof HDLNodeCustom || p.getParent() instanceof HDLNodeSplitterManyToOne))
                return false;
        return true;
    }
}
