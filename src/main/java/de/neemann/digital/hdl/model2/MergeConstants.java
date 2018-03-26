/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.hdl.model2.expression.ExprConstant;

import java.util.ArrayList;

/**
 * Multiple usages of the same constant are mapped to a single constant signal.
 */
public class MergeConstants {
    private final ArrayList<HDLNode> nodes;
    private final HDLCircuit circuit;

    /**
     * Creates a new instance
     *
     * @param nodes   the node to simplify
     * @param circuit the circuit
     */
    MergeConstants(ArrayList<HDLNode> nodes, HDLCircuit circuit) {
        this.nodes = nodes;
        this.circuit = circuit;
    }

    /**
     * Merges the usages of the constants
     *
     * @return the simplified list of nodes
     * @throws HDLException HDLException
     */
    public ArrayList<HDLNode> merge() throws HDLException {
        int n1 = 0;
        while (n1 < nodes.size()) {
            final HDLNode node1 = nodes.get(n1);
            ExprConstant con1 = getConstant(node1);
            if (con1 != null) {
                //modification of loop variable n2 is intended!
                //CHECKSTYLE.OFF: ModifiedControlVariable
                for (int n2 = n1 + 1; n2 < nodes.size(); n2++) {
                    final HDLNode node2 = nodes.get(n2);
                    ExprConstant con2 = getConstant(node2);
                    if (con2 != null) {
                        if (con1.isEqualTo(con2)) {
                            merge(node1, node2);
                            nodes.remove(n2);
                            n2--;
                        }
                    }
                }
                //CHECKSTYLE.ON: ModifiedControlVariable
            }

            n1++;
        }
        return nodes;
    }

    private void merge(HDLNode node1, HDLNode node2) throws HDLException {
        HDLNet usedNet = node1.getOutput().getNet();
        HDLNet obsoleteNet = node2.getOutput().getNet();

        final ArrayList<HDLPort> ins = obsoleteNet.getInputs();
        for (HDLPort p : ins.toArray(new HDLPort[ins.size()]))
            p.setNet(usedNet);

        circuit.removeNet(obsoleteNet);
    }

    private ExprConstant getConstant(HDLNode node) {
        if (node instanceof HDLNodeExpression) {
            HDLNodeExpression expr = (HDLNodeExpression) node;
            if (expr.getExpression() instanceof ExprConstant) {
                return (ExprConstant) expr.getExpression();
            }
        }

        return null;
    }

}
