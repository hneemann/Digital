/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.optimizations;

import de.neemann.digital.hdl.model2.*;
import de.neemann.digital.hdl.model2.expression.ExprConstant;

import java.util.Iterator;

/**
 * Removes all constant signals which are only used in nodes which support constant inlining.
 * Use only if hdl creator supports constant inlining at {@link HDLNodeBuildIn}, {@link HDLNodeCustom}
 * and {@link HDLNodeSplitterManyToOne}.
 */
public class RemoveConstantSignals implements Optimization {

    @Override
    public void optimize(HDLCircuit circuit) {
        Iterator<HDLNet> it = circuit.getNets().iterator();
        while (it.hasNext()) {
            HDLNet net = it.next();
            final ExprConstant constant = net.isConstant();
            if (constant != null && isOnlyUsedInSupportedNodes(net)) {
                circuit.getNodes().remove(net.getOutput().getParent());
                it.remove();
                circuit.replaceNetByExpression(net, new ExprConstant(constant));
            }
        }
    }

    private boolean isOnlyUsedInSupportedNodes(HDLNet net) {
        for (HDLPort p : net.getInputs())
            if (!(p.getParent() instanceof HDLNodeBuildIn
                    || p.getParent() instanceof HDLNodeCustom
                    || p.getParent() instanceof HDLNodeSplitterManyToOne))
                return false;
        return true;
    }
}
