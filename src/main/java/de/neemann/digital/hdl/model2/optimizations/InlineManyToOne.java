/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.optimizations;

import de.neemann.digital.hdl.model2.*;

import java.util.Iterator;

/**
 * Inlines the inputs of the HDLNodeSplitterManyToOne.
 */
public class InlineManyToOne implements Optimization {
    @Override
    public void optimize(HDLCircuit circuit) {
        Iterator<HDLNode> it = circuit.iterator();
        while (it.hasNext()) {
            HDLNode node = it.next();
            if (node instanceof HDLNodeAssignment) {
                HDLNodeAssignment assign = (HDLNodeAssignment) node;
                final HDLNet net = assign.getTargetNet();
                if (net != null && net.getInputs().size() == 1) {
                    HDLNode receiver = net.getInputs().get(0).getParent();
                    if (receiver instanceof HDLNodeSplitterManyToOne) {
                        HDLNodeSplitterManyToOne mto = (HDLNodeSplitterManyToOne) receiver;
                        mto.replaceNetByExpression(net, assign.getExpression());
                        it.remove();
                        circuit.removeNet(net);
                    }
                }
            }
        }
    }
}
