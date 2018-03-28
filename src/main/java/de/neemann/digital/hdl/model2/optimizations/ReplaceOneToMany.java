/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.optimizations;

import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.hdl.model2.*;
import de.neemann.digital.hdl.model2.expression.ExprVarRange;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Replace {@link HDLNodeSplitterOneToMany} by several {@link HDLNodeAssignment} instances.
 */
public class ReplaceOneToMany implements Optimization {

    @Override
    public void optimize(HDLCircuit circuit) throws HDLException {
        ArrayList<HDLNodeAssignment> newNodes = new ArrayList<>();

        Iterator<HDLNode> it = circuit.getNodes().iterator();
        while (it.hasNext()) {
            HDLNode n = it.next();
            if (n instanceof HDLNodeSplitterOneToMany) {
                it.remove();
                replace((HDLNodeSplitterOneToMany) n, newNodes);
            }
        }

        circuit.getNodes().addAll(newNodes);
    }

    private void replace(HDLNodeSplitterOneToMany n, ArrayList<HDLNodeAssignment> newNodes) throws HDLException {
        final HDLPort inPort = n.getInputs().get(0);
        HDLNet inNet = inPort.getNet();
        inPort.setNet(null);

        int i = 0;
        for (Splitter.Port p : n.getOutputSplit()) {
            final HDLPort outPort = n.getOutputs().get(i);
            if (outPort.getNet()!=null) {
                ExprVarRange exp = new ExprVarRange(inNet, p.getPos() + p.getBits() - 1, p.getPos());
                HDLNodeAssignment node = new HDLNodeAssignment("splitter", null, null);
                node.setExpression(exp);

                node.addPort(new HDLPort("in", inNet, HDLPort.Direction.IN, inPort.getBits()));
                node.addPort(outPort);
                newNodes.add(node);
            }
            i++;
        }

    }
}
