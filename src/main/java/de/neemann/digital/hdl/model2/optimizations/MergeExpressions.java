/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.optimizations;

import de.neemann.digital.hdl.model2.*;
import de.neemann.digital.hdl.model2.expression.Expression;

import java.util.ArrayList;

/**
 * Merges the bool expression by inlining nodes which also represent a bool expression.
 */
public class MergeExpressions implements Optimization {
    private HDLCircuit circuit;
    private ArrayList<HDLNode> nodes;

    @Override
    public void optimize(HDLCircuit circuit) {
        this.circuit = circuit;
        this.nodes = circuit.getNodes();

        boolean wasOptimization;
        do {
            wasOptimization = false;
            outer:
            for (int i = 0; i < nodes.size(); i++) {
                HDLNode n1 = nodes.get(i);
                if (n1 instanceof HDLNodeExpression) {
                    for (HDLPort p : n1.getInputs()) {
                        HDLNode n2 = searchCreator(p.getNet());
                        if (n2 != null && n2 instanceof HDLNodeExpression) {
                            if (n2.getOutputs().size() == 1 && n2.getOutput().getNet().getInputs().size() == 1) {
                                if (inlinePossible(n2.getOutput().getNet())) {
                                    nodes.set(i, merge((HDLNodeExpression) n1, (HDLNodeExpression) n2));
                                    nodes.remove(n2);
                                    wasOptimization = true;
                                    break outer;
                                }
                            }
                        }
                    }
                }
            }
        } while (wasOptimization);
    }

    private boolean inlinePossible(HDLNet net) {
        for (HDLNode n : circuit.getNodes())
            if (!n.inliningPossible(net))
                return false;
        return true;
    }

    private HDLNodeExpression merge(HDLNodeExpression host, HDLNodeExpression include) {
        final Expression expression = host.getExpression();
        final HDLNet obsoleteNet = include.getOutput().getNet();
        expression.replace(obsoleteNet, include.getExpression());

        HDLNodeExpression node = new HDLNodeExpression("merged expression",
                null, name -> host.getOutput().getBits());
        node.setExpression(expression);

        circuit.removeNet(obsoleteNet);

        node.addPort(host.getOutput());
        for (HDLPort i : host.getInputs())
            if (i.getNet() != obsoleteNet)
                node.addPort(i);

        for (HDLPort i : include.getInputs())
            if (!node.hasInput(i))
                node.addPort(i);
            else
                i.getNet().remove(i);

        return node;
    }

    private HDLNode searchCreator(HDLNet net) {
        for (HDLNode n : nodes)
            for (HDLPort p : n.getOutputs())
                if (p.getNet() == net)
                    return n;
        return null;
    }
}
