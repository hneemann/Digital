/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.hdl.model2.expression.Expression;

import java.util.ArrayList;

/**
 * Helper to merge expressions
 */
class OperationMerger {
    private final ArrayList<HDLNode> nodes;
    private final HDLCircuit circuit;

    OperationMerger(ArrayList<HDLNode> nodes, HDLCircuit circuit) {
        this.nodes = nodes;
        this.circuit = circuit;
    }

    ArrayList<HDLNode> merge() throws HDLException {
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
                                nodes.set(i, merge((HDLNodeExpression) n1, (HDLNodeExpression) n2));
                                nodes.remove(n2);
                                wasOptimization = true;
                                break outer;
                            }
                        }
                    }
                }
            }
        } while (wasOptimization);

        return nodes;
    }

    private HDLNodeExpression merge(HDLNodeExpression host, HDLNodeExpression include) throws HDLException {
        final Expression expression = host.getExpression();
        expression.replace(include.getOutput().getNet(), include.getExpression());

        HDLNodeExpression node = new HDLNodeExpression("merged expression",
                null, name -> host.getOutput().getBits());
        node.setExpression(expression);

        circuit.removeNet(include.getOutput().getNet());

        node.addOutput(host.getOutput());
        for (HDLPort i : host.getInputs())
            node.addInput(i);

        for (HDLPort i : include.getInputs())
            if (!node.hasInput(i))
                node.addInput(i);

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
